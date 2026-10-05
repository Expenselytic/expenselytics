package com.expenlytics.db.repository;

import com.expenlytics.db.entity.ExperimentSavingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExperimentSavingRepository
    extends JpaRepository<ExperimentSavingEntity, Long>
{
    @org.springframework.data.jpa.repository.Modifying(
        flushAutomatically = true
    )
    @org.springframework.data.jpa.repository.Query(
        "delete from ExperimentSavingEntity l where l.experiment.id = :id"
    )
    void deleteForExperiment(
        @org.springframework.data.repository.query.Param("id") Long id
    );
}
