package com.enterprise.service;

import java.util.List;

import com.enterprise.model.DashboardConfig;
import com.enterprise.model.User;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DashboardService {

    public DashboardConfig getDashboardConfig(String username) {
        User user = getUserByUsername(username);
        List<String> widgets = getWidgetsForRole(user.getRole());
        return new DashboardConfig(user, widgets);
    }

    private User getUserByUsername(String username) {
        return switch (username) {
            case "admin" -> new User("admin", "ADMIN", "admin@company.com");
            case "manager" -> new User("manager", "MANAGER", "manager@company.com");
            default -> new User(username, "EMPLOYEE", username + "@company.com");
        };
    }

    private List<String> getWidgetsForRole(String role) {
        return switch (role) {
            case "ADMIN" -> List.of(
                    "widgets/admin-stats",
                    "widgets/user-management",
                    "widgets/system-health",
                    "widgets/audit-log");
            case "MANAGER" -> List.of(
                    "widgets/team-performance",
                    "widgets/approval-queue",
                    "widgets/reports");
            default -> List.of(
                    "widgets/my-tasks",
                    "widgets/timesheet");
        };
    }
}