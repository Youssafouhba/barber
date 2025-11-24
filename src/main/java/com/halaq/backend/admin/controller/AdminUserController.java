package com.halaq.backend.admin.controller;

import com.halaq.backend.core.controller.AbstractController;
import com.halaq.backend.core.security.controller.converter.UserConverter;
import com.halaq.backend.core.security.controller.dto.UserDto;
import com.halaq.backend.core.security.entity.User;
import com.halaq.backend.core.security.repository.criteria.core.UserCriteria;
import com.halaq.backend.core.security.service.facade.UserService;
import com.halaq.backend.core.util.PaginatedList;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin controller for managing all users in the system.
 * Only accessible by users with ADMIN role.
 */
@Tag(name = "Admin - User Management")
@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController extends AbstractController<User, UserDto, UserCriteria, UserService, UserConverter> {

    public AdminUserController(UserService service, UserConverter converter) {
        super(service, converter);
    }

    @Operation(summary = "Get all users (Admin only)")
    @GetMapping
    public ResponseEntity<List<UserDto>> findAll() throws Exception {
        return super.findAll();
    }

    @Operation(summary = "Get all users optimized (Admin only)")
    @GetMapping("/optimized")
    public ResponseEntity<List<UserDto>> findAllOptimized() throws Exception {
        return super.findAllOptimized();
    }

    @Operation(summary = "Get user by ID (Admin only)")
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> findById(@PathVariable Long id) {
        return super.findById(id);
    }

    @Operation(summary = "Get user with associated lists by ID (Admin only)")
    @GetMapping("/{id}/with-lists")
    public ResponseEntity<UserDto> findWithAssociatedLists(@PathVariable Long id) {
        return super.findWithAssociatedLists(id);
    }

    @Operation(summary = "Get user by email (Admin only)")
    @GetMapping("/email/{email}")
    public ResponseEntity<UserDto> findByEmail(@PathVariable String email) {
        return super.findByReferenceEntity(new User(email));
    }

    @Operation(summary = "Get user by username (Admin only)")
    @GetMapping("/username/{username}")
    public ResponseEntity<UserDto> findByUsername(@PathVariable String username) {
        User user = service.findByUsername(username);
        if (user != null) {
            UserDto userDto = converter.toDto(user);
            return ResponseEntity.ok(userDto);
        }
        return ResponseEntity.notFound().build();
    }

    @Operation(summary = "Create a new user (Admin only)")
    @PostMapping
    public ResponseEntity<UserDto> create(@RequestBody UserDto dto) throws Exception {
        return super.save(dto);
    }

    @Operation(summary = "Update user (Admin only)")
    @PutMapping
    public ResponseEntity<UserDto> update(@RequestBody UserDto dto) throws Exception {
        return super.update(dto);
    }

    @Operation(summary = "Delete user by ID (Admin only)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Long> deleteById(@PathVariable Long id) throws Exception {
        return super.deleteById(id);
    }

    @Operation(summary = "Delete multiple users by IDs (Admin only)")
    @DeleteMapping("/multiple")
    public ResponseEntity<List<Long>> deleteByIdIn(@RequestBody List<Long> ids) throws Exception {
        return super.deleteByIdIn(ids);
    }

    @Operation(summary = "Delete user by username (Admin only)")
    @DeleteMapping("/username/{username}")
    public ResponseEntity<Void> deleteByUsername(@PathVariable String username) {
        int deleted = service.deleteByUsername(username);
        if (deleted > 0) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @Operation(summary = "Find users by criteria (Admin only)")
    @PostMapping("/find-by-criteria")
    public ResponseEntity<List<UserDto>> findByCriteria(@RequestBody UserCriteria criteria) throws Exception {
        return super.findByCriteria(criteria);
    }

    @Operation(summary = "Find paginated users by criteria (Admin only)")
    @PostMapping("/find-paginated-by-criteria")
    public ResponseEntity<PaginatedList> findPaginatedByCriteria(@RequestBody UserCriteria criteria) throws Exception {
        return super.findPaginatedByCriteria(criteria);
    }

    @Operation(summary = "Export users by criteria (Admin only)")
    @PostMapping("/export")
    public ResponseEntity<InputStreamResource> export(@RequestBody UserCriteria criteria) throws Exception {
        return super.export(criteria);
    }

    @Operation(summary = "Get user data size by criteria (Admin only)")
    @PostMapping("/data-size-by-criteria")
    public ResponseEntity<Integer> getDataSize(@RequestBody UserCriteria criteria) throws Exception {
        return super.getDataSize(criteria);
    }
}

