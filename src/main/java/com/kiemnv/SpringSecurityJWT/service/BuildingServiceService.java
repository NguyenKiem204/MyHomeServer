package com.kiemnv.SpringSecurityJWT.service;

import com.kiemnv.SpringSecurityJWT.entity.BuildingServiceDto;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class BuildingServiceService {
    private final List<BuildingServiceDto> services;
    // Map để track registration theo user (userId -> Set of serviceIds)
    private final Map<String, Set<Long>> userRegistrations = new ConcurrentHashMap<>();

    public BuildingServiceService() {
        this.services = initializeMockServices();
    }

    public List<BuildingServiceDto> getAllServices() {
        return services;
    }

    public List<BuildingServiceDto> getAllServicesForUser(String userId) {
        Set<Long> userRegisteredServices = userRegistrations.getOrDefault(userId, new HashSet<>());

        return services.stream().map(service -> {
            BuildingServiceDto userService = new BuildingServiceDto(service);
            userService.setRegistered(userRegisteredServices.contains(service.getId()));
            return userService;
        }).toList();
    }

    public BuildingServiceDto getServiceById(Long id) {
        return services.stream()
                .filter(s -> s.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public BuildingServiceDto getServiceByIdForUser(Long id, String userId) {
        BuildingServiceDto service = getServiceById(id);
        if (service != null) {
            BuildingServiceDto userService = new BuildingServiceDto(service);
            Set<Long> userRegisteredServices = userRegistrations.getOrDefault(userId, new HashSet<>());
            userService.setRegistered(userRegisteredServices.contains(id));
            return userService;
        }
        return null;
    }

    public boolean registerService(Long id, String userId) {
        BuildingServiceDto service = getServiceById(id);
        if (service != null) {
            userRegistrations.computeIfAbsent(userId, k -> new HashSet<>()).add(id);
            return true;
        }
        return false;
    }

    public boolean cancelService(Long id, String userId) {
        Set<Long> userRegisteredServices = userRegistrations.get(userId);
        if (userRegisteredServices != null && userRegisteredServices.contains(id)) {
            userRegisteredServices.remove(id);
            return true;
        }
        return false;
    }

    public List<BuildingServiceDto> getRegisteredServicesForUser(String userId) {
        Set<Long> userRegisteredServices = userRegistrations.getOrDefault(userId, new HashSet<>());

        return services.stream()
                .filter(service -> userRegisteredServices.contains(service.getId()))
                .map(service -> {
                    BuildingServiceDto userService = new BuildingServiceDto(service);
                    userService.setRegistered(true);
                    return userService;
                })
                .toList();
    }

    private List<BuildingServiceDto> initializeMockServices() {
        return Arrays.asList(
                new BuildingServiceDto(1L, "Dịch vụ đậu xe",
                        "Đăng ký chỗ đậu xe cố định trong hầm/bãi đỗ xe của tòa nhà",
                        "Car", "#4F46E5", "parking", false, "800.000đ/tháng",
                        Arrays.asList("Chỗ đậu xe cố định", "An ninh 24/7", "Camera giám sát", "Thẻ từ ra vào tự động"),
                        "156", "Tầng B1-B2", "24/7"),

                new BuildingServiceDto(2L, "Dịch vụ giữ xe máy",
                        "Khu vực giữ xe máy an toàn với bảo vệ và camera giám sát",
                        "Shield", "#10B981", "parking", false, "200.000đ/tháng",
                        Arrays.asList("Khu vực riêng biệt", "Bảo vệ 24/7", "Bảo hiểm xe máy", "Rửa xe miễn phí 2 lần/tháng"),
                        "324", "Tầng trệt", "5:00 - 23:00"),

                new BuildingServiceDto(3L, "Dịch vụ gym & fitness",
                        "Phòng tập gym hiện đại với đầy đủ thiết bị và huấn luyện viên",
                        "Dumbbell", "#F59E0B", "fitness", false, "500.000đ/tháng",
                        Arrays.asList("Thiết bị gym hiện đại", "Huấn luyện viên cá nhân", "Lớp học nhóm", "Khăn tắm và nước uống miễn phí"),
                        "89", "Tầng 5", "5:00 - 22:00"),

                new BuildingServiceDto(4L, "Hồ bơi & spa",
                        "Hồ bơi trong nhà với dịch vụ spa và massage thư giãn",
                        "Waves", "#0EA5E9", "fitness", false, "1.200.000đ/tháng",
                        Arrays.asList("Hồ bơi 4 mùa", "Jacuzzi và sauna", "Dịch vụ massage", "Khu vực thư giãn"),
                        "67", "Tầng 6", "6:00 - 22:00"),

                new BuildingServiceDto(5L, "Dịch vụ vệ sinh nhà",
                        "Dịch vụ dọn dẹp, vệ sinh căn hộ định kỳ chuyên nghiệp",
                        "Wrench", "#8B5CF6", "cleaning", false, "300.000đ/lần",
                        Arrays.asList("Đội ngũ chuyên nghiệp", "Dụng cụ và hóa chất chuyên dùng", "Linh hoạt lịch hẹn", "Bảo hiểm tài sản"),
                        "143", "Tại căn hộ", "8:00 - 18:00"),

                new BuildingServiceDto(6L, "Dịch vụ giặt ủi",
                        "Giặt ủi quần áo chuyên nghiệp, giao nhận tận nơi",
                        "Package", "#EC4899", "cleaning", false, "25.000đ/kg",
                        Arrays.asList("Giặt khô và giặt ướt", "Giao nhận tận căn hộ", "Bảo quản quần áo cẩn thận", "Dịch vụ ủi chuyên nghiệp"),
                        "201", "Tầng 2", "7:00 - 19:00"),

                new BuildingServiceDto(7L, "Khu vui chơi trẻ em",
                        "Khu vui chơi an toàn cho trẻ em với nhiều trò chơi hấp dẫn",
                        "Baby", "#14B8A6", "entertainment", false, "100.000đ/lần",
                        Arrays.asList("Khu vui chơi an toàn", "Nhân viên trông nom", "Đồ chơi đa dạng", "Không gian thoáng mát"),
                        "78", "Tầng 3", "8:00 - 20:00"),

                new BuildingServiceDto(8L, "Phòng họp & sự kiện",
                        "Cho thuê phòng họp, tổ chức sự kiện với đầy đủ trang thiết bị",
                        "Users", "#6366F1", "meeting", false, "500.000đ/4 giờ",
                        Arrays.asList("Phòng họp hiện đại", "Máy chiếu và âm thanh", "WiFi tốc độ cao", "Dịch vụ trà coffee"),
                        "45", "Tầng 4", "8:00 - 22:00"),

                new BuildingServiceDto(9L, "Dịch vụ đặt đồ ăn",
                        "Đặt đồ ăn từ các nhà hàng đối tác, giao tận căn hộ",
                        "Utensils", "#F97316", "food", false, "Miễn phí giao hàng",
                        Arrays.asList("Đa dạng nhà hàng đối tác", "Giao hàng miễn phí", "Đặt hàng qua app", "Khuyến mại thường xuyên"),
                        "267", "Giao tận cửa", "6:00 - 23:00"),

                new BuildingServiceDto(10L, "Siêu thị mini",
                        "Cửa hàng tiện lợi trong tòa nhà với đầy đủ nhu yếu phẩm hàng ngày",
                        "ShoppingCart", "#EF4444", "shopping", false, "Giá thị trường",
                        Arrays.asList("Mở cửa 24/7", "Đa dạng sản phẩm", "Giá cả cạnh tranh", "Giao hàng tận căn hộ"),
                        "412", "Tầng trệt", "24/7")
        );
    }
}