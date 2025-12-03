package com.example.ui;

import com.example.task.TaskService;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.inject.Inject;

@Route(value = "stats", layout = MainLayout.class)
@PageTitle("Task Statistics")
public class StatsView extends VerticalLayout {

    private final TaskService taskService;

    @Inject
    public StatsView(TaskService taskService) {
        this.taskService = taskService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H2 heading = new H2("Task Statistics");

        long open = taskService.countOpen();
        long done = taskService.countDone();
        long total = open + done;

        Paragraph summary = new Paragraph(
                "Open: " + open + " • Done: " + done + " • Total: " + total);

        ProgressBar completion = new ProgressBar(0, 1, total == 0 ? 0 : (double) done / total);
        completion.setWidth("300px");

        add(heading, summary, completion);
    }
}