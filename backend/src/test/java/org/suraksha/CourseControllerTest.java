package org.suraksha;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.suraksha.entity.Course;
import org.suraksha.entity.Lesson;
import org.suraksha.repository.CourseRepository;
import org.suraksha.repository.LessonRepository;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CourseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private LessonRepository lessonRepository;

    private Course course1;
    private Course course2;

    @BeforeEach
    void setUp() {
        courseRepository.deleteAll();

        course1 = new Course(
                "earthquake-101",
                "Earthquake Preparedness & Response",
                "EARTHQUAKE",
                "BEGINNER",
                25,
                "Master the Drop, Cover, and Hold On technique and prepare a 72-hour family survival kit."
        );
        Lesson l1 = new Lesson("eq-lesson-1", "The Science of Shaking", 1, "<p>Seismic waves content</p>", "DO", "DONT");
        Lesson l2 = new Lesson("eq-lesson-2", "Drop, Cover, and Hold On", 2, "<p>Drop cover content</p>", "DO", "DONT");
        course1.addLesson(l1);
        course1.addLesson(l2);
        course1 = courseRepository.save(course1);

        course2 = new Course(
                "flood-101",
                "Flood & Flash Flood Safety",
                "FLOOD",
                "BEGINNER",
                20,
                "Learn the danger of moving water and evacuation tactics."
        );
        Lesson fl1 = new Lesson("fl-lesson-1", "Flash Flood Warnings", 1, "<p>Moving water risks</p>", "DO", "DONT");
        course2.addLesson(fl1);
        course2 = courseRepository.save(course2);
    }

    @Test
    void testGetAllCoursesPublic() throws Exception {
        mockMvc.perform(get("/api/courses")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Courses retrieved successfully"))
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].slug").value("earthquake-101"))
                .andExpect(jsonPath("$.data[0].lessonsCount").value(2))
                .andExpect(jsonPath("$.data[1].slug").value("flood-101"))
                .andExpect(jsonPath("$.data[1].lessonsCount").value(1));
    }

    @Test
    void testGetCoursesByCategoryFilter() throws Exception {
        mockMvc.perform(get("/api/courses")
                .param("category", "FLOOD")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].slug").value("flood-101"))
                .andExpect(jsonPath("$.data[0].category").value("FLOOD"));
    }

    @Test
    void testGetCoursesBySearchQuery() throws Exception {
        mockMvc.perform(get("/api/courses")
                .param("search", "survival")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].slug").value("earthquake-101"));
    }

    @Test
    void testGetCourseBySlugSuccess() throws Exception {
        mockMvc.perform(get("/api/courses/earthquake-101")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.slug").value("earthquake-101"))
                .andExpect(jsonPath("$.data.title").value("Earthquake Preparedness & Response"))
                .andExpect(jsonPath("$.data.lessons", hasSize(2)))
                .andExpect(jsonPath("$.data.lessons[0].slug").value("eq-lesson-1"))
                .andExpect(jsonPath("$.data.lessons[0].sequenceOrder").value(1))
                .andExpect(jsonPath("$.data.lessons[1].slug").value("eq-lesson-2"))
                .andExpect(jsonPath("$.data.lessons[1].sequenceOrder").value(2));
    }

    @Test
    void testGetCourseBySlugNotFound() throws Exception {
        mockMvc.perform(get("/api/courses/nonexistent-hazard-slug")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Course not found with slug: nonexistent-hazard-slug"));
    }
}
