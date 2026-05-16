package com.eulerity.taskmanager.controller;

import com.eulerity.taskmanager.dto.SuggestTaskRequest;
import com.eulerity.taskmanager.dto.TaskRequest;
import com.eulerity.taskmanager.dto.TaskResponse;
import com.eulerity.taskmanager.dto.TaskSuggestionResponse;
import com.eulerity.taskmanager.service.AiSuggestionService;
import com.eulerity.taskmanager.service.TaskService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService taskService;
    private final AiSuggestionService aiSuggestionService;

    public TaskController(TaskService taskService, AiSuggestionService aiSuggestionService) {
        this.taskService = taskService;
        this.aiSuggestionService = aiSuggestionService;
    }

    @PostMapping
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody TaskRequest request) {
        TaskResponse response = taskService.createTask(request);
        return ResponseEntity.created(URI.create("/tasks/" + response.id())).body(response);
    }

    @GetMapping
    public List<TaskResponse> getAllTasks() {
        return taskService.getAllTasks();
    }

    @GetMapping("/{id}")
    public TaskResponse getTaskById(@PathVariable Long id) {
        return taskService.getTaskById(id);
    }

    @PutMapping("/{id}")
    public TaskResponse updateTask(@PathVariable Long id, @Valid @RequestBody TaskRequest request) {
        return taskService.updateTask(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/suggest")
    public TaskSuggestionResponse suggestTask(@Valid @RequestBody SuggestTaskRequest request) {
        return aiSuggestionService.suggestTask(request.text());
    }
}
