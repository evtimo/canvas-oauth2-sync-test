package com.canvas.sync.service;

import com.canvas.sync.dao.entity.AccountEntity;
import com.canvas.sync.dao.entity.CourseEntity;
import com.canvas.sync.dao.store.AccountStore;
import com.canvas.sync.dao.store.CourseStore;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
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
    private int BATCH_SIZE = 1;

    public Mono<Void> syncContent() {
        Mono<Void> accountsMono = fetchAndSaveAccounts(API_HOST + API_ACCOUNTS_ENDPOINT);
        Mono<Void> coursesMono = fetchCoursesAndSaveBatch(API_HOST + API_COURSES_ENDPOINT, BATCH_SIZE);
        // TODO: reactive loader response with % of sync courses completion
        // Chain accountsMono and coursesMono, so coursesMono starts after accountsMono completes
        return accountsMono
            .then(coursesMono) // Start coursesMono after accountsMono completes
            .then(); // Return a Mono<Void> that completes when both operations are complete
    }

    private Mono<Void> fetchCoursesAndSaveBatch(String baseUrl, int pageSize) {
        return webClient.get()
            .uri(baseUrl)
            .header("Authorization", "Bearer " + tokenService.getCurrentUserAccessToken())
            .retrieve()
            .bodyToFlux(CourseEntity.class) // Use Flux since we're expecting multiple items
            .buffer(pageSize) // Buffer elements into lists of pageSize
            .flatMap(courses -> Mono.fromRunnable(() -> courseStore.saveAll(courses))
                .subscribeOn(Schedulers.boundedElastic()) // Offload blocking operation to a boundedElastic scheduler
                .onErrorResume(e -> {
                    // Log the exception or handle it as needed
                    log.error("Exception occurred while saving courses: " + e.getMessage());
                    return Mono.empty();
                })
            )
            .then(); // Subscribe to trigger the processing
    }

    private Mono<Void> fetchAndSaveAccounts(String baseUrl) {
        return webClient.get()
            .uri(baseUrl)
            .header("Authorization", "Bearer " + tokenService.getCurrentUserAccessToken())
            .retrieve()
            .bodyToFlux(AccountEntity.class)
            .collectList()
            .flatMap(accounts -> Mono.fromRunnable(() -> accountStore.saveAll(accounts))
                .subscribeOn(Schedulers.boundedElastic()) // Offload blocking operation to a boundedElastic scheduler
                .onErrorResume(e -> {
                    // Log the exception or handle it as needed
                    log.error("Exception occurred while saving accounts: " + e.getMessage());
                    return Mono.empty();
                })
            )
            .then(); // Subscribe to trigger the processing
    }

}