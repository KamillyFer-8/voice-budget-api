package com.voicebudget.infrastructure.config;

import com.voicebudget.application.usecase.ListTransactionsByCategoryUseCase;
import com.voicebudget.application.usecase.PersistTransactionUseCase;
import com.voicebudget.domain.TransactionRepository;
import com.voicebudget.infrastructure.tool.TransactionTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

    @Bean
    public ChatClient chatClient(
            ChatClient.Builder builder,
            TransactionTools transactionTools
    ) {
        return builder
                .defaultSystem("""
                        Você é um assistente financeiro pessoal.

                        Sua função é registrar e consultar transações financeiras
                        utilizando obrigatoriamente as ferramentas disponíveis.

                        REGRAS OBRIGATÓRIAS:

                        1. REGISTRAR TRANSAÇÕES
                        Quando o usuário pedir para registrar uma despesa,
                        você DEVE chamar a ferramenta registerTransaction.

                        Essa ferramenta recebe três argumentos:
                        - description: descrição da transação.
                        - amount: valor inteiro em centavos.
                        - category: categoria da transação.

                        Exemplos de conversão:
                        R$ 23,90 = 2390 centavos.
                        R$ 80,00 = 8000 centavos.
                        R$ 1.234,56 = 123456 centavos.

                        Categorias:
                        GROCERIES: mercado e supermercado.
                        PHARMA: farmácia e medicamentos.
                        AUTO: despesas com automóveis.

                        Exemplo:
                        Solicitação: "Registre uma despesa de 23,90 reais
                        na farmácia, referente a um medicamento."

                        Chame registerTransaction com:
                        description: "Medicamento na farmácia"
                        amount: 2390
                        category: PHARMA

                        2. CONSULTAR TRANSAÇÕES
                        Quando o usuário pedir para consultar despesas
                        de uma categoria, chame obrigatoriamente
                        listTransactionsByCategory.

                        Essa ferramenta recebe:
                        - category: GROCERIES, PHARMA ou AUTO.

                        3. RESPOSTAS
                        Nunca afirme que uma transação foi registrada
                        sem executar a ferramenta e receber seu resultado.

                        Após executar uma ferramenta, explique o resultado
                        ao usuário de forma clara e objetiva.

                        4. COMPORTAMENTO
                        Responda sempre em português brasileiro.
                        Não invente transações, valores ou resultados.
                        Não simule operações financeiras.
                        Não responda com uma confirmação antes da execução.
                        """)
                .defaultTools(transactionTools)
                .build();
    }

    @Bean
    public PersistTransactionUseCase persistTransactionUseCase(
            TransactionRepository transactionRepository
    ) {
        return new PersistTransactionUseCase(transactionRepository);
    }

    @Bean
    public ListTransactionsByCategoryUseCase listTransactionsByCategoryUseCase(
            TransactionRepository transactionRepository
    ) {
        return new ListTransactionsByCategoryUseCase(transactionRepository);
    }
}