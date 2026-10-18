package org.suraksha.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.suraksha.entity.UserLessonProgress;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserLessonProgressRepository extends JpaRepository<UserLessonProgress, Long> {
    Optional<UserLessonProgress> findByUserIdAndLessonId(Long userId, Long lessonId);
    List<UserLessonProgress> findByUserId(Long userId);
    List<UserLessonProgress> findByUserIdAndCompletedTrue(Long userId);
    long countByUserIdAndCompletedTrue(Long userId);
}
