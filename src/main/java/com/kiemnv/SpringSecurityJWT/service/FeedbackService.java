package com.kiemnv.SpringSecurityJWT.service;

import com.kiemnv.SpringSecurityJWT.dto.request.FeedbackRequest;
import com.kiemnv.SpringSecurityJWT.dto.request.StrapiFeedbackRequest;
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
public class FeedbackService {

    private final RestTemplate restTemplate;
    private static final String STRAPI_URL = "https://usable-dinosaurs-b795619e4a.strapiapp.com/api/feedbacks";

    public boolean sendFeedback(FeedbackRequest request) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Accept", "application/json");
            headers.set("User-Agent", "Spring Boot Application");
            headers.set("Accept-Encoding", "gzip, deflate, br");
            headers.set("Connection", "keep-alive");

            StrapiFeedbackRequest.FeedbackData data = new StrapiFeedbackRequest.FeedbackData(
                    request.getType(),
                    request.getTitle(),
                    request.getContent(),
                    request.getPhoneNumber()
            );
            StrapiFeedbackRequest strapiRequest = new StrapiFeedbackRequest(data);

            HttpEntity<StrapiFeedbackRequest> entity = new HttpEntity<>(strapiRequest, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    STRAPI_URL, HttpMethod.POST, entity, String.class
            );

            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            return false;
        }
    }
}
