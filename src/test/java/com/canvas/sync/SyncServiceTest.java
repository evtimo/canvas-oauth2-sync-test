package com.canvas.sync;

import com.canvas.sync.dao.store.AccountStore;
import com.canvas.sync.dao.store.CourseStore;
import com.canvas.sync.service.SyncService;
import com.canvas.sync.service.UserAccessTokenService;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.mockito.MockitoAnnotations;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SyncServiceTest {

    private MockWebServer mockWebServer;
    private SyncService syncService;

    @Mock
    private CourseStore courseStore;

    @Mock
    private AccountStore accountStore;

    @Mock
    private UserAccessTokenService tokenService;

    @BeforeEach
    public void setUp() throws IOException {

        mockWebServer = new MockWebServer();
        mockWebServer.start();

        MockitoAnnotations.openMocks(this);

        // Mock responses for API endpoints
        String accountsJson = "[{\"id\":\"1\",\"name\":\"Test Account\"}]";
        mockWebServer.enqueue(new MockResponse()
            .addHeader("Content-Type", "application/json")
            .setBody(accountsJson));

        String coursesJson = "[{\"id\":\"1\",\"title\":\"Test Course\"}]";
        mockWebServer.enqueue(new MockResponse()
            .addHeader("Content-Type", "application/json")
            .setBody(coursesJson));

        // Configure WebClient to use the mock server
        WebClient webClient = WebClient.builder()
            .baseUrl(mockWebServer.url("/").toString())
            .build();

        // Mock the behavior of the tokenService to return a valid token
        Mockito.when(tokenService.getCurrentUserAccessToken()).thenReturn("mockedToken");

        // Initialize your service with the mocked WebClient and other necessary mocks
        syncService = new SyncService(webClient, courseStore, accountStore, tokenService);
    }

    @AfterEach
    public void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    public void testSyncContent() {
        // Execute the service method
        Mono<Void> result = syncService.syncContent();

        // Verify the completion of the operation
        StepVerifier.create(result)
            .verifyComplete();

        assertEquals(2, mockWebServer.getRequestCount(), "Expected exactly two requests to the mock server.");

    }
}
