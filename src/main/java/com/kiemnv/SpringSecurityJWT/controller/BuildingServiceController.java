package com.kiemnv.SpringSecurityJWT.controller;

import com.kiemnv.SpringSecurityJWT.entity.BuildingServiceDto;
import com.kiemnv.SpringSecurityJWT.entity.User;
import com.kiemnv.SpringSecurityJWT.service.BuildingServiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/building-services")
@CrossOrigin(origins = "*")
public class BuildingServiceController {

    @Autowired
    private BuildingServiceService buildingServiceService;

    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof User) { // Cast to your UserDetails implementation
                User user = (User) principal;
                return String.valueOf(user.getId()); // Convert Long ID to String if BuildingServiceService expects String
            }
            // Fallback for other principal types or anonymous
            return authentication.getName();
        }
        return "anonymous"; // fallback for testing
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllServices() {
        try {
            String userId = getCurrentUserId();
            List<BuildingServiceDto> services = buildingServiceService.getAllServicesForUser(userId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Lấy danh sách dịch vụ thành công",
                    "data", services
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Lỗi khi lấy danh sách dịch vụ: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getServiceById(@PathVariable Long id) {
        try {
            String userId = getCurrentUserId();
            BuildingServiceDto service = buildingServiceService.getServiceByIdForUser(id, userId);
            if (service != null) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Lấy thông tin dịch vụ thành công",
                        "data", service
                ));
            } else {
                return ResponseEntity.status(404).body(Map.of(
                        "success", false,
                        "message", "Không tìm thấy dịch vụ với ID: " + id
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Lỗi khi lấy thông tin dịch vụ: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/{id}/register")
    public ResponseEntity<Map<String, Object>> registerService(@PathVariable Long id) {
        try {
            String userId = getCurrentUserId();
            boolean success = buildingServiceService.registerService(id, userId);
            if (success) {
                BuildingServiceDto service = buildingServiceService.getServiceByIdForUser(id, userId);
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Đăng ký dịch vụ thành công",
                        "data", service
                ));
            } else {
                return ResponseEntity.status(400).body(Map.of(
                        "success", false,
                        "message", "Không thể đăng ký dịch vụ. Dịch vụ không tồn tại."
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Lỗi khi đăng ký dịch vụ: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Map<String, Object>> cancelService(@PathVariable Long id) {
        try {
            String userId = getCurrentUserId();
            boolean success = buildingServiceService.cancelService(id, userId);
            if (success) {
                BuildingServiceDto service = buildingServiceService.getServiceByIdForUser(id, userId);
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Hủy đăng ký dịch vụ thành công",
                        "data", service
                ));
            } else {
                return ResponseEntity.status(400).body(Map.of(
                        "success", false,
                        "message", "Không thể hủy đăng ký dịch vụ. Dịch vụ chưa được đăng ký."
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Lỗi khi hủy đăng ký dịch vụ: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/registered")
    public ResponseEntity<Map<String, Object>> getRegisteredServices() {
        try {
            String userId = getCurrentUserId();
            List<BuildingServiceDto> registeredServices = buildingServiceService.getRegisteredServicesForUser(userId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Lấy danh sách dịch vụ đã đăng ký thành công",
                    "data", registeredServices
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Lỗi khi lấy danh sách dịch vụ đã đăng ký: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<Map<String, Object>> getServicesByCategory(@PathVariable String category) {
        try {
            String userId = getCurrentUserId();
            List<BuildingServiceDto> allServices = buildingServiceService.getAllServicesForUser(userId);
            List<BuildingServiceDto> categoryServices = allServices.stream()
                    .filter(service -> service.getCategory().equals(category))
                    .toList();

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Lấy danh sách dịch vụ theo danh mục thành công",
                    "data", categoryServices
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Lỗi khi lấy danh sách dịch vụ theo danh mục: " + e.getMessage()
            ));
        }
    }
}