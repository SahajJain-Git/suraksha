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
import org.suraksha.entity.User;
import org.suraksha.entity.UserLessonProgress;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * DataJpaTest repository slice test for UserLessonProgressRepository.
 * Tests compound uniqueness constraint on (user_id, lesson_id) and completed filters.
 */
@DataJpaTest
@ActiveProfiles("test")
class UserLessonProgressRepositoryJpaTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserLessonProgressRepository progressRepository;

    private User user1;
    private User user2;
    private Lesson lesson1;
    private Lesson lesson2;

    @BeforeEach
    void setUp() {
        user1 = new User("Student A", "student.a@suraksha.org", "pwd1");
        user2 = new User("Student B", "student.b@suraksha.org", "pwd2");
        entityManager.persist(user1);
        entityManager.persist(user2);

        Course course = new Course("cyclone-101", "Cyclone Safety", "CYCLONE", "BEGINNER", 20, "Summary");
        entityManager.persist(course);

        lesson1 = new Lesson("cyc-1", "Warning Signal 1", 1, "<p>Signal 1</p>", "DO", "DONT");
        lesson1.setCourse(course);
        lesson2 = new Lesson("cyc-2", "Evacuation Route", 2, "<p>Route</p>", "DO", "DONT");
        lesson2.setCourse(course);
        entityManager.persist(lesson1);
        entityManager.persist(lesson2);

        entityManager.flush();
    }

    @Test
    @DisplayName("Queries: findByUserIdAndLessonId and completed filter enforce user isolation")
    void testProgressQueriesAndUserIsolation() {
        // User1 completes lesson1
        UserLessonProgress p1 = new UserLessonProgress(user1, lesson1, true);
        entityManager.persist(p1);

        // User1 starts lesson2 but not completed
        UserLessonProgress p2 = new UserLessonProgress(user1, lesson2, false);
        entityManager.persist(p2);

        // User2 completes lesson2
        UserLessonProgress p3 = new UserLessonProgress(user2, lesson2, true);
        entityManager.persist(p3);
        entityManager.flush();

        // 1. Check findByUserIdAndLessonId
        Optional<UserLessonProgress> found = progressRepository.findByUserIdAndLessonId(user1.getId(), lesson1.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getCompleted()).isTrue();

        // 2. Check completed filter for User1: only 1 completed
        List<UserLessonProgress> user1Completed = progressRepository.findByUserIdAndCompletedTrue(user1.getId());
        assertThat(user1Completed).hasSize(1);
        assertThat(user1Completed.get(0).getLesson().getId()).isEqualTo(lesson1.getId());

        // 3. User2 completed list does NOT leak User1 data
        List<UserLessonProgress> user2Completed = progressRepository.findByUserIdAndCompletedTrue(user2.getId());
        assertThat(user2Completed).hasSize(1);
        assertThat(user2Completed.get(0).getLesson().getId()).isEqualTo(lesson2.getId());
    }

    @Test
    @DisplayName("Constraint: Duplicate (user_id, lesson_id) throws DataIntegrityViolationException on flush")
    void testDuplicateUserLessonConstraintThrows() {
        UserLessonProgress progress1 = new UserLessonProgress(user1, lesson1, true);
        progressRepository.save(progress1);
        entityManager.flush();

        UserLessonProgress duplicate = new UserLessonProgress(user1, lesson1, false);

        assertThatThrownBy(() -> {
            progressRepository.save(duplicate);
            entityManager.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }
}
