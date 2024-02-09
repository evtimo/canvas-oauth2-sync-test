package com.canvas.sync.controller;

import com.canvas.sync.service.SyncService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/sync")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class SyncController {

    SyncService syncService;

    @PostMapping
    public ResponseEntity<String> sync() {
        log.info("[SyncController.sync] start sync");
        syncService.syncContent();
        log.info("[SyncController.sync] end sync");
        return ResponseEntity.ok("OK");
    }

}