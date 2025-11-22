package com.progressoft.fxdealsystem.service;

import com.progressoft.fxdealsystem.dto.DealRequest;
import com.progressoft.fxdealsystem.dto.DealResponse;
import com.progressoft.fxdealsystem.exception.DuplicateDealException;
import com.progressoft.fxdealsystem.exception.InvalidDealException;
import com.progressoft.fxdealsystem.model.Deal;
import com.progressoft.fxdealsystem.repository.DealRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.Currency;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DealService {

    private final DealRepository dealRepository;

    /**
     * PlatformTransactionManager is optional to allow unit tests to construct the service
     * without a full Spring context. When present we use TransactionTemplate with
     * PROPAGATION_REQUIRES_NEW so that each save is independent.
     */
    @Autowired(required = false)
    private PlatformTransactionManager txManager;

    /**
     * Import a single deal.
     * Validations are performed BEFORE any repository call to satisfy unit test expectations.
     */
    public DealResponse importDeal(DealRequest request) {
        log.info("Importing deal with ID: {}", request.getDealUniqueId());

        // 1) Validations (must be done before repository interactions)
        validateMandatoryFields(request);
        validateCurrencyIsoCodes(request);

        if (request.getFromCurrencyIsoCode().equalsIgnoreCase(request.getToCurrencyIsoCode())) {
            throw new InvalidDealException("From and To currencies must be different");
        }

        if (request.getDealAmount() == null || request.getDealAmount().signum() <= 0) {
            throw new InvalidDealException("Deal amount must be positive");
        }

        if (request.getDealTimestamp() != null && request.getDealTimestamp().isAfter(LocalDateTime.now())) {
            throw new InvalidDealException("Deal timestamp cannot be in the future");
        }

        // 2) Check duplicates (after validation)
        if (dealRepository.existsByDealUniqueId(request.getDealUniqueId())) {
            throw new DuplicateDealException("Deal with ID " + request.getDealUniqueId() + " already exists");
        }

        // 3) Save (use REQUIRES_NEW if txManager available)
        try {
            Deal savedDeal;
            if (txManager != null) {
                TransactionTemplate tt = new TransactionTemplate(txManager);
                tt.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
                savedDeal = tt.execute(status -> {
                    Deal toSave = convertToEntity(request);
                    return dealRepository.save(toSave);
                });
            } else {
                Deal toSave = convertToEntity(request);
                savedDeal = dealRepository.save(toSave);
            }

            if (savedDeal == null) {
                log.error("Failed to save deal - repository returned null for {}", request.getDealUniqueId());
                throw new InvalidDealException("Failed to persist deal to database");
            }

            DealResponse response = new DealResponse();
            response.setId(savedDeal.getId());
            response.setDealUniqueId(savedDeal.getDealUniqueId());
            response.setStatus("SUCCESS");
            response.setMessage("Deal imported successfully");
            response.setFromCurrencyIsoCode(savedDeal.getFromCurrencyIsoCode());
            response.setToCurrencyIsoCode(savedDeal.getToCurrencyIsoCode());
            response.setDealTimestamp(savedDeal.getDealTimestamp());
            response.setDealAmount(savedDeal.getDealAmount());
            response.setCreatedAt(savedDeal.getCreatedAt());
            return response;

        } catch (DataIntegrityViolationException ex) {
            // Normalize DB constraint violations into the expected DuplicateDealException message
            log.warn("DataIntegrityViolation while saving deal {}: {}", request.getDealUniqueId(), ex.getMessage());
            throw new DuplicateDealException("Deal with ID " + request.getDealUniqueId() + " already exists");
        } catch (DuplicateDealException ex) {
            // propagate expected duplicate exception
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected error while importing deal {}: {}", request.getDealUniqueId(), ex.getMessage(), ex);
            throw new InvalidDealException("Invalid deal data: " + ex.getMessage());
        }
    }

    /**
     * Bulk import — each deal is treated independently; one failing import should not roll back others.
     */
    public List<DealResponse> importDeals(List<DealRequest> requests) {
        return requests.stream()
                .map(r -> {
                    try {
                        return importDeal(r);
                    } catch (Exception ex) {
                        DealResponse failed = new DealResponse();
                        failed.setId(null);
                        failed.setDealUniqueId(r.getDealUniqueId());
                        failed.setStatus("FAILED");
                        failed.setMessage(ex.getMessage());
                        return failed;
                    }
                })
                .collect(Collectors.toList());
    }

    /**
     * Get all deals
     */
    public List<DealResponse> getAllDeals() {
        return dealRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get a deal by its unique ID.
     * Tests expect InvalidDealException (mapped to 400) when not found.
     */
    public DealResponse getDealByUniqueId(String dealUniqueId) {
        if (dealUniqueId == null || dealUniqueId.trim().isEmpty()) {
            throw new InvalidDealException("Deal unique ID cannot be null or empty");
        }

        Deal deal = dealRepository.findByDealUniqueId(dealUniqueId)
                .orElseThrow(() -> new InvalidDealException("Deal not found with ID: " + dealUniqueId));

        return convertToResponse(deal);
    }

    /* ----------------- Validation helpers ----------------- */

    private void validateMandatoryFields(DealRequest request) {
        if (request.getDealUniqueId() == null || request.getDealUniqueId().isBlank()) {
            throw new InvalidDealException("Deal unique ID is required");
        }
        if (request.getFromCurrencyIsoCode() == null || request.getFromCurrencyIsoCode().isBlank()) {
            throw new InvalidDealException("From currency ISO code is required");
        }
        if (request.getToCurrencyIsoCode() == null || request.getToCurrencyIsoCode().isBlank()) {
            throw new InvalidDealException("To currency ISO code is required");
        }
        if (request.getDealAmount() == null) {
            throw new InvalidDealException("Deal amount is required");
        }
        if (request.getDealTimestamp() == null) {
            throw new InvalidDealException("Deal timestamp is required");
        }
    }

    /**
     * Validate currency ISO codes against the available currencies set.
     * Throws InvalidDealException with message containing "Invalid currency ISO code" which tests expect.
     */
    private void validateCurrencyIsoCodes(DealRequest request) {
        String from = request.getFromCurrencyIsoCode() == null ? "" : request.getFromCurrencyIsoCode().toUpperCase();
        String to = request.getToCurrencyIsoCode() == null ? "" : request.getToCurrencyIsoCode().toUpperCase();

        // Format strict : 3 lettres A-Z
        if (!from.matches("^[A-Z]{3}$")) {
            throw new InvalidDealException("Invalid currency ISO code: " + request.getFromCurrencyIsoCode());
        }
        if (!to.matches("^[A-Z]{3}$")) {
            throw new InvalidDealException("Invalid currency ISO code: " + request.getToCurrencyIsoCode());
        }

        // Reject well-known pseudo-codes explicitly (tests expect XXX to be invalid)
        if ("XXX".equals(from) || "XXX".equals(to)) {
            throw new InvalidDealException("Invalid currency ISO code: " + ( "XXX".equals(from) ? request.getFromCurrencyIsoCode() : request.getToCurrencyIsoCode() ));
        }

        // Vérifier dans la liste officielle
        Set<String> available = Currency.getAvailableCurrencies()
                .stream()
                .map(Currency::getCurrencyCode)
                .collect(Collectors.toSet());

        if (!available.contains(from)) {
            throw new InvalidDealException("Invalid currency ISO code: " + request.getFromCurrencyIsoCode());
        }
        if (!available.contains(to)) {
            throw new InvalidDealException("Invalid currency ISO code: " + request.getToCurrencyIsoCode());
        }
    }



    /* ----------------- Converters ----------------- */

    private Deal convertToEntity(DealRequest r) {
        Deal d = new Deal();
        d.setDealUniqueId(r.getDealUniqueId());
        d.setFromCurrencyIsoCode(r.getFromCurrencyIsoCode().toUpperCase());
        d.setToCurrencyIsoCode(r.getToCurrencyIsoCode().toUpperCase());
        d.setDealTimestamp(r.getDealTimestamp());
        d.setDealAmount(r.getDealAmount());
        return d;
    }

    private DealResponse convertToResponse(Deal d) {
        DealResponse response = new DealResponse();
        response.setId(d.getId());
        response.setDealUniqueId(d.getDealUniqueId());
        response.setStatus("SUCCESS");
        response.setMessage("Deal fetched successfully");
        response.setFromCurrencyIsoCode(d.getFromCurrencyIsoCode());
        response.setToCurrencyIsoCode(d.getToCurrencyIsoCode());
        response.setDealTimestamp(d.getDealTimestamp());
        response.setDealAmount(d.getDealAmount());
        response.setCreatedAt(d.getCreatedAt());
        return response;
    }
}
