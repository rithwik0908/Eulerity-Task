package com.eulerity.taskmanager.dto;

import com.eulerity.taskmanager.model.Priority;
import com.eulerity.taskmanager.model.TaskStatus;
import java.time.LocalDate;

public record TaskSuggestionResponse(
        String title,
        String description,
        LocalDate dueDate,
        Priority priority,
        TaskStatus status
) {
}
