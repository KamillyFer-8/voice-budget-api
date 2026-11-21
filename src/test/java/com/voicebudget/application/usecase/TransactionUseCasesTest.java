package com.voicebudget.application.usecase;

import com.voicebudget.domain.Category;
import com.voicebudget.domain.Transaction;
import com.voicebudget.domain.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionUseCasesTest {

    @Mock
    private TransactionRepository transactionRepository;

    private PersistTransactionUseCase persistUseCase;
    private ListTransactionsByCategoryUseCase listUseCase;

    @BeforeEach
    void setUp() {
        persistUseCase = new PersistTransactionUseCase(transactionRepository);
        listUseCase = new ListTransactionsByCategoryUseCase(transactionRepository);
    }

    @Test
    void shouldPersistTransaction() {
        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Transaction result = persistUseCase.execute(
                "Compra no mercado",
                8000,
                Category.GROCERIES
        );

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("Compra no mercado", result.getDescription());
        assertEquals(8000, result.getAmount());
        assertEquals(Category.GROCERIES, result.getCategory());

        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void shouldRejectBlankDescription() {
        assertThrows(
                IllegalArgumentException.class,
                () -> persistUseCase.execute("", 8000, Category.GROCERIES)
        );

        verifyNoInteractions(transactionRepository);
    }

    @Test
    void shouldRejectZeroAmount() {
        assertThrows(
                IllegalArgumentException.class,
                () -> persistUseCase.execute("Compra", 0, Category.GROCERIES)
        );

        verifyNoInteractions(transactionRepository);
    }

    @Test
    void shouldRejectNullCategoryWhenPersisting() {
        assertThrows(
                NullPointerException.class,
                () -> persistUseCase.execute("Compra", 8000, null)
        );

        verifyNoInteractions(transactionRepository);
    }

    @Test
    void shouldListTransactionsByCategory() {
        List<Transaction> expected = List.of(
                new Transaction("Medicamento", 2390, Category.PHARMA),
                new Transaction("Vitamina", 4500, Category.PHARMA)
        );

        when(transactionRepository.findAllByCategory(Category.PHARMA))
                .thenReturn(expected);

        List<Transaction> result = listUseCase.execute(Category.PHARMA);

        assertEquals(2, result.size());
        assertSame(expected, result);

        verify(transactionRepository).findAllByCategory(Category.PHARMA);
    }

    @Test
    void shouldRejectNullCategoryWhenListing() {
        assertThrows(
                NullPointerException.class,
                () -> listUseCase.execute(null)
        );

        verifyNoInteractions(transactionRepository);
    }
}