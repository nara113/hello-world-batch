package io.spring.batch.helloworld.config.v3;

import io.spring.batch.helloworld.validator.MyParameterValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersValidator;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.CompositeJobParametersValidator;
import org.springframework.batch.core.job.DefaultJobParametersValidator;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@RequiredArgsConstructor
@Configuration
public class JobConfigV3 {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

//    @Bean
//    public JobParametersValidator validator() {
//        DefaultJobParametersValidator validator = new DefaultJobParametersValidator();
//
//        validator.setRequiredKeys(new String[]{"fileName"});
//        validator.setOptionalKeys(new String[]{"name"});
//
//        return validator;
//    }

    // 여러개의 validator를 등록할 때
   @Bean
    public CompositeJobParametersValidator validator() {
        CompositeJobParametersValidator validator = new CompositeJobParametersValidator();

        DefaultJobParametersValidator defaultValidator = new DefaultJobParametersValidator();

        defaultValidator.setRequiredKeys(new String[]{"fileName"});
        defaultValidator.setOptionalKeys(new String[]{"name"});

        defaultValidator.afterPropertiesSet();

        validator.setValidators(Arrays.asList(defaultValidator, new MyParameterValidator()));

        return validator;
    }

    @Bean
    public Job jobV3() {
        return this.jobBuilderFactory.get("basicJobV3")
                .start(stepV3())
                .validator(validator())
                .build();
    }

    @Bean
    public Step stepV3() {
        return this.stepBuilderFactory.get("step1")
                .tasklet(taskletV3(null, null)).build();
    }

    @StepScope
    @Bean
    public Tasklet taskletV3(@Value("#{jobParameters['fileName']}") String fileName,
                             @Value("#{jobParameters['name']}") String name) {
        return (stepContribution, chunkContext) -> {
            System.out.println("fileName " + fileName);
            System.out.println("name " + name);
            return RepeatStatus.FINISHED;
        };
    }
}
