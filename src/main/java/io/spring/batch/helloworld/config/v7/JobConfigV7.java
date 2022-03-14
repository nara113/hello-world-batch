package io.spring.batch.helloworld.config.v7;

import io.spring.batch.helloworld.validator.MyParameterValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.listener.ExecutionContextPromotionListener;
import org.springframework.batch.core.step.tasklet.CallableTaskletAdapter;
import org.springframework.batch.core.step.tasklet.MethodInvokingTaskletAdapter;
import org.springframework.batch.core.step.tasklet.SystemCommandTasklet;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Callable;

@RequiredArgsConstructor
//@Configuration
public class JobConfigV7 {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job jobV7() {
        return this.jobBuilderFactory.get("job")
                .start(stepV7())
                .validator(new MyParameterValidator())
                .incrementer(new RunIdIncrementer())
                .build();
    }

    @Bean
    public Step stepV7() {
        return this.stepBuilderFactory.get("step1")
                .tasklet(tasklet())
                .build();
    }

    @Bean
    public Callable<RepeatStatus> callableObject() {
        return () -> {
            System.out.println("thread : " + Thread.currentThread());
            return RepeatStatus.FINISHED;
        };
    }

    @Bean
    public CallableTaskletAdapter tasklet() {
        CallableTaskletAdapter adapter = new CallableTaskletAdapter();

        adapter.setCallable(callableObject());
        return adapter;
    }

    @Bean
    public CustomService service() {
        return new CustomService();
    }

    @StepScope
    @Bean
    public MethodInvokingTaskletAdapter methodInvokingTasklet(@Value("#{jobParameters['message']}") String message) {
        MethodInvokingTaskletAdapter adapter = new MethodInvokingTaskletAdapter();

        adapter.setTargetObject(service());
        adapter.setTargetMethod("serviceMethod");
        adapter.setArguments(new String[]{message});

        return adapter;
    }

    @Bean
    public SystemCommandTasklet systemCommandTasklet() {
        SystemCommandTasklet tasklet = new SystemCommandTasklet();

        tasklet.setCommand("rm -rf /tmp.txt");
        tasklet.setTimeout(5000);
        tasklet.setInterruptOnCancel(true);

        return tasklet;
    }
}
