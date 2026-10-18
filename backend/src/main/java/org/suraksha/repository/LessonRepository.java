package org.suraksha.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.suraksha.entity.Lesson;
import java.util.List;
import java.util.Optional;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {
    List<Lesson> findByCourseIdOrderBySequenceOrderAsc(Long courseId);
    Optional<Lesson> findByCourseIdAndSlug(Long courseId, String slug);
    long countByCourseId(Long courseId);
}
