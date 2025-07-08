package com.kiemnv.SpringSecurityJWT.controller;

import com.kiemnv.SpringSecurityJWT.dto.response.BlogResponse;
import com.kiemnv.SpringSecurityJWT.service.BlogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // Allow requests from any origin, consider restricting in production
@RequiredArgsConstructor
public class BlogController {

    private final BlogService blogService;

    @GetMapping("/blogs")
    public ResponseEntity<BlogResponse> getBlogs() {
        BlogResponse blogs = blogService.getBlogs();

        if (blogs != null && !blogs.getData().isEmpty()) {
            return ResponseEntity.ok(blogs);
        } else {
            return ResponseEntity.noContent().build(); // Or ResponseEntity.notFound().build() if no blogs are found
        }
    }
}