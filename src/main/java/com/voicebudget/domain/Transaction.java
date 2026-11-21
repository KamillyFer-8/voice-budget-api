
package com.voicebudget.domain;

import java.util.Objects;

public class Transaction {

    private final TransactionId id;
    private final String description;
    private final long amount;
    private final Category category;

    public Transaction(
            TransactionId id,
            String description,
            long amount,
            Category category) {

        this.id = Objects.requireNonNull(
                id, "O identificador é obrigatório");

        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException(
                    "A descrição é obrigatória");
        }

        if (amount <= 0) {
            throw new IllegalArgumentException(
                    "O valor deve ser maior que zero");
        }

        this.description = description;
        this.amount = amount;
        this.category = Objects.requireNonNull(
                category, "A categoria é obrigatória");
    }

    public Transaction(
            String description,
            long amount,
            Category category) {

        this(new TransactionId(), description, amount, category);
    }

    public TransactionId getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public long getAmount() {
        return amount;
    }

    public Category getCategory() {
        return category;
    }
}
