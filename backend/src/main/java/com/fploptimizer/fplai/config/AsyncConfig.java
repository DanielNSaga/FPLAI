package com.fploptimizer.fplai.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Configuration class for enabling asynchronous method execution in the Spring application.
 *
 * <p>This configuration class allows Spring to run methods asynchronously,
 * which can improve performance by allowing non-blocking operations.
 * The {@code @EnableAsync} annotation is used to enable asynchronous processing in the application.
 *
 * <p>For example, you can annotate a method with {@code @Async} to execute it in a separate thread.
 * This can be useful for tasks like sending emails, file processing, or other background tasks
 * that don't need to block the main execution thread.
 *
 * <p>Note that methods annotated with {@code @Async} should return {@code void}, {@code Future},
 * or {@code CompletableFuture}. Starting from Spring version 6.0, {@code ListenableFuture} has been deprecated
 * and is no longer recommended for use.
 *
 * @see org.springframework.scheduling.annotation.Async
 * @see org.springframework.scheduling.annotation.EnableAsync
 * @see java.util.concurrent.Future
 * @see java.util.concurrent.CompletableFuture
 */
@Configuration
@EnableAsync
public class AsyncConfig {
}
