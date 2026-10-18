package org.suraksha.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.suraksha.dto.CourseDetailDto;
import org.suraksha.dto.CourseSummaryDto;
import org.suraksha.dto.LessonDetailDto;
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
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class CourseService {

    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final QuizRepository quizRepository;
    private final DrillScenarioRepository drillScenarioRepository;

    public CourseService(CourseRepository courseRepository,
                         LessonRepository lessonRepository,
                         QuizRepository quizRepository,
                         DrillScenarioRepository drillScenarioRepository) {
        this.courseRepository = courseRepository;
        this.lessonRepository = lessonRepository;
        this.quizRepository = quizRepository;
        this.drillScenarioRepository = drillScenarioRepository;
    }

    public List<CourseSummaryDto> getAllCourses(String category, String search) {
        String catParam = (category != null && !category.trim().isEmpty()) ? category.trim() : null;
        String searchParam = (search != null && !search.trim().isEmpty()) ? search.trim() : null;

        List<Course> courses = courseRepository.searchCourses(catParam, searchParam);

        return courses.stream().map(this::mapToSummaryDto).collect(Collectors.toList());
    }

    public CourseDetailDto getCourseBySlug(String slug) {
        Course course = courseRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with slug: " + slug));

        List<Lesson> lessons = lessonRepository.findByCourseIdOrderBySequenceOrderAsc(course.getId());
        List<LessonDetailDto> lessonDtos = lessons.stream()
                .map(this::mapToLessonDetailDto)
                .collect(Collectors.toList());

        CourseDetailDto dto = new CourseDetailDto(
                course.getId(),
                course.getSlug(),
                course.getTitle(),
                course.getCategory(),
                course.getDifficulty(),
                course.getEstimatedMinutes(),
                course.getSummary(),
                lessonDtos
        );

        Optional<Quiz> quiz = quizRepository.findByCourseId(course.getId());
        quiz.ifPresent(q -> {
            dto.setQuizId(q.getId());
            dto.setQuizSlug(q.getSlug());
        });

        Optional<DrillScenario> drill = drillScenarioRepository.findByCourseId(course.getId());
        drill.ifPresent(d -> {
            dto.setDrillId(d.getId());
            dto.setDrillSlug(d.getSlug());
        });

        return dto;
    }

    private CourseSummaryDto mapToSummaryDto(Course course) {
        long count = lessonRepository.countByCourseId(course.getId());
        return new CourseSummaryDto(
                course.getId(),
                course.getSlug(),
                course.getTitle(),
                course.getCategory(),
                course.getDifficulty(),
                course.getEstimatedMinutes(),
                course.getSummary(),
                (int) count
        );
    }

    private LessonDetailDto mapToLessonDetailDto(Lesson lesson) {
        return new LessonDetailDto(
                lesson.getId(),
                lesson.getSlug(),
                lesson.getTitle(),
                lesson.getSequenceOrder(),
                lesson.getContent(),
                lesson.getDosGuidelines(),
                lesson.getDontsGuidelines()
        );
    }
}
