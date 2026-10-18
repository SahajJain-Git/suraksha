package org.suraksha.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.suraksha.dto.*;
import org.suraksha.entity.*;
import org.suraksha.exception.ResourceNotFoundException;
import org.suraksha.repository.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ProgressService {

    private final UserLessonProgressRepository userLessonProgressRepository;
    private final UserRepository userRepository;
    private final LessonRepository lessonRepository;
    private final CourseRepository courseRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final DrillAttemptRepository drillAttemptRepository;

    public ProgressService(UserLessonProgressRepository userLessonProgressRepository,
                           UserRepository userRepository,
                           LessonRepository lessonRepository,
                           CourseRepository courseRepository,
                           QuizAttemptRepository quizAttemptRepository,
                           DrillAttemptRepository drillAttemptRepository) {
        this.userLessonProgressRepository = userLessonProgressRepository;
        this.userRepository = userRepository;
        this.lessonRepository = lessonRepository;
        this.courseRepository = courseRepository;
        this.quizAttemptRepository = quizAttemptRepository;
        this.drillAttemptRepository = drillAttemptRepository;
    }

    /**
     * Marks a specific lesson as completed for the authenticated user.
     * Operation is idempotent: multiple calls for the same lesson gracefully succeed without error.
     */
    @Transactional
    public LessonProgressResponseDto markLessonCompleted(Long lessonId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found with id: " + lessonId));

        Course course = lesson.getCourse();

        Optional<UserLessonProgress> existingProgress =
                userLessonProgressRepository.findByUserIdAndLessonId(user.getId(), lesson.getId());

        UserLessonProgress progress;
        if (existingProgress.isPresent()) {
            progress = existingProgress.get();
            if (!Boolean.TRUE.equals(progress.getCompleted())) {
                progress.setCompleted(true);
                progress.setCompletedAt(LocalDateTime.now());
                progress = userLessonProgressRepository.save(progress);
            }
        } else {
            progress = new UserLessonProgress(user, lesson, true);
            progress = userLessonProgressRepository.save(progress);
        }

        return new LessonProgressResponseDto(
                lesson.getId(),
                lesson.getSlug(),
                lesson.getTitle(),
                course != null ? course.getId() : null,
                course != null ? course.getSlug() : null,
                course != null ? course.getTitle() : null,
                progress.getCompleted(),
                progress.getCompletedAt()
        );
    }

    /**
     * Retrieves the comprehensive learning progress summary for the authenticated user.
     * Enforces strict user isolation: queries only filter by the authenticated user's ID.
     */
    public UserProgressSummaryDto getUserProgress(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        Long userId = user.getId();

        // 1. Fetch completed lessons for user
        List<UserLessonProgress> progressList = userLessonProgressRepository.findByUserIdAndCompletedTrue(userId);
        Set<Long> completedLessonIds = progressList.stream()
                .map(p -> p.getLesson().getId())
                .collect(Collectors.toSet());

        // 2. Fetch all courses and calculate course-level completion
        List<Course> courses = courseRepository.findAll();
        List<CourseProgressSummaryDto> courseProgressList = new ArrayList<>();
        int completedCoursesCount = 0;

        for (Course course : courses) {
            List<Lesson> lessons = lessonRepository.findByCourseIdOrderBySequenceOrderAsc(course.getId());
            int totalLessons = lessons.size();

            int completedInCourse = 0;
            for (Lesson l : lessons) {
                if (completedLessonIds.contains(l.getId())) {
                    completedInCourse++;
                }
            }

            boolean isCourseComplete = (totalLessons > 0 && completedInCourse == totalLessons);
            if (isCourseComplete) {
                completedCoursesCount++;
            }

            int percent = totalLessons > 0
                    ? (int) Math.round(((double) completedInCourse / totalLessons) * 100)
                    : 0;

            courseProgressList.add(new CourseProgressSummaryDto(
                    course.getId(),
                    course.getSlug(),
                    course.getTitle(),
                    course.getCategory(),
                    totalLessons,
                    completedInCourse,
                    isCourseComplete,
                    percent
            ));
        }

        // 3. Fetch Quiz Attempts
        List<QuizAttempt> quizAttempts = quizAttemptRepository.findByUserIdOrderByAttemptedAtDesc(userId);
        int quizzesPassed = 0;
        List<QuizAttemptSummaryDto> quizAttemptDtos = new ArrayList<>();

        for (QuizAttempt qa : quizAttempts) {
            if (Boolean.TRUE.equals(qa.getPassed())) {
                quizzesPassed++;
            }
            quizAttemptDtos.add(new QuizAttemptSummaryDto(
                    qa.getId(),
                    qa.getQuiz().getId(),
                    qa.getQuiz().getSlug(),
                    qa.getQuiz().getTitle(),
                    qa.getScorePercent(),
                    qa.getTotalQuestions(),
                    qa.getCorrectAnswers(),
                    qa.getPassed(),
                    qa.getAttemptedAt()
            ));
        }

        // 4. Fetch Drill Attempts
        List<DrillAttempt> drillAttempts = drillAttemptRepository.findByUserIdOrderByCompletedAtDesc(userId);
        List<DrillAttemptSummaryDto> drillAttemptDtos = drillAttempts.stream()
                .map(da -> new DrillAttemptSummaryDto(
                        da.getId(),
                        da.getScenario().getId(),
                        da.getScenario().getSlug(),
                        da.getScenario().getTitle(),
                        da.getTotalScore(),
                        da.getRatingLabel(),
                        da.getCompletedAt()
                )).collect(Collectors.toList());

        // 5. Build Aggregated Summary
        UserProgressSummaryDto summary = new UserProgressSummaryDto();
        summary.setUserId(userId);
        summary.setUserEmail(user.getEmail());
        summary.setFullName(user.getFullName());
        summary.setTotalCourses(courses.size());
        summary.setCompletedCoursesCount(completedCoursesCount);
        summary.setTotalCompletedLessonsCount(completedLessonIds.size());
        summary.setCompletedLessonIds(new ArrayList<>(completedLessonIds));
        summary.setCourseProgress(courseProgressList);
        summary.setTotalQuizAttempts(quizAttempts.size());
        summary.setTotalQuizzesPassed(quizzesPassed);
        summary.setQuizAttempts(quizAttemptDtos);
        summary.setTotalDrillAttempts(drillAttempts.size());
        summary.setDrillAttempts(drillAttemptDtos);

        return summary;
    }
}
