package com.bookloop.service;

import com.bookloop.entity.Cart;
import com.bookloop.entity.CartItem;
import com.bookloop.entity.ListingStatus;
import com.bookloop.entity.Product;
import com.bookloop.entity.User;
import com.bookloop.exception.ListingNotFoundException;
import com.bookloop.repository.CartItemRepository;
import com.bookloop.repository.CartRepository;
import com.bookloop.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service managing user shopping cart operations.
 */
@Service
@Transactional
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    public CartService(CartRepository cartRepository,
                       CartItemRepository cartItemRepository,
                       ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
    }

    /**
     * Retrieves or creates a cart for the specified user.
     */
    public Cart getOrCreateCart(User user) {
        return cartRepository.findByUserIdWithItems(user.getId())
                .orElseGet(() -> {
                    Cart newCart = new Cart(user);
                    return cartRepository.save(newCart);
                });
    }

    /**
     * Gets user's cart with items eagerly loaded.
     */
    @Transactional(readOnly = true)
    public Cart getCart(User user) {
        return cartRepository.findByUserIdWithItems(user.getId())
                .orElseGet(() -> new Cart(user));
    }

    /**
     * Adds a book listing to the user's cart.
     * Enforces rules:
     * 1. Product must exist and be AVAILABLE.
     * 2. User cannot add their own listing.
     * 3. Prevents duplicate cart entries for unique second-hand listings.
     */
    public void addToCart(User buyer, Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ListingNotFoundException("Book listing not found with ID: " + productId));

        // Rule: cannot buy your own book
        if (product.getSeller() != null && product.getSeller().getId().equals(buyer.getId())) {
            throw new IllegalArgumentException("You cannot add your own listed book to your cart.");
        }

        // Rule: only AVAILABLE books can be added
        if (product.getStatus() != ListingStatus.AVAILABLE) {
            throw new IllegalArgumentException("This book is currently " + product.getStatus().name() + " and cannot be purchased.");
        }

        Cart cart = getOrCreateCart(buyer);

        // Check if already in cart
        Optional<CartItem> existingItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId);
        if (existingItem.isPresent()) {
            // Second-hand textbooks are single unique copies — ensure quantity is 1
            CartItem item = existingItem.get();
            item.setQuantity(1);
            cartItemRepository.save(item);
        } else {
            CartItem newItem = new CartItem(cart, product, 1);
            cart.getItems().add(newItem);
            cartItemRepository.save(newItem);
        }
    }

    /**
     * Removes an item from the user's cart.
     */
    public void removeFromCart(User user, Long productId) {
        Cart cart = getOrCreateCart(user);
        if (cart.getItems() != null) {
            cart.getItems().removeIf(item -> item.getProduct() != null && item.getProduct().getId().equals(productId));
        }
        cartItemRepository.deleteByCartIdAndProductId(cart.getId(), productId);
    }

    /**
     * Clears all items from the user's cart.
     */
    public void clearCart(User user) {
        Cart cart = getOrCreateCart(user);
        if (cart.getItems() != null) {
            cart.getItems().clear();
        }
        cartItemRepository.deleteByCartId(cart.getId());
    }

    /**
     * Returns total item count in user's cart (for navbar badge).
     */
    @Transactional(readOnly = true)
    public int getCartItemCount(User user) {
        if (user == null || user.getId() == null) {
            return 0;
        }
        return cartRepository.findByUserIdWithItems(user.getId())
                .map(Cart::getItemCount)
                .orElse(0);
    }
}
