package com.progressoft.fxdealsystem.controller;

import com.progressoft.fxdealsystem.dto.DealRequest;
import com.progressoft.fxdealsystem.dto.DealResponse;
import com.progressoft.fxdealsystem.exception.InvalidDealException;
import com.progressoft.fxdealsystem.service.DealService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/deals")
@RequiredArgsConstructor
@Slf4j
public class DealController {

    private final DealService dealService;

    /**
     * Importer un seul deal
     */
    @PostMapping
    public ResponseEntity<DealResponse> importDeal(@Valid @RequestBody DealRequest request) {
        log.info("Received request to import deal: {}", request.getDealUniqueId());
        DealResponse response = dealService.importDeal(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Importer plusieurs deals (bulk)
     */
    @PostMapping("/bulk")
    public ResponseEntity<List<DealResponse>> importDeals(@Valid @RequestBody List<DealRequest> requests) {
        log.info("Received bulk request with {} deals", requests.size());
        List<DealResponse> responses = dealService.importDeals(requests);
        return ResponseEntity.status(HttpStatus.CREATED).body(responses);
    }

    /**
     * Récupérer tous les deals
     */
    @GetMapping
    public ResponseEntity<List<DealResponse>> getAllDeals() {
        log.info("Request to get all deals");
        return ResponseEntity.ok(dealService.getAllDeals());
    }

    /**
     * Récupérer un deal par uniqueId
     */
    @GetMapping("/{dealUniqueId}")
    public ResponseEntity<DealResponse> getDealByUniqueId(@PathVariable String dealUniqueId) {
        log.info("Request to get deal: {}", dealUniqueId);

        DealResponse response = dealService.getDealByUniqueId(dealUniqueId);

        return ResponseEntity.ok(response);
    }

    /**
     * Health check
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("FX Deal System is running!");
    }
}
