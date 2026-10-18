package org.suraksha.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.suraksha.entity.DrillChoice;
import java.util.List;

@Repository
public interface DrillChoiceRepository extends JpaRepository<DrillChoice, Long> {
    List<DrillChoice> findByStepIdOrderBySequenceOrderAsc(Long stepId);
}
