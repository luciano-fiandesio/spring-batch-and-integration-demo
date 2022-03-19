package com.lf.playground.producer.config;

import com.lf.playground.producer.domain.User;
import com.lf.playground.producer.downloader.ImageDownloader;
import com.lf.playground.producer.processor.ShopItemProcessor;
import lombok.AllArgsConstructor;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.kafka.KafkaItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.kafka.core.KafkaTemplate;

@Configuration
@EnableBatchProcessing
@AllArgsConstructor
public class BatchConfiguration {

    private KafkaTemplate<String, User> kafkaTemplate;
    private ImageDownloader imageDownloader;

    @Bean
    @StepScope
    public FlatFileItemReader<User> flatFileItemReader(@Value("#{jobParameters[file_path]}") String filePath) {
        FlatFileItemReader<User> reader = new FlatFileItemReader<>();
        reader.setLinesToSkip(1);
        final FileSystemResource fileResource = new FileSystemResource(filePath);
        reader.setResource(fileResource);

        DefaultLineMapper<User> customerLineMapper = new DefaultLineMapper<>();

        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setNames("tenant_id", "first_name", "last_name", "avatar_url");

        customerLineMapper.setLineTokenizer(tokenizer);
        customerLineMapper.setFieldSetMapper(new ShopItemFieldSetMapper());
        customerLineMapper.afterPropertiesSet();
        reader.setLineMapper(customerLineMapper);
        return reader;
    }

    @Bean
    public ShopItemProcessor processor() {
        return new ShopItemProcessor(imageDownloader);
    }

    @Bean
    public KafkaItemWriter<String, User> writer() throws Exception {
        KafkaItemWriter<String, User> writer = new KafkaItemWriter<>();
        writer.setKafkaTemplate(kafkaTemplate);
        writer.setItemKeyMapper(User::idAsString);
        writer.setDelete(false);
        writer.afterPropertiesSet();
        return writer;
    }
}
