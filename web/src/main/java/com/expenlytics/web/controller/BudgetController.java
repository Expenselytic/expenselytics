package com.expenlytics.web.controller;

import com.expenlytics.core.usecase.AccountService;
import com.expenlytics.db.entity.BudgetEntity;
import com.expenlytics.db.repository.BudgetRepository;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
@RequestMapping(ApiRootController.ROOT + "/budgets")
public class BudgetController {
    private final BudgetRepository budgets;
    private final AccountService accounts;

    public BudgetController(
        BudgetRepository budgets, AccountService accounts
    ) {
        this.budgets = budgets;
        this.accounts = accounts;
    }

    public record Request(String category, BigDecimal amount) {}

    private void validateMonth(String month) {
        try {
            if (!month.matches("[0-9]{4}-[0-9]{2}")) {
                throw new IllegalArgumentException();
            }
            YearMonth.parse(month);
        } catch (RuntimeException error) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "Choose a valid month"
            );
        }
    }

    @GetMapping("/{month}")
    public Mono<List<BudgetEntity>> list(
        @PathVariable String month,
        @CookieValue("expenselytics_session") String token
    ) {
        return Mono.fromCallable(() -> {
            validateMonth(month);
            return budgets.findByUserIdAndMonth(
                accounts.current(token).userId(), month
            );
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @PutMapping("/{month}")
    public Mono<BudgetEntity> save(
        @PathVariable String month,
        @RequestBody Request request,
        @CookieValue("expenselytics_session") String token
    ) {
        return Mono.fromCallable(() -> {
            validateMonth(month);
            if (request.category() == null ||
                request.category().length() > 255 ||
                request.amount() == null ||
                request.amount().signum() <= 0 ||
                request.amount().scale() > 2 ||
                request.amount().compareTo(
                    new BigDecimal("9999999999.99")
                ) > 0) {
                throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Enter a positive budget with up to two decimals"
                );
            }
            Long userId = accounts.current(token).userId();
            String category = request.category().trim();
            var budget = budgets.findByUserIdAndMonthAndCategory(
                userId, month, category
            ).orElseGet(BudgetEntity::new);
            budget.userId = userId;
            budget.month = month;
            budget.category = category;
            budget.amount = request.amount();
            return budgets.save(budget);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> delete(
        @PathVariable Long id,
        @CookieValue("expenselytics_session") String token
    ) {
        return Mono.fromRunnable(() -> {
            Long userId = accounts.current(token).userId();
            var budget = budgets.findById(id).filter(
                item -> item.userId.equals(userId)
            ).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Budget not found"
            ));
            budgets.delete(budget);
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }
}
