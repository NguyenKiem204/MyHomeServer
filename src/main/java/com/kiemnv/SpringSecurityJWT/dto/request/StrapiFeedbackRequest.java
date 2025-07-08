package com.kiemnv.SpringSecurityJWT.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StrapiFeedbackRequest {
    private FeedbackData data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FeedbackData {
        @JsonProperty("Type")
        private String type;

        @JsonProperty("Title")
        private String title;

        @JsonProperty("Content")
        private String content;

        @JsonProperty("PhoneNumber")
        private String phoneNumber;
    }
}