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

import com.taskflow.model.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@lombok.extern.slf4j.Slf4j
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return (User) auth.getPrincipal(); // Principals are User objects in our setup
    }

    public Page<Task> getTasks(Pageable pageable, TaskStatus status, String search) {
        log.debug("Retrieving tasks for user: {}", getCurrentUser().getUsername());
        Specification<Task> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("user").get("id"), getCurrentUser().getId()));

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (StringUtils.hasText(search)) {
                String likePattern = "%" + search.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), likePattern),
                        cb.like(cb.lower(root.get("description")), likePattern)));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return taskRepository.findAll(spec, pageable);
    }

    public Task createTask(TaskRequest request) {
        var user = getCurrentUser();
        log.debug("Creating new task '{}' for user: {}", request.title(), user.getUsername());
        var task = Task.builder()
                .title(request.title())
                .description(request.description())
                .status(request.status())
                .priority(request.priority())
                .dueDate(request.dueDate())
                .user(user)
                .build();
        Task savedTask = taskRepository.save(task);
        log.info("Task created with ID: {}", savedTask.getId());
        return savedTask;
    }

    public Task updateTask(Long id, TaskRequest request) {
        log.debug("Updating task ID: {}", id);
        var task = taskRepository.findById(id).orElseThrow(() -> new RuntimeException("Task not found"));
        // Check ownership
        if (!task.getUser().getId().equals(getCurrentUser().getId())) {
            log.warn("Unauthorized update attempt on task ID: {} by user: {}", id, getCurrentUser().getUsername());
            throw new RuntimeException("Not authorized");
        }

        if (request.title() != null)
            task.setTitle(request.title());
        if (request.description() != null)
            task.setDescription(request.description());
        if (request.status() != null)
            task.setStatus(request.status());
        if (request.priority() != null)
            task.setPriority(request.priority());
        if (request.dueDate() != null)
            task.setDueDate(request.dueDate());

        Task updatedTask = taskRepository.save(task);
        log.info("Task ID: {} updated successfully", id);
        return updatedTask;
    }

    public void deleteTask(Long id) {
        log.debug("Deleting task ID: {}", id);
        var task = taskRepository.findById(id).orElseThrow(() -> new RuntimeException("Task not found"));
        if (!task.getUser().getId().equals(getCurrentUser().getId())) {
            log.warn("Unauthorized delete attempt on task ID: {} by user: {}", id, getCurrentUser().getUsername());
            throw new RuntimeException("Not authorized");
        }
        taskRepository.delete(task);
        log.info("Task ID: {} deleted successfully", id);
    }

}
