package com.bookloop.service;

import com.bookloop.dto.SellerFinanceOverviewDto;
import com.bookloop.entity.*;
import com.bookloop.payment.dto.SellerOnboardingRequest;
import com.bookloop.payment.dto.SellerOnboardingResponse;
import com.bookloop.payment.provider.PaymentProvider;
import com.bookloop.repository.CommissionRecordRepository;
import com.bookloop.repository.OrderRepository;
import com.bookloop.repository.PayoutTransactionRepository;
import com.bookloop.repository.SellerPaymentAccountRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Service managing financial balance, payout history, linked bank account,
 * and commission views for sellers.
 */
@Service
@Transactional
public class SellerFinanceService {

    private final SellerPaymentAccountRepository accountRepository;
    private final PayoutTransactionRepository payoutRepository;
    private final CommissionRecordRepository commissionRepository;
    private final OrderRepository orderRepository;
    private final PaymentProvider paymentProvider;
    private final FinancialAuditService auditService;
    private final PayoutService payoutService;

    public SellerFinanceService(SellerPaymentAccountRepository accountRepository,
                                PayoutTransactionRepository payoutRepository,
                                CommissionRecordRepository commissionRepository,
                                OrderRepository orderRepository,
                                PaymentProvider paymentProvider,
                                FinancialAuditService auditService,
                                PayoutService payoutService) {
        this.accountRepository = accountRepository;
        this.payoutRepository = payoutRepository;
        this.commissionRepository = commissionRepository;
        this.orderRepository = orderRepository;
        this.paymentProvider = paymentProvider;
        this.auditService = auditService;
        this.payoutService = payoutService;
    }

    @Transactional(readOnly = true)
    public SellerFinanceOverviewDto getFinanceOverview(Long sellerId) {
        SellerFinanceOverviewDto dto = new SellerFinanceOverviewDto();

        // 1. Total delivered or completed sales volume
        BigDecimal deliveredSales = orderRepository.sumTotalAmountBySellerIdAndStatusIn(
                sellerId, Arrays.asList(OrderStatus.DELIVERED, OrderStatus.COMPLETED));
        dto.setTotalRevenue(deliveredSales != null ? deliveredSales : BigDecimal.ZERO);

        // 2. Pending escrow (funds held by partner for orders in progress)
        BigDecimal pendingEscrow = orderRepository.sumTotalAmountBySellerIdAndStatusIn(
                sellerId, Arrays.asList(OrderStatus.PAID_HELD, OrderStatus.SHIPPED));
        dto.setPendingEscrowAmount(pendingEscrow != null ? pendingEscrow : BigDecimal.ZERO);

        // 3. Net disbursed payouts
        BigDecimal netPaid = payoutRepository.sumSellerAmountByStatus(sellerId, PayoutStatus.PAID);
        BigDecimal netConfirmed = payoutRepository.sumSellerAmountByStatus(sellerId, PayoutStatus.CONFIRMED);
        BigDecimal netCompleted = payoutRepository.sumSellerAmountByStatus(sellerId, PayoutStatus.COMPLETED);
        dto.setNetDisbursed((netPaid != null ? netPaid : BigDecimal.ZERO)
                .add(netConfirmed != null ? netConfirmed : BigDecimal.ZERO)
                .add(netCompleted != null ? netCompleted : BigDecimal.ZERO));

        // 4. Total commission deducted
        BigDecimal totalComm = commissionRepository.sumSellerCommissionByStatus(sellerId, CommissionStatus.SETTLED);
        dto.setTotalCommissionPaid(totalComm != null ? totalComm : BigDecimal.ZERO);

        // 5. Counts
        long pendingOrders = orderRepository.countBySellerIdAndStatus(sellerId, OrderStatus.PAID_HELD)
                + orderRepository.countBySellerIdAndStatus(sellerId, OrderStatus.SHIPPED);
        long deliveredOrders = orderRepository.countBySellerIdAndStatus(sellerId, OrderStatus.DELIVERED)
                + orderRepository.countBySellerIdAndStatus(sellerId, OrderStatus.COMPLETED);
        dto.setPendingOrdersCount(pendingOrders);
        dto.setDeliveredOrdersCount(deliveredOrders);

        // 6. Linked payment account
        dto.setPaymentAccount(accountRepository.findBySellerId(sellerId).orElse(null));

        // 7. Undisbursed revenue from delivered/completed orders
        BigDecimal totalAccountedPayout = dto.getNetDisbursed().add(dto.getTotalCommissionPaid());
        BigDecimal undisbursed = dto.getTotalRevenue().subtract(totalAccountedPayout);
        if (undisbursed.compareTo(BigDecimal.ZERO) < 0) {
            undisbursed = BigDecimal.ZERO;
        }
        dto.setUndisbursedRevenue(undisbursed);

        return dto;
    }

