package org.suraksha.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.suraksha.dto.ApiResponse;
import org.suraksha.dto.ContactRequestDto;
import org.suraksha.dto.ContactResponseDto;
import org.suraksha.service.ContactService;

@RestController
@RequestMapping("/api/contact")
public class ContactController {

    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    /**
     * Submits an inquiry or institutional partnership message from the contact form.
     * PUBLIC ACCESS: Open to all site visitors.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ContactResponseDto>> submitContactMessage(
            @Valid @RequestBody ContactRequestDto request
    ) {
        ContactResponseDto response = contactService.submitContactMessage(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Thank you for reaching out. Your message has been received.", response));
    }
}
