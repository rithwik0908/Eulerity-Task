package com.eulerity.taskmanager.service;

import com.eulerity.taskmanager.dto.TaskRequest;
import com.eulerity.taskmanager.dto.TaskResponse;
import com.eulerity.taskmanager.exception.TaskNotFoundException;
import com.eulerity.taskmanager.model.Task;
import com.eulerity.taskmanager.repository.TaskRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Transactional
    public TaskResponse createTask(TaskRequest request) {
        Task task = new Task(
                null,
                request.title(),
                request.description(),
                request.dueDate(),
                request.priority(),
                request.status()
        );
        return TaskResponse.from(taskRepository.save(task));
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getAllTasks() {
        return taskRepository.findAll().stream()
                .map(TaskResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public TaskResponse getTaskById(Long id) {
        return TaskResponse.from(findTask(id));
    }

    @Transactional
    public TaskResponse updateTask(Long id, TaskRequest request) {
        Task task = findTask(id);
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setDueDate(request.dueDate());
        task.setPriority(request.priority());
        task.setStatus(request.status());
        return TaskResponse.from(taskRepository.save(task));
    }

    @Transactional
    public void deleteTask(Long id) {
        Task task = findTask(id);
        taskRepository.delete(task);
    }

    private Task findTask(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
    }
}
