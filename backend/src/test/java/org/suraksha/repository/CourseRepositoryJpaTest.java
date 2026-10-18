package org.suraksha.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.suraksha.entity.Course;
import org.suraksha.entity.Lesson;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * DataJpaTest repository slice test for CourseRepository and LessonRepository.
 * Tests custom JPQL search query, ordering, relationships, and uniqueness constraints.
 */
@DataJpaTest
@ActiveProfiles("test")
class CourseRepositoryJpaTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private LessonRepository lessonRepository;

    private Course eqCourse;
    private Course floodCourse;
    private Course fireCourse;

    @BeforeEach
    void setUp() {
        eqCourse = new Course("earthquake-101", "Earthquake Preparedness", "EARTHQUAKE", "BEGINNER", 20, "Drop Cover Hold");
        Lesson l1 = new Lesson("eq-l1", "Drop Cover Hold", 1, "<p>Technique</p>", "DO anchor", "DONT elevator");
        Lesson l2 = new Lesson("eq-l2", "Evacuation Plan", 2, "<p>Routes</p>", "DO walk", "DONT push");
        eqCourse.addLesson(l1);
        eqCourse.addLesson(l2);
        entityManager.persist(eqCourse);

        floodCourse = new Course("flood-101", "Flood & Flash Flood", "FLOOD", "INTERMEDIATE", 30, "Moving water risks");
        Lesson fl1 = new Lesson("fl-l1", "Rising Waters", 1, "<p>Depth</p>", "DO climb high", "DONT drive");
        floodCourse.addLesson(fl1);
        entityManager.persist(floodCourse);

        fireCourse = new Course("fire-101", "Fire Evacuation Safety", "FIRE", "BEGINNER", 15, "Smoke inhalation protocols");
        entityManager.persist(fireCourse);

        entityManager.flush();
    }

    @Test
    @DisplayName("Query searchCourses: Filters by exact category (case-insensitive)")
    void testSearchCoursesByCategory() {
        List<Course> results = courseRepository.searchCourses("earthquake", null);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getSlug()).isEqualTo("earthquake-101");
    }

    @Test
    @DisplayName("Query searchCourses: Filters by keyword in title or summary")
    void testSearchCoursesByKeyword() {
        List<Course> results = courseRepository.searchCourses(null, "Smoke");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getSlug()).isEqualTo("fire-101");
    }

    @Test
    @DisplayName("Query searchCourses: Both category and keyword matching")
    void testSearchCoursesByCategoryAndKeyword() {
        List<Course> results = courseRepository.searchCourses("FLOOD", "Moving");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getSlug()).isEqualTo("flood-101");

        List<Course> mismatch = courseRepository.searchCourses("EARTHQUAKE", "Moving");
        assertThat(mismatch).isEmpty();
    }

    @Test
    @DisplayName("Query searchCourses: Neither filter returns all courses ordered by id ASC")
    void testSearchCoursesNoFilterReturnsAll() {
        List<Course> results = courseRepository.searchCourses(null, null);

        assertThat(results).hasSize(3);
        assertThat(results.get(0).getId()).isLessThan(results.get(1).getId());
        assertThat(results.get(1).getId()).isLessThan(results.get(2).getId());
    }

    @Test
    @DisplayName("Relationship: Cascades lessons and countByCourseId accurately counts")
    void testLessonCountByCourseId() {
        long count = lessonRepository.countByCourseId(eqCourse.getId());
        assertThat(count).isEqualTo(2L);

        List<Lesson> orderedLessons = lessonRepository.findByCourseIdOrderBySequenceOrderAsc(eqCourse.getId());
        assertThat(orderedLessons).hasSize(2);
        assertThat(orderedLessons.get(0).getSequenceOrder()).isEqualTo(1);
        assertThat(orderedLessons.get(1).getSequenceOrder()).isEqualTo(2);
    }

    @Test
    @DisplayName("Constraint: Duplicate course slug throws DataIntegrityViolationException on flush")
    void testDuplicateSlugConstraintViolation() {
        Course duplicate = new Course("earthquake-101", "Another Earthquake", "EARTHQUAKE", "ADVANCED", 40, "Dup");
        assertThatThrownBy(() -> {
            courseRepository.save(duplicate);
            entityManager.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }
}
