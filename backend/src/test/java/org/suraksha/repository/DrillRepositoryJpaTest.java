package org.suraksha.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.suraksha.entity.*;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * DataJpaTest repository slice test for Drill Scenario, Steps, Choices, and Attempts.
 */
@DataJpaTest
@ActiveProfiles("test")
class DrillRepositoryJpaTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private DrillScenarioRepository drillScenarioRepository;

    @Autowired
    private DrillStepRepository drillStepRepository;

    @Autowired
    private DrillChoiceRepository drillChoiceRepository;

    @Autowired
    private DrillAttemptRepository drillAttemptRepository;

    private User testUser;
    private Course testCourse;
    private DrillScenario scenario;
    private DrillStep step1;
    private DrillChoice choiceSafe;

    @BeforeEach
    void setUp() {
        testUser = new User("Drill Learner", "learner.drill@suraksha.org", "pwdHash");
        entityManager.persist(testUser);

        testCourse = new Course("fire-101", "Fire Safety", "FIRE", "BEGINNER", 15, "Summary");
        entityManager.persist(testCourse);

        scenario = new DrillScenario("drill-fire-evac", "School Fire Evacuation Drill", "FIRE", "Simulated chemistry lab fire");
        scenario.setCourse(testCourse);
        scenario = drillScenarioRepository.save(scenario);

        step1 = new DrillStep("step-smoke", "Smoke detected in biology corridor", "What is your immediate posture?", false);
        step1.setScenario(scenario);
        step1 = drillStepRepository.save(step1);

        scenario.setInitialStep(step1);
        scenario = drillScenarioRepository.save(scenario);

        choiceSafe = new DrillChoice("Drop low to floor and crawl toward primary emergency exit", "SAFE", "Avoided rising toxic gases", 30, 1);
        choiceSafe.setStep(step1);
        choiceSafe = drillChoiceRepository.save(choiceSafe);

        entityManager.flush();
    }

    @Test
    @DisplayName("Queries: findBySlug and findByCourseId resolve drill scenario")
    void testFindScenarioBySlugAndCourse() {
        Optional<DrillScenario> bySlug = drillScenarioRepository.findBySlug("drill-fire-evac");
        assertThat(bySlug).isPresent();
        assertThat(bySlug.get().getInitialStep().getId()).isEqualTo(step1.getId());

        Optional<DrillScenario> byCourse = drillScenarioRepository.findByCourseId(testCourse.getId());
        assertThat(byCourse).isPresent();
        assertThat(byCourse.get().getSlug()).isEqualTo("drill-fire-evac");
    }

    @Test
    @DisplayName("Hierarchy: Drill steps and choices resolve in order")
    void testStepsAndChoicesOrder() {
        List<DrillStep> steps = drillStepRepository.findByScenarioId(scenario.getId());
        assertThat(steps).hasSize(1);
        assertThat(steps.get(0).getStepKey()).isEqualTo("step-smoke");

        List<DrillChoice> choices = drillChoiceRepository.findByStepIdOrderBySequenceOrderAsc(step1.getId());
        assertThat(choices).hasSize(1);
        assertThat(choices.get(0).getOutcomeType()).isEqualTo("SAFE");
        assertThat(choices.get(0).getScoreImpact()).isEqualTo(30);
    }

    @Test
    @DisplayName("Attempts: Logs drill completion with total score and rating label")
    void testDrillAttemptLogging() {
        DrillAttempt attempt = new DrillAttempt(testUser, scenario, 70, "Safety Conscious");
        drillAttemptRepository.save(attempt);
        entityManager.flush();

        List<DrillAttempt> attempts = drillAttemptRepository.findByUserIdOrderByCompletedAtDesc(testUser.getId());
        assertThat(attempts).hasSize(1);
        assertThat(attempts.get(0).getTotalScore()).isEqualTo(70);
        assertThat(attempts.get(0).getRatingLabel()).isEqualTo("Safety Conscious");
    }

    @Test
    @DisplayName("Constraint: Duplicate scenario slug throws DataIntegrityViolationException on flush")
    void testDuplicateScenarioSlugThrows() {
        DrillScenario duplicate = new DrillScenario("drill-fire-evac", "Another Drill", "FIRE", "Dup");
        assertThatThrownBy(() -> {
            drillScenarioRepository.save(duplicate);
            entityManager.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }
}
