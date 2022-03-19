package com.lf.playground.producer.config;

import com.lf.playground.producer.domain.User;
import com.lf.playground.producer.processor.ShopItemProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.kafka.KafkaItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class JobConfig {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final FlatFileItemReader<User> flatFileItemReader;
    private final ShopItemProcessor shopItemProcessor;
    private final KafkaItemWriter<String, User> writer;

    @Bean
    public Job processJob() {

        var step = stepBuilderFactory.get("orderStep1").<User, User>chunk(10)
                .reader(flatFileItemReader)
                .processor(shopItemProcessor)
                .writer(writer).build();

        return jobBuilderFactory.get("processJob")
                .incrementer(new RunIdIncrementer())
                //.listener(listener())
                .flow(step)
                .end()
                .build();
    }
}
