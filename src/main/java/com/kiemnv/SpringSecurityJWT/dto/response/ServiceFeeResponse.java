package com.kiemnv.SpringSecurityJWT.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime; // Import for LocalDateTime
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceFeeResponse {
    private List<ServiceFeeData> data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServiceFeeData {
        private int id;
        @JsonProperty("documentId")
        private String documentId;
        @JsonProperty("FeeName")
        private String feeName;
        @JsonProperty("Description")
        private String description;
        @JsonProperty("DefaultAmount")
        private Integer defaultAmount;
        @JsonProperty("FeeUnit")
        private String feeUnit;
        @JsonProperty("createdAt") // Add createdAt field
        private LocalDateTime createdAt;
        @JsonProperty("updatedAt") // Add updatedAt field
        private LocalDateTime updatedAt;
        @JsonProperty("publishedAt") // Add publishedAt field
        private LocalDateTime publishedAt;
    }
}