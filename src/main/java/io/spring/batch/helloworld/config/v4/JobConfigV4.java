package io.spring.batch.helloworld.config.v4;

import io.spring.batch.helloworld.validator.MyParameterValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.CompositeJobParametersValidator;
import org.springframework.batch.core.job.DefaultJobParametersValidator;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.Date;

@RequiredArgsConstructor
@Configuration
public class JobConfigV4 {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    static class DailyJobTimestamper implements JobParametersIncrementer {

        @Override
        public JobParameters getNext(JobParameters jobParameters) {
            return new JobParametersBuilder(jobParameters)
                    .addDate("currentDate", new Date())
                    .toJobParameters();
        }
    }

   @Bean
    public CompositeJobParametersValidator validator() {
        CompositeJobParametersValidator validator = new CompositeJobParametersValidator();

        DefaultJobParametersValidator defaultValidator = new DefaultJobParametersValidator();

        defaultValidator.setRequiredKeys(new String[]{"fileName"});
        defaultValidator.setOptionalKeys(new String[]{"name", "run.id", "currentDate"});

        defaultValidator.afterPropertiesSet();

        validator.setValidators(Arrays.asList(defaultValidator, new MyParameterValidator()));

        return validator;
    }

    @Bean
    public Job jobV4() {
        return this.jobBuilderFactory.get("basicJobV4")
                .start(stepV4())
                .validator(validator())
//                .incrementer(new RunIdIncrementer())
                .incrementer(new DailyJobTimestamper())
                .build();
    }

    @Bean
    public Step stepV4() {
        return this.stepBuilderFactory.get("step1")
                .tasklet(taskletV4(null, null)).build();
    }

    @StepScope
    @Bean
    public Tasklet taskletV4(@Value("#{jobParameters['fileName']}") String fileName,
                             @Value("#{jobParameters['name']}") String name) {
        return (stepContribution, chunkContext) -> {
            System.out.println("fileName " + fileName);
            System.out.println("name " + name);
            return RepeatStatus.FINISHED;
        };
    }
}
