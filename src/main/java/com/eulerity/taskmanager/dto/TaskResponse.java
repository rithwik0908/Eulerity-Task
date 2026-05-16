package com.eulerity.taskmanager.dto;

import com.eulerity.taskmanager.model.Priority;
import com.eulerity.taskmanager.model.Task;
import com.eulerity.taskmanager.model.TaskStatus;
import java.time.LocalDate;

public record TaskResponse(
        Long id,
        String title,
        String description,
        LocalDate dueDate,
        Priority priority,
        TaskStatus status
) {
    public static TaskResponse from(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getDueDate(),
                task.getPriority(),
                task.getStatus()
        );
    }
}
