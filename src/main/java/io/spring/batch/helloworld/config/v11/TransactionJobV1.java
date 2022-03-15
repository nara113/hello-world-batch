package io.spring.batch.helloworld.config.v11;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.mapping.PassThroughFieldSetMapper;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import javax.sql.DataSource;

@RequiredArgsConstructor
@Configuration
public class TransactionJobV1 {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final ResourceLoader resourceLoader;

    @Bean
    @StepScope
    public TransactionReader transactionReader(@Value("#{jobParameters['transactionFile']}") Resource inputFile) {
        Resource tempFile = resourceLoader.getResource("classpath:input.txt");

        final FlatFileItemReader<FieldSet> fileItemReader = new FlatFileItemReaderBuilder<FieldSet>()
                .name("fileItemReader")
                .resource(tempFile)
                .lineTokenizer(new DelimitedLineTokenizer())
                .fieldSetMapper(new PassThroughFieldSetMapper())
                .build();
        return new TransactionReader(fileItemReader);
    }

    @Bean
    public JdbcBatchItemWriter<Transaction> transactionWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<Transaction>()
                .itemSqlParameterSourceProvider(
                        new BeanPropertyItemSqlParameterSourceProvider<>())
                .sql("insert into transaction " +
                        "(account_number, timestamp, amount) " +
                        "values ((select id from account_summary " +
                        "where account_number = :accountNumber), " +
                        ":timestamp, :amount)")
                .dataSource(dataSource)
                .build();
    }

    @Bean
    public Step importTransactionFileStep() {
        return stepBuilderFactory.get("importTransactionFileStep")
                .<Transaction, Transaction>chunk(100)
                .reader(transactionReader(null))
                .writer(transactionWriter(null))
                .allowStartIfComplete(true)
                .listener(transactionReader(null))
                .build();
    }

    @Bean
    @StepScope
    public JdbcCursorItemReader<AccountSummary> accountSummaryReader(DataSource dataSource) {
        return new JdbcCursorItemReaderBuilder<AccountSummary>()
                .name("accountSummaryReader")
                .dataSource(dataSource)
                .sql("select account_number, current_balance " +
                        "from account_summary a " +
                        "where a.id in ( " +
                        "      select distinct t.account_number " +
                        "      from transaction t) " +
                        "order by a.account_number")
                .rowMapper(((rs, rowNum) -> {
                    AccountSummary summary = new AccountSummary();

                    summary.setAccountNumber(rs.getString("account_number"));
                    summary.setCurrentBalance(rs.getDouble("current_balance"));

                    return summary;
                }))
                .build();
    }

    @Bean
    public TransactionDao transactionDao(DataSource dataSource) {
        return new TransactionDaoSupport(dataSource);
    }

    @Bean
    public TransactionApplierProcessor transactionApplierProcessor() {
        return new TransactionApplierProcessor(transactionDao(null));
    }

    @Bean
    public JdbcBatchItemWriter<AccountSummary> accountSummaryWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<AccountSummary>()
                .dataSource(dataSource)
                .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                .sql("update account_summary " +
                        "set current_balance = :currentBalance " +
                        "where account_number = :accountNumber")
                .build();
    }

    @Bean
    public Step applyTransactionsStep() {
        return stepBuilderFactory.get("applyTransactionsStep")
                .<AccountSummary, AccountSummary>chunk(100)
                .reader(accountSummaryReader(null))
                .processor(transactionApplierProcessor())
                .writer(accountSummaryWriter(null))
                .build();
    }

    @Bean
    @StepScope
    public FlatFileItemWriter<AccountSummary> accountSummaryFileWriter(
            @Value("#(jobParameters['summaryFile'])") Resource summaryFile) {
        Resource tempFile = resourceLoader.getResource("classpath:summary.txt");

        BeanWrapperFieldExtractor<AccountSummary> fieldExtractor =
                new BeanWrapperFieldExtractor<>();
        fieldExtractor.setNames(new String[]{"accountNumber", "currentBalance"});
        fieldExtractor.afterPropertiesSet();

        DelimitedLineAggregator<AccountSummary> aggregator =
                new DelimitedLineAggregator<>();
        aggregator.setFieldExtractor(fieldExtractor);

        return new FlatFileItemWriterBuilder<AccountSummary>()
                .name("accountSummaryFileWriter")
                .resource(tempFile)
                .lineAggregator(aggregator)
                .build();
    }

    @Bean
    public Step generateAccountSummaryStep() {
        return stepBuilderFactory.get("generateAccountSummaryStep")
                .<AccountSummary, AccountSummary>chunk(100)
                .reader(accountSummaryReader(null))
                .writer(accountSummaryFileWriter(null))
                .build();
    }

    @Bean
    public Job transactionJob() {
        return jobBuilderFactory.get("transactionJob")
                .start(importTransactionFileStep())
                .on("STOPPED").stopAndRestart(importTransactionFileStep())
                .from(importTransactionFileStep()).on("*").to(applyTransactionsStep())
                .from(applyTransactionsStep()).next(generateAccountSummaryStep())
                .end()
                .incrementer(new RunIdIncrementer())
                .build();
    }
}
