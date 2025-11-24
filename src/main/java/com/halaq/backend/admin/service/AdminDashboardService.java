package com.halaq.backend.admin.service;

import java.util.Map;

/**
 * Service interface for admin dashboard statistics and analytics.
 */
public interface AdminDashboardService {

    /**
     * Get overview statistics for the admin dashboard.
     * @return Map containing overview statistics
     */
    Map<String, Object> getOverviewStatistics();

    /**
     * Get user statistics.
     * @return Map containing user statistics
     */
    Map<String, Object> getUserStatistics();

    /**
     * Get barber statistics.
     * @return Map containing barber statistics
     */
    Map<String, Object> getBarberStatistics();

    /**
     * Get booking statistics.
     * @return Map containing booking statistics
     */
    Map<String, Object> getBookingStatistics();

    /**
     * Get transaction statistics.
     * @return Map containing transaction statistics
     */
    Map<String, Object> getTransactionStatistics();

    /**
     * Get revenue statistics.
     * @return Map containing revenue statistics
     */
    Map<String, Object> getRevenueStatistics();
}

