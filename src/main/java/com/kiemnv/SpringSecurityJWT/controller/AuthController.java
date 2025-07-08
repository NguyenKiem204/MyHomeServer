package com.kiemnv.SpringSecurityJWT.controller;

import com.kiemnv.SpringSecurityJWT.dto.request.LoginRequest;
import com.kiemnv.SpringSecurityJWT.dto.request.RefreshTokenRequest;
import com.kiemnv.SpringSecurityJWT.dto.request.RegisterRequest;
import com.kiemnv.SpringSecurityJWT.dto.request.ZaloLoginRequest;
import com.kiemnv.SpringSecurityJWT.dto.response.ApiResponse;
import com.kiemnv.SpringSecurityJWT.dto.response.AuthResponse;
import com.kiemnv.SpringSecurityJWT.entity.User;
import com.kiemnv.SpringSecurityJWT.exception.PendingApprovalException;
import com.kiemnv.SpringSecurityJWT.exception.UserAccountStatusException;
import com.kiemnv.SpringSecurityJWT.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j // Thêm annotation này
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request,
                                                           HttpServletResponse response) {
        AuthResponse authResponse = authService.login(request, response);
        return ResponseEntity.ok(ApiResponse.success(authResponse, "Login successful"));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request,
                                                              HttpServletResponse response) {
        AuthResponse authResponse = authService.register(request, response);
        return ResponseEntity.ok(ApiResponse.success(authResponse, "Registration successful"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(HttpServletRequest request,
                                                                  HttpServletResponse response) {
        String refreshToken = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refreshToken".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }

        if (refreshToken == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Refresh token not found in cookie", 400));
        }

        AuthResponse authResponse = authService.refreshToken(refreshToken, response);
        return ResponseEntity.ok(ApiResponse.success(authResponse, "Token refreshed successfully"));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success(null, "Logout successful"));
    }

    @PostMapping("/logout-all")
    public ResponseEntity<ApiResponse<Void>> logoutAll(Authentication authentication) {
        authService.logoutAll(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(null, "Logged out from all devices"));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<AuthResponse.UserInfo>> getCurrentUser(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        AuthResponse.UserInfo userInfo = AuthResponse.UserInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .roles(user.getAuthorities().stream()
                        .map(auth -> auth.getAuthority())
                        .collect(java.util.stream.Collectors.toSet()))
                .lastLogin(user.getLastLogin())
                .avatarUrl(user.getAvatarUrl())
                .phoneNumber(user.getPhoneNumber())
                .build();

        return ResponseEntity.ok(ApiResponse.success(userInfo, "Thông tin người dùng được lấy thành công"));
    }

    @PostMapping("/zalo-login")
    public ResponseEntity<ApiResponse<AuthResponse>> zaloLogin(@Valid @RequestBody ZaloLoginRequest request,
                                                               HttpServletResponse response) {
        try {
            // Sửa: truyền accessToken thay vì authCode
            AuthResponse authResponse = authService.zaloLogin(request.getAccessToken(), response);
            return ResponseEntity.ok(ApiResponse.success(authResponse, "Zalo login successful"));
        } catch (PendingApprovalException e) {
            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body(ApiResponse.error(e.getMessage(), 202));
        } catch (UserAccountStatusException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(e.getMessage(), 403));
        } catch (Exception e) {
            log.error("Zalo login failed: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Zalo login failed: " + e.getMessage(), 401));
        }
    }
}