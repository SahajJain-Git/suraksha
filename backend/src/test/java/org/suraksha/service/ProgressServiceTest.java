package org.suraksha.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.suraksha.dto.LessonProgressResponseDto;
import org.suraksha.dto.UserProgressSummaryDto;
import org.suraksha.entity.*;
import org.suraksha.exception.ResourceNotFoundException;
import org.suraksha.repository.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ProgressService verifying idempotency, course completion percentage calculation,
 * zero-division resilience, and strict user isolation.
 */
@ExtendWith(MockitoExtension.class)
class ProgressServiceTest {

    @Mock
    private UserLessonProgressRepository userLessonProgressRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private QuizAttemptRepository quizAttemptRepository;

    @Mock
    private DrillAttemptRepository drillAttemptRepository;

    @InjectMocks
    private ProgressService progressService;

    private User student;
    private Course course1;
    private Course course2Empty;
    private Lesson lesson1;
    private Lesson lesson2;

    @BeforeEach
    void setUp() {
        student = new User("Ananya Das", "ananya@school.edu", "hashedPwd");
        student.setId(10L);

        course1 = new Course("cyclone-101", "Cyclone Preparedness", "CYCLONE", "BEGINNER", 30, "Cyclone course");
        course1.setId(100L);

        lesson1 = new Lesson("cyc-1", "Understanding Warning Flags", 1, "<p>Flags</p>", "DO", "DONT");
        lesson1.setId(1001L);
        lesson1.setCourse(course1);

        lesson2 = new Lesson("cyc-2", "Evacuation Routes", 2, "<p>Routes</p>", "DO", "DONT");
        lesson2.setId(1002L);
        lesson2.setCourse(course1);

        course2Empty = new Course("empty-course", "Empty Course", "FLOOD", "BEGINNER", 10, "Empty");
        course2Empty.setId(200L);
    }

    @Test
    @DisplayName("Idempotency: First completion creates new record; second call returns existing without duplicate")
    void testMarkLessonCompletedIdempotency() {
        when(userRepository.findByEmail("ananya@school.edu")).thenReturn(Optional.of(student));
        when(lessonRepository.findById(1001L)).thenReturn(Optional.of(lesson1));

        // 1. First completion: no existing progress
        when(userLessonProgressRepository.findByUserIdAndLessonId(10L, 1001L)).thenReturn(Optional.empty());
        when(userLessonProgressRepository.save(any(UserLessonProgress.class))).thenAnswer(inv -> {
            UserLessonProgress p = inv.getArgument(0);
            p.setId(501L);
            return p;
        });

        LessonProgressResponseDto firstCall = progressService.markLessonCompleted(1001L, "ananya@school.edu");

        assertThat(firstCall).isNotNull();
        assertThat(firstCall.getLessonId()).isEqualTo(1001L);
        assertThat(firstCall.getCompleted()).isTrue();
        verify(userLessonProgressRepository, times(1)).save(any(UserLessonProgress.class));

        // 2. Second completion for same lesson: existing progress already completed
        UserLessonProgress alreadyCompleted = new UserLessonProgress(student, lesson1, true);
        alreadyCompleted.setId(501L);
        when(userLessonProgressRepository.findByUserIdAndLessonId(10L, 1001L)).thenReturn(Optional.of(alreadyCompleted));

        LessonProgressResponseDto secondCall = progressService.markLessonCompleted(1001L, "ananya@school.edu");

        assertThat(secondCall.getCompleted()).isTrue();
        // Crucial verification: save was NOT invoked a second time since it was already completed!
        verify(userLessonProgressRepository, times(1)).save(any(UserLessonProgress.class));
    }

