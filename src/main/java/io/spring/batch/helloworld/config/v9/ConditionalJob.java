package io.spring.batch.helloworld.config.v9;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.step.job.DefaultJobParametersExtractor;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@RequiredArgsConstructor
//@Configuration
public class ConditionalJob {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Flow preProcessingFlow() {
        return new FlowBuilder<Flow>("preProcessingFlow")
                .start(firstStep())
                .next(successStep())
                .build();
    }

    @Bean
    public Job preProcessingJob() {
        return this.jobBuilderFactory.get("preProcessingJob")
                .start(firstStep())
                .next(successStep())
                .build();
    }

    @Bean
    public Job flowJob() {
        return this.jobBuilderFactory.get("flowJob")
                .start(initializeBatch())
                .next(failStep())
                .build();
    }

    @Bean
    public Step initializeBatch() {
        return stepBuilderFactory.get("initializeBatch")
                .job(preProcessingJob())
                .parametersExtractor(new DefaultJobParametersExtractor())
                .build();
    }

    @Bean
    public Job job() {
        return this.jobBuilderFactory.get("ConditionalJob")
                .start(firstStep())
                .next(decider())
                .from(decider())
//                    .on("FAILED").to(failStep())
                //step이 반환한 ExitStatus에 상관없이 COMPLETE 저장
//                    .on("FAILED").end()
//                .on("FAILED").fail()
                    .on("FAILED").stopAndRestart(successStep())
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
