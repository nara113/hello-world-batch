package io.spring.batch.helloworld.config.v8;

import org.springframework.batch.repeat.CompletionPolicy;
import org.springframework.batch.repeat.RepeatContext;
import org.springframework.batch.repeat.RepeatStatus;

import java.util.Random;

public class RandomChunkSizePolicy implements CompletionPolicy {
    private int chunkSize;
    private int totalProcessed;
    private Random random = new Random();

    @Override
    public boolean isComplete(RepeatContext repeatContext, RepeatStatus repeatStatus) {
        if (RepeatStatus.FINISHED == repeatStatus) {
            return true;
        } else {
            return isComplete(repeatContext);
        }
    }

    @Override
    public boolean isComplete(RepeatContext repeatContext) {
        return totalProcessed >= chunkSize;
    }

    @Override
    public RepeatContext start(RepeatContext repeatContext) {
        this.chunkSize = random.nextInt(1000);
        totalProcessed = 0;

        System.out.println("chunk size has been set to " + chunkSize);

        return repeatContext;
    }

    @Override
    public void update(RepeatContext repeatContext) {
        this.totalProcessed++;
    }
}
