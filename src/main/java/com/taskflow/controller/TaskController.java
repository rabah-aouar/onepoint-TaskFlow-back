package com.taskflow.controller;

import com.taskflow.dto.TaskRequest;
import com.taskflow.model.Task;
import com.taskflow.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.taskflow.model.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
@lombok.extern.slf4j.Slf4j
public class TaskController {

    private final TaskService service;

    @GetMapping
    public ResponseEntity<Page<Task>> getAllTasks(
            Pageable pageable,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) String search) {
        log.debug("Fetching tasks. Page: {}, Size: {}, Status: {}, Search: '{}'",
                pageable.getPageNumber(), pageable.getPageSize(), status, search);
        return ResponseEntity.ok(service.getTasks(pageable, status, search));
    }

    @PostMapping
    public ResponseEntity<Task> createTask(@RequestBody TaskRequest request) {
        log.debug("Creating task: {}", request.title());
        return ResponseEntity.ok(service.createTask(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable Long id, @RequestBody TaskRequest request) {
        log.debug("Updating task with ID: {}", id);
        return ResponseEntity.ok(service.updateTask(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        log.debug("Deleting task with ID: {}", id);
        service.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

}
