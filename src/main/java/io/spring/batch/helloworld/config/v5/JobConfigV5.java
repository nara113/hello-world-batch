package io.spring.batch.helloworld.config.v5;

import io.spring.batch.helloworld.validator.MyParameterValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.*;
import org.springframework.batch.core.annotation.AfterJob;
import org.springframework.batch.core.annotation.BeforeJob;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.CompositeJobParametersValidator;
import org.springframework.batch.core.job.DefaultJobParametersValidator;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.listener.JobListenerFactoryBean;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.Date;

@RequiredArgsConstructor
@Configuration
public class JobConfigV5 {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    static class JobLoggerListener implements JobExecutionListener{
        private static String START_MESSAGE = "%s is beginning execution";
        private static String END_MESSAGE = "%s has completed with the status %s";

        @Override
        public void beforeJob(JobExecution jobExecution) {
            System.out.println(String.format(START_MESSAGE, jobExecution.getJobInstance().getJobName()));
        }

        @Override
        public void afterJob(JobExecution jobExecution) {
            System.out.println(String.format(END_MESSAGE, jobExecution.getJobInstance().getJobName(), jobExecution.getStatus()));
        }
    }

    static class JobLoggerListenerV2 {
        private static String START_MESSAGE = "%s is beginning execution";
        private static String END_MESSAGE = "%s has completed with the status %s";

        @BeforeJob
        public void beforeJob(JobExecution jobExecution) {
            System.out.println(String.format(START_MESSAGE, jobExecution.getJobInstance().getJobName()));
        }

        @AfterJob
        public void afterJob(JobExecution jobExecution) {
            System.out.println(String.format(END_MESSAGE, jobExecution.getJobInstance().getJobName(), jobExecution.getStatus()));
        }
    }

    @Bean
    public Job jobV5() {
        return this.jobBuilderFactory.get("basicJobV5")
                .start(stepV5())
                .validator(new MyParameterValidator())
                .incrementer(new RunIdIncrementer())
                // 이렇게 등록해도 동작함
//                .listener(new JobLoggerListenerV2())
                .listener(JobListenerFactoryBean.getListener(new JobLoggerListenerV2()))
                .build();
    }

    @Bean
    public Step stepV5() {
        return this.stepBuilderFactory.get("step")
                .tasklet(taskletV5(null, null)).build();
    }

    @StepScope
    @Bean
    public Tasklet taskletV5(@Value("#{jobParameters['fileName']}") String fileName,
                             @Value("#{jobParameters['name']}") String name) {
        return (stepContribution, chunkContext) -> {
            System.out.println("fileName " + fileName);
            System.out.println("name " + name);
            return RepeatStatus.FINISHED;
        };
    }
}
