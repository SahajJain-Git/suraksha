package org.suraksha.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.suraksha.dto.*;
import org.suraksha.entity.*;
import org.suraksha.exception.ResourceNotFoundException;
import org.suraksha.repository.*;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class DrillService {

    private final DrillScenarioRepository drillScenarioRepository;
    private final DrillStepRepository drillStepRepository;
    private final DrillChoiceRepository drillChoiceRepository;
    private final DrillAttemptRepository drillAttemptRepository;
    private final UserRepository userRepository;

    public DrillService(DrillScenarioRepository drillScenarioRepository,
                        DrillStepRepository drillStepRepository,
                        DrillChoiceRepository drillChoiceRepository,
                        DrillAttemptRepository drillAttemptRepository,
                        UserRepository userRepository) {
        this.drillScenarioRepository = drillScenarioRepository;
        this.drillStepRepository = drillStepRepository;
        this.drillChoiceRepository = drillChoiceRepository;
        this.drillAttemptRepository = drillAttemptRepository;
        this.userRepository = userRepository;
    }

    public DrillScenarioDto getDrillByIdOrSlug(String idOrSlug) {
        DrillScenario scenario = findScenarioEntity(idOrSlug);
        List<DrillStep> steps = drillStepRepository.findByScenarioId(scenario.getId());

        List<DrillStepDto> stepDtos = steps.stream().map(step -> {
            List<DrillChoice> choices = drillChoiceRepository.findByStepIdOrderBySequenceOrderAsc(step.getId());
            List<DrillChoiceDto> choiceDtos = choices.stream()
                    .map(c -> new DrillChoiceDto(
                            c.getId(),
                            c.getChoiceText(),
                            c.getOutcomeType(),
                            c.getFeedbackText(),
                            c.getScoreImpact(),
                            c.getNextStep() != null ? c.getNextStep().getId() : null,
                            c.getSequenceOrder()
                    )).collect(Collectors.toList());

            return new DrillStepDto(
                    step.getId(),
                    step.getStepKey(),
                    step.getSituationText(),
                    step.getQuestionText(),
                    step.getIsTerminal(),
                    choiceDtos
            );
        }).collect(Collectors.toList());

        Long initialStepId = scenario.getInitialStep() != null ? scenario.getInitialStep().getId() : null;
        Long courseId = scenario.getCourse() != null ? scenario.getCourse().getId() : null;
        String courseTitle = scenario.getCourse() != null ? scenario.getCourse().getTitle() : null;

        return new DrillScenarioDto(
                scenario.getId(),
                courseId,
                courseTitle,
                scenario.getSlug(),
                scenario.getTitle(),
                scenario.getHazardCategory(),
                scenario.getDescription(),
                initialStepId,
                stepDtos
        );
    }

    @Transactional
    public DrillResultDto submitDrill(String idOrSlug, DrillSubmissionRequestDto submission, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        DrillScenario scenario = findScenarioEntity(idOrSlug);

        if (submission == null || submission.getSelectedChoiceIds() == null || submission.getSelectedChoiceIds().isEmpty()) {
            throw new IllegalArgumentException("Drill submission must contain at least one selected choice.");
        }

        int totalScore = 0;
        List<DrillChoiceFeedbackDto> feedbacks = new ArrayList<>();

        for (Long choiceId : submission.getSelectedChoiceIds()) {
            DrillChoice choice = drillChoiceRepository.findById(choiceId)
                    .orElseThrow(() -> new ResourceNotFoundException("Drill choice not found with id: " + choiceId));

            // Verify choice belongs to this scenario
            if (!choice.getStep().getScenario().getId().equals(scenario.getId())) {
                throw new IllegalArgumentException(
                        String.format("Choice id %d does not belong to drill scenario '%s'", choiceId, scenario.getTitle())
                );
            }

            totalScore += choice.getScoreImpact();
            feedbacks.add(new DrillChoiceFeedbackDto(
                    choice.getId(),
                    choice.getChoiceText(),
                    choice.getOutcomeType(),
                    choice.getFeedbackText(),
                    choice.getScoreImpact()
            ));
        }

        String ratingLabel = determineRatingLabel(totalScore);

        DrillAttempt attempt = new DrillAttempt(user, scenario, totalScore, ratingLabel);
        DrillAttempt savedAttempt = drillAttemptRepository.save(attempt);

        return new DrillResultDto(
                savedAttempt.getId(),
                scenario.getId(),
                scenario.getSlug(),
                scenario.getTitle(),
                totalScore,
                ratingLabel,
                savedAttempt.getCompletedAt(),
                feedbacks
        );
    }

    private String determineRatingLabel(int totalScore) {
        if (totalScore >= 80) {
            return "Preparedness Master";
        } else if (totalScore >= 50) {
            return "Safety Conscious";
        } else if (totalScore >= 20) {
            return "Developing Awareness";
        } else {
            return "Needs Practice";
        }
    }

    private DrillScenario findScenarioEntity(String idOrSlug) {
        if (idOrSlug.matches("\\d+")) {
            Long id = Long.parseLong(idOrSlug);
            return drillScenarioRepository.findById(id)
                    .orElseGet(() -> drillScenarioRepository.findBySlug(idOrSlug)
                            .orElseThrow(() -> new ResourceNotFoundException("Drill scenario not found with identifier: " + idOrSlug)));
        }
        return drillScenarioRepository.findBySlug(idOrSlug)
                .orElseThrow(() -> new ResourceNotFoundException("Drill scenario not found with identifier: " + idOrSlug));
    }
}
