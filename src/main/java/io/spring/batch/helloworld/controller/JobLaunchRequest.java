package io.spring.batch.helloworld.controller;

import lombok.Getter;
import lombok.Setter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;

import java.util.Properties;

@Getter
@Setter
public class JobLaunchRequest {
    private String name;
    private Properties jobParameters;

    public JobParameters getJobParameters() {
        Properties properties = new Properties();
        properties.putAll(jobParameters);

        return new JobParametersBuilder(properties)
                .toJobParameters();
    }
}
