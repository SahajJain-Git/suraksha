package org.suraksha.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.suraksha.entity.DrillAttempt;
import java.util.List;

@Repository
public interface DrillAttemptRepository extends JpaRepository<DrillAttempt, Long> {
    List<DrillAttempt> findByUserIdOrderByCompletedAtDesc(Long userId);
    long countByUserId(Long userId);
}
