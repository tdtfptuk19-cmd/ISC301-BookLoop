package com.bookloop.service;

import com.bookloop.dto.CheckoutForm;
import com.bookloop.entity.*;
import com.bookloop.exception.ListingNotFoundException;
import com.bookloop.exception.UnauthorizedOperationException;
import com.bookloop.repository.CartItemRepository;
import com.bookloop.repository.CartRepository;
import com.bookloop.repository.OrderRepository;
import com.bookloop.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Service managing orders, multi-seller cart checkout, and lifecycle order transitions.
 */
@Service
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final PayoutService payoutService;

    public OrderService(OrderRepository orderRepository,
                        CartRepository cartRepository,
                        CartItemRepository cartItemRepository,
                        ProductRepository productRepository,
                        PayoutService payoutService) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.payoutService = payoutService;
    }

    /**
     * Executes the checkout process. Supports multi-seller carts by grouping items
     * by seller and creating distinct orders linked under a shared parentCheckoutGroup.
     */
    public Order checkout(User buyer, CheckoutForm form) {
        List<Order> orders = checkoutMulti(buyer, form);
        return orders.get(0);
    }

    /**
     * Creates independent orders per seller for multi-seller cart support.
     */
    public List<Order> checkoutMulti(User buyer, CheckoutForm form) {
        Cart cart = cartRepository.findByUserIdWithItems(buyer.getId())
                .orElseThrow(() -> new IllegalArgumentException("Cart not found for user."));

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new IllegalArgumentException("Cannot checkout with an empty cart.");
        }

        // Validate items and group by Seller
        Map<Long, List<CartItem>> itemsBySellerId = new LinkedHashMap<>();
        Map<Long, User> sellerMap = new HashMap<>();

        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();
            if (product == null) {
                throw new ListingNotFoundException("One of the items in your cart references an invalid product.");
            }

            Product freshProduct = productRepository.findById(product.getId())
                    .orElseThrow(() -> new ListingNotFoundException("Product not found: " + product.getTitle()));

            if (freshProduct.getStatus() != ListingStatus.AVAILABLE) {
                throw new IllegalArgumentException("The book '" + freshProduct.getTitle()
                        + "' is no longer available (current status: " + freshProduct.getStatus().name() + "). Please remove it from your cart.");
            }

            if (freshProduct.getSeller() != null && freshProduct.getSeller().getId().equals(buyer.getId())) {
                throw new IllegalArgumentException("You cannot purchase your own listed book: " + freshProduct.getTitle());
            }

            User seller = freshProduct.getSeller();
            Long sellerId = seller != null ? seller.getId() : 0L;
            itemsBySellerId.computeIfAbsent(sellerId, k -> new ArrayList<>()).add(cartItem);
            if (seller != null) {
                sellerMap.put(sellerId, seller);
            }
        }

        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String shortId = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        String checkoutGroup = "CHK-" + dateStr + "-" + shortId;

        List<Order> createdOrders = new ArrayList<>();
        int sellerIndex = 1;

        for (Map.Entry<Long, List<CartItem>> entry : itemsBySellerId.entrySet()) {
            User seller = sellerMap.get(entry.getKey());
            List<CartItem> sellerItems = entry.getValue();

            BigDecimal sellerSubtotal = BigDecimal.ZERO;
            for (CartItem item : sellerItems) {
                Product p = item.getProduct();
                sellerSubtotal = sellerSubtotal.add(p.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            }

            String orderNumber = itemsBySellerId.size() > 1
                    ? "ORD-" + dateStr + "-" + shortId + "-S" + sellerIndex
                    : "ORD-" + dateStr + "-" + shortId;
            sellerIndex++;

            Order order = new Order();
            order.setOrderNumber(orderNumber);
            order.setParentCheckoutGroup(checkoutGroup);
            order.setBuyer(buyer);
            order.setSeller(seller);
            order.setOrderDate(LocalDateTime.now());
            order.setTotalAmount(sellerSubtotal);
            order.setPaymentMethod(form.getPaymentMethod());

            // Initial status: AWAITING_PAYMENT for escrow, PENDING for COD
            if (form.getPaymentMethod() == PaymentMethod.MARKETPLACE_HOLDING) {
                order.setStatus(OrderStatus.AWAITING_PAYMENT);
            } else {
                order.setStatus(OrderStatus.PENDING);
            }

            order.setMeetupNote(form.getMeetupNote());
            order.setContactPhone(form.getContactPhone() != null && !form.getContactPhone().trim().isEmpty()
                    ? form.getContactPhone().trim()
                    : buyer.getPhone());

            for (CartItem cartItem : sellerItems) {
                Product freshProduct = productRepository.findById(cartItem.getProduct().getId()).orElseThrow();
                OrderItem orderItem = new OrderItem(
                        order,
                        freshProduct,
                        freshProduct.getSeller(),
                        freshProduct.getPrice(),
                        cartItem.getQuantity()
                );
                order.addItem(orderItem);

                // Mark book as SOLD to lock listing
                freshProduct.setStatus(ListingStatus.SOLD);
                productRepository.save(freshProduct);
            }

            createdOrders.add(orderRepository.save(order));
        }

        // Clear buyer's cart
        cart.getItems().clear();
        cartItemRepository.deleteByCartId(cart.getId());

        return createdOrders;
    }

    @Transactional(readOnly = true)
    public List<Order> findBuyerOrders(Long buyerId) {
        return orderRepository.findByBuyerIdOrderByOrderDateDesc(buyerId);
    }

    @Transactional(readOnly = true)
    public List<Order> findSellerOrders(Long sellerId) {
        return orderRepository.findBySellerIdOrderByOrderDateDesc(sellerId);
    }

    @Transactional(readOnly = true)
    public Order getOrderDetails(Long orderId, User user) {
        Order order = orderRepository.findByIdWithDetails(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with ID: " + orderId));

        boolean isBuyer = order.isBuyer(user.getId());
        boolean isSeller = order.hasSeller(user.getId());

        if (!isBuyer && !isSeller) {
            throw new UnauthorizedOperationException("You do not have permission to view order #" + order.getOrderNumber());
        }

        return order;
    }

    /**
     * Seller accepts an incoming order.
     */
    public Order sellerAcceptOrder(Long orderId, User seller) {
        Order order = orderRepository.findByIdWithDetails(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with ID: " + orderId));

        if (!order.hasSeller(seller.getId())) {
            throw new UnauthorizedOperationException("Only the seller of this order can accept it.");
        }

        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.PAID_HELD) {
            throw new IllegalStateException("Only orders in PENDING or PAID_HELD can be accepted (current: " + order.getStatus() + ").");
        }

        order.setAcceptedAt(LocalDateTime.now());
        if (order.getStatus() == OrderStatus.PENDING) {
            order.setStatus(OrderStatus.CONFIRMED);
        }
        return orderRepository.save(order);
    }

    /**
     * Seller dispatches the order to the buyer.
     */
    public Order sellerDispatchOrder(Long orderId, User seller) {
        Order order = orderRepository.findByIdWithDetails(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with ID: " + orderId));

        if (!order.hasSeller(seller.getId())) {
            throw new UnauthorizedOperationException("Only the seller of this order can dispatch it.");
        }

        if (order.getStatus() != OrderStatus.PAID_HELD && order.getStatus() != OrderStatus.CONFIRMED) {
            throw new IllegalStateException("Order must be paid or confirmed before shipping (current: " + order.getStatus() + ").");
        }

        order.setStatus(OrderStatus.SHIPPED);
        order.setShippedAt(LocalDateTime.now());
        return orderRepository.save(order);
    }

    /**
     * Buyer confirms receipt of the book. Triggers conditional payout disbursement to seller.
     */
    public Order buyerConfirmReceipt(Long orderId, User buyer) {
        Order order = orderRepository.findByIdWithDetails(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with ID: " + orderId));

        if (!order.isBuyer(buyer.getId())) {
            throw new UnauthorizedOperationException("Only the buyer can confirm receipt of this order.");
        }

        if (order.getStatus() != OrderStatus.SHIPPED && order.getStatus() != OrderStatus.PAID_HELD && order.getStatus() != OrderStatus.CONFIRMED) {
            throw new IllegalStateException("Order cannot be confirmed received in status: " + order.getStatus());
        }

        order.setStatus(OrderStatus.DELIVERED);
        order.setReceivedConfirmedAt(LocalDateTime.now());
        order.setCompletedAt(LocalDateTime.now());
        Order savedOrder = orderRepository.save(order);

        // If paid via escrow holding, disburse payout to seller
        if (order.getPaymentMethod() == PaymentMethod.MARKETPLACE_HOLDING) {
            payoutService.triggerConditionalPayout(order.getId(), buyer);
            savedOrder.setStatus(OrderStatus.COMPLETED);
            savedOrder = orderRepository.save(savedOrder);
        } else {
            savedOrder.setStatus(OrderStatus.COMPLETED);
            savedOrder = orderRepository.save(savedOrder);
        }

        return savedOrder;
    }

    /**
     * Legacy confirm method for COD workflows.
     */
    public Order confirmOrder(Long orderId, User seller) {
        return sellerAcceptOrder(orderId, seller);
    }

    /**
     * Legacy complete method for COD workflows.
     */
    public Order completeOrder(Long orderId, User seller) {
        Order order = orderRepository.findByIdWithDetails(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with ID: " + orderId));

        if (!order.hasSeller(seller.getId())) {
            throw new UnauthorizedOperationException("Only the seller can mark this order as completed.");
        }

        if (order.getStatus() != OrderStatus.CONFIRMED && order.getStatus() != OrderStatus.DELIVERED) {
            throw new IllegalStateException("Only CONFIRMED orders can be marked as COMPLETED (current: " + order.getStatus() + ").");
        }

        order.setStatus(OrderStatus.COMPLETED);
        order.setCompletedAt(LocalDateTime.now());
        return orderRepository.save(order);
    }

    /**
     * Cancels an order. Restores products back to AVAILABLE.
     */
    public Order cancelOrder(Long orderId, User user) {
        Order order = orderRepository.findByIdWithDetails(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with ID: " + orderId));

        boolean isBuyer = order.isBuyer(user.getId());
        boolean isSeller = order.hasSeller(user.getId());

        if (!isBuyer && !isSeller) {
            throw new UnauthorizedOperationException("You are not authorized to cancel order #" + order.getOrderNumber());
        }

        if (order.getStatus() == OrderStatus.COMPLETED) {
            throw new IllegalStateException("Completed orders cannot be cancelled.");
        }

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("This order is already cancelled.");
        }

        order.setStatus(OrderStatus.CANCELLED);

        // Restore all products back to AVAILABLE
        for (OrderItem item : order.getItems()) {
            if (item.getProduct() != null) {
                Product product = item.getProduct();
                product.setStatus(ListingStatus.AVAILABLE);
                productRepository.save(product);
            }
        }

        return orderRepository.save(order);
    }
}
