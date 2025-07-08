package com.kiemnv.SpringSecurityJWT.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StrapiBuildingServiceResponse {
    private List<StrapiBuildingServiceData> data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StrapiBuildingServiceData {
        private Long id;
        // Các trường này nằm trực tiếp trong "data" object, không phải trong "attributes"
        @JsonProperty("name")
        private String name;
        @JsonProperty("description")
        private String description;
        @JsonProperty("icon")
        private String icon;
        @JsonProperty("color")
        private String color;
        @JsonProperty("category")
        private String category;
        // isRegistered được trả về là null, nên giữ nguyên kiểu Boolean hoặc xử lý sau
        @JsonProperty("isRegistered")
        private Boolean isRegistered; // Changed to Boolean to handle null
        @JsonProperty("price")
        private String price;
        @JsonProperty("features")
        private List<String> features;
        @JsonProperty("userCount")
        private String userCount;
        @JsonProperty("location")
        private String location;
        @JsonProperty("operatingHours")
        private String operatingHours;
        @JsonProperty("createdAt")
        private LocalDateTime createdAt;
        @JsonProperty("updatedAt")
        private LocalDateTime updatedAt;
        @JsonProperty("publishedAt")
        private LocalDateTime publishedAt;
    }
}