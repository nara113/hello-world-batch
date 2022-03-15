package io.spring.batch.helloworld.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class JobLaunchingController {
    private final JobLauncher jobLauncher;
    private final ApplicationContext applicationContext;
    private final JobExplorer jobExplorer;

    // curl -H "Content-Type: application/json" -X POST -d "{\"name\":\"explorerJob\",\"jobParameters\":{\"foo\":\"bar\"}}" 'http://localhost:8080/run'
    @PostMapping("/run")
    public ExitStatus runJob(@RequestBody JobLaunchRequest request) throws Exception {
        final Job job = applicationContext.getBean(request.getName(), Job.class);

        // SimpleJobLauncher에서는 job 실행전에 파라미터를 조작해야한다.
        JobParameters jobParameters =
                new JobParametersBuilder(request.getJobParameters(), jobExplorer)
                .getNextJobParameters(job)
                .toJobParameters();

        return jobLauncher.run(job, jobParameters).getExitStatus();
    }
}
