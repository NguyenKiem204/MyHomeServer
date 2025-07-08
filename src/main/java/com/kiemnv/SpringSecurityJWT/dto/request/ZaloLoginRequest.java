package com.kiemnv.SpringSecurityJWT.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ZaloLoginRequest {
    @NotBlank(message = "Access token is required")
    private String accessToken;
}