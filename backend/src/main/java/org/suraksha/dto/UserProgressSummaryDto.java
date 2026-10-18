package org.suraksha.dto;

import java.util.ArrayList;
import java.util.List;

public class UserProgressSummaryDto {
    private Long userId;
    private String userEmail;
    private String fullName;
    private Integer totalCourses;
    private Integer completedCoursesCount;
    private Integer totalCompletedLessonsCount;
    private List<Long> completedLessonIds = new ArrayList<>();
    private List<CourseProgressSummaryDto> courseProgress = new ArrayList<>();
    private Integer totalQuizAttempts;
    private Integer totalQuizzesPassed;
    private List<QuizAttemptSummaryDto> quizAttempts = new ArrayList<>();
    private Integer totalDrillAttempts;
    private List<DrillAttemptSummaryDto> drillAttempts = new ArrayList<>();

    public UserProgressSummaryDto() {}

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public Integer getTotalCourses() { return totalCourses; }
    public void setTotalCourses(Integer totalCourses) { this.totalCourses = totalCourses; }

    public Integer getCompletedCoursesCount() { return completedCoursesCount; }
    public void setCompletedCoursesCount(Integer completedCoursesCount) { this.completedCoursesCount = completedCoursesCount; }

    public Integer getTotalCompletedLessonsCount() { return totalCompletedLessonsCount; }
    public void setTotalCompletedLessonsCount(Integer totalCompletedLessonsCount) { this.totalCompletedLessonsCount = totalCompletedLessonsCount; }

    public List<Long> getCompletedLessonIds() { return completedLessonIds; }
    public void setCompletedLessonIds(List<Long> completedLessonIds) { this.completedLessonIds = completedLessonIds; }

    public List<CourseProgressSummaryDto> getCourseProgress() { return courseProgress; }
    public void setCourseProgress(List<CourseProgressSummaryDto> courseProgress) { this.courseProgress = courseProgress; }

    public Integer getTotalQuizAttempts() { return totalQuizAttempts; }
    public void setTotalQuizAttempts(Integer totalQuizAttempts) { this.totalQuizAttempts = totalQuizAttempts; }

    public Integer getTotalQuizzesPassed() { return totalQuizzesPassed; }
    public void setTotalQuizzesPassed(Integer totalQuizzesPassed) { this.totalQuizzesPassed = totalQuizzesPassed; }

    public List<QuizAttemptSummaryDto> getQuizAttempts() { return quizAttempts; }
    public void setQuizAttempts(List<QuizAttemptSummaryDto> quizAttempts) { this.quizAttempts = quizAttempts; }

    public Integer getTotalDrillAttempts() { return totalDrillAttempts; }
    public void setTotalDrillAttempts(Integer totalDrillAttempts) { this.totalDrillAttempts = totalDrillAttempts; }

    public List<DrillAttemptSummaryDto> getDrillAttempts() { return drillAttempts; }
    public void setDrillAttempts(List<DrillAttemptSummaryDto> drillAttempts) { this.drillAttempts = drillAttempts; }
}
