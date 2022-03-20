package io.spring.batch.helloworld.config.chapter7.service;

import io.spring.batch.helloworld.config.chapter7.cursor.Customer;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.adapter.ItemReaderAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@RequiredArgsConstructor
@Configuration
public class CopyJobConfig {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public ItemReaderAdapter<Customer> customerItemReader(CustomerService customerService) {
        ItemReaderAdapter<Customer> adapter = new ItemReaderAdapter<>();

        adapter.setTargetObject(customerService);
        adapter.setTargetMethod("getCustomer");

        return adapter;
    }

    @Bean
    public ItemWriter<Customer> itemWriter() {
        return customers -> customers.forEach(System.out::println);
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