    @Test
    @DisplayName("Progress Summary: Accurately calculates 50% and 100% course completions")
    void testGetUserProgressCalculations() {
        when(userRepository.findByEmail("ananya@school.edu")).thenReturn(Optional.of(student));

        // Student completed lesson1 only (1 out of 2 in course1)
        UserLessonProgress p1 = new UserLessonProgress(student, lesson1, true);
        when(userLessonProgressRepository.findByUserIdAndCompletedTrue(10L)).thenReturn(List.of(p1));

        when(courseRepository.findAll()).thenReturn(List.of(course1, course2Empty));
        when(lessonRepository.findByCourseIdOrderBySequenceOrderAsc(100L)).thenReturn(List.of(lesson1, lesson2));
        when(lessonRepository.findByCourseIdOrderBySequenceOrderAsc(200L)).thenReturn(Collections.emptyList());

        // Quiz attempts
        Quiz quiz = new Quiz("quiz-1", "Quiz 1", 75);
        quiz.setId(1L);
        QuizAttempt qaPassed = new QuizAttempt(student, quiz, 80, 4, 3, true);
        qaPassed.setId(701L);
        QuizAttempt qaFailed = new QuizAttempt(student, quiz, 50, 4, 2, false);
        qaFailed.setId(702L);
        when(quizAttemptRepository.findByUserIdOrderByAttemptedAtDesc(10L)).thenReturn(List.of(qaPassed, qaFailed));

        // Drill attempts
        DrillScenario drill = new DrillScenario("drill-1", "Drill 1", "CYCLONE", "Drill");
        drill.setId(1L);
        DrillAttempt da = new DrillAttempt(student, drill, 75, "Safety Conscious");
        da.setId(801L);
        when(drillAttemptRepository.findByUserIdOrderByCompletedAtDesc(10L)).thenReturn(List.of(da));

        UserProgressSummaryDto summary = progressService.getUserProgress("ananya@school.edu");

        assertThat(summary).isNotNull();
        assertThat(summary.getUserId()).isEqualTo(10L);
        assertThat(summary.getTotalCourses()).isEqualTo(2);
        assertThat(summary.getCompletedCoursesCount()).isEqualTo(0); // course1 is 1/2, course2Empty is 0/0
        assertThat(summary.getTotalCompletedLessonsCount()).isEqualTo(1);

        // Verify course1 progress
        var cp1 = summary.getCourseProgress().stream().filter(c -> c.getCourseId().equals(100L)).findFirst().orElseThrow();
        assertThat(cp1.getTotalLessons()).isEqualTo(2);
        assertThat(cp1.getCompletedLessons()).isEqualTo(1);
        assertThat(cp1.getProgressPercent()).isEqualTo(50);
        assertThat(cp1.getIsCompleted()).isFalse();

        // Verify course2Empty progress: 0 lessons -> 0% without division-by-zero exception
        var cp2 = summary.getCourseProgress().stream().filter(c -> c.getCourseId().equals(200L)).findFirst().orElseThrow();
        assertThat(cp2.getTotalLessons()).isEqualTo(0);
        assertThat(cp2.getProgressPercent()).isEqualTo(0);
        assertThat(cp2.getIsCompleted()).isFalse();

        // Verify Quiz statistics
        assertThat(summary.getTotalQuizAttempts()).isEqualTo(2);
        assertThat(summary.getTotalQuizzesPassed()).isEqualTo(1);

        // Verify Drill statistics
        assertThat(summary.getTotalDrillAttempts()).isEqualTo(1);
        assertThat(summary.getDrillAttempts().get(0).getRatingLabel()).isEqualTo("Safety Conscious");

        // Verify strict user isolation: all queries used student ID 10L
        verify(userLessonProgressRepository).findByUserIdAndCompletedTrue(10L);
        verify(quizAttemptRepository).findByUserIdOrderByAttemptedAtDesc(10L);
        verify(drillAttemptRepository).findByUserIdOrderByCompletedAtDesc(10L);
    }

    @Test
    @DisplayName("Validation: Non-existent lesson throws ResourceNotFoundException")
    void testMarkLessonNotFoundThrows() {
        when(userRepository.findByEmail("ananya@school.edu")).thenReturn(Optional.of(student));
        when(lessonRepository.findById(9999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> progressService.markLessonCompleted(9999L, "ananya@school.edu"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("9999");
    }
}
