// src/main/java/com/kiemnv/SpringSecurityJWT/entity/User.java
// (Giữ nguyên các trường đã thêm cho zaloId, avatarUrl, phoneNumber)
package com.kiemnv.SpringSecurityJWT.entity;

import jakarta.persistence.*;

import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = true)
    private String email;

    @Column(nullable = true)
    private String password;

    @Column(name = "first_name", columnDefinition = "NVARCHAR(255)")
    private String firstName;

    @Column(name = "last_name", columnDefinition = "NVARCHAR(255)")
    private String lastName;

    @Column(unique = true, nullable = true)
    private String zaloId;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Builder.Default // Giá trị mặc định khi dùng @Builder.build()
    private UserStatus status = UserStatus.ACTIVE;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    @Builder.Default
    private Set<Role> roles = Set.of(Role.USER); // Tuy nhiên, sẽ override trong AuthService cho Zalo User

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                .collect(Collectors.toSet());
    }

    @Override
    public boolean isAccountNonExpired() {
        return status == UserStatus.ACTIVE;
    }

    @Override
    public boolean isAccountNonLocked() {
        return status != UserStatus.LOCKED && status != UserStatus.PENDING_APPROVAL;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return status == UserStatus.ACTIVE;
    }
}