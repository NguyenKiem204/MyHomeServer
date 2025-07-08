package com.kiemnv.SpringSecurityJWT.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Slf4j
@Service
public class ZaloService {

    @Value("${zalo.app.id}")
    private String zaloAppId;

    @Value("${zalo.app.secret-key}")
    private String zaloAppSecretKey;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate;

    public ZaloService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Tính toán appsecret_proof theo yêu cầu của Zalo API (bắt buộc từ 01/01/2024)
     * @param accessToken Zalo Access Token
     * @return appsecret_proof string
     */
    private String calculateAppSecretProof(String accessToken) {
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(zaloAppSecretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            byte[] mac_data = sha256_HMAC.doFinal(accessToken.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(mac_data);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("Error calculating appsecret_proof", e);
            throw new RuntimeException("Error calculating appsecret_proof", e);
        }
    }

    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    /**
     * Lấy thông tin profile người dùng Zalo từ Access Token
     * Theo hướng dẫn chính thức: Mini App sẽ gửi trực tiếp Access Token lên server
     * @param accessToken Zalo Access Token nhận từ Mini App frontend
     * @return JsonNode chứa thông tin người dùng Zalo
     */
    public JsonNode getZaloUserProfile(String accessToken) {
        try {
            // Tính toán appsecret_proof (bắt buộc từ 01/01/2024)
            String appSecretProof = calculateAppSecretProof(accessToken);

            String url = "https://graph.zalo.me/v2.0/me";

            HttpHeaders headers = new HttpHeaders();
            headers.set("access_token", accessToken);
            headers.set("appsecret_proof", appSecretProof);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            String fullUrl = url + "?fields=id,name,picture,birthday";

            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    fullUrl,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.getBody() != null) {
                JsonNode userProfile = objectMapper.readTree(responseEntity.getBody());

                if (userProfile.has("error") && userProfile.get("error").asInt() != 0) {
                    String errorMessage = userProfile.has("message") ? userProfile.get("message").asText() : "Unknown error";
                    log.error("Zalo API returned error: {}", errorMessage);
                    throw new RuntimeException("Zalo API error: " + errorMessage);
                }

                log.info("Successfully retrieved Zalo user profile for user ID: {}",
                        userProfile.has("id") ? userProfile.get("id").asText() : "unknown");

                return userProfile;
            } else {
                log.error("Failed to retrieve Zalo user profile. Status: {}, Body: {}",
                        responseEntity.getStatusCode(), responseEntity.getBody());
                throw new RuntimeException("Failed to retrieve Zalo user profile: HTTP " + responseEntity.getStatusCode());
            }

        } catch (Exception e) {
            log.error("Error calling Zalo Open API to get user profile: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve Zalo user profile: " + e.getMessage(), e);
        }
    }

    /**
     * Validate access token bằng cách thử gọi API lấy thông tin user
     * @param accessToken Zalo Access Token cần validate
     * @return true nếu token hợp lệ, false nếu không
     */
    public boolean validateAccessToken(String accessToken) {
        try {
            JsonNode userProfile = getZaloUserProfile(accessToken);
            return userProfile != null && userProfile.has("id");
        } catch (Exception e) {
            log.warn("Access token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Lấy Zalo User ID từ access token
     * @param accessToken Zalo Access Token
     * @return Zalo User ID
     */
    public String getZaloUserId(String accessToken) {
        JsonNode userProfile = getZaloUserProfile(accessToken);
        if (userProfile != null && userProfile.has("id")) {
            return userProfile.get("id").asText();
        }
        throw new RuntimeException("Cannot get Zalo User ID from access token");
    }
}