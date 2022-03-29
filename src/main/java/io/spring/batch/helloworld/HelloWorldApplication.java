package io.spring.batch.helloworld;

import io.spring.batch.helloworld.config.chapter8.ValidationJob;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

@RequiredArgsConstructor
@EnableBatchProcessing
@SpringBootApplication
public class HelloWorldApplication {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final DataSource dataSource;

//    @Bean
//    public Job job() {
//        return this.jobBuilderFactory.get("basicJob")
//                .start(step1())
//                .build();
//    }
//
//    @Bean
//    public Step step1() {
//        return this.stepBuilderFactory.get("step1")
//                .tasklet((stepContribution, chunkContext) -> {
//                    System.out.println("Hello world!");
//                    return RepeatStatus.FINISHED;
//                }).build();
//    }


    public static void main(String[] args) {
//        SpringApplication.run(HelloWorldApplication.class, args);
        SpringApplication.run(HelloWorldApplication.class, "customerFile=classpath:customer.csv");
    }

    @Bean
    ApplicationRunner applicationRunner() {
        return args -> {
            try(Connection connection = dataSource.getConnection()) {
//                System.out.println(dataSource.getClass());
//                System.out.println(connection.getClass());
//                System.out.println(connection.getMetaData().getDriverName());
//                System.out.println(connection.getMetaData().getURL());
//                System.out.println(connection.getMetaData().getUserName());

//                final Statement statement = connection.createStatement();
//                final String SQL = "CREATE TABLE USERR(idd INTEGER NOT NULL, name VARCHAR(255), PRIMARY KEY (idd))";
//                statement.executeUpdate(SQL);
            }
        };
    }
}
