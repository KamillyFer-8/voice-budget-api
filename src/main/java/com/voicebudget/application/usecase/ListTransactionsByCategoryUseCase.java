
package com.voicebudget.application.usecase;

import com.voicebudget.domain.Category;
import com.voicebudget.domain.Transaction;
import com.voicebudget.domain.TransactionRepository;

import java.util.List;
import java.util.Objects;

public class ListTransactionsByCategoryUseCase {

    private final TransactionRepository transactionRepository;

    public ListTransactionsByCategoryUseCase(
            TransactionRepository transactionRepository) {

        this.transactionRepository = Objects.requireNonNull(
                transactionRepository,
                "O repositório de transações é obrigatório"
        );
    }

    public List<Transaction> execute(Category category) {

        Objects.requireNonNull(
                category,
                "A categoria é obrigatória"
        );

        return transactionRepository.findAllByCategory(category);
    }
}
