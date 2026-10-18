package org.suraksha.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.suraksha.dto.DrillResultDto;
import org.suraksha.dto.DrillScenarioDto;
import org.suraksha.dto.DrillSubmissionRequestDto;
import org.suraksha.entity.*;
import org.suraksha.exception.ResourceNotFoundException;
import org.suraksha.repository.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for DrillService verifying scenario graph retrieval, choice validation,
 * positive/negative scoring impacts, and performance rating thresholds.
 */
@ExtendWith(MockitoExtension.class)
class DrillServiceTest {

    @Mock
    private DrillScenarioRepository drillScenarioRepository;

    @Mock
    private DrillStepRepository drillStepRepository;

    @Mock
    private DrillChoiceRepository drillChoiceRepository;

    @Mock
    private DrillAttemptRepository drillAttemptRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DrillService drillService;

    private User student;
    private DrillScenario scenario;
    private DrillStep step1;
    private DrillStep step2;
    private DrillChoice choiceSafe;
    private DrillChoice choiceHazardous;

    @BeforeEach
    void setUp() {
        student = new User("Rohan Verma", "rohan@school.edu", "hashedPwd");
        student.setId(60L);

        Course course = new Course("fire-101", "Fire Safety", "FIRE", "BEGINNER", 15, "Fire drill");
        course.setId(20L);

        scenario = new DrillScenario("drill-fire", "Laboratory Fire Drill", "FIRE", "Chemical fire outbreak");
        scenario.setId(1L);
        scenario.setCourse(course);

        step1 = new DrillStep("step-1", "Smoke fills corridor", "What is your immediate action?", false);
        step1.setId(10L);
        step1.setScenario(scenario);
        scenario.setInitialStep(step1);

        step2 = new DrillStep("step-2", "Exit door reached", "Door handle is hot. Proceed?", true);
        step2.setId(20L);
        step2.setScenario(scenario);

        choiceSafe = new DrillChoice("Crawl low under smoke and feel door with back of hand", "SAFE", "Avoided toxic gas inhalation", 40, 1);
        choiceSafe.setId(101L);
        choiceSafe.setStep(step1);
        choiceSafe.setNextStep(step2);

        choiceHazardous = new DrillChoice("Stand tall and open hot door without checking", "DANGEROUS", "Inhaled toxic smoke, risked backdraft", -20, 2);
        choiceHazardous.setId(102L);
        choiceHazardous.setStep(step1);
    }

    @Test
    @DisplayName("Retrieve Scenario: Returns graph metadata, initial step, and choices")
    void testGetDrillByIdOrSlug() {
        when(drillScenarioRepository.findBySlug("drill-fire")).thenReturn(Optional.of(scenario));
        when(drillStepRepository.findByScenarioId(1L)).thenReturn(List.of(step1, step2));
        when(drillChoiceRepository.findByStepIdOrderBySequenceOrderAsc(10L)).thenReturn(List.of(choiceSafe, choiceHazardous));
        when(drillChoiceRepository.findByStepIdOrderBySequenceOrderAsc(20L)).thenReturn(Collections.emptyList());

        DrillScenarioDto dto = drillService.getDrillByIdOrSlug("drill-fire");

        assertThat(dto).isNotNull();
        assertThat(dto.getSlug()).isEqualTo("drill-fire");
        assertThat(dto.getInitialStepId()).isEqualTo(10L);
        assertThat(dto.getSteps()).hasSize(2);
        assertThat(dto.getSteps().get(0).getChoices()).hasSize(2);
        assertThat(dto.getSteps().get(0).getChoices().get(0).getScoreImpact()).isEqualTo(40);
    }

    @Test
    @DisplayName("Rating: Score >= 80 yields 'Preparedness Master'")
    void testSubmitDrillPreparednessMasterRating() {
        when(userRepository.findByEmail("rohan@school.edu")).thenReturn(Optional.of(student));
        when(drillScenarioRepository.findBySlug("drill-fire")).thenReturn(Optional.of(scenario));

        DrillChoice masterChoice1 = new DrillChoice("Safe Action 1", "SAFE", "Good", 50, 1);
        masterChoice1.setId(201L);
        masterChoice1.setStep(step1);

        DrillChoice masterChoice2 = new DrillChoice("Safe Action 2", "SAFE", "Good", 35, 2);
        masterChoice2.setId(202L);
        masterChoice2.setStep(step2);

        when(drillChoiceRepository.findById(201L)).thenReturn(Optional.of(masterChoice1));
        when(drillChoiceRepository.findById(202L)).thenReturn(Optional.of(masterChoice2));
        when(drillAttemptRepository.save(any(DrillAttempt.class))).thenAnswer(inv -> inv.getArgument(0));

        DrillSubmissionRequestDto submission = new DrillSubmissionRequestDto(List.of(201L, 202L));
        DrillResultDto result = drillService.submitDrill("drill-fire", submission, "rohan@school.edu");

        // 50 + 35 = 85 (>= 80)
        assertThat(result.getTotalScore()).isEqualTo(85);
        assertThat(result.getRatingLabel()).isEqualTo("Preparedness Master");
    }

