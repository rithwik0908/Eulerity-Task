package com.eulerity.taskmanager.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.eulerity.taskmanager.dto.TaskSuggestionResponse;
import com.eulerity.taskmanager.model.Priority;
import com.eulerity.taskmanager.model.TaskStatus;
import com.eulerity.taskmanager.repository.TaskRepository;
import com.eulerity.taskmanager.service.AiSuggestionService;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class TaskControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskRepository taskRepository;

    @MockitoBean
    private AiSuggestionService aiSuggestionService;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
    }

    @Test
    void crudEndpointsWorkEndToEnd() throws Exception {
        String createBody = """
                {
                  "title": "Submit report",
                  "description": "Submit the quarterly report",
                  "dueDate": "2026-05-22",
                  "priority": "MEDIUM",
                  "status": "TODO"
                }
                """;

        String location = mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.title").value("Submit report"))
                .andExpect(jsonPath("$.status").value("TODO"))
                .andReturn()
                .getResponse()
                .getHeader("Location");

        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Submit report"));

        mockMvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Submit report"));

        String updateBody = """
                {
                  "title": "Submit final report",
                  "description": "Submit the final quarterly report",
                  "dueDate": "2026-05-23",
                  "priority": "HIGH",
                  "status": "IN_PROGRESS"
                }
                """;

        mockMvc.perform(put(location)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Submit final report"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        mockMvc.perform(delete(location))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void suggestEndpointUsesMockedAiService() throws Exception {
        when(aiSuggestionService.suggestTask("remind me to submit the quarterly report before Friday"))
                .thenReturn(new TaskSuggestionResponse(
                        "Submit quarterly report",
                        "Submit the quarterly report before Friday",
                        LocalDate.of(2026, 5, 22),
                        Priority.MEDIUM,
                        TaskStatus.TODO
                ));

        mockMvc.perform(post("/tasks/suggest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "text": "remind me to submit the quarterly report before Friday"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Submit quarterly report"))
                .andExpect(jsonPath("$.description").value("Submit the quarterly report before Friday"))
                .andExpect(jsonPath("$.dueDate").value("2026-05-22"))
                .andExpect(jsonPath("$.priority").value("MEDIUM"))
                .andExpect(jsonPath("$.status").value("TODO"));
    }
}
