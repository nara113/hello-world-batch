package io.spring.batch.helloworld.config.chapter7.paging;

import io.spring.batch.helloworld.config.chapter7.cursor.Customer;
import io.spring.batch.helloworld.config.chapter7.cursor.CustomerRowMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.ArgumentPreparedStatementSetter;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Configuration
public class CopyJobConfig {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @StepScope
    @Bean
    public JdbcPagingItemReader<Customer> customerItemReader(DataSource dataSource,
                                                             PagingQueryProvider queryProvider,
                                                             @Value("#{jobParameters['city']}") String city) {
        Map<String, Object> parameterValues = new HashMap<>(1);
        parameterValues.put("city", city);

        return new JdbcPagingItemReaderBuilder<Customer>()
                .name("customerItemReader")
                .dataSource(dataSource)
                .queryProvider(queryProvider)
                .parameterValues(parameterValues)
                .pageSize(10)
                .rowMapper(new CustomerRowMapper())
                .build();
    }

    @Bean
    public SqlPagingQueryProviderFactoryBean pagingQueryProvider(DataSource dataSource) {
        SqlPagingQueryProviderFactoryBean factoryBean =
                new SqlPagingQueryProviderFactoryBean();

        factoryBean.setSelectClause("select *");
        factoryBean.setFromClause("from customer");
        factoryBean.setWhereClause("where city = :city");
        factoryBean.setSortKey("lastName");
        factoryBean.setDataSource(dataSource);  // dataSource를 사용해 데이터베이스 타입을 결정한다.

        return factoryBean;
    }

    @Bean
    public ItemWriter<Customer> itemWriter() {
        return customers -> customers.forEach(System.out::println);
    }

    @Bean
    public Step copyFileStep() {
        return stepBuilderFactory.get("copyFileStep")
                .<Customer, Customer>chunk(10)
                .reader(customerItemReader(null, null, null))
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

