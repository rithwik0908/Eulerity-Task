package com.eulerity.taskmanager.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.eulerity.taskmanager.dto.TaskRequest;
import com.eulerity.taskmanager.dto.TaskResponse;
import com.eulerity.taskmanager.model.Priority;
import com.eulerity.taskmanager.model.Task;
import com.eulerity.taskmanager.model.TaskStatus;
import com.eulerity.taskmanager.repository.TaskRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    @Test
    void createTaskReturnsSavedTask() {
        TaskRequest request = request("Submit report", TaskStatus.TODO);
        Task saved = task(1L, "Submit report", TaskStatus.TODO);
        when(taskRepository.save(org.mockito.ArgumentMatchers.any(Task.class))).thenReturn(saved);

        TaskResponse response = taskService.createTask(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.title()).isEqualTo("Submit report");
        assertThat(response.priority()).isEqualTo(Priority.MEDIUM);
    }

    @Test
    void getAllTasksReturnsRepositoryTasks() {
        when(taskRepository.findAll()).thenReturn(List.of(
                task(1L, "First", TaskStatus.TODO),
                task(2L, "Second", TaskStatus.DONE)
        ));

        List<TaskResponse> responses = taskService.getAllTasks();

        assertThat(responses).extracting(TaskResponse::title).containsExactly("First", "Second");
    }

    @Test
    void getTaskByIdReturnsTask() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task(1L, "Submit report", TaskStatus.TODO)));

        TaskResponse response = taskService.getTaskById(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.title()).isEqualTo("Submit report");
    }

    @Test
    void updateTaskMutatesAndSavesExistingTask() {
        Task existing = task(1L, "Old", TaskStatus.TODO);
        Task updated = task(1L, "Updated", TaskStatus.DONE);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(taskRepository.save(existing)).thenReturn(updated);

        TaskResponse response = taskService.updateTask(1L, request("Updated", TaskStatus.DONE));

        assertThat(response.title()).isEqualTo("Updated");
        assertThat(response.status()).isEqualTo(TaskStatus.DONE);
    }

    @Test
    void deleteTaskDeletesExistingTask() {
        Task existing = task(1L, "Submit report", TaskStatus.TODO);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(existing));

        taskService.deleteTask(1L);

        verify(taskRepository).delete(existing);
    }

    private TaskRequest request(String title, TaskStatus status) {
        return new TaskRequest(title, "Description", LocalDate.of(2026, 5, 22), Priority.MEDIUM, status);
    }

    private Task task(Long id, String title, TaskStatus status) {
        return new Task(id, title, "Description", LocalDate.of(2026, 5, 22), Priority.MEDIUM, status);
    }
}
