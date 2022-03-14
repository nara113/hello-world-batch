package io.spring.batch.helloworld.config.v9;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@RequiredArgsConstructor
@Configuration
public class ConditionalJob {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job job() {
        return this.jobBuilderFactory.get("ConditionalJob")
                .start(firstStep())
                .next(decider())
                .from(decider())
                    .on("FAILED").to(failStep())
                .from(decider())
                    .on("*").to(successStep())
                .end()
                .incrementer(new RunIdIncrementer())
                .build();
    }

    @Bean
    public Step firstStep() {
        return stepBuilderFactory.get("firstStep")
                .tasklet(passTasklet())
                .build();
    }

    @Bean
    public Step successStep() {
        return stepBuilderFactory.get("successStep")
                .tasklet(successTasklet())
                .build();
    }

    @Bean
    public Step failStep() {
        return stepBuilderFactory.get("failStep")
                .tasklet(failTasklet())
                .build();
    }

    @Bean
    public Tasklet passTasklet() {
        return ((stepContribution, chunkContext) -> RepeatStatus.FINISHED);
    }

    @Bean
    public Tasklet successTasklet() {
        return ((stepContribution, chunkContext) -> {
            System.out.println("success!");
            return RepeatStatus.FINISHED;
        });
    }

    @Bean
    public Tasklet failTasklet() {
        return ((stepContribution, chunkContext) -> {
            System.out.println("fail!");
            return RepeatStatus.FINISHED;
        });
    }

    @Bean
    public JobExecutionDecider decider() {
        return new RandomDecider();
    }
}