    @Test
    @DisplayName("Rating: Score >= 50 and < 80 yields 'Safety Conscious'")
    void testSubmitDrillSafetyConsciousRating() {
        when(userRepository.findByEmail("rohan@school.edu")).thenReturn(Optional.of(student));
        when(drillScenarioRepository.findBySlug("drill-fire")).thenReturn(Optional.of(scenario));

        DrillChoice choice1 = new DrillChoice("Safe Action 1", "SAFE", "Good", 40, 1);
        choice1.setId(301L);
        choice1.setStep(step1);

        DrillChoice choice2 = new DrillChoice("Safe Action 2", "SAFE", "Good", 20, 2);
        choice2.setId(302L);
        choice2.setStep(step2);

        when(drillChoiceRepository.findById(301L)).thenReturn(Optional.of(choice1));
        when(drillChoiceRepository.findById(302L)).thenReturn(Optional.of(choice2));
        when(drillAttemptRepository.save(any(DrillAttempt.class))).thenAnswer(inv -> inv.getArgument(0));

        // 40 + 20 = 60
        DrillSubmissionRequestDto submission = new DrillSubmissionRequestDto(List.of(301L, 302L));
        DrillResultDto result = drillService.submitDrill("drill-fire", submission, "rohan@school.edu");

        assertThat(result.getTotalScore()).isEqualTo(60);
        assertThat(result.getRatingLabel()).isEqualTo("Safety Conscious");
    }

    @Test
    @DisplayName("Rating: Score >= 20 and < 50 yields 'Developing Awareness'")
    void testSubmitDrillDevelopingAwarenessRating() {
        when(userRepository.findByEmail("rohan@school.edu")).thenReturn(Optional.of(student));
        when(drillScenarioRepository.findBySlug("drill-fire")).thenReturn(Optional.of(scenario));

        when(drillChoiceRepository.findById(101L)).thenReturn(Optional.of(choiceSafe)); // +40
        when(drillChoiceRepository.findById(102L)).thenReturn(Optional.of(choiceHazardous)); // -20
        when(drillAttemptRepository.save(any(DrillAttempt.class))).thenAnswer(inv -> inv.getArgument(0));

        // 40 - 20 = 20 (boundary condition)
        DrillSubmissionRequestDto submission = new DrillSubmissionRequestDto(List.of(101L, 102L));
        DrillResultDto result = drillService.submitDrill("drill-fire", submission, "rohan@school.edu");

        assertThat(result.getTotalScore()).isEqualTo(20);
        assertThat(result.getRatingLabel()).isEqualTo("Developing Awareness");
    }

    @Test
    @DisplayName("Rating: Score < 20 yields 'Needs Practice'")
    void testSubmitDrillNeedsPracticeRating() {
        when(userRepository.findByEmail("rohan@school.edu")).thenReturn(Optional.of(student));
        when(drillScenarioRepository.findBySlug("drill-fire")).thenReturn(Optional.of(scenario));

        when(drillChoiceRepository.findById(102L)).thenReturn(Optional.of(choiceHazardous)); // -20
        when(drillAttemptRepository.save(any(DrillAttempt.class))).thenAnswer(inv -> inv.getArgument(0));

        // -20 < 20
        DrillSubmissionRequestDto submission = new DrillSubmissionRequestDto(List.of(102L));
        DrillResultDto result = drillService.submitDrill("drill-fire", submission, "rohan@school.edu");

        assertThat(result.getTotalScore()).isEqualTo(-20);
        assertThat(result.getRatingLabel()).isEqualTo("Needs Practice");
    }

    @Test
    @DisplayName("Choice Validation: Submitting choice belonging to another scenario throws IllegalArgumentException")
    void testSubmitDrillForeignChoiceThrows() {
        DrillScenario otherScenario = new DrillScenario("flood-drill", "Flood", "FLOOD", "Flood drill");
        otherScenario.setId(99L);

        DrillStep foreignStep = new DrillStep("foreign-step", "Water rises", "Action?", false);
        foreignStep.setId(999L);
        foreignStep.setScenario(otherScenario);

        DrillChoice foreignChoice = new DrillChoice("Move to roof", "SAFE", "Elevated", 50, 1);
        foreignChoice.setId(9999L);
        foreignChoice.setStep(foreignStep);

        when(userRepository.findByEmail("rohan@school.edu")).thenReturn(Optional.of(student));
        when(drillScenarioRepository.findBySlug("drill-fire")).thenReturn(Optional.of(scenario));
        when(drillChoiceRepository.findById(9999L)).thenReturn(Optional.of(foreignChoice));

        DrillSubmissionRequestDto submission = new DrillSubmissionRequestDto(List.of(9999L));

        assertThatThrownBy(() -> drillService.submitDrill("drill-fire", submission, "rohan@school.edu"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("does not belong to drill scenario");
    }

    @Test
    @DisplayName("Validation: Empty choice submission throws IllegalArgumentException")
    void testSubmitDrillEmptyChoicesThrows() {
        when(userRepository.findByEmail("rohan@school.edu")).thenReturn(Optional.of(student));
        when(drillScenarioRepository.findBySlug("drill-fire")).thenReturn(Optional.of(scenario));

        DrillSubmissionRequestDto submission = new DrillSubmissionRequestDto(Collections.emptyList());

        assertThatThrownBy(() -> drillService.submitDrill("drill-fire", submission, "rohan@school.edu"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must contain at least one selected choice");
    }
}
