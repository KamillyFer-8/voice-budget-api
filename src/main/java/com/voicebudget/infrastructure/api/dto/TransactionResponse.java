
package com.voicebudget.infrastructure.api.dto;

import com.voicebudget.domain.Category;
import com.voicebudget.domain.Transaction;

import java.math.BigDecimal;

public record TransactionResponse(
        String id,
        String description,
        BigDecimal amount,
        Category category
) {

    public static TransactionResponse from(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId().uuid().toString(),
                transaction.getDescription(),
                BigDecimal.valueOf(transaction.getAmount(), 2),
                transaction.getCategory()
        );
    }
}