    /**
     * Disburses all completed or delivered sales orders that haven't been disbursed yet.
     */
    public int disburseUndisbursedOrders(Long sellerId, User seller) {
        List<Order> sellerOrders = orderRepository.findBySellerIdOrderByOrderDateDesc(sellerId);
        int count = 0;
        for (Order order : sellerOrders) {
            if (order.getStatus() == OrderStatus.COMPLETED || order.getStatus() == OrderStatus.DELIVERED) {
                List<PayoutTransaction> existing = payoutRepository.findByOrderId(order.getId());
                boolean hasCompletedPayout = existing.stream().anyMatch(p ->
                        p.getStatus() == PayoutStatus.COMPLETED || p.getStatus() == PayoutStatus.PAID || p.getStatus() == PayoutStatus.CONFIRMED);
                if (!hasCompletedPayout) {
                    payoutService.triggerConditionalPayout(order.getId(), seller);
                    count++;
                }
            }
        }
        return count;
    }

    @Transactional(readOnly = true)
    public Optional<SellerPaymentAccount> getPaymentAccount(Long sellerId) {
        return accountRepository.findBySellerId(sellerId);
    }

    public SellerPaymentAccount onboardAccount(User seller, SellerOnboardingRequest req) {
        SellerPaymentAccount account = accountRepository.findBySellerId(seller.getId())
                .orElseGet(() -> {
                    SellerPaymentAccount newAcc = new SellerPaymentAccount();
                    newAcc.setSeller(seller);
                    return newAcc;
                });

        req.setSellerId(seller.getId());
        req.setEmail(seller.getEmail());
        if (req.getFullName() == null || req.getFullName().isBlank()) {
            req.setFullName(seller.getFullName());
        }

        SellerOnboardingResponse response = paymentProvider.onboardSeller(req);

        account.setProviderCode(paymentProvider.getProviderCode());
        account.setProviderSellerId(response.getProviderSellerId());
        account.setBankCode(req.getBankCode());
        account.setBankName(req.getBankName());
        account.setAccountNumber(req.getAccountNumber());
        account.setAccountHolderName(req.getAccountHolderName());
        account.setCitizenId(req.getCitizenId());
        account.setKycStatus(KycStatus.VERIFIED);
        account.setStatus(SellerAccountStatus.ACTIVE);

        SellerPaymentAccount saved = accountRepository.save(account);

        auditService.logEvent(
                "SELLER_ACCOUNT_ONBOARDED",
                "SellerPaymentAccount",
                saved.getId(),
                seller.getEmail(),
                "Seller connected payment account for bank " + saved.getBankName() + " (" + saved.getAccountNumber() + ")",
                null,
                "Provider ID: " + saved.getProviderSellerId()
        );

        return saved;
    }

    @Transactional(readOnly = true)
    public Page<PayoutTransaction> getPayouts(Long sellerId, Pageable pageable) {
        return payoutRepository.findBySellerId(sellerId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<CommissionRecord> getCommissions(Long sellerId, Pageable pageable) {
        return commissionRepository.findBySellerId(sellerId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Order> getSellerOrders(Long sellerId, Pageable pageable) {
        return orderRepository.findBySellerIdWithDetails(sellerId, pageable);
    }
}

