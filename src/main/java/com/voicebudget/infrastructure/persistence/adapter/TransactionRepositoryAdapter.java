
package com.voicebudget.infrastructure.persistence.adapter;

import com.voicebudget.domain.Category;
import com.voicebudget.domain.Transaction;
import com.voicebudget.domain.TransactionId;
import com.voicebudget.domain.TransactionRepository;
import com.voicebudget.infrastructure.persistence.entity.TransactionEntity;
import com.voicebudget.infrastructure.persistence.repository.SpringDataTransactionRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class TransactionRepositoryAdapter implements TransactionRepository {

    private final SpringDataTransactionRepository repository;

    public TransactionRepositoryAdapter(
            SpringDataTransactionRepository repository
    ) {
        this.repository = repository;
    }

    @Override
    public Transaction save(Transaction transaction) {
        TransactionEntity entity = toEntity(transaction);

        TransactionEntity savedEntity = repository.save(entity);

        return toDomain(savedEntity);
    }

    @Override
    public List<Transaction> findAllByCategory(Category category) {
        return repository.findAllByCategory(category)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private TransactionEntity toEntity(Transaction transaction) {
        return new TransactionEntity(
                transaction.getId().uuid(),
                transaction.getDescription(),
                transaction.getAmount(),
                transaction.getCategory()
        );
    }

    private Transaction toDomain(TransactionEntity entity) {
        return new Transaction(
                new TransactionId(entity.getId()),
                entity.getDescription(),
                entity.getAmount(),
                entity.getCategory()
        );
    }
}
