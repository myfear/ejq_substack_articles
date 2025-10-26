package com.enterprise.model;

import java.util.List;

public class DashboardConfig {
    private User user;
    private List<String> widgets;

    public DashboardConfig(User user, List<String> widgets) {
        this.user = user;
        this.widgets = widgets;
    }

    public User getUser() {
        return user;
    }

    public List<String> getWidgets() {
        return widgets;
    }
}