package com.kiemnv.SpringSecurityJWT.service;

import com.kiemnv.SpringSecurityJWT.dto.response.ServiceFeeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class ServiceFeeService {

    private final RestTemplate restTemplate;
    private static final String STRAPI_SERVICE_FEES_URL = "https://usable-dinosaurs-b795619e4a.strapiapp.com/api/service-fees";

    public ServiceFeeResponse getServiceFees() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
            headers.set("User-Agent", "Spring Boot Application");

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<ServiceFeeResponse> response = restTemplate.exchange(
                    STRAPI_SERVICE_FEES_URL, HttpMethod.GET, entity, ServiceFeeResponse.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            } else {
                System.err.println("Failed to retrieve service fees: " + response.getStatusCode());
                return null;
            }
        } catch (Exception e) {
            System.err.println("Error while fetching service fees: " + e.getMessage());
            return null;
        }
    }
}