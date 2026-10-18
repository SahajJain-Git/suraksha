package org.suraksha.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.suraksha.entity.Course;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    Optional<Course> findBySlug(String slug);
    List<Course> findByCategory(String category);

    @Query("SELECT c FROM Course c WHERE " +
           "(:category IS NULL OR LOWER(c.category) = LOWER(:category)) AND " +
           "(:search IS NULL OR LOWER(c.title) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(c.summary) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY c.id ASC")
    List<Course> searchCourses(@Param("category") String category, @Param("search") String search);
}

