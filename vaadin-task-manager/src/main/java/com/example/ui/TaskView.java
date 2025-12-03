package com.example.ui;

import java.time.LocalDate;

import com.example.task.Task;
import com.example.task.TaskService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.inject.Inject;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Tasks")
@CssImport("./styles.css")
public class TaskView extends VerticalLayout {

    private final TaskService taskService;

    private final Grid<Task> grid = new Grid<>(Task.class, false);

    private final TextField title = new TextField("Title");
    private final TextArea description = new TextArea("Description");
    private final DatePicker dueDate = new DatePicker("Due date");
    private final Button add = new Button("Add task");

    @Inject
    public TaskView(TaskService taskService) {
        this.taskService = taskService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);
        addClassName("tasks-view-root");

        configureForm();
        configureGrid();

        HorizontalLayout formLayout = new HorizontalLayout(title, description, dueDate, add);
        formLayout.setWidthFull();
        formLayout.setAlignItems(Alignment.END);

        add(formLayout, grid);

        refreshGrid();
    }

    private void configureForm() {
        title.setRequiredIndicatorVisible(true);
        title.setWidth("200px");

        description.setWidth("300px");
        description.setMaxLength(255);

        dueDate.setValue(LocalDate.now());

        add.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        add.addClickListener(event -> saveTask());
    }

    private void configureGrid() {
        grid.addColumn(task -> task.id).setHeader("ID").setAutoWidth(true);
        grid.addColumn(task -> task.title).setHeader("Title").setFlexGrow(2);
        grid.addColumn(task -> task.dueDate != null ? task.dueDate : "").setHeader("Due");
        grid.addColumn(task -> task.done ? "Done" : "Open").setHeader("Status");

        grid.addComponentColumn(task -> {
            Button toggle = new Button(task.done ? "Reopen" : "Done");
            toggle.addClickListener(click -> {
                taskService.toggleDone(task.id);
                refreshGrid();
            });
            return toggle;
        }).setHeader("Toggle");

        grid.addComponentColumn(task -> {
            Button delete = new Button("Delete");
            delete.addThemeVariants(ButtonVariant.LUMO_ERROR);
            delete.addClickListener(click -> {
                taskService.delete(task.id);
                refreshGrid();
            });
            return delete;
        }).setHeader("Actions");

        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        grid.setSizeFull();
    }

    private void saveTask() {
        if (title.isEmpty()) {
            Notification.show("Title is required");
            return;
        }

        taskService.create(title.getValue(), description.getValue(), dueDate.getValue());
        title.clear();
        description.clear();
        dueDate.setValue(LocalDate.now());
        refreshGrid();
    }

    private void refreshGrid() {
        grid.setItems(taskService.findAll());
    }
}