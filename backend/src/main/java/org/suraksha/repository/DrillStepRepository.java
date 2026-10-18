package org.suraksha.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.suraksha.entity.DrillStep;
import java.util.Optional;

import java.util.List;

@Repository
public interface DrillStepRepository extends JpaRepository<DrillStep, Long> {
    Optional<DrillStep> findByScenarioIdAndStepKey(Long scenarioId, String stepKey);
    List<DrillStep> findByScenarioId(Long scenarioId);
}
