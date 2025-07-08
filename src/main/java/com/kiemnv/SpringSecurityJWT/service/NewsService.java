package com.kiemnv.SpringSecurityJWT.service;

import com.kiemnv.SpringSecurityJWT.dto.response.NewsResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class NewsService {

    @Value("${news.api.key}")
    private String apiKey;

    @Value("${news.api.base-url:https://newsapi.org/v2}")
    private String baseUrl;

    private final RestTemplate restTemplate;

    public NewsService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

//    @Cacheable(value = "news", key = "#q + '-' + #domains + '-' + #from + '-' + #to + '-' + #sortBy + '-' + #language + '-' + #pageSize + '-' + #page",
//            unless = "#result == null")
    public NewsResponse getEverything(String q, String domains, String from, String to,
                                      String sortBy, String language, int pageSize, int page) {

        log.info("NewsService.getEverything called with params: q={}, domains={}, from={}, to={}, sortBy={}, language={}, pageSize={}, page={}",
                q, domains, from, to, sortBy, language, pageSize, page);

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl + "/everything")
                .queryParam("apiKey", apiKey)
                .queryParam("sortBy", sortBy)
                .queryParam("language", language)
                .queryParam("pageSize", pageSize)
                .queryParam("page", page);

        if (q != null && !q.trim().isEmpty()) {
            builder.queryParam("q", q);
        }
        if (domains != null && !domains.trim().isEmpty()) {
            builder.queryParam("domains", domains);
        }
        if (from != null && !from.trim().isEmpty()) {
            builder.queryParam("from", from);
        }
        if (to != null && !to.trim().isEmpty()) {
            builder.queryParam("to", to);
        }

        String url = builder.toUriString();
        log.info("Making request to URL: {}", url);
        System.out.println(url);
        return makeRequest(url);
    }

    @Cacheable(value = "headlines", key = "#category + '-' + #country + '-' + #sources + '-' + #q + '-' + #pageSize + '-' + #page",
            unless = "#result == null")
    public NewsResponse getTopHeadlines(String category, String country, String sources, String q, int pageSize, int page) {

        log.info("NewsService.getTopHeadlines called with params: category={}, country={}, sources={}, q={}, pageSize={}, page={}",
                category, country, sources, q, pageSize, page);

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl + "/top-headlines")
                .queryParam("apiKey", apiKey)
                .queryParam("pageSize", pageSize)
                .queryParam("page", page);

        if (category != null && !category.trim().isEmpty()) {
            builder.queryParam("category", category);
        }
        if (country != null && !country.trim().isEmpty()) {
            builder.queryParam("country", country);
        }
        if (sources != null && !sources.trim().isEmpty()) {
            builder.queryParam("sources", sources);
        }
        if (q != null && !q.trim().isEmpty()) {
            builder.queryParam("q", q);
        }

        String url = builder.toUriString();
        log.info("Making request to URL: {}", url);

        return makeRequest(url);
    }

    @Cacheable(value = "search", key = "#query + '-' + #category + '-' + #pageSize + '-' + #page", unless = "#result == null")
    public NewsResponse searchNews(String query, String category, int pageSize, int page) {

        log.info("NewsService.searchNews called with params: query={}, category={}, pageSize={}, page={}",
                query, category, pageSize, page);

        if (query == null || query.trim().isEmpty()) {
            throw new IllegalArgumentException("Query không được để trống");
        }

        String finalQuery = query.trim();

        if (category != null && !category.trim().isEmpty() && !"all".equalsIgnoreCase(category.trim())) {
            String categoryKeywords = mapCategoryToKeywords(category);
            if (categoryKeywords != null) {
                finalQuery = "(" + query + ") AND (" + categoryKeywords + ")";
            }
        }

        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDate weekAgo = today.minusDays(7);

        return getEverything(finalQuery, null, weekAgo.toString(), today.toString(),
                "relevancy", "en", pageSize, page);
    }

    public void testConnection() {
        try {
            log.info("Testing connection to NewsAPI...");

            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl + "/top-headlines")
                    .queryParam("apiKey", apiKey)
                    .queryParam("country", "us")
                    .queryParam("pageSize", "1");

            String url = builder.toUriString();
            NewsResponse response = makeRequest(url);

            if (response != null) {
                log.info("NewsAPI connection test successful");
            } else {
                throw new RuntimeException("NewsAPI connection test failed - null response");
            }

        } catch (Exception e) {
            log.error("NewsAPI connection test failed: {}", e.getMessage());
            throw new RuntimeException("NewsAPI connection failed: " + e.getMessage(), e);
        }
    }

    private String mapCategoryToKeywords(String category) {
        if (category == null) return null;

        switch (category.toLowerCase().trim()) {
            case "tech":
            case "technology":
                return "technology OR programming OR software OR AI OR tech OR startup OR blockchain";
            case "business":
                return "business OR finance OR economy OR market OR company OR corporate";
            case "science":
                return "science OR research OR study OR discovery OR scientific OR innovation";
            case "health":
                return "health OR medical OR healthcare OR medicine OR wellness";
            case "sports":
                return "sports OR football OR basketball OR soccer OR athletics";
            case "entertainment":
                return "entertainment OR movie OR music OR celebrity OR hollywood";
            default:
                return null;
        }
    }

    private NewsResponse makeRequest(String url) {
        try {
            log.debug("Making HTTP request to: {}", url);

            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "NewsApp/1.0");
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(java.util.Collections.singletonList(MediaType.APPLICATION_JSON));

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<NewsResponse> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, NewsResponse.class);

            log.debug("Received response with status: {}", response.getStatusCode());

            if (response.getStatusCode() == HttpStatus.OK) {
                NewsResponse newsResponse = response.getBody();
                if (newsResponse != null) {
                    log.info("Successfully received {} articles",
                            newsResponse.getArticles() != null ? newsResponse.getArticles().size() : 0);
                }
                return newsResponse;
            } else {
                throw new RuntimeException("NewsAPI returned non-OK status: " + response.getStatusCode());
            }

        } catch (HttpClientErrorException e) {
            log.error("HTTP Client Error: Status={}, Body={}", e.getStatusCode(), e.getResponseBodyAsString());

            switch (e.getStatusCode()) {
                case HttpStatus.UNAUTHORIZED:
                    throw new RuntimeException("NewsAPI: API key không hợp lệ hoặc đã hết hạn", e);
                case HttpStatus.TOO_MANY_REQUESTS:
                    throw new RuntimeException("NewsAPI: Đã vượt quá giới hạn số lượng request", e);
                case HttpStatus.BAD_REQUEST:
                    throw new RuntimeException("NewsAPI: Tham số request không hợp lệ", e);
                default:
                    throw new RuntimeException("NewsAPI Error (" + e.getStatusCode() + "): " + e.getResponseBodyAsString(), e);
            }

        } catch (Exception e) {
            log.error("Unexpected error calling NewsAPI: {}", e.getMessage(), e);
            throw new RuntimeException("Lỗi kết nối NewsAPI: " + e.getMessage(), e);
        }
    }
}