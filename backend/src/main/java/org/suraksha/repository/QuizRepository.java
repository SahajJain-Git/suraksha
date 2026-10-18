package org.suraksha.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.suraksha.entity.Quiz;
import java.util.Optional;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {
    Optional<Quiz> findBySlug(String slug);
    Optional<Quiz> findByCourseId(Long courseId);
}
