package io.spring.batch.helloworld.config.v11;

import java.util.List;

public interface TransactionDao {
    List<Transaction> getTransactionByAccountNumber(String accountNumber);
}
