package org.suraksha.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.suraksha.dto.ApiResponse;
import org.suraksha.dto.CourseDetailDto;
import org.suraksha.dto.CourseSummaryDto;
import org.suraksha.service.CourseService;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    /**
     * Lists educational courses with optional category and search filtering.
     * Public endpoint: Accessible to all learners without authentication.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<CourseSummaryDto>>> getAllCourses(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search
    ) {
        List<CourseSummaryDto> courses = courseService.getAllCourses(category, search);
        return ResponseEntity.ok(ApiResponse.success("Courses retrieved successfully", courses));
    }

    /**
     * Retrieves detailed information and lessons for a specific course by slug.
     * Public endpoint: Accessible to all learners without authentication.
     */
    @GetMapping("/{slug}")
    public ResponseEntity<ApiResponse<CourseDetailDto>> getCourseBySlug(@PathVariable String slug) {
        CourseDetailDto course = courseService.getCourseBySlug(slug);
        return ResponseEntity.ok(ApiResponse.success("Course details retrieved successfully", course));
    }
}
