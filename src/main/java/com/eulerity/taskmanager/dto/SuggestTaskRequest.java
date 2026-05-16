package com.eulerity.taskmanager.dto;

import jakarta.validation.constraints.NotBlank;

public record SuggestTaskRequest(@NotBlank String text) {
}
