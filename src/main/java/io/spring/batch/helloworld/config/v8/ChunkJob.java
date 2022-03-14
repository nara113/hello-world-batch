package io.spring.batch.helloworld.config.v8;

import io.spring.batch.helloworld.validator.MyParameterValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.batch.repeat.CompletionPolicy;
import org.springframework.batch.repeat.policy.CompositeCompletionPolicy;
import org.springframework.batch.repeat.policy.SimpleCompletionPolicy;
import org.springframework.batch.repeat.policy.TimeoutTerminationPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.UUID;

@RequiredArgsConstructor
@Configuration
public class ChunkJob {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job chunkBasedJob() {
        return this.jobBuilderFactory.get("chunkBaseJob")
                .start(chunkStep())
                .validator(new MyParameterValidator())
                .incrementer(new RunIdIncrementer())
                .build();
    }

    @Bean
    public Step chunkStep() {
        return this.stepBuilderFactory.get("chunkStep")
                .<String, String>chunk(randomCompletionPolicy())
                .reader(itemReader())
                .writer(itemWriter())
                .build();
    }

    @Bean
    public ListItemReader<String> itemReader() {
        final ArrayList<String> items = new ArrayList<>(10000);

        for (int i = 0; i < 10000; i++) {
            items.add(UUID.randomUUID().toString());
        }

        return new ListItemReader<>(items);
    }


    @Bean
    public ItemWriter<String> itemWriter() {
        return items -> {
            System.out.println("count : " + items.size());

            for (String item : items) {
//                System.out.println("current item = " + item);
            }
        };
    }

    @Bean
    public CompletionPolicy randomCompletionPolicy() {
        return new RandomChunkSizePolicy();
    }

    @Bean
    public CompletionPolicy completionPolicy() {
        CompositeCompletionPolicy policy = new CompositeCompletionPolicy();

        // 1초 이상 이거나 아이템 개수가 1000개 이상이거나 청크 완료로 판단.
        policy.setPolicies(new CompletionPolicy[] {
                new TimeoutTerminationPolicy(1),
                new SimpleCompletionPolicy(1000)
        });

        return policy;
    }
}
