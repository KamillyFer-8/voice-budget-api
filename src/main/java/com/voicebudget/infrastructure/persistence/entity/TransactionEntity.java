
package com.voicebudget.infrastructure.persistence.entity;

import com.voicebudget.domain.Category;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "transactions")
public class TransactionEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false, length = 150)
    private String description;

    @Column(nullable = false)
    private long amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Category category;

    protected TransactionEntity() {
    }

    public TransactionEntity(
            UUID id,
            String description,
            long amount,
            Category category
    ) {
        this.id = id;
        this.description = description;
        this.amount = amount;
        this.category = category;
    }

    public UUID getId() {
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
