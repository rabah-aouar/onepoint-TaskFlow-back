package com.taskflow.service;

import com.taskflow.dto.TaskRequest;
import com.taskflow.model.Task;
import com.taskflow.model.TaskPriority;
import com.taskflow.model.TaskStatus;
import com.taskflow.model.User;
import com.taskflow.repository.TaskRepository;
import com.taskflow.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @InjectMocks
    private TaskService taskService;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = User.builder().id(1L).username("testuser").build();

        // Security Context Mocking
        SecurityContextHolder.setContext(securityContext);
    }

    private void mockAuthenticatedUser() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(mockUser);
    }

    @Test
    void createTask_ShouldCreateTask_WhenRequestIsValid() {
        mockAuthenticatedUser();
        TaskRequest request = new TaskRequest("Task 1", "Desc 1", null, null, null);

        when(taskRepository.save(any(Task.class))).thenAnswer(i -> {
            Task t = i.getArgument(0);
            t.setId(10L);
            return t;
        });

        Task createdTask = taskService.createTask(request);

        assertNotNull(createdTask);
        assertEquals("Task 1", createdTask.getTitle());
        assertEquals(mockUser, createdTask.getUser());
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void updateTask_ShouldUpdateTask_WhenUserIsOwner() {
        mockAuthenticatedUser();
        Task existingTask = Task.builder().id(10L).title("Old Title").user(mockUser).build();
        TaskRequest request = new TaskRequest("New Title", null, null, null, null);

        when(taskRepository.findById(10L)).thenReturn(Optional.of(existingTask));
        when(taskRepository.save(any(Task.class))).thenAnswer(i -> i.getArgument(0));

        Task updatedTask = taskService.updateTask(10L, request);

        assertEquals("New Title", updatedTask.getTitle());
        verify(taskRepository).save(existingTask);
    }

    @Test
    void updateTask_ShouldThrowException_WhenUserIsNotOwner() {
        mockAuthenticatedUser();
        User otherUser = User.builder().id(2L).username("other").build();
        Task existingTask = Task.builder().id(10L).title("Old Title").user(otherUser).build();
        TaskRequest request = new TaskRequest(null, null, null, null, null);

        when(taskRepository.findById(10L)).thenReturn(Optional.of(existingTask));

        assertThrows(RuntimeException.class, () -> taskService.updateTask(10L, request));
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void deleteTask_ShouldDeleteTask_WhenUserIsOwner() {
        mockAuthenticatedUser();
        Task existingTask = Task.builder().id(10L).user(mockUser).build();

        when(taskRepository.findById(10L)).thenReturn(Optional.of(existingTask));

        taskService.deleteTask(10L);

        verify(taskRepository).delete(existingTask);
    }

    @Test
    void deleteTask_ShouldThrowException_WhenUserIsNotOwner() {
        mockAuthenticatedUser();
        User otherUser = User.builder().id(2L).username("other").build();
        Task existingTask = Task.builder().id(10L).user(otherUser).build();

        when(taskRepository.findById(10L)).thenReturn(Optional.of(existingTask));

        assertThrows(RuntimeException.class, () -> taskService.deleteTask(10L));
        verify(taskRepository, never()).delete(any(Task.class));
    }
}
