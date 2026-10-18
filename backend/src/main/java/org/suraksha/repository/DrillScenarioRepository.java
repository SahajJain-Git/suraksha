package org.suraksha.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.suraksha.entity.DrillScenario;
import java.util.List;
import java.util.Optional;

@Repository
public interface DrillScenarioRepository extends JpaRepository<DrillScenario, Long> {
    Optional<DrillScenario> findBySlug(String slug);
    Optional<DrillScenario> findByCourseId(Long courseId);
    List<DrillScenario> findByHazardCategory(String hazardCategory);
}
