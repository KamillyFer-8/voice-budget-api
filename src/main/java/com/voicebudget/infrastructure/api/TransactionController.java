
package com.voicebudget.infrastructure.api;

import com.voicebudget.application.usecase.ListTransactionsByCategoryUseCase;
import com.voicebudget.application.usecase.PersistTransactionUseCase;
import com.voicebudget.domain.Category;
import com.voicebudget.domain.Transaction;
import com.voicebudget.infrastructure.api.dto.CreateTransactionRequest;
import com.voicebudget.infrastructure.api.dto.TransactionResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private final PersistTransactionUseCase persistTransactionUseCase;
    private final ListTransactionsByCategoryUseCase listTransactionsByCategoryUseCase;

    public TransactionController(
            PersistTransactionUseCase persistTransactionUseCase,
            ListTransactionsByCategoryUseCase listTransactionsByCategoryUseCase
    ) {
        this.persistTransactionUseCase = persistTransactionUseCase;
        this.listTransactionsByCategoryUseCase = listTransactionsByCategoryUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse create(
            @Valid @RequestBody CreateTransactionRequest request
    ) {
        long amountInCents = request.amount()
                .movePointRight(2)
                .longValueExact();

        Transaction transaction = persistTransactionUseCase.execute(
                request.description(),
                amountInCents,
                request.category()
        );

        return TransactionResponse.from(transaction);
    }

    @GetMapping("/{category}")
    public List<TransactionResponse> findByCategory(
            @PathVariable Category category
    ) {
        return listTransactionsByCategoryUseCase.execute(category)
                .stream()
                .map(TransactionResponse::from)
                .toList();
    }
}
