package io.spring.batch.helloworld.config.v10;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@RequiredArgsConstructor
//@Configuration
public class ExploreJob {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final JobExplorer jobExplorer;

    @Bean
    public Tasklet explorerTasklet() {
        return ((stepContribution, chunkContext) -> {
            String jobName = chunkContext.getStepContext().getJobName();

            // 현재 job도 함께 반환된다.
            final List<JobInstance> jobInstances =
                    jobExplorer.getJobInstances(jobName, 0, Integer.MAX_VALUE);

            System.out.println("There are " + jobInstances.size() + " job instances for the job " + jobName);
            System.out.println("=======================================");

            for (JobInstance jobInstance: jobInstances) {
                final List<JobExecution> executions =
                        jobExplorer.getJobExecutions(jobInstance);

                System.out.println("instance "
                        + jobInstance.getInstanceId() + " had "
                        + executions.size() + " executions");

                for (JobExecution execution: executions) {
                    System.out.println("execution "
                            + execution.getId() + " resulted in Exit Status "
                            + execution.getExitStatus());
                }
            }

            return RepeatStatus.FINISHED;
        });
    }

    @Bean
    public Step explorerStep() {
        return stepBuilderFactory.get("explorerStep")
                .tasklet(explorerTasklet())
                .build();
    }

    @Bean
    public Job explorerJob() {
        return jobBuilderFactory.get("explorerJob")
                .start(explorerStep())
                .incrementer(new RunIdIncrementer())
                .build();
    }
}
