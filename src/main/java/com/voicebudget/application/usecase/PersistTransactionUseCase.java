
package com.voicebudget.application.usecase;

import com.voicebudget.domain.Category;
import com.voicebudget.domain.Transaction;
import com.voicebudget.domain.TransactionRepository;

import java.util.Objects;

public class PersistTransactionUseCase {

    private final TransactionRepository transactionRepository;

    public PersistTransactionUseCase(
            TransactionRepository transactionRepository) {

        this.transactionRepository = Objects.requireNonNull(
                transactionRepository,
                "O repositório de transações é obrigatório"
        );
    }

    public Transaction execute(
            String description,
            long amount,
            Category category) {

        Transaction transaction = new Transaction(
                description,
                amount,
                category
        );

        return transactionRepository.save(transaction);
    }
}
