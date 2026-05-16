package com.eulerity.taskmanager.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.eulerity.taskmanager.dto.TaskSuggestionResponse;
import com.eulerity.taskmanager.model.Priority;
import com.eulerity.taskmanager.model.TaskStatus;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

class GeminiAiSuggestionServiceTest {

    @Test
    void suggestTaskReturnsDemoSuggestionWhenApiKeyIsMissing() {
        GeminiAiSuggestionService service = new GeminiAiSuggestionService(
                mock(RestClient.class),
                new ObjectMapper(),
                "",
                "https://example.test"
        );

        TaskSuggestionResponse response = service.suggestTask(
                "remind me to submit the quarterly report before Friday"
        );

        assertThat(response.title()).isEqualTo("Submit quarterly report");
        assertThat(response.description()).isEqualTo("Submit the quarterly report before Friday");
        assertThat(response.dueDate()).isEqualTo(LocalDate.of(2026, 5, 22));
        assertThat(response.priority()).isEqualTo(Priority.MEDIUM);
        assertThat(response.status()).isEqualTo(TaskStatus.TODO);
    }
}
