package com.kiemnv.SpringSecurityJWT.controller;

import com.kiemnv.SpringSecurityJWT.dto.response.ServiceFeeResponse;
import com.kiemnv.SpringSecurityJWT.service.ServiceFeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // Consider restricting this in production
@RequiredArgsConstructor
public class ServiceFeeController {

    private final ServiceFeeService serviceFeeService;

    @GetMapping("/service-fees")
    public ResponseEntity<ServiceFeeResponse> getServiceFees() {
        ServiceFeeResponse serviceFees = serviceFeeService.getServiceFees();

        if (serviceFees != null && !serviceFees.getData().isEmpty()) {
            return ResponseEntity.ok(serviceFees);
        } else {
            return ResponseEntity.noContent().build(); // Or ResponseEntity.notFound().build()
        }
    }
}