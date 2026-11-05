package com.bookloop.dto;

/**
 * DTO for admin dashboard summary statistics.
 * All values are derived from real database queries.
 */
public class AdminDashboardStats {

    private long totalUsers;
    private long activeUsers;
    private long totalListings;
    private long availableListings;
    private long soldListings;
    private long hiddenListings;
    private long totalOrders;
    private long completedOrders;
    private long pendingOrders;
    private long pendingReports;
    private long totalReviews;

    public AdminDashboardStats() {
    }

    public long getTotalUsers() { return totalUsers; }
    public void setTotalUsers(long totalUsers) { this.totalUsers = totalUsers; }

    public long getActiveUsers() { return activeUsers; }
    public void setActiveUsers(long activeUsers) { this.activeUsers = activeUsers; }

    public long getTotalListings() { return totalListings; }
    public void setTotalListings(long totalListings) { this.totalListings = totalListings; }

    public long getAvailableListings() { return availableListings; }
    public void setAvailableListings(long availableListings) { this.availableListings = availableListings; }

    public long getSoldListings() { return soldListings; }
    public void setSoldListings(long soldListings) { this.soldListings = soldListings; }

    public long getHiddenListings() { return hiddenListings; }
    public void setHiddenListings(long hiddenListings) { this.hiddenListings = hiddenListings; }

    public long getTotalOrders() { return totalOrders; }
    public void setTotalOrders(long totalOrders) { this.totalOrders = totalOrders; }

    public long getCompletedOrders() { return completedOrders; }
    public void setCompletedOrders(long completedOrders) { this.completedOrders = completedOrders; }

    public long getPendingOrders() { return pendingOrders; }
    public void setPendingOrders(long pendingOrders) { this.pendingOrders = pendingOrders; }

    public long getPendingReports() { return pendingReports; }
    public void setPendingReports(long pendingReports) { this.pendingReports = pendingReports; }

    public long getTotalReviews() { return totalReviews; }
    public void setTotalReviews(long totalReviews) { this.totalReviews = totalReviews; }
}

