package com.expenlytics.db.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "experiment_saving")
public class ExperimentSavingEntity {

    @Id
    public Long savingId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "experiment_id", nullable = false)
    public SpendingExperimentEntity experiment;

    @OneToOne(optional = false)
    @MapsId
    @JoinColumn(name = "saving_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    public SavingEntity saving;
}
