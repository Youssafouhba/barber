package com.halaq.backend.admin.controller;

import com.halaq.backend.admin.service.AdminDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Admin controller for dashboard statistics and analytics.
 * Only accessible by users with ADMIN role.
 */
@Tag(name = "Admin - Dashboard")
@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    public AdminDashboardController(AdminDashboardService adminDashboardService) {
        this.adminDashboardService = adminDashboardService;
    }

    @Operation(summary = "Get dashboard overview statistics (Admin only)")
    @GetMapping("/overview")
    public ResponseEntity<Map<String, Object>> getOverview() {
        Map<String, Object> overview = adminDashboardService.getOverviewStatistics();
        return ResponseEntity.ok(overview);
    }

    @Operation(summary = "Get user statistics (Admin only)")
    @GetMapping("/users")
    public ResponseEntity<Map<String, Object>> getUserStatistics() {
        Map<String, Object> stats = adminDashboardService.getUserStatistics();
        return ResponseEntity.ok(stats);
    }

    @Operation(summary = "Get barber statistics (Admin only)")
    @GetMapping("/barbers")
    public ResponseEntity<Map<String, Object>> getBarberStatistics() {
        Map<String, Object> stats = adminDashboardService.getBarberStatistics();
        return ResponseEntity.ok(stats);
    }

    @Operation(summary = "Get booking statistics (Admin only)")
    @GetMapping("/bookings")
    public ResponseEntity<Map<String, Object>> getBookingStatistics() {
        Map<String, Object> stats = adminDashboardService.getBookingStatistics();
        return ResponseEntity.ok(stats);
    }

    @Operation(summary = "Get transaction statistics (Admin only)")
    @GetMapping("/transactions")
    public ResponseEntity<Map<String, Object>> getTransactionStatistics() {
        Map<String, Object> stats = adminDashboardService.getTransactionStatistics();
        return ResponseEntity.ok(stats);
    }

    @Operation(summary = "Get revenue statistics (Admin only)")
    @GetMapping("/revenue")
    public ResponseEntity<Map<String, Object>> getRevenueStatistics() {
        Map<String, Object> stats = adminDashboardService.getRevenueStatistics();
        return ResponseEntity.ok(stats);
    }
}

