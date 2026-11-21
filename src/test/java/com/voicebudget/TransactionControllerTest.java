package com.voicebudget;

import com.voicebudget.application.usecase.ListTransactionsByCategoryUseCase;
import com.voicebudget.application.usecase.PersistTransactionUseCase;
import com.voicebudget.domain.Category;
import com.voicebudget.domain.Transaction;
import com.voicebudget.infrastructure.api.TransactionController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {

    @Mock
    private PersistTransactionUseCase persistUseCase;

    @Mock
    private ListTransactionsByCategoryUseCase listUseCase;

    private MockMvc mockMvc;
    private LocalValidatorFactoryBean validator;

    @BeforeEach
    void setUp() {
        TransactionController controller = new TransactionController(
                persistUseCase,
                listUseCase
        );

        validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setValidator(validator)
                .build();
    }

    @Test
    void shouldCreateTransactionAndReturn201() throws Exception {
        Transaction transaction = new Transaction(
                "Compra no mercado",
                1250,
                Category.GROCERIES
        );

        when(persistUseCase.execute(
                "Compra no mercado",
                1250,
                Category.GROCERIES
        )).thenReturn(transaction);

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "description": "Compra no mercado",
                                  "amount": 12.50,
                                  "category": "GROCERIES"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.description").value("Compra no mercado"))
                .andExpect(jsonPath("$.amount").value(12.50))
                .andExpect(jsonPath("$.category").value("GROCERIES"));

        verify(persistUseCase).execute(
                "Compra no mercado",
                1250,
                Category.GROCERIES
        );
    }

    @Test
    void shouldListTransactionsByCategory() throws Exception {
        List<Transaction> transactions = List.of(
                new Transaction("Medicamento", 2390, Category.PHARMA)
        );

        when(listUseCase.execute(Category.PHARMA))
                .thenReturn(transactions);

        mockMvc.perform(get("/transactions/PHARMA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].description").value("Medicamento"))
                .andExpect(jsonPath("$[0].amount").value(23.90))
                .andExpect(jsonPath("$[0].category").value("PHARMA"));

        verify(listUseCase).execute(Category.PHARMA);
    }

    @Test
    void shouldRejectInvalidTransactionRequest() throws Exception {
        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "description": "",
                                  "amount": 0,
                                  "category": null
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(persistUseCase);
    }
}