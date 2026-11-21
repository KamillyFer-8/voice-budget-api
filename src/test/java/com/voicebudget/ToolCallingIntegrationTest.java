
package com.voicebudget;

import com.voicebudget.domain.Category;
import com.voicebudget.domain.Transaction;
import com.voicebudget.domain.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ToolCallingIntegrationTest {

    private final ChatClient chatClient;
    private final TransactionRepository transactionRepository;

    @Autowired
    ToolCallingIntegrationTest(
            ChatClient chatClient,
            TransactionRepository transactionRepository
    ) {
        this.chatClient = chatClient;
        this.transactionRepository = transactionRepository;
    }

    @Test
    void shouldRegisterTransactionUsingToolCalling() {

        String response = chatClient.prompt()
                .user("Registre uma despesa de 80 reais no mercado.")
                .call()
                .content();

        List<Transaction> transactions =
                transactionRepository.findAllByCategory(Category.GROCERIES);

        assertNotNull(response);
        assertFalse(response.isBlank());

        assertTrue(
                transactions.stream().anyMatch(transaction ->
                        transaction.getDescription().toLowerCase().contains("mercado")
                                && transaction.getAmount() == 8000
                ),
                "A IA deveria registrar uma despesa de 80 reais no mercado."
        );

        System.out.println("\nResposta da IA:");
        System.out.println(response);

        System.out.println("\nTransações encontradas:");
        transactions.forEach(transaction ->
                System.out.println(
                        transaction.getDescription()
                                + " | R$ "
                                + transaction.getAmount() / 100.0
                                + " | "
                                + transaction.getCategory()
                )
        );
    }

    @Test
    void shouldListTransactionsByCategoryUsingToolCalling() {

        Transaction transaction = new Transaction(
                "Compra na farmacia",
                5000,
                Category.PHARMA
        );

        transactionRepository.save(transaction);

        String response = chatClient.prompt()
                .user("Liste meus gastos com farmácia.")
                .call()
                .content();

        assertNotNull(response);
        assertFalse(response.isBlank());

        assertTrue(
                response.toLowerCase().contains("farmácia")
                        || response.toLowerCase().contains("farmacia")
                        || response.contains("50"),
                "A IA deveria apresentar a transação da categoria PHARMA."
        );

        System.out.println("\nResposta da IA ao consultar farmácia:");
        System.out.println(response);
    }
}
