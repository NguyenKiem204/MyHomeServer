package com.kiemnv.SpringSecurityJWT.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.kiemnv.SpringSecurityJWT.config.JwtProperties;
import com.kiemnv.SpringSecurityJWT.dto.request.LoginRequest;
import com.kiemnv.SpringSecurityJWT.dto.request.RegisterRequest;
import com.kiemnv.SpringSecurityJWT.dto.response.AuthResponse;
import com.kiemnv.SpringSecurityJWT.entity.RefreshToken;
import com.kiemnv.SpringSecurityJWT.entity.Role;
import com.kiemnv.SpringSecurityJWT.entity.User;
import com.kiemnv.SpringSecurityJWT.entity.UserStatus;
import com.kiemnv.SpringSecurityJWT.exception.PendingApprovalException;
import com.kiemnv.SpringSecurityJWT.exception.TokenException;
import com.kiemnv.SpringSecurityJWT.exception.UserAccountStatusException;
import com.kiemnv.SpringSecurityJWT.exception.UserAlreadyExistsException;
import com.kiemnv.SpringSecurityJWT.repository.RefreshTokenRepository;
import com.kiemnv.SpringSecurityJWT.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final ZaloService zaloService;

    @Transactional
    public AuthResponse login(LoginRequest request, HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        User user = (User) authentication.getPrincipal();

        userRepository.updateLastLogin(user.getId(), LocalDateTime.now());

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        saveRefreshToken(user, refreshToken);

        addRefreshTokenCookie(response, refreshToken);

        return buildAuthResponse(user, accessToken, null);
    }

    @Transactional(noRollbackFor = {PendingApprovalException.class, UserAccountStatusException.class})
    public AuthResponse zaloLogin(String accessToken, HttpServletResponse response) {
        try {
            if (!zaloService.validateAccessToken(accessToken)) {
                throw new RuntimeException("Access token không hợp lệ hoặc đã hết hạn");
            }

            JsonNode zaloUserProfile = zaloService.getZaloUserProfile(accessToken);

            String zaloId = zaloUserProfile.get("id").asText();
            String name = zaloUserProfile.has("name") ? zaloUserProfile.get("name").asText() : null;
            String avatarUrl = zaloUserProfile.has("picture") && zaloUserProfile.get("picture").has("data")
                    ? zaloUserProfile.get("picture").get("data").get("url").asText() : null;

            User user;
            Optional<User> existingUserByZaloId = userRepository.findByZaloId(zaloId);

            if (existingUserByZaloId.isPresent()) {
                user = existingUserByZaloId.get();

                if (user.getStatus() == UserStatus.PENDING_APPROVAL) {
                    throw new PendingApprovalException("Tài khoản đang chờ duyệt. Vui lòng chờ quản trị viên phê duyệt.");
                } else if (user.getStatus() != UserStatus.ACTIVE) {
                    throw new UserAccountStatusException("Tài khoản của bạn đang " + user.getStatus().name().toLowerCase() + ". Vui lòng liên hệ hỗ trợ.");
                }

                updateUserInfoFromZalo(user, name, avatarUrl);
                user.setLastLogin(LocalDateTime.now());
                userRepository.save(user);

                // Tạo JWT tokens
                String jwtAccessToken = jwtService.generateAccessToken(user);
                String refreshToken = jwtService.generateRefreshToken(user);
                saveRefreshToken(user, refreshToken);
                addRefreshTokenCookie(response, refreshToken);

                return buildAuthResponse(user, jwtAccessToken, null);

            } else {
                // Tạo user mới
                String newUsername = generateUniqueUsername(zaloId);

                user = User.builder()
                        .username(newUsername)
                        .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                        .email(null)
                        .firstName(extractFirstName(name))
                        .lastName(extractLastName(name))
                        .zaloId(zaloId)
                        .avatarUrl(avatarUrl)
                        .phoneNumber(null)
                        .roles(Set.of(Role.USER))
                        .status(UserStatus.PENDING_APPROVAL)
                        .lastLogin(null)
                        .build();

                user = userRepository.save(user);
                throw new PendingApprovalException("Tài khoản của bạn đã được tạo và đang chờ phê duyệt. Vui lòng chờ quản trị viên phê duyệt.");
            }

        } catch (PendingApprovalException | UserAccountStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Lỗi trong quá trình đăng nhập Zalo: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể xác thực với Zalo: " + e.getMessage());
        }
    }

    private void updateUserInfoFromZalo(User user, String name, String avatarUrl) {
        if (name != null) {
            user.setFirstName(extractFirstName(name));
            user.setLastName(extractLastName(name));
        }
        if (avatarUrl != null) {
            user.setAvatarUrl(avatarUrl);
        }
    }

    private String extractFirstName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) return null;
        String[] parts = fullName.trim().split("\\s+");
        return parts[0];
    }

    private String extractLastName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) return null;
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length > 1) {
            return String.join(" ", Arrays.copyOfRange(parts, 1, parts.length));
        }
        return null;
    }

    private String generateUniqueUsername(String zaloId) {
        String baseUsername = "zalo_" + zaloId;
        if (!userRepository.existsByUsername(baseUsername)) {
            return baseUsername;
        }
        return "zalo_" + UUID.randomUUID().toString().substring(0, 8);
    }


    @Transactional
    public AuthResponse register(RegisterRequest request, HttpServletResponse response) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email already exists");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .roles(Set.of(Role.USER))
                .status(UserStatus.ACTIVE)
                .build();

        user = userRepository.save(user);

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        saveRefreshToken(user, refreshToken);

        addRefreshTokenCookie(response, refreshToken);

        return buildAuthResponse(user, accessToken, null);
    }

    @Transactional
    public AuthResponse refreshToken(String oldRefreshToken, HttpServletResponse response) {
        RefreshToken storedToken = refreshTokenRepository.findByToken(oldRefreshToken)
                .orElseThrow(() -> new TokenException("Invalid refresh token"));

        HttpServletRequest currentRequest = getCurrentRequest();
        String currentIpAddress = currentRequest != null ? getClientIpAddress(currentRequest) : "Unknown";

        if (!storedToken.getIpAddress().equals(currentIpAddress)) {
            log.warn("Refresh token used from different IP. Revoking token. Original IP: {}, Current IP: {}",
                    storedToken.getIpAddress(), currentIpAddress);
            refreshTokenRepository.revokeToken(oldRefreshToken);
            throw new TokenException("Refresh token used from unauthorized location.");
        }

        if (!storedToken.isValid()) {
            refreshTokenRepository.revokeToken(oldRefreshToken);
            throw new TokenException("Refresh token is expired or revoked");
        }

        User user = storedToken.getUser();

        if (user.getStatus() == UserStatus.PENDING_APPROVAL) {
            refreshTokenRepository.revokeToken(oldRefreshToken);
            throw new PendingApprovalException("Account is pending approval. Please wait for admin approval.");
        } else if (user.getStatus() != UserStatus.ACTIVE) {
            refreshTokenRepository.revokeToken(oldRefreshToken);
            throw new UserAccountStatusException("Your account is " + user.getStatus().name().toLowerCase() + ". Please contact support.");
        }

        String newAccessToken = jwtService.generateAccessToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user);

        refreshTokenRepository.revokeToken(oldRefreshToken);
        saveRefreshToken(user, newRefreshToken);

        addRefreshTokenCookie(response, newRefreshToken);

        return buildAuthResponse(user, newAccessToken, null);
    }

    @Transactional
    public void logout(String refreshToken) {
        refreshTokenRepository.findByToken(refreshToken)
                .ifPresent(token -> refreshTokenRepository.revokeToken(refreshToken));
    }

    @Transactional
    public void logoutAll(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        refreshTokenRepository.revokeAllUserTokens(user);
    }

    private void saveRefreshToken(User user, String token) {
        HttpServletRequest request = getCurrentRequest();

        RefreshToken refreshToken = RefreshToken.builder()
                .token(token)
                .user(user)
                .expiresAt(LocalDateTime.now().plusSeconds(jwtProperties.getRefreshTokenExpiration() / 1000))
                .deviceInfo(request != null ? request.getHeader("User-Agent") : "Unknown")
                .ipAddress(request != null ? getClientIpAddress(request) : "Unknown")
                .build();

        refreshTokenRepository.save(refreshToken);
    }

    private void addRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        long maxAgeSeconds = jwtProperties.getRefreshTokenExpiration() / 1000;

        String cookieValue = String.format("%s=%s; Path=/; HttpOnly; Secure; Max-Age=%d; SameSite=Lax",
                "refreshToken",
                refreshToken,
                maxAgeSeconds);

        response.addHeader("Set-Cookie", cookieValue);
    }

    private AuthResponse buildAuthResponse(User user, String accessToken, String refreshToken) {
        Set<String> roles = user.getRoles().stream()
                .map(role -> "ROLE_" + role.name())
                .collect(Collectors.toSet());

        AuthResponse.UserInfo userInfo = AuthResponse.UserInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .roles(roles)
                .lastLogin(user.getLastLogin())
                .build();

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtProperties.getAccessTokenExpiration() / 1000)
                .user(userInfo)
                .build();
    }

    private HttpServletRequest getCurrentRequest() {
        try {
            return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        } catch (Exception e) {
            return null;
        }
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}
