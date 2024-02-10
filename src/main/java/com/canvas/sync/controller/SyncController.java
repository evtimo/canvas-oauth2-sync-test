package com.canvas.sync.controller;

import com.canvas.sync.aop.annotation.Audit;
import com.canvas.sync.service.SyncService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/sync")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class SyncController {

    SyncService syncService;

    @Audit
    @PostMapping
    public ResponseEntity<String> sync() {
        return syncService.syncContent()
            .then(Mono.just(ResponseEntity.ok("OK")))
            .onErrorResume(e -> {
                String errorMessage = "Sync operation failed: " + e.getMessage();
                return Mono.just(ResponseEntity.status(500).body(errorMessage));
            }).block();
    }

}