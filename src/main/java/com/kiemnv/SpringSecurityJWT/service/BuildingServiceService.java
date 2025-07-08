package com.kiemnv.SpringSecurityJWT.service;

import com.kiemnv.SpringSecurityJWT.entity.BuildingServiceDto;
import com.kiemnv.SpringSecurityJWT.dto.response.StrapiBuildingServiceResponse; // Import DTO mới
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BuildingServiceService {

    private final RestTemplate restTemplate;
    private static final String STRAPI_BUILDING_SERVICES_URL = "https://usable-dinosaurs-b795619e4a.strapiapp.com/api/building-services";

    private final Map<String, Set<Long>> userRegistrations = new ConcurrentHashMap<>();

    public List<BuildingServiceDto> getAllServicesFromStrapi() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
            headers.set("User-Agent", "Spring Boot Application");

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<StrapiBuildingServiceResponse> response = restTemplate.exchange(
                    STRAPI_BUILDING_SERVICES_URL, HttpMethod.GET, entity, StrapiBuildingServiceResponse.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody().getData().stream()
                        .map(this::mapStrapiDataToDto)
                        .collect(Collectors.toList());
            } else {
                System.err.println("Failed to retrieve building services from Strapi: " + response.getStatusCode());
                return Collections.emptyList();
            }
        } catch (Exception e) {
            System.err.println("Error while fetching building services from Strapi: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    // Phương thức ánh xạ từ Strapi DTO sang BuildingServiceDto của bạn
    private BuildingServiceDto mapStrapiDataToDto(StrapiBuildingServiceResponse.StrapiBuildingServiceData strapiData) {
        // Trực tiếp truy cập các trường từ strapiData
        return BuildingServiceDto.builder()
                .id(strapiData.getId())
                .name(strapiData.getName())
                .description(strapiData.getDescription())
                .icon(strapiData.getIcon())
                .color(strapiData.getColor())
                .category(strapiData.getCategory())
                .price(strapiData.getPrice())
                .features(strapiData.getFeatures())
                .userCount(strapiData.getUserCount())
                .location(strapiData.getLocation())
                .operatingHours(strapiData.getOperatingHours())
                .createdAt(strapiData.getCreatedAt())
                .updatedAt(strapiData.getUpdatedAt())
                .publishedAt(strapiData.getPublishedAt())
                .isRegistered(false) // Mặc định là false, sẽ cập nhật sau
                .build();
    }

    public List<BuildingServiceDto> getAllServices() {
        return getAllServicesFromStrapi();
    }

    public List<BuildingServiceDto> getAllServicesForUser(String userId) {
        Set<Long> userRegisteredServices = userRegistrations.getOrDefault(userId, new HashSet<>());
        return getAllServicesFromStrapi().stream().map(service -> {
            BuildingServiceDto userService = new BuildingServiceDto(service);
            userService.setRegistered(userRegisteredServices.contains(service.getId()));
            return userService;
        }).toList();
    }

    public BuildingServiceDto getServiceById(Long id) {
        return getAllServicesFromStrapi().stream()
                .filter(s -> s.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public BuildingServiceDto getServiceByIdForUser(Long id, String userId) {
        BuildingServiceDto service = getServiceById(id);
        if (service != null) {
            BuildingServiceDto userService = new BuildingServiceDto(service);
            Set<Long> userRegisteredServices = userRegistrations.getOrDefault(userId, new HashSet<>());
            userService.setRegistered(userRegisteredServices.contains(id));
            return userService;
        }
        return null;
    }

    public boolean registerService(Long id, String userId) {
        BuildingServiceDto service = getServiceById(id);
        if (service != null) {
            userRegistrations.computeIfAbsent(userId, k -> new HashSet<>()).add(id);
            return true;
        }
        return false;
    }

    public boolean cancelService(Long id, String userId) {
        Set<Long> userRegisteredServices = userRegistrations.get(userId);
        if (userRegisteredServices != null && userRegisteredServices.contains(id)) {
            userRegisteredServices.remove(id);
            return true;
        }
        return false;
    }

    public List<BuildingServiceDto> getRegisteredServicesForUser(String userId) {
        Set<Long> userRegisteredServices = userRegistrations.getOrDefault(userId, new HashSet<>());

        return getAllServicesFromStrapi().stream()
                .filter(service -> userRegisteredServices.contains(service.getId()))
                .map(service -> {
                    BuildingServiceDto userService = new BuildingServiceDto(service);
                    userService.setRegistered(true);
                    return userService;
                })
                .toList();
    }
}