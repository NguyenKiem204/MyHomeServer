package com.kiemnv.SpringSecurityJWT.dto.response;

import com.kiemnv.SpringSecurityJWT.entity.Article;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewsResponse {
    private String status;
    private int totalResults;
    private List<Article> articles;
    private String code;
    private String message;

    public NewsResponse(String status, int totalResults, List<Article> articles) {
        this.status = status;
        this.totalResults = totalResults;
        this.articles = articles;
    }
}