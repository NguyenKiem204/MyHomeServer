package com.kiemnv.SpringSecurityJWT.controller;

import com.kiemnv.SpringSecurityJWT.dto.response.ApiResponse;
import com.kiemnv.SpringSecurityJWT.entity.User;
import com.kiemnv.SpringSecurityJWT.entity.Role;
import com.kiemnv.SpringSecurityJWT.entity.UserStatus;
import com.kiemnv.SpringSecurityJWT.repository.UserRepository;
import com.kiemnv.SpringSecurityJWT.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')") // Chỉ ADMIN mới có thể truy cập
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<String>> getDashboard() {
        return ResponseEntity.ok(ApiResponse.success("Admin Dashboard Data", "Dashboard loaded"));
    }

    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<Object>> getStatistics() {
        // Implementation for admin statistics
        return ResponseEntity.ok(ApiResponse.success(null, "Statistics retrieved"));
    }

    @GetMapping("/users/pending-approval")
    public ResponseEntity<ApiResponse<Page<User>>> getPendingApprovalUsers(Pageable pageable) {
        Page<User> pendingUsers = userRepository.findByStatus(UserStatus.PENDING_APPROVAL, pageable);
        return ResponseEntity.ok(ApiResponse.success(pendingUsers, "Pending approval users retrieved successfully"));
    }
    @PutMapping("/users/{userId}/approve")
    public ResponseEntity<ApiResponse<User>> approveUser(@PathVariable Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);

        if (userOptional.isEmpty()) {
            return ResponseEntity.status(404).body(ApiResponse.error("User not found", 404));
        }

        User user = userOptional.get();
        if (user.getStatus() != UserStatus.PENDING_APPROVAL) {
            return ResponseEntity.status(400).body(ApiResponse.error("User is not in pending approval status", 400));
        }

        user.setStatus(UserStatus.ACTIVE);
        if (user.getRoles().isEmpty()) {
            user.setRoles(Set.of(Role.USER));
        } else if (!user.getRoles().contains(Role.USER)) {
            user.getRoles().add(Role.USER);
        }
        user.setUpdatedAt(LocalDateTime.now());
        User approvedUser = userRepository.save(user);

        return ResponseEntity.ok(ApiResponse.success(approvedUser, "User approved successfully"));
    }

    @PutMapping("/users/{userId}/reject")
    public ResponseEntity<ApiResponse<User>> rejectUser(@PathVariable Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);

        if (userOptional.isEmpty()) {
            return ResponseEntity.status(404).body(ApiResponse.error("User not found", 404));
        }

        User user = userOptional.get();
        if (user.getStatus() != UserStatus.PENDING_APPROVAL) {
            return ResponseEntity.status(400).body(ApiResponse.error("User is not in pending approval status", 400));
        }

        user.setStatus(UserStatus.INACTIVE);
        user.setUpdatedAt(LocalDateTime.now());
        User rejectedUser = userRepository.save(user);

        return ResponseEntity.ok(ApiResponse.success(rejectedUser, "User rejected successfully"));
    }

    @GetMapping("/users/by-status/{status}")
    public ResponseEntity<ApiResponse<Page<User>>> getUsersByStatus(@PathVariable String status, Pageable pageable) {
        try {
            UserStatus userStatus = UserStatus.valueOf(status.toUpperCase());
            Page<User> users = userRepository.findByStatus(userStatus, pageable);
            return ResponseEntity.ok(ApiResponse.success(users, "Users by status retrieved successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(ApiResponse.error("Invalid user status: " + status, 400));
        }
    }
}