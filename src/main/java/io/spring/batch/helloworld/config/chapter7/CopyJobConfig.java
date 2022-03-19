package io.spring.batch.helloworld.config.chapter7;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.ArgumentPreparedStatementSetter;

import javax.sql.DataSource;

@RequiredArgsConstructor
@Configuration
public class CopyJobConfig {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public JdbcCursorItemReader<Customer> customerItemReader(DataSource dataSource) {
        return new JdbcCursorItemReaderBuilder<Customer>()
                .name("customerItemReader")
                .dataSource(dataSource)
                .sql("select * from customer where city = ?")
                .rowMapper(new CustomerRowMapper())
                .preparedStatementSetter(citySetter(null))
                .build();
    }

    @Bean
    @StepScope
    public ArgumentPreparedStatementSetter citySetter(
            @Value("#{jobParameters['city']}") String city) {
        return new ArgumentPreparedStatementSetter(new Object[]{city});
    }

    @Bean
    public ItemWriter<Customer> itemWriter() {
        return customers -> customers.forEach(customer -> System.out.println(customer));
    }

    @Bean
    public Step copyFileStep() {
        return stepBuilderFactory.get("copyFileStep")
                .<Customer, Customer>chunk(10)
                .reader(customerItemReader(null))
                .writer(itemWriter())
                .build();
    }

    @Bean
    public Job copyFileJob() {
        return jobBuilderFactory.get("copyFileJob")
                .start(copyFileStep())
                .incrementer(new RunIdIncrementer())
                .build();
    }
}

