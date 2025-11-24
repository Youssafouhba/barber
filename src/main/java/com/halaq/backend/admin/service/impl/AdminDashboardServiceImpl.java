package com.halaq.backend.admin.service.impl;

import com.halaq.backend.admin.service.AdminDashboardService;
import com.halaq.backend.core.security.repository.criteria.core.UserCriteria;
import com.halaq.backend.core.security.service.facade.RoleUserService;
import com.halaq.backend.core.security.service.facade.UserService;
import com.halaq.backend.payment.criteria.TransactionCriteria;
import com.halaq.backend.payment.entity.Transaction;
import com.halaq.backend.payment.service.facade.TransactionService;
import com.halaq.backend.service.criteria.BookingCriteria;
import com.halaq.backend.service.entity.Booking;
import com.halaq.backend.service.service.facade.BookingService;
import com.halaq.backend.shared.BarberStatus;
import com.halaq.backend.shared.BookingStatus;
import com.halaq.backend.shared.TransactionStatus;
import com.halaq.backend.shared.TransactionType;
import com.halaq.backend.user.criteria.BarberCriteria;
import com.halaq.backend.user.entity.Barber;
import com.halaq.backend.user.repository.BarberRepository;
import com.halaq.backend.user.service.facade.BarberService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private static final Logger logger = LoggerFactory.getLogger(AdminDashboardServiceImpl.class);

    private final UserService userService;
    private final BarberService barberService;
    private final BarberRepository barberRepository;
    private final BookingService bookingService;
    private final TransactionService transactionService;
    private final RoleUserService roleUserService;

    public AdminDashboardServiceImpl(
            UserService userService,
            BarberService barberService,
            BarberRepository barberRepository,
            BookingService bookingService,
            TransactionService transactionService,
            RoleUserService roleUserService) {
        this.userService = userService;
        this.barberService = barberService;
        this.barberRepository = barberRepository;
        this.bookingService = bookingService;
        this.transactionService = transactionService;
        this.roleUserService = roleUserService;
    }

    @Override
    public Map<String, Object> getOverviewStatistics() {
        Map<String, Object> overview = new HashMap<>();
        try {
            // Total users
            UserCriteria userCriteria = new UserCriteria();
            int totalUsers = userService.getDataSize(userCriteria);
            overview.put("totalUsers", totalUsers);

            // Total barbers
            BarberCriteria barberCriteria = new BarberCriteria();
            int totalBarbers = barberService.getDataSize(barberCriteria);
            overview.put("totalBarbers", totalBarbers);

            // Total bookings
            BookingCriteria bookingCriteria = new BookingCriteria();
            int totalBookings = bookingService.getDataSize(bookingCriteria);
            overview.put("totalBookings", totalBookings);

            // Total transactions
            TransactionCriteria transactionCriteria = new TransactionCriteria();
            int totalTransactions = transactionService.getDataSize(transactionCriteria);
            overview.put("totalTransactions", totalTransactions);

            // Get barber statistics
            Map<String, Object> barberStats = getBarberStatistics();
            overview.put("barberStats", barberStats);

            // Get booking statistics
            Map<String, Object> bookingStats = getBookingStatistics();
            overview.put("bookingStats", bookingStats);

            // Get revenue statistics
            Map<String, Object> revenueStats = getRevenueStatistics();
            overview.put("revenueStats", revenueStats);

        } catch (Exception e) {
            logger.error("Error getting overview statistics", e);
        }
        return overview;
    }

    @Override
    public Map<String, Object> getUserStatistics() {
        Map<String, Object> stats = new HashMap<>();
        try {
            UserCriteria criteria = new UserCriteria();
            int totalUsers = userService.getDataSize(criteria);
            stats.put("totalUsers", totalUsers);

            // Count users by role (if needed, you can implement this)
            // For now, we'll use roleUserService to count by role
            long adminCount = roleUserService.countByRoleAuthority("ROLE_ADMIN");
            long barberCount = roleUserService.countByRoleAuthority("ROLE_BARBER");
            long clientCount = roleUserService.countByRoleAuthority("ROLE_CLIENT");

            stats.put("adminCount", adminCount);
            stats.put("barberCount", barberCount);
            stats.put("clientCount", clientCount);

        } catch (Exception e) {
            logger.error("Error getting user statistics", e);
        }
        return stats;
    }

    @Override
    public Map<String, Object> getBarberStatistics() {
        Map<String, Object> stats = new HashMap<>();
        try {
            List<Barber> allBarbers = barberService.findAllOptimized();
            long totalBarbers = allBarbers.size();

            // Count barbers by status
            long pendingBarbers = barberRepository.findByStatus(BarberStatus.PENDING_VALIDATION).size();
            long activeBarbers = barberRepository.findByStatus(BarberStatus.ACTIVE).size();
            long approvedBarbers = barberRepository.findByStatus(BarberStatus.APPROVED).size();
            long inactiveBarbers = barberRepository.findByStatus(BarberStatus.INACTIVE).size();
            long suspendedBarbers = barberRepository.findByStatus(BarberStatus.SUSPENDED).size();
            long rejectedBarbers = barberRepository.findByStatus(BarberStatus.REJECTED).size();

            stats.put("totalBarbers", totalBarbers);
            stats.put("pendingBarbers", pendingBarbers);
            stats.put("activeBarbers", activeBarbers);
            stats.put("approvedBarbers", approvedBarbers);
            stats.put("inactiveBarbers", inactiveBarbers);
            stats.put("suspendedBarbers", suspendedBarbers);
            stats.put("rejectedBarbers", rejectedBarbers);

            // Average rating
            double avgRating = allBarbers.stream()
                    .filter(b -> b.getAverageRating() != null && b.getAverageRating() > 0)
                    .mapToDouble(Barber::getAverageRating)
                    .average()
                    .orElse(0.0);
            stats.put("averageRating", avgRating);

        } catch (Exception e) {
            logger.error("Error getting barber statistics", e);
        }
        return stats;
    }

    @Override
    public Map<String, Object> getBookingStatistics() {
        Map<String, Object> stats = new HashMap<>();
        try {
            List<Booking> allBookings = bookingService.findAllOptimized();
            long totalBookings = allBookings.size();

            // Count bookings by status
            Map<BookingStatus, Long> bookingsByStatus = allBookings.stream()
                    .filter(b -> b.getStatus() != null)
                    .collect(Collectors.groupingBy(Booking::getStatus, Collectors.counting()));

            stats.put("totalBookings", totalBookings);
            stats.put("requestedBookings", bookingsByStatus.getOrDefault(BookingStatus.REQUESTED, 0L));
            stats.put("confirmedBookings", bookingsByStatus.getOrDefault(BookingStatus.CONFIRMED, 0L));
            stats.put("canceledBookings", bookingsByStatus.getOrDefault(BookingStatus.CANCELED, 0L));
            stats.put("inProgressBookings", bookingsByStatus.getOrDefault(BookingStatus.IN_PROGRESS, 0L));
            stats.put("completedBookings", bookingsByStatus.getOrDefault(BookingStatus.COMPLETED, 0L));
            stats.put("rejectedBookings", bookingsByStatus.getOrDefault(BookingStatus.REJECTED, 0L));

            // Upcoming bookings
            List<Booking> upcomingBookings = bookingService.findUpcomingBookings();
            stats.put("upcomingBookings", upcomingBookings.size());

        } catch (Exception e) {
            logger.error("Error getting booking statistics", e);
        }
        return stats;
    }

    @Override
    public Map<String, Object> getTransactionStatistics() {
        Map<String, Object> stats = new HashMap<>();
        try {
            List<Transaction> allTransactions = transactionService.findAllOptimized();
            long totalTransactions = allTransactions.size();

            // Count transactions by status
            Map<TransactionStatus, Long> transactionsByStatus = allTransactions.stream()
                    .filter(t -> t.getStatus() != null)
                    .collect(Collectors.groupingBy(Transaction::getStatus, Collectors.counting()));

            // Count transactions by type
            Map<TransactionType, Long> transactionsByType = allTransactions.stream()
                    .filter(t -> t.getType() != null)
                    .collect(Collectors.groupingBy(Transaction::getType, Collectors.counting()));

            stats.put("totalTransactions", totalTransactions);
            stats.put("pendingTransactions", transactionsByStatus.getOrDefault(TransactionStatus.PENDING, 0L));
            stats.put("completedTransactions", transactionsByStatus.getOrDefault(TransactionStatus.COMPLETED, 0L));
            stats.put("failedTransactions", transactionsByStatus.getOrDefault(TransactionStatus.FAILED, 0L));

            stats.put("rechargeTransactions", transactionsByType.getOrDefault(TransactionType.RECHARGE, 0L));
            stats.put("paymentTransactions", transactionsByType.getOrDefault(TransactionType.PAIEMENT, 0L));
            stats.put("refundTransactions", transactionsByType.getOrDefault(TransactionType.REMBOURSEMENT, 0L));

        } catch (Exception e) {
            logger.error("Error getting transaction statistics", e);
        }
        return stats;
    }

    @Override
    public Map<String, Object> getRevenueStatistics() {
        Map<String, Object> stats = new HashMap<>();
        try {
            List<Transaction> allTransactions = transactionService.findAllOptimized();

            // Calculate total revenue from completed payment transactions
            BigDecimal totalRevenue = allTransactions.stream()
                    .filter(t -> t.getType() == TransactionType.PAIEMENT)
                    .filter(t -> t.getStatus() == TransactionStatus.COMPLETED)
                    .filter(t -> t.getAmount() != null)
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Calculate total from recharge transactions
            BigDecimal totalRecharges = allTransactions.stream()
                    .filter(t -> t.getType() == TransactionType.RECHARGE)
                    .filter(t -> t.getStatus() == TransactionStatus.COMPLETED)
                    .filter(t -> t.getAmount() != null)
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Calculate total refunds
            BigDecimal totalRefunds = allTransactions.stream()
                    .filter(t -> t.getType() == TransactionType.REMBOURSEMENT)
                    .filter(t -> t.getStatus() == TransactionStatus.COMPLETED)
                    .filter(t -> t.getAmount() != null)
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            stats.put("totalRevenue", totalRevenue);
            stats.put("totalRecharges", totalRecharges);
            stats.put("totalRefunds", totalRefunds);
            stats.put("netRevenue", totalRevenue.subtract(totalRefunds));

        } catch (Exception e) {
            logger.error("Error getting revenue statistics", e);
        }
        return stats;
    }
}

