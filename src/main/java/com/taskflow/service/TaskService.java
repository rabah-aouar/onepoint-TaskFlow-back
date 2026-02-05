package com.taskflow.service;

import com.taskflow.dto.TaskRequest;
import com.taskflow.model.Task;
import com.taskflow.model.User;
import com.taskflow.repository.TaskRepository;
import com.taskflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return (User) auth.getPrincipal(); // Principals are User objects in our setup
    }

    public List<Task> getMyTasks() {
        return taskRepository.findByUserId(getCurrentUser().getId());
    }

    public Task createTask(TaskRequest request) {
        var user = getCurrentUser();
        var task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .status(request.getStatus())
                .priority(request.getPriority())
                .dueDate(request.getDueDate())
                .user(user)
                .build();
        return taskRepository.save(task);
    }

    public Task updateTask(Long id, TaskRequest request) {
        var task = taskRepository.findById(id).orElseThrow(() -> new RuntimeException("Task not found"));
        // Check ownership
        if (!task.getUser().getId().equals(getCurrentUser().getId())) {
            throw new RuntimeException("Not authorized");
        }

        if (request.getTitle() != null)
            task.setTitle(request.getTitle());
        if (request.getDescription() != null)
            task.setDescription(request.getDescription());
        if (request.getStatus() != null)
            task.setStatus(request.getStatus());
        if (request.getPriority() != null)
            task.setPriority(request.getPriority());
        if (request.getDueDate() != null)
            task.setDueDate(request.getDueDate());

        return taskRepository.save(task);
    }

    public void deleteTask(Long id) {
        var task = taskRepository.findById(id).orElseThrow(() -> new RuntimeException("Task not found"));
        if (!task.getUser().getId().equals(getCurrentUser().getId())) {
            throw new RuntimeException("Not authorized");
        }
        taskRepository.delete(task);
    }

    @Transactional
    public Set<Task> toggleFavorite(Long taskId) {
        var user = userRepository.findById(getCurrentUser().getId()).orElseThrow();
        var task = taskRepository.findById(taskId).orElseThrow(() -> new RuntimeException("Task not found"));

        if (user.getFavorites().contains(task)) {
            user.getFavorites().remove(task);
        } else {
            user.getFavorites().add(task);
        }
        userRepository.save(user);
        return user.getFavorites();
    }

    public Set<Task> getMyFavorites() {
        var user = userRepository.findById(getCurrentUser().getId()).orElseThrow();
        return user.getFavorites();
    }
}
