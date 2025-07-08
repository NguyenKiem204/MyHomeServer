package com.kiemnv.SpringSecurityJWT.service;

import com.kiemnv.SpringSecurityJWT.dto.response.BlogResponse;
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
public class BlogService {

    private final RestTemplate restTemplate;
    private static final String STRAPI_BLOGS_URL = "https://usable-dinosaurs-b795619e4a.strapiapp.com/api/blogs";

    public BlogResponse getBlogs() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
            headers.set("User-Agent", "Spring Boot Application"); // Good practice to set a User-Agent

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<BlogResponse> response = restTemplate.exchange(
                    STRAPI_BLOGS_URL, HttpMethod.GET, entity, BlogResponse.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            } else {
                // Handle error cases, e.g., log the error or throw a custom exception
                System.err.println("Failed to retrieve blogs: " + response.getStatusCode());
                return null;
            }
        } catch (Exception e) {
            System.err.println("Error while fetching blogs: " + e.getMessage());
            return null;
        }
    }
}