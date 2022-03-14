package io.spring.batch.helloworld.config.v2;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@RequiredArgsConstructor
@Configuration
public class JobConfigV2 {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job jobV2() {
        return this.jobBuilderFactory.get("basicJobV2")
                .start(stepV2())
                .build();
    }

    @Bean
    public Step stepV2() {
        return this.stepBuilderFactory.get("step1")
                .tasklet(taskletV2(null)).build();
    }

    @StepScope
    @Bean
    public Tasklet taskletV2(@Value("#{jobParameters['name']}") String name) {
        return (stepContribution, chunkContext) -> {
            System.out.println("Hello world! v2 " + name);
            return RepeatStatus.FINISHED;
        };
    }
}
