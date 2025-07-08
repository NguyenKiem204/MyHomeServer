package com.kiemnv.SpringSecurityJWT.service;


import com.kiemnv.SpringSecurityJWT.config.JwtProperties;
import com.kiemnv.SpringSecurityJWT.entity.User;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtProperties jwtProperties;

    public String generateAccessToken(User user) {
        try {
            JWSSigner signer = new MACSigner(jwtProperties.getSecretKey().getBytes());

            Instant now = Instant.now();
            Instant expiration = now.plusMillis(jwtProperties.getAccessTokenExpiration());

            List<String> authorities = user.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .subject(user.getUsername())
                    .issuer(jwtProperties.getIssuer())
                    .issueTime(Date.from(now))
                    .expirationTime(Date.from(expiration))
                    .claim("userId", user.getId())
                    .claim("email", user.getEmail())
                    .claim("authorities", authorities)
                    .claim("firstName", user.getFirstName())
                    .claim("lastName", user.getLastName())
                    .build();

            SignedJWT signedJWT = new SignedJWT(
                    new JWSHeader.Builder(JWSAlgorithm.HS512)
                            .type(JOSEObjectType.JWT)
                            .build(),
                    claimsSet
            );

            signedJWT.sign(signer);
            return signedJWT.serialize();

        } catch (JOSEException e) {
            log.error("Error generating access token", e);
            throw new RuntimeException("Could not generate access token", e);
        }
    }

    public String generateRefreshToken(User user) {
        try {
            JWSSigner signer = new MACSigner(jwtProperties.getSecretKey().getBytes());

            Instant now = Instant.now();
            Instant expiration = now.plusMillis(jwtProperties.getRefreshTokenExpiration());

            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .subject(user.getUsername())
                    .issuer(jwtProperties.getIssuer())
                    .issueTime(Date.from(now))
                    .expirationTime(Date.from(expiration))
                    .claim("userId", user.getId())
                    .claim("tokenType", "refresh")
                    .build();

            SignedJWT signedJWT = new SignedJWT(
                    new JWSHeader.Builder(JWSAlgorithm.HS512)
                            .type(JOSEObjectType.JWT)
                            .build(),
                    claimsSet
            );

            signedJWT.sign(signer);
            return signedJWT.serialize();

        } catch (JOSEException e) {
            log.error("Error generating refresh token", e);
            throw new RuntimeException("Could not generate refresh token", e);
        }
    }

    @Cacheable(value = "jwtValidation", key = "#token")
    public boolean validateToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            JWSVerifier verifier = new MACVerifier(jwtProperties.getSecretKey().getBytes());

            if (!signedJWT.verify(verifier)) {
                return false;
            }

            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
            return !claims.getExpirationTime().before(new Date());

        } catch (ParseException | JOSEException e) {
            log.debug("Invalid token: {}", e.getMessage());
            return false;
        }
    }

    public String getUsernameFromToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            return signedJWT.getJWTClaimsSet().getSubject();
        } catch (ParseException e) {
            log.error("Error extracting username from token", e);
            throw new RuntimeException("Could not extract username from token", e);
        }
    }

    public Long getUserIdFromToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            return signedJWT.getJWTClaimsSet().getLongClaim("userId");
        } catch (ParseException e) {
            log.error("Error extracting user ID from token", e);
            throw new RuntimeException("Could not extract user ID from token", e);
        }
    }

    public Date getExpirationFromToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            return signedJWT.getJWTClaimsSet().getExpirationTime();
        } catch (ParseException e) {
            log.error("Error extracting expiration from token", e);
            throw new RuntimeException("Could not extract expiration from token", e);
        }
    }
}