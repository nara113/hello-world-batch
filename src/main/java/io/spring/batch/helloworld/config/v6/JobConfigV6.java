package io.spring.batch.helloworld.config.v6;

import io.spring.batch.helloworld.validator.MyParameterValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.annotation.BeforeJob;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.listener.ExecutionContextPromotionListener;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@RequiredArgsConstructor
//@Configuration
public class JobConfigV6 {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public StepExecutionListener promotionListener() {
        ExecutionContextPromotionListener listener = new ExecutionContextPromotionListener();

        // step이 성공적으로 완료 상태로 종료된 이후에 step의 ExecutionContext에서 name키를 찾으면 job의 ExecutionContext로 복사한다.
        listener.setKeys(new String[]{"name"});

        return listener;
    }

    @Bean
    public Job jobV6() {
        return this.jobBuilderFactory.get("job")
                .start(stepV6_2())
                .validator(new MyParameterValidator())
                .incrementer(new RunIdIncrementer())
                .build();
    }

    @Bean
    public Step stepV6_1() {
        return this.stepBuilderFactory.get("step1")
                .tasklet((stepContribution, chunkContext) -> {
                    System.out.println("hi");
                    return RepeatStatus.FINISHED;
                })
                .listener(promotionListener())
                .build();
    }

    @Bean
    public Step stepV6_2() {
        return this.stepBuilderFactory.get("step2")
                .tasklet(taskletV6()).build();
    }

    @StepScope
    @Bean
    public Tasklet taskletV6() {
        return (stepContribution, chunkContext) -> {
            String name = (String) chunkContext.getStepContext()
                    .getJobParameters()
                    .getOrDefault("name", "default");

            // Job의 ExecutionContext 조작하기.
            // 각 job과 step들마다 ExecutionContext를 가지고 있다.
            final ExecutionContext jobExecutionContext = chunkContext.getStepContext()
                    .getStepExecution()
                    .getJobExecution()
                    .getExecutionContext();

            jobExecutionContext.put("user.name", name);

            // Step의 ExecutionContext
            final ExecutionContext stepExecutionContext = chunkContext.getStepContext()
                    .getStepExecution()
                    .getExecutionContext();

            stepExecutionContext.put("user.name", name);

            System.out.println("name : " + name);

            return RepeatStatus.FINISHED;
        };
    }
}
