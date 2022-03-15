package io.spring.batch.helloworld.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
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

    // curl -H "Content-Type: application/json" -X POST -d "{\"name\":\"explorerJob\",\"jobParameters\":{\"foo\":\"bar\"}}" 'http://localhost:8080/run'
    @PostMapping("/run")
    public ExitStatus runJob(@RequestBody JobLaunchRequest request) throws Exception {
        final Job job = applicationContext.getBean(request.getName(), Job.class);

        return jobLauncher.run(job, request.getJobParameters()).getExitStatus();
    }
}
