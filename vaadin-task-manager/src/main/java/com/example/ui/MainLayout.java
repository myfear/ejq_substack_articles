package com.example.ui;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.theme.lumo.LumoUtility;

public class MainLayout extends AppLayout {

    public MainLayout() {
        createHeader();
        createDrawer();
    }

    private void createHeader() {
        H1 logo = new H1("Task Manager");
        logo.addClassNames(
                LumoUtility.FontSize.LARGE,
                LumoUtility.Margin.NONE);

        var header = new com.vaadin.flow.component.orderedlayout.HorizontalLayout(
                new DrawerToggle(), logo);
        header.setWidthFull();
        header.setPadding(true);
        header.setAlignItems(com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER);

        addToNavbar(header);
    }

    private void createDrawer() {
        RouterLink tasksLink = new RouterLink("Tasks", TaskView.class);
        RouterLink statsLink = new RouterLink("Statistics", StatsView.class);

        VerticalLayout menu = new VerticalLayout(
                new Span("Views"),
                tasksLink,
                statsLink);
        menu.setPadding(true);
        menu.setSpacing(false);

        addToDrawer(menu);
    }
}