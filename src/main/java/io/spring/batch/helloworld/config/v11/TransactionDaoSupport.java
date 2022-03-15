package io.spring.batch.helloworld.config.v11;

import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.List;

public class TransactionDaoSupport extends JdbcTemplate implements TransactionDao {

    public TransactionDaoSupport(DataSource dataSource) {
        super(dataSource);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Transaction> getTransactionByAccountNumber(String accountNumber) {
        return query("select t.id, t.timestamp, t.amount " +
                "from transaction t inner join account_summary a on " +
                "a.id = t.account_summary_id " +
                "where a.account_number = ?",
                (rs, rowNum) -> {
                    Transaction transaction = new Transaction();
                    transaction.setAmount(rs.getDouble("amount"));
                    transaction.setTimestamp(rs.getDate("timestamp"));
                    return transaction;
                },
                accountNumber);
    }
}
