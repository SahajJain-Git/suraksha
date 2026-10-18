package org.suraksha;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.suraksha.entity.*;
import org.suraksha.repository.*;
import org.suraksha.security.CustomUserDetails;
import org.suraksha.security.JwtService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ProgressControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserLessonProgressRepository userLessonProgressRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private User testStudent;
    private String jwtToken;
    private Course testCourse;
    private Lesson lesson1;
    private Lesson lesson2;

    @BeforeEach
    void setUp() {
        userLessonProgressRepository.deleteAll();
        lessonRepository.deleteAll();
        courseRepository.deleteAll();
        userRepository.deleteAll();

        // 1. Setup Student User & JWT
        Role studentRole = roleRepository.findByName("ROLE_STUDENT")
                .orElseGet(() -> roleRepository.save(new Role("ROLE_STUDENT", "Student role")));

        testStudent = new User("Aarav Student", "student.progress@suraksha.org", passwordEncoder.encode("Password123!"));
        testStudent.addRole(studentRole);
        testStudent = userRepository.save(testStudent);

        jwtToken = jwtService.generateToken(testStudent);

        // 2. Setup Course with 2 Lessons
        testCourse = new Course("earthquake-101", "Earthquake Preparedness", "EARTHQUAKE", "BEGINNER", 20, "Summary");
        lesson1 = new Lesson("eq-lesson-1", "Lesson 1", 1, "<p>L1 content</p>", "DO", "DONT");
        lesson2 = new Lesson("eq-lesson-2", "Lesson 2", 2, "<p>L2 content</p>", "DO", "DONT");
        testCourse.addLesson(lesson1);
        testCourse.addLesson(lesson2);
        testCourse = courseRepository.save(testCourse);
    }

    @Test
    void testMarkLessonCompletedUnauthorizedWithoutToken() throws Exception {
        mockMvc.perform(post("/api/progress/lessons/" + lesson1.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testMarkLessonCompletedSuccessAndIdempotency() throws Exception {
        // First completion
        mockMvc.perform(post("/api/progress/lessons/" + lesson1.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.lessonId").value(lesson1.getId()))
                .andExpect(jsonPath("$.data.completed").value(true));

        assertThat(userLessonProgressRepository.countByUserIdAndCompletedTrue(testStudent.getId())).isEqualTo(1);

        // Second call for the same lesson must be idempotent without creating a second record
        mockMvc.perform(post("/api/progress/lessons/" + lesson1.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.completed").value(true));

        assertThat(userLessonProgressRepository.countByUserIdAndCompletedTrue(testStudent.getId())).isEqualTo(1);
    }

    @Test
    void testGetUserProgressSummary() throws Exception {
        // Complete lesson 1
        userLessonProgressRepository.save(new UserLessonProgress(testStudent, lesson1, true));

        mockMvc.perform(get("/api/progress")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value(testStudent.getId()))
                .andExpect(jsonPath("$.data.userEmail").value("student.progress@suraksha.org"))
                .andExpect(jsonPath("$.data.totalCompletedLessonsCount").value(1))
                .andExpect(jsonPath("$.data.completedLessonIds", hasItem(lesson1.getId().intValue())))
                .andExpect(jsonPath("$.data.completedCoursesCount").value(0)) // 1 of 2 lessons completed -> course not yet complete
                .andExpect(jsonPath("$.data.courseProgress[0].completedLessons").value(1))
                .andExpect(jsonPath("$.data.courseProgress[0].totalLessons").value(2))
                .andExpect(jsonPath("$.data.courseProgress[0].progressPercent").value(50))
                .andExpect(jsonPath("$.data.courseProgress[0].isCompleted").value(false));

        // Now complete lesson 2 -> course completes
        userLessonProgressRepository.save(new UserLessonProgress(testStudent, lesson2, true));

        mockMvc.perform(get("/api/progress")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalCompletedLessonsCount").value(2))
                .andExpect(jsonPath("$.data.completedCoursesCount").value(1))
                .andExpect(jsonPath("$.data.courseProgress[0].progressPercent").value(100))
                .andExpect(jsonPath("$.data.courseProgress[0].isCompleted").value(true));
    }
}
