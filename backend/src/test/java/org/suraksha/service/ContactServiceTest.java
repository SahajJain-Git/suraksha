package org.suraksha.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.suraksha.dto.ContactRequestDto;
import org.suraksha.dto.ContactResponseDto;
import org.suraksha.entity.ContactMessage;
import org.suraksha.repository.ContactMessageRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for ContactService verifying message trimming, email normalization,
 * and entity persistence.
 */
@ExtendWith(MockitoExtension.class)
class ContactServiceTest {

    @Mock
    private ContactMessageRepository contactMessageRepository;

    @InjectMocks
    private ContactService contactService;

    @Test
    @DisplayName("Submit: Trims input whitespace, lowercases email, and defaults status to NEW")
    void testSubmitContactMessageNormalization() {
        ContactRequestDto request = new ContactRequestDto(
                "  Principal Roy  ",
                "  PRINCIPAL@DELHISCHOOL.EDU  ",
                "  SCHOOL  ",
                "  Campus Evacuation Drill  ",
                "  Please organize an interactive earthquake drill for 500 students.  "
        );

        when(contactMessageRepository.save(any(ContactMessage.class))).thenAnswer(inv -> {
            ContactMessage m = inv.getArgument(0);
            m.setId(99L);
            return m;
        });

        ContactResponseDto response = contactService.submitContactMessage(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(99L);
        assertThat(response.getFullName()).isEqualTo("Principal Roy");
        assertThat(response.getEmail()).isEqualTo("principal@delhischool.edu");
        assertThat(response.getCategory()).isEqualTo("SCHOOL");
        assertThat(response.getSubject()).isEqualTo("Campus Evacuation Drill");
        assertThat(response.getStatus()).isEqualTo("NEW");

        verify(contactMessageRepository).save(argThat(m ->
                m.getFullName().equals("Principal Roy") &&
                m.getEmail().equals("principal@delhischool.edu") &&
                m.getCategory().equals("SCHOOL") &&
                m.getSubject().equals("Campus Evacuation Drill") &&
                m.getMessage().equals("Please organize an interactive earthquake drill for 500 students.")
        ));
    }
}
