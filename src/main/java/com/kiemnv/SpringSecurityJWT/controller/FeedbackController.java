package com.kiemnv.SpringSecurityJWT.controller;

import com.kiemnv.SpringSecurityJWT.dto.request.FeedbackRequest;
import com.kiemnv.SpringSecurityJWT.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    @PostMapping("/feedback")
    public ResponseEntity<String> sendFeedback(@RequestBody FeedbackRequest request) {
        boolean success = feedbackService.sendFeedback(request);

        return success ?
                ResponseEntity.ok("Gửi feedback thành công!") :
                ResponseEntity.status(500).body("Gửi feedback thất bại!");
    }
}
