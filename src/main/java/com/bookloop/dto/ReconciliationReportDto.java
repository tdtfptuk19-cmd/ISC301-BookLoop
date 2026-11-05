package com.bookloop.dto;

import com.bookloop.entity.ReconciliationStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReconciliationReportDto {

    private LocalDateTime runAt;
    private long totalOrdersChecked;
    private long totalPaymentsChecked;
    private long totalPayoutsChecked;
    private BigDecimal expectedEscrowBalance;
    private BigDecimal calculatedEscrowBalance;
    private boolean escrowBalanced;
    private int discrepancyCount;
    private List<DiscrepancyItem> discrepancies = new ArrayList<>();

    // Detailed audited items
    private List<ReconciliationItemDto> items = new ArrayList<>();
    private long matchedCount;
    private long mismatchCount;
    private long pendingCount;
    private long missingDataCount;
    private long errorCount;
    private BigDecimal totalGmvChecked;
    private BigDecimal totalDisbursedChecked;
    private BigDecimal totalCommissionChecked;

    public ReconciliationReportDto() {
        this.runAt = LocalDateTime.now();
        this.expectedEscrowBalance = BigDecimal.ZERO;
        this.calculatedEscrowBalance = BigDecimal.ZERO;
        this.escrowBalanced = true;
        this.discrepancyCount = 0;
        this.matchedCount = 0;
        this.mismatchCount = 0;
        this.pendingCount = 0;
        this.missingDataCount = 0;
        this.errorCount = 0;
        this.totalGmvChecked = BigDecimal.ZERO;
        this.totalDisbursedChecked = BigDecimal.ZERO;
        this.totalCommissionChecked = BigDecimal.ZERO;
    }

    public void addDiscrepancy(String rule, String reference, String description, String severity) {
        discrepancies.add(new DiscrepancyItem(rule, reference, description, severity));
        this.discrepancyCount = discrepancies.size();
    }

    public void addItem(ReconciliationItemDto item) {
        this.items.add(item);
        if (item.getStatus() == ReconciliationStatus.MATCHED) {
            this.matchedCount++;
        } else if (item.getStatus() == ReconciliationStatus.MISMATCH) {
            this.mismatchCount++;
        } else if (item.getStatus() == ReconciliationStatus.PENDING) {
            this.pendingCount++;
        } else if (item.getStatus() == ReconciliationStatus.MISSING_PROVIDER_DATA || item.getStatus() == ReconciliationStatus.MISSING_INTERNAL_DATA) {
            this.missingDataCount++;
        } else if (item.getStatus() == ReconciliationStatus.ERROR) {
            this.errorCount++;
        }
        if (item.getOrderGrossAmount() != null) {
            this.totalGmvChecked = this.totalGmvChecked.add(item.getOrderGrossAmount());
        }
        if (item.getPayoutAmount() != null) {
            this.totalDisbursedChecked = this.totalDisbursedChecked.add(item.getPayoutAmount());
        }
        if (item.getCommissionAmount() != null) {
            this.totalCommissionChecked = this.totalCommissionChecked.add(item.getCommissionAmount());
        }
    }

    public List<ReconciliationItemDto> getFilteredItems(String statusFilter, String keyword) {
        return items.stream()
                .filter(item -> {
                    if (statusFilter != null && !statusFilter.isBlank() && !"ALL".equalsIgnoreCase(statusFilter)) {
                        if (!item.getStatus().name().equalsIgnoreCase(statusFilter.trim())) {
                            return false;
                        }
                    }
                    if (keyword != null && !keyword.isBlank()) {
                        String kw = keyword.trim().toLowerCase();
                        boolean matchOrder = item.getOrderNumber() != null && item.getOrderNumber().toLowerCase().contains(kw);
                        boolean matchRef = item.getPaymentReference() != null && item.getPaymentReference().toLowerCase().contains(kw);
                        boolean matchProv = item.getProviderReference() != null && item.getProviderReference().toLowerCase().contains(kw);
                        boolean matchNotes = item.getNotes() != null && item.getNotes().toLowerCase().contains(kw);
                        if (!matchOrder && !matchRef && !matchProv && !matchNotes) {
                            return false;
                        }
                    }
                    return true;
                })
                .toList();
    }

    // --- Getters & Setters ---

    public LocalDateTime getRunAt() {
        return runAt;
    }

    public void setRunAt(LocalDateTime runAt) {
        this.runAt = runAt;
    }

    public long getTotalOrdersChecked() {
        return totalOrdersChecked;
    }

    public void setTotalOrdersChecked(long totalOrdersChecked) {
        this.totalOrdersChecked = totalOrdersChecked;
    }

    public long getTotalPaymentsChecked() {
        return totalPaymentsChecked;
    }

    public void setTotalPaymentsChecked(long totalPaymentsChecked) {
        this.totalPaymentsChecked = totalPaymentsChecked;
    }

    public long getTotalPayoutsChecked() {
        return totalPayoutsChecked;
    }

    public void setTotalPayoutsChecked(long totalPayoutsChecked) {
        this.totalPayoutsChecked = totalPayoutsChecked;
    }

    public BigDecimal getExpectedEscrowBalance() {
        return expectedEscrowBalance;
    }

    public void setExpectedEscrowBalance(BigDecimal expectedEscrowBalance) {
        this.expectedEscrowBalance = expectedEscrowBalance;
    }

    public BigDecimal getCalculatedEscrowBalance() {
        return calculatedEscrowBalance;
    }

    public void setCalculatedEscrowBalance(BigDecimal calculatedEscrowBalance) {
        this.calculatedEscrowBalance = calculatedEscrowBalance;
    }

    public boolean isEscrowBalanced() {
        return escrowBalanced;
    }

    public void setEscrowBalanced(boolean escrowBalanced) {
        this.escrowBalanced = escrowBalanced;
    }

    public int getDiscrepancyCount() {
        return discrepancyCount;
    }

    public void setDiscrepancyCount(int discrepancyCount) {
        this.discrepancyCount = discrepancyCount;
    }

    public List<DiscrepancyItem> getDiscrepancies() {
        return discrepancies;
    }

    public void setDiscrepancies(List<DiscrepancyItem> discrepancies) {
        this.discrepancies = discrepancies;
        this.discrepancyCount = discrepancies.size();
    }

    public List<ReconciliationItemDto> getItems() {
        return items;
    }

    public void setItems(List<ReconciliationItemDto> items) {
        this.items = items;
    }

    public long getMatchedCount() {
        return matchedCount;
    }

    public void setMatchedCount(long matchedCount) {
        this.matchedCount = matchedCount;
    }

    public long getMismatchCount() {
        return mismatchCount;
    }

    public void setMismatchCount(long mismatchCount) {
        this.mismatchCount = mismatchCount;
    }

    public long getPendingCount() {
        return pendingCount;
    }

    public void setPendingCount(long pendingCount) {
        this.pendingCount = pendingCount;
    }

    public long getMissingDataCount() {
        return missingDataCount;
    }

    public void setMissingDataCount(long missingDataCount) {
        this.missingDataCount = missingDataCount;
    }

    public long getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(long errorCount) {
        this.errorCount = errorCount;
    }

    public BigDecimal getTotalGmvChecked() {
        return totalGmvChecked;
    }

    public void setTotalGmvChecked(BigDecimal totalGmvChecked) {
        this.totalGmvChecked = totalGmvChecked;
    }

    public BigDecimal getTotalDisbursedChecked() {
        return totalDisbursedChecked;
    }

    public void setTotalDisbursedChecked(BigDecimal totalDisbursedChecked) {
        this.totalDisbursedChecked = totalDisbursedChecked;
    }

    public BigDecimal getTotalCommissionChecked() {
        return totalCommissionChecked;
    }

    public void setTotalCommissionChecked(BigDecimal totalCommissionChecked) {
        this.totalCommissionChecked = totalCommissionChecked;
    }

    public static class DiscrepancyItem {
        private String rule;
        private String reference;
        private String description;
        private String severity; // HIGH, MEDIUM, LOW

        public DiscrepancyItem(String rule, String reference, String description, String severity) {
            this.rule = rule;
            this.reference = reference;
            this.description = description;
            this.severity = severity;
        }

        public String getRule() {
            return rule;
        }

        public String getReference() {
            return reference;
        }

        public String getDescription() {
            return description;
        }

        public String getSeverity() {
            return severity;
        }
    }
}
