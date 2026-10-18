package org.suraksha.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.suraksha.entity.QuizAttempt;
import java.util.List;

@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {
    List<QuizAttempt> findByUserIdOrderByAttemptedAtDesc(Long userId);
    List<QuizAttempt> findByUserIdAndQuizIdOrderByAttemptedAtDesc(Long userId, Long quizId);
    long countByUserId(Long userId);
    long countByUserIdAndPassedTrue(Long userId);
}
