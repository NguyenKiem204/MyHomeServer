package com.kiemnv.SpringSecurityJWT.controller;


import com.kiemnv.SpringSecurityJWT.dto.response.ApiResponse;
import com.kiemnv.SpringSecurityJWT.entity.User;
import com.kiemnv.SpringSecurityJWT.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<User>>> getAllUsers(Pageable pageable) {
        Page<User> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(ApiResponse.success(users, "Users retrieved successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<ApiResponse<User>> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(user, "User retrieved successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<ApiResponse<User>> updateUser(@PathVariable Long id, @RequestBody User userUpdate) {
        User user = userService.updateUser(id, userUpdate);
        return ResponseEntity.ok(ApiResponse.success(user, "User updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success(null, "User deleted successfully"));
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<User>> getProfile(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.success(user, "Profile retrieved successfully"));
    }
}