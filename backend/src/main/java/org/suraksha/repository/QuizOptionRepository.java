package org.suraksha.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.suraksha.entity.QuizOption;
import java.util.List;

import java.util.Optional;

@Repository
public interface QuizOptionRepository extends JpaRepository<QuizOption, Long> {
    List<QuizOption> findByQuestionIdOrderBySequenceOrderAsc(Long questionId);
    Optional<QuizOption> findByQuestionIdAndIsCorrectTrue(Long questionId);
}
