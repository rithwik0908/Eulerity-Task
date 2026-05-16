package com.eulerity.taskmanager.service;

import com.eulerity.taskmanager.dto.TaskSuggestionResponse;

public interface AiSuggestionService {

    TaskSuggestionResponse suggestTask(String plainLanguageText);
}
