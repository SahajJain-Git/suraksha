package org.suraksha;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.suraksha.dto.ContactRequestDto;
import org.suraksha.entity.ContactMessage;
import org.suraksha.repository.ContactMessageRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ContactControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ContactMessageRepository contactMessageRepository;

    @BeforeEach
    void setUp() {
        contactMessageRepository.deleteAll();
    }

    @Test
    void testSubmitContactMessagePublicSuccess() throws Exception {
        ContactRequestDto request = new ContactRequestDto(
                "Priya Sharma",
                "priya.sharma@school.edu",
                "SCHOOL",
                "Request for School Safety Workshop",
                "We would like to integrate Suraksha virtual drills into our safety week."
        );

        mockMvc.perform(post("/api/contact")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Thank you for reaching out. Your message has been received."))
                .andExpect(jsonPath("$.data.fullName").value("Priya Sharma"))
                .andExpect(jsonPath("$.data.email").value("priya.sharma@school.edu"))
                .andExpect(jsonPath("$.data.status").value("NEW"));

        List<ContactMessage> messages = contactMessageRepository.findAll();
        assertThat(messages).hasSize(1);
        assertThat(messages.get(0).getFullName()).isEqualTo("Priya Sharma");
        assertThat(messages.get(0).getCategory()).isEqualTo("SCHOOL");
        assertThat(messages.get(0).getStatus()).isEqualTo("NEW");
    }

    @Test
    void testSubmitContactValidationFailureShortMessage() throws Exception {
        ContactRequestDto request = new ContactRequestDto(
                "Priya Sharma",
                "priya@school.edu",
                "GENERAL",
                "Question about drills",
                "Too short" // Less than 15 chars
        );

        mockMvc.perform(post("/api/contact")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed for one or more fields"))
                .andExpect(jsonPath("$.details[0]").value(org.hamcrest.Matchers.containsString("Message must be at least 15 characters")));
    }

    @Test
    void testSubmitContactValidationFailureInvalidEmail() throws Exception {
        ContactRequestDto request = new ContactRequestDto(
                "Priya Sharma",
                "not-an-email",
                "GENERAL",
                "Valid Subject Header",
                "This is a sufficiently long message body providing helpful details."
        );

        mockMvc.perform(post("/api/contact")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}
