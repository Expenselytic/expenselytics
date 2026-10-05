package com.expenlytics.db.impl;

import com.expenlytics.db.entity.*;
import com.expenlytics.db.repository.*;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SpendingExperimentService {
    private final SpendingExperimentRepository experiments;
    private final ExperimentSavingRepository links;
    private final jakarta.persistence.EntityManager em;
    public SpendingExperimentService(SpendingExperimentRepository experiments, ExperimentSavingRepository links,
            jakarta.persistence.EntityManager em) {
        this.experiments = experiments; this.links = links; this.em = em;
    }
    public record Request(String name, String category, LocalDate startDate, Integer durationDays) {}
    public record View(Long id, String name, String category, LocalDate startDate, int durationDays, List<Long> savingIds) {}

    @Transactional(readOnly = true)
    public List<View> list() {
        var allLinks = links.findAll();
        return experiments.findAll(Sort.by(Sort.Direction.DESC, "id")).stream().map(e ->
            new View(e.id, e.name, e.category, e.startDate, e.durationDays,
                allLinks.stream().filter(l -> l.experiment.id.equals(e.id)).map(l -> l.savingId).toList())).toList();
    }
    @Transactional
    public View create(Request request) {
        if (request == null || request.name() == null || request.name().isBlank() || request.name().trim().length() > 100
            || request.category() == null || request.category().isBlank() || request.category().trim().length() > 255
            || request.startDate() == null || request.durationDays() == null || request.durationDays() < 1 || request.durationDays() > 90) {
            throw new IllegalArgumentException("Provide a name, category, start date, and duration of 1–90 days.");
        }
        var entity = new SpendingExperimentEntity();
        entity.name = request.name().trim(); entity.category = request.category().trim();
        entity.startDate = request.startDate(); entity.durationDays = request.durationDays();
        experiments.save(entity);
        return new View(entity.id, entity.name, entity.category, entity.startDate, entity.durationDays, List.of());
    }
    @Transactional
    public void link(Long experimentId, Long savingId) {
        var experiment = experiments.findById(experimentId).orElseThrow(() -> new IllegalArgumentException("Experiment not found."));
        // Serialize changes to one deposit; it can count toward only one experiment.
        var saving = em.find(SavingEntity.class, savingId, jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
        if (saving == null) throw new IllegalArgumentException("Saving not found.");
        var existing = links.findById(savingId);
        if (existing.isPresent()) {
            if (!existing.get().experiment.id.equals(experimentId)) throw new IllegalArgumentException("This deposit is already linked to another experiment.");
            return;
        }
        if (saving.getDate().toLocalDate().isBefore(experiment.startDate))
            throw new IllegalArgumentException("Choose a deposit dated on or after the experiment start.");
        var link = new ExperimentSavingEntity(); link.experiment = experiment; link.saving = saving;
        em.persist(link);
    }
    @Transactional
    public void unlink(Long experimentId, Long savingId) {
        var saving = em.find(SavingEntity.class, savingId, jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
        if (saving == null) return;
        links.findById(savingId).filter(l -> l.experiment.id.equals(experimentId)).ifPresent(links::delete);
    }
}
