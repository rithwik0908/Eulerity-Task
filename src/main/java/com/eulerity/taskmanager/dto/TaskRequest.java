package com.eulerity.taskmanager.dto;

import com.eulerity.taskmanager.model.Priority;
import com.eulerity.taskmanager.model.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record TaskRequest(
        @NotBlank String title,
        String description,
        LocalDate dueDate,
        @NotNull Priority priority,
        @NotNull TaskStatus status
) {
}
