package com.expenlytics.web.controller;

import com.expenlytics.core.model.Expense;
import com.expenlytics.core.usecase.AddExpenseImpl;
import com.expenlytics.db.entity.SavingEntity;
import com.expenlytics.db.repository.SavingRepository;
import com.expenlytics.web.model.ExpenseRequest;
import java.net.URI;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
@RequestMapping(ApiRootController.ROOT + "/savings")
@CrossOrigin(origins = {"http://localhost:8080", "http://localhost:4200"})
public class SavingController {
    private final SavingRepository savings;
    private final AddExpenseImpl create;

    public SavingController(SavingRepository savings) {
        this.savings = savings;
        this.create = new AddExpenseImpl(expense -> toDomain(savings.save(new SavingEntity(expense))));
    }

    @PostMapping
    public Mono<ResponseEntity<Expense>> add(@RequestBody ExpenseRequest request) {
        return create.createExpense(request.toDomain()).map(saved ->
            ResponseEntity.created(URI.create(ApiRootController.ROOT + "/savings/" + saved.getId())).body(saved));
    }

    @GetMapping
    public Mono<List<Expense>> list() {
        return Mono.fromCallable(() -> savings.findAll(Sort.by(Sort.Direction.DESC, "date", "id"))
            .stream().map(SavingController::toDomain).toList()).subscribeOn(Schedulers.boundedElastic());
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Expense>> get(@PathVariable Long id) {
        return Mono.fromCallable(() -> savings.findById(id).map(SavingController::toDomain)
            .map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build()))
            .subscribeOn(Schedulers.boundedElastic());
    }

    private static Expense toDomain(SavingEntity saved) {
        return new Expense(saved.getId(), saved.getName(), saved.getCategory(), saved.getAmount(), saved.getDate());
    }
}
