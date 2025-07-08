package com.kiemnv.SpringSecurityJWT.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime; // Import for LocalDateTime
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BuildingServiceDto {
    private Long id;
    private String name;
    private String description;
    private String icon;
    private String color;
    private String category;
    private boolean isRegistered;
    private String price;
    private List<String> features;
    private String userCount;
    private String location;
    private String operatingHours;
    private LocalDateTime createdAt; // Add createdAt
    private LocalDateTime updatedAt; // Add updatedAt
    private LocalDateTime publishedAt; // Add publishedAt

    public BuildingServiceDto(BuildingServiceDto other) {
        this.id = other.id;
        this.name = other.name;
        this.description = other.description;
        this.icon = other.icon;
        this.color = other.color;
        this.category = other.category;
        this.isRegistered = other.isRegistered;
        this.price = other.price;
        this.features = other.features != null ? new ArrayList<>(other.features) : null;
        this.userCount = other.userCount;
        this.location = other.location;
        this.operatingHours = other.operatingHours;
        this.createdAt = other.createdAt;
        this.updatedAt = other.updatedAt;
        this.publishedAt = other.publishedAt;
    }
}