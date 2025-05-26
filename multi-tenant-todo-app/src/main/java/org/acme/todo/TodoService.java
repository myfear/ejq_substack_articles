package org.acme.todo;

import java.util.List;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class TodoService {

    @Inject
    TodoRepository todoRepository;

    public List<Todo> getTodosForCurrentUser() {
        return todoRepository.listAll();
    }

    public Optional<Todo> getTodoByIdForCurrentUser(Long id) {
        return todoRepository.findByIdOptional(id);
    }

    @Transactional
    public Todo createTodoForCurrentUser(Todo todoData) {
        Todo todo = new Todo();
        todo.title = todoData.title;
        todo.completed = todoData.completed;
        todo.tenantId = null; // Let Hibernate fill this
        todoRepository.persist(todo);
        return todo;
    }

    @Transactional
    public Optional<Todo> updateTodoForCurrentUser(Long id, Todo data) {
        return todoRepository.findByIdOptional(id).map(existing -> {
            existing.title = data.title;
            existing.completed = data.completed;
            todoRepository.persist(existing);
            return existing;
        });
    }

    @Transactional
    public boolean deleteTodoForCurrentUser(Long id) {
        return todoRepository.deleteById(id);
    }
}