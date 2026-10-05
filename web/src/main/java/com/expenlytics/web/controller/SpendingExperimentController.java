package com.expenlytics.web.controller;

import com.expenlytics.db.impl.SpendingExperimentService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
@RequestMapping(ApiRootController.ROOT + "/experiments")
public class SpendingExperimentController {
    private final SpendingExperimentService service;
    public SpendingExperimentController(SpendingExperimentService service) { this.service = service; }
    @GetMapping
    public Mono<List<SpendingExperimentService.View>> list() {
        return Mono.fromCallable(service::list).subscribeOn(Schedulers.boundedElastic());
    }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public Mono<SpendingExperimentService.View> create(@RequestBody SpendingExperimentService.Request request) {
        return Mono.fromCallable(() -> service.create(request)).subscribeOn(Schedulers.boundedElastic());
    }
    @PutMapping("/{id}/savings/{savingId}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> link(@PathVariable Long id, @PathVariable Long savingId) {
        return Mono.fromRunnable(() -> service.link(id, savingId)).subscribeOn(Schedulers.boundedElastic()).then();
    }
    @DeleteMapping("/{id}/savings/{savingId}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> unlink(@PathVariable Long id, @PathVariable Long savingId) {
        return Mono.fromRunnable(() -> service.unlink(id, savingId)).subscribeOn(Schedulers.boundedElastic()).then();
    }
}
