package org.suraksha;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.suraksha.entity.*;
import org.suraksha.repository.*;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class RepositoryTests {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private ContactMessageRepository contactMessageRepository;

    @Test
    void testUserAndRolePersistence() {
        Role studentRole = roleRepository.save(new Role("ROLE_STUDENT", "Student Role"));
        assertThat(studentRole.getId()).isNotNull();

        User user = new User("Test Student", "student.test@suraksha.org", "$2a$10$hashedPasswordSample");
        user.addRole(studentRole);
        User savedUser = userRepository.save(user);

        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getRoles()).hasSize(1);
        assertThat(savedUser.getCreatedAt()).isNotNull();

        Optional<User> fetched = userRepository.findByEmail("student.test@suraksha.org");
        assertThat(fetched).isPresent();
        assertThat(fetched.get().getFullName()).isEqualTo("Test Student");
    }

    @Test
    void testCourseAndLessonRelationship() {
        Course course = new Course("earthquake-test", "Earthquake Test", "EARTHQUAKE", "BEGINNER", 20, "Test summary");
        Lesson lesson1 = new Lesson("lesson-1", "Lesson 1 Title", 1, "<p>Content</p>", "DO", "DONT");
        course.addLesson(lesson1);

        Course savedCourse = courseRepository.save(course);
        assertThat(savedCourse.getId()).isNotNull();
        assertThat(savedCourse.getLessons()).hasSize(1);
        assertThat(savedCourse.getLessons().get(0).getId()).isNotNull();

        Optional<Course> fetchedCourse = courseRepository.findBySlug("earthquake-test");
        assertThat(fetchedCourse).isPresent();
        assertThat(fetchedCourse.get().getLessons()).hasSize(1);
    }

    @Test
    void testContactMessagePersistence() {
        ContactMessage msg = new ContactMessage("Priya Sharma", "priya@example.com", "SCHOOL", "Inquiry", "Message content");
        ContactMessage saved = contactMessageRepository.save(msg);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getStatus()).isEqualTo("NEW");
        assertThat(saved.getCreatedAt()).isNotNull();
    }
}
