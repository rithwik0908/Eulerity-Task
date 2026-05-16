package com.eulerity.taskmanager.service;

import com.eulerity.taskmanager.dto.TaskSuggestionResponse;
import com.eulerity.taskmanager.model.Priority;
import com.eulerity.taskmanager.model.TaskStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
public class GeminiAiSuggestionService implements AiSuggestionService {

    private static final LocalDate DEMO_DUE_DATE = LocalDate.of(2026, 5, 22);
    private static final Logger LOGGER = LoggerFactory.getLogger(GeminiAiSuggestionService.class);

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String apiUrl;

    public GeminiAiSuggestionService(
            RestClient restClient,
            ObjectMapper objectMapper,
            @Value("${gemini.api.key:}") String apiKey,
            @Value("${gemini.api.url}") String apiUrl
    ) {
        this.restClient = restClient;
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.apiUrl = apiUrl;
    }

    @Override
    public TaskSuggestionResponse suggestTask(String plainLanguageText) {
        if (apiKey == null || apiKey.isBlank()) {
            LOGGER.info("GEMINI_API_KEY is not configured; returning deterministic demo task suggestion");
            return demoSuggestion(plainLanguageText);
        }

        try {
            Map<String, Object> request = Map.of(
                    "contents", List.of(Map.of(
                            "parts", List.of(Map.of("text", buildPrompt(plainLanguageText)))
                    )),
                    "generationConfig", Map.of(
                            "responseMimeType", "application/json",
                            "temperature", 0.2
                    )
            );

            String responseBody = restClient.post()
                    .uri(apiUrl)
                    .header("x-goog-api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(String.class);

            return parseGeminiResponse(responseBody);
        } catch (RuntimeException exception) {
            LOGGER.warn("Gemini task suggestion failed; returning deterministic demo task suggestion: {}",
                    exception.getMessage());
            return demoSuggestion(plainLanguageText);
        }
    }

    private String buildPrompt(String plainLanguageText) {
        return """
                Convert this plain-language reminder into one JSON object for a task manager.
                Today is %s.
                Return only JSON with these exact fields:
                title: concise required string in title case
                description: original meaning as a complete sentence
                dueDate: ISO-8601 date or null if no date is implied
                priority: LOW, MEDIUM, or HIGH
                status: TODO

                Reminder: %s
                """.formatted(LocalDate.now(), plainLanguageText);
    }

    private TaskSuggestionResponse parseGeminiResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            String text = root.at("/candidates/0/content/parts/0/text").asText();
            String json = stripMarkdownFence(text);
            return objectMapper.readValue(json, TaskSuggestionResponse.class);
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to parse Gemini task suggestion", exception);
        }
    }

    private String stripMarkdownFence(String text) {
        String trimmed = text.trim();
        if (trimmed.startsWith("```")) {
            trimmed = trimmed.replaceFirst("^```(?:json)?\\s*", "");
            trimmed = trimmed.replaceFirst("\\s*```$", "");
        }
        return trimmed;
    }

    private TaskSuggestionResponse demoSuggestion(String plainLanguageText) {
        return new TaskSuggestionResponse(
                inferTitle(plainLanguageText),
                normalizeDescription(plainLanguageText),
                DEMO_DUE_DATE,
                Priority.MEDIUM,
                TaskStatus.TODO
        );
    }

    private String inferTitle(String plainLanguageText) {
        String cleaned = plainLanguageText == null ? "" : plainLanguageText.trim();
        cleaned = cleaned.replaceFirst("(?i)^remind me to\\s+", "");
        cleaned = cleaned.replaceFirst("(?i)^the\\s+", "");
        cleaned = cleaned.replaceFirst("(?i)\\s+before\\s+.+$", "");
        cleaned = cleaned.replaceFirst("(?i)\\s+by\\s+.+$", "");
        cleaned = cleaned.replaceFirst("(?i)\\s+on\\s+.+$", "");
        if (cleaned.isBlank()) {
            return "Review task";
        }
        cleaned = cleaned.replaceAll("(?i)\\bthe\\b", "").replaceAll("\\s+", " ").trim().toLowerCase();
        return cleaned.substring(0, 1).toUpperCase() + cleaned.substring(1);
    }

    private String normalizeDescription(String plainLanguageText) {
        if (plainLanguageText == null || plainLanguageText.isBlank()) {
            return "Review task details.";
        }
        String trimmed = plainLanguageText.trim();
        trimmed = trimmed.replaceFirst("(?i)^remind me to\\s+", "");
        return trimmed.substring(0, 1).toUpperCase() + trimmed.substring(1);
    }
}
