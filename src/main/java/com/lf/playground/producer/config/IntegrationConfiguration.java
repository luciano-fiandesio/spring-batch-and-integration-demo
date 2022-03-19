package com.lf.playground.producer.config;

import lombok.AllArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.integration.launch.JobLaunchingGateway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.file.DirectoryScanner;
import org.springframework.integration.file.FileReadingMessageSource;
import org.springframework.integration.file.RecursiveDirectoryScanner;
import org.springframework.integration.file.filters.AcceptOnceFileListFilter;
import org.springframework.integration.file.filters.CompositeFileListFilter;
import org.springframework.integration.file.filters.RegexPatternFileListFilter;
import org.springframework.integration.transaction.DefaultTransactionSynchronizationFactory;
import org.springframework.integration.transaction.ExpressionEvaluatingTransactionSynchronizationProcessor;
import org.springframework.integration.transaction.PseudoTransactionManager;
import org.springframework.integration.transaction.TransactionSynchronizationFactory;
import org.springframework.messaging.MessageChannel;

import java.io.File;
import java.util.Arrays;

@Configuration
@AllArgsConstructor
public class IntegrationConfiguration {

    private File inboundReadDirectory;
    private JobRepository jobRepository;
    private Job feedIngestionJob;
    private ApplicationContext applicationContext;

    public static final String INBOUND_CHANNEL = "inbound-channel";

    @Bean
    FileMessageToJobRequest fileMessageToJobRequest() {
        FileMessageToJobRequest transformer = new FileMessageToJobRequest();
        transformer.setJob(feedIngestionJob);
        transformer.setFileParameterName("file_path");
        return transformer;
    }

    @Bean
    public JobLaunchingGateway jobLaunchingGateway() {
        SimpleJobLauncher simpleJobLauncher = new SimpleJobLauncher();

        simpleJobLauncher.setJobRepository(jobRepository);
        simpleJobLauncher.setTaskExecutor(new SyncTaskExecutor());

        return new JobLaunchingGateway(simpleJobLauncher);
    }

    @Bean(name = INBOUND_CHANNEL)
    public MessageChannel inboundFilePollingChannel() {
        return MessageChannels.direct().get();
    }

    @Bean
    public IntegrationFlow inboundFileIntegration(@Value("${inbound.file.poller.fixed.delay}") long period,
                                                  @Value("${inbound.file.poller.max.messages.per.poll}") int maxMessagesPerPoll,
                                                  TaskExecutor taskExecutor,
                                                  MessageSource<File> fileReadingMessageSource) {
        return IntegrationFlows.from(fileReadingMessageSource,
                        c -> c.poller(Pollers.fixedDelay(period)
                                .taskExecutor(taskExecutor)
                                .maxMessagesPerPoll(maxMessagesPerPoll)
                                .transactionSynchronizationFactory(transactionSynchronizationFactory())
                                .transactional(new PseudoTransactionManager())))
                .channel(INBOUND_CHANNEL)
                .transform(fileMessageToJobRequest()) //
                .handle(jobLaunchingGateway()) //
                .handle(jobExecution -> {
                    System.out.println(jobExecution.getPayload());
                }) //
                .get();
    }

//    @Bean
//    TaskExecutor taskExecutor(@Value("${inbound.file.poller.thread.pool.size}") int poolSize) {
//        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
//        taskExecutor.setCorePoolSize(poolSize);
//        return taskExecutor;
//    }

    @Bean
    TransactionSynchronizationFactory transactionSynchronizationFactory() {
        ExpressionParser parser = new SpelExpressionParser();
        ExpressionEvaluatingTransactionSynchronizationProcessor syncProcessor =
                new ExpressionEvaluatingTransactionSynchronizationProcessor();
        syncProcessor.setBeanFactory(applicationContext.getAutowireCapableBeanFactory());
        syncProcessor.setAfterCommitExpression(parser.parseExpression("payload.renameTo(new java.io.File(@inboundProcessedDirectory.path " +
                " + T(java.io.File).separator + payload.name))"));
        syncProcessor.setAfterRollbackExpression(parser.parseExpression("payload.renameTo(new java.io.File(@inboundFailedDirectory.path " +
                " + T(java.io.File).separator + payload.name))"));
        return new DefaultTransactionSynchronizationFactory(syncProcessor);
    }

    @Bean
    public FileReadingMessageSource fileReadingMessageSource(DirectoryScanner directoryScanner) {
        FileReadingMessageSource source = new FileReadingMessageSource();
        source.setDirectory(this.inboundReadDirectory);
        source.setScanner(directoryScanner);
        source.setAutoCreateDirectory(true);
        return source;
    }

    @Bean
    public DirectoryScanner directoryScanner(@Value("${inbound.filename.regex}") String regex) {
        DirectoryScanner scanner = new RecursiveDirectoryScanner();
        CompositeFileListFilter<File> filter = new CompositeFileListFilter<>(
                Arrays.asList(new AcceptOnceFileListFilter<>(),
                        new RegexPatternFileListFilter(regex))
        );
        scanner.setFilter(filter);
        return scanner;
    }
}
