package com.voicebudget;

import com.voicebudget.domain.Category;
import com.voicebudget.domain.Transaction;
import com.voicebudget.domain.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class TransactionRepositoryAdapterIntegrationTest {

    private final TransactionRepository transactionRepository;

    @Autowired
    TransactionRepositoryAdapterIntegrationTest(
            TransactionRepository transactionRepository
    ) {
        this.transactionRepository = transactionRepository;
    }

    @Test
    void shouldSaveAndFindTransactionByCategory() {
        // Arrange: cria uma transação de teste
        Transaction transaction = new Transaction(
                "Compra no mercado",
                8000,
                Category.GROCERIES
        );

        // Act: salva no PostgreSQL
        Transaction saved = transactionRepository.save(transaction);

        // Assert: verifica se foi salva
        assertNotNull(saved.getId());
        assertEquals("Compra no mercado", saved.getDescription());
        assertEquals(8000, saved.getAmount());
        assertEquals(Category.GROCERIES, saved.getCategory());

        // Act: busca as transações pela categoria
        List<Transaction> results =
                transactionRepository.findAllByCategory(Category.GROCERIES);

        // Assert: verifica se a transação salva foi encontrada
        assertTrue(
                results.stream().anyMatch(
                        result -> result.getId().equals(saved.getId())
                )
        );
    }
}