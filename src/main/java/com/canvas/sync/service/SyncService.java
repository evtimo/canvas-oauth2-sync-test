package com.canvas.sync.service;

import com.canvas.sync.aop.annotation.Audit;
import com.canvas.sync.dao.entity.AccountEntity;
import com.canvas.sync.dao.entity.CourseEntity;
import com.canvas.sync.dao.store.AccountStore;
import com.canvas.sync.dao.store.CourseStore;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class SyncService {

    WebClient webClient;
    CourseStore courseStore;
    AccountStore accountStore;
    UserAccessTokenService tokenService;

    @NonFinal
    @Value("${api.host}")
    private String API_HOST;

    @NonFinal
    @Value("${api.courses}")
    private String API_COURSES_ENDPOINT;

    @NonFinal
    @Value("${api.accounts}")
    private String API_ACCOUNTS_ENDPOINT;

    @NonFinal
    @Value("${api.batch-size}")
    private int BATCH_SIZE;

    @Audit
    public void syncContent() {
        fetchAndSaveAccounts(API_HOST + API_ACCOUNTS_ENDPOINT);
        fetchCoursesAndSaveBatch(API_HOST + API_COURSES_ENDPOINT, BATCH_SIZE);
    }

    private void fetchCoursesAndSaveBatch(String baseUrl, int pageSize) {
        webClient.get()
            .uri(baseUrl)
            .header("Authorization", "Bearer " + tokenService.getCurrentUserAccessToken())
            .retrieve()
            .bodyToFlux(CourseEntity.class) // Use Flux since we're expecting multiple items
            .buffer(pageSize) // Buffer elements into lists of pageSize
            .flatMap(courses -> {
                // Process each batch of courseEntities
                System.out.println(courses);
                // Wrap the blocking operation in a reactive context
                return Mono.fromRunnable(() -> courseStore.saveAll(courses))
                    // Handle any exceptions by returning an empty Mono
                    .onErrorResume(e -> {
                        // Log the exception or handle it as needed
                        System.err.println("Exception occurred while saving courses: " + e.getMessage());
                        return Mono.empty();
                    });
            })
            .subscribe();// Subscribe to trigger the processing
    }

    private void fetchAndSaveAccounts(String baseUrl) {
        webClient.get()
            .uri(baseUrl)
            .header("Authorization", "Bearer " + tokenService.getCurrentUserAccessToken())
            .retrieve()
            .bodyToFlux(AccountEntity.class)
            .collectList()
            .flatMap(accounts -> Mono.fromRunnable(() -> accountStore.saveAll(accounts))
                .onErrorResume(e -> {
                    // Log the exception or handle it as needed
                    System.err.println("Exception occurred while saving account: " + e.getMessage());
                    return Mono.empty();
                }))
            .subscribe();
    }

}