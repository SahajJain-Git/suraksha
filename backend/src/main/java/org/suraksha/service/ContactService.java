package org.suraksha.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.suraksha.dto.ContactRequestDto;
import org.suraksha.dto.ContactResponseDto;
import org.suraksha.entity.ContactMessage;
import org.suraksha.repository.ContactMessageRepository;

@Service
public class ContactService {

    private final ContactMessageRepository contactMessageRepository;

    public ContactService(ContactMessageRepository contactMessageRepository) {
        this.contactMessageRepository = contactMessageRepository;
    }

    @Transactional
    public ContactResponseDto submitContactMessage(ContactRequestDto request) {
        ContactMessage contactMessage = new ContactMessage(
                request.getFullName().trim(),
                request.getEmail().trim().toLowerCase(),
                request.getCategory().trim(),
                request.getSubject().trim(),
                request.getMessage().trim()
        );

        ContactMessage savedMessage = contactMessageRepository.save(contactMessage);

        return new ContactResponseDto(
                savedMessage.getId(),
                savedMessage.getFullName(),
                savedMessage.getEmail(),
                savedMessage.getCategory(),
                savedMessage.getSubject(),
                savedMessage.getStatus(),
                savedMessage.getCreatedAt()
        );
    }
}
