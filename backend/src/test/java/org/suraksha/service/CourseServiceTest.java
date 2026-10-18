package org.suraksha.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.suraksha.dto.CourseDetailDto;
import org.suraksha.dto.CourseSummaryDto;
import org.suraksha.entity.Course;
import org.suraksha.entity.DrillScenario;
import org.suraksha.entity.Lesson;
import org.suraksha.entity.Quiz;
import org.suraksha.exception.ResourceNotFoundException;
import org.suraksha.repository.CourseRepository;
import org.suraksha.repository.DrillScenarioRepository;
import org.suraksha.repository.LessonRepository;
import org.suraksha.repository.QuizRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CourseService verifying category/search filtering, lesson counting,
 * and course detail assembly with linked quizzes and mock drills.
 */
@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private QuizRepository quizRepository;

    @Mock
    private DrillScenarioRepository drillScenarioRepository;

    @InjectMocks
    private CourseService courseService;

    private Course course1;
    private Course course2;

    @BeforeEach
    void setUp() {
        course1 = new Course("earthquake-101", "Earthquake Preparedness", "EARTHQUAKE", "BEGINNER", 25, "Drop, Cover, Hold");
        course1.setId(1L);

        course2 = new Course("flood-101", "Flood Safety & Rescue", "FLOOD", "INTERMEDIATE", 35, "Rising waters");
        course2.setId(2L);
    }

    @Test
    @DisplayName("Filter: Passes trimmed category and search parameters to repository")
    void testGetAllCoursesWithFilters() {
        when(courseRepository.searchCourses("EARTHQUAKE", "Drop")).thenReturn(List.of(course1));
        when(lessonRepository.countByCourseId(1L)).thenReturn(3L);

        List<CourseSummaryDto> result = courseService.getAllCourses("  EARTHQUAKE  ", "  Drop  ");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSlug()).isEqualTo("earthquake-101");
        assertThat(result.get(0).getLessonsCount()).isEqualTo(3);
        verify(courseRepository).searchCourses("EARTHQUAKE", "Drop");
    }

    @Test
    @DisplayName("Filter: Treats empty or blank filter strings as null for open query")
    void testGetAllCoursesWithBlankFilters() {
        when(courseRepository.searchCourses(null, null)).thenReturn(List.of(course1, course2));
        when(lessonRepository.countByCourseId(1L)).thenReturn(2L);
        when(lessonRepository.countByCourseId(2L)).thenReturn(4L);

        List<CourseSummaryDto> result = courseService.getAllCourses("   ", "");

        assertThat(result).hasSize(2);
        verify(courseRepository).searchCourses(null, null);
    }

    @Test
    @DisplayName("Course Details: Maps lessons and populates linked quiz and drill slugs")
    void testGetCourseBySlugWithQuizAndDrill() {
        when(courseRepository.findBySlug("earthquake-101")).thenReturn(Optional.of(course1));

        Lesson l1 = new Lesson("l-1", "Lesson 1", 1, "<p>Text</p>", "DO", "DONT");
        l1.setId(10L);
        when(lessonRepository.findByCourseIdOrderBySequenceOrderAsc(1L)).thenReturn(List.of(l1));

        Quiz quiz = new Quiz("quiz-earthquake", "EQ Quiz", 75);
        quiz.setId(50L);
        when(quizRepository.findByCourseId(1L)).thenReturn(Optional.of(quiz));

        DrillScenario drill = new DrillScenario("drill-earthquake", "EQ Drill", "EARTHQUAKE", "Drill text");
        drill.setId(60L);
        when(drillScenarioRepository.findByCourseId(1L)).thenReturn(Optional.of(drill));

        CourseDetailDto dto = courseService.getCourseBySlug("earthquake-101");

        assertThat(dto).isNotNull();
        assertThat(dto.getSlug()).isEqualTo("earthquake-101");
        assertThat(dto.getLessons()).hasSize(1);
        assertThat(dto.getQuizId()).isEqualTo(50L);
        assertThat(dto.getQuizSlug()).isEqualTo("quiz-earthquake");
        assertThat(dto.getDrillId()).isEqualTo(60L);
        assertThat(dto.getDrillSlug()).isEqualTo("drill-earthquake");
    }

    @Test
    @DisplayName("Validation: Non-existent course slug throws ResourceNotFoundException")
    void testGetCourseBySlugNotFoundThrows() {
        when(courseRepository.findBySlug("unknown-course")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseService.getCourseBySlug("unknown-course"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("unknown-course");
    }
}
