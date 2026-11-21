
package com.voicebudget.infrastructure.persistence.repository;

import com.voicebudget.domain.Category;
import com.voicebudget.infrastructure.persistence.entity.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataTransactionRepository
        extends JpaRepository<TransactionEntity, UUID> {

    List<TransactionEntity> findAllByCategory(Category category);
}
