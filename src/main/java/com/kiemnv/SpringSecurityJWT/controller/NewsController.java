package com.kiemnv.SpringSecurityJWT.controller;

import com.kiemnv.SpringSecurityJWT.dto.response.NewsResponse;
import com.kiemnv.SpringSecurityJWT.service.NewsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class NewsController {
    private final NewsService newsService;

    @GetMapping("/everything")
    public ResponseEntity<?> getEverything(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String domains,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false, defaultValue = "popularity") String sortBy,
            @RequestParam(required = false, defaultValue = "en") String language,
            @RequestParam(required = false, defaultValue = "10") int pageSize,
            @RequestParam(required = false, defaultValue = "1") int page) {

        try {
            log.info("Getting everything with params: q={}, domains={}, from={}, to={}, sortBy={}, language={}, pageSize={}, page={}",
                    q, domains, from, to, sortBy, language, pageSize, page);

            if (q == null || q.trim().isEmpty()) {
                q = "trump";
                log.info("Using default query: {}", q);
            }

            NewsResponse response = newsService.getEverything(q, domains, from, to, sortBy, language, pageSize, page);

            if (response != null && response.getArticles() != null) {
                log.info("Returning {} articles", response.getArticles().size());
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error in getEverything: ", e);
            return createErrorResponse("Lỗi khi lấy tin tức: " + e.getMessage());
        }
    }

    @GetMapping("/top-headlines")
    public ResponseEntity<?> getTopHeadlines(
            @RequestParam(required = false) String category,
            @RequestParam(required = false, defaultValue = "us") String country,
            @RequestParam(required = false) String sources,
            @RequestParam(required = false) String q,
            @RequestParam(required = false, defaultValue = "10") int pageSize,
            @RequestParam(required = false, defaultValue = "1") int page) {

        try {
            log.info("Getting top headlines with params: category={}, country={}, sources={}, q={}, pageSize={}, page={}",
                    category, country, sources, q, pageSize, page);

            NewsResponse response = newsService.getTopHeadlines(category, country, sources, q, pageSize, page);

            if (response != null && response.getArticles() != null) {
                log.info("Returning {} headlines", response.getArticles().size());
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error in getTopHeadlines: ", e);
            return createErrorResponse("Lỗi khi lấy tin tức nổi bật: " + e.getMessage());
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchNews(
            @RequestParam String query,
            @RequestParam(required = false) String category,
            @RequestParam(required = false, defaultValue = "10") int pageSize,
            @RequestParam(required = false, defaultValue = "1") int page) {

        try {
            log.info("Searching news with params: query={}, category={}, pageSize={}, page={}",
                    query, category, pageSize, page);

            if (query == null || query.trim().isEmpty()) {
                return createErrorResponse("Query không được để trống");
            }

            NewsResponse response = newsService.searchNews(query, category, pageSize, page);

            if (response != null && response.getArticles() != null) {
                log.info("Search returned {} articles", response.getArticles().size());
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error in searchNews: ", e);
            return createErrorResponse("Lỗi khi tìm kiếm tin tức: " + e.getMessage());
        }
    }

    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "ok");
            response.put("message", "News API is running");
            response.put("timestamp", System.currentTimeMillis());
            response.put("service", "NewsController");

            try {
                newsService.testConnection();
                response.put("newsapi_status", "connected");
            } catch (Exception e) {
                response.put("newsapi_status", "error");
                response.put("newsapi_error", e.getMessage());
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error in health check: ", e);
            return createErrorResponse("Health check failed: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> handleBaseNewsRequest() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "info");
        response.put("message", "News API endpoints");
        response.put("endpoints", new String[]{
                "GET /api/news/everything - Lấy tất cả tin tức",
                "GET /api/news/top-headlines - Lấy tin tức nổi bật",
                "GET /api/news/search - Tìm kiếm tin tức",
                "GET /api/news/health - Kiểm tra trạng thái"
        });
        response.put("example_urls", new String[]{
                "/api/news/everything?q=technology&pageSize=10",
                "/api/news/top-headlines?category=business&country=us",
                "/api/news/search?query=AI&category=tech"
        });
        return ResponseEntity.ok(response);
    }

    private ResponseEntity<?> createErrorResponse(String message) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", "error");
        errorResponse.put("message", message);
        errorResponse.put("timestamp", System.currentTimeMillis());
        errorResponse.put("articles", new Object[0]);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleNewsServiceException(RuntimeException ex) {
        log.error("RuntimeException in News API: {}", ex.getMessage(), ex);
        return createErrorResponse("Lỗi xử lý tin tức: " + ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.error("IllegalArgumentException in News API: {}", ex.getMessage(), ex);
        return createErrorResponse("Tham số không hợp lệ: " + ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGenericException(Exception ex) {
        log.error("Unexpected error in News API: {}", ex.getMessage(), ex);
        return createErrorResponse("Đã xảy ra lỗi không mong muốn. Vui lòng thử lại sau.");
    }
}