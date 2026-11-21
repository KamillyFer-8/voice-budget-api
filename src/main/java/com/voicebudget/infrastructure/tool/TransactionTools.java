
package com.voicebudget.infrastructure.tool;

import com.voicebudget.application.usecase.ListTransactionsByCategoryUseCase;
import com.voicebudget.application.usecase.PersistTransactionUseCase;
import com.voicebudget.domain.Category;
import com.voicebudget.domain.Transaction;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TransactionTools {

    private final PersistTransactionUseCase persistTransactionUseCase;
    private final ListTransactionsByCategoryUseCase listTransactionsByCategoryUseCase;

    public TransactionTools(
            PersistTransactionUseCase persistTransactionUseCase,
            ListTransactionsByCategoryUseCase listTransactionsByCategoryUseCase
    ) {
        this.persistTransactionUseCase = persistTransactionUseCase;
        this.listTransactionsByCategoryUseCase = listTransactionsByCategoryUseCase;
    }

    @Tool(description = "Registra uma nova transação financeira do usuário.")
    public Transaction registerTransaction(
            @ToolParam(description = "Descrição da transação, por exemplo: compra no mercado")
            String description,

            @ToolParam(description = "Valor da transação em centavos. Exemplo: 80 reais = 8000")
            long amount,

            @ToolParam(description = "Categoria da transação: GROCERIES, PHARMA ou AUTO")
            Category category
    ) {
        return persistTransactionUseCase.execute(description, amount, category);
    }

    @Tool(description = "Lista as transações financeiras de uma categoria.")
    public List<Transaction> listTransactionsByCategory(
            @ToolParam(description = "Categoria: GROCERIES, PHARMA ou AUTO")
            Category category
    ) {
        return listTransactionsByCategoryUseCase.execute(category);
    }
}
