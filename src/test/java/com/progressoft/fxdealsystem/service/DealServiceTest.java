package com.progressoft.fxdealsystem.service;

import com.progressoft.fxdealsystem.dto.DealRequest;
import com.progressoft.fxdealsystem.exception.DuplicateDealException;
import com.progressoft.fxdealsystem.exception.InvalidDealException;
import com.progressoft.fxdealsystem.model.Deal;
import com.progressoft.fxdealsystem.repository.DealRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DealServiceTest {

    private DealRepository dealRepository;
    private DealService dealService;

    @BeforeEach
    void setUp() {
        dealRepository = mock(DealRepository.class);
        // Construct service without txManager (null) — comportement supporté
        dealService = new DealService(dealRepository);
    }

    @Test
    @DisplayName("importDeal - success")
    void testImportDeal_Success() {
        DealRequest request = new DealRequest("DEAL001", "USD", "EUR",
                LocalDateTime.of(2024,1,15,10,30), new BigDecimal("1000.50"));

        Deal saved = new Deal();
        saved.setId(1L);
        saved.setDealUniqueId("DEAL001");
        saved.setFromCurrencyIsoCode("USD");
        saved.setToCurrencyIsoCode("EUR");
        saved.setDealTimestamp(request.getDealTimestamp());
        saved.setDealAmount(request.getDealAmount());

        when(dealRepository.existsByDealUniqueId("DEAL001")).thenReturn(false);
        when(dealRepository.save(any(Deal.class))).thenReturn(saved);

        var resp = dealService.importDeal(request);

        assertThat(resp).isNotNull();
        assertThat(resp.getId()).isEqualTo(1L);
        assertThat(resp.getDealUniqueId()).isEqualTo("DEAL001");
        assertThat(resp.getStatus()).isEqualTo("SUCCESS");

        // verify repository save was called with uppercased currency codes
        ArgumentCaptor<Deal> captor = ArgumentCaptor.forClass(Deal.class);
        verify(dealRepository, times(1)).save(captor.capture());
        Deal captured = captor.getValue();
        assertThat(captured.getFromCurrencyIsoCode()).isEqualTo("USD");
        assertThat(captured.getToCurrencyIsoCode()).isEqualTo("EUR");
    }

    @Test
    @DisplayName("importDeal - duplicate deal should throw DuplicateDealException")
    void testImportDeal_Duplicate() {
        DealRequest request = new DealRequest("DEAL001", "USD", "EUR",
                LocalDateTime.now(), new BigDecimal("100.00"));

        when(dealRepository.existsByDealUniqueId("DEAL001")).thenReturn(true);

        assertThatThrownBy(() -> dealService.importDeal(request))
                .isInstanceOf(DuplicateDealException.class)
                .hasMessageContaining("Deal with ID DEAL001");
        verify(dealRepository, never()).save(any());
    }

    @Test
    @DisplayName("importDeal - invalid currency code should throw InvalidDealException")
    void testImportDeal_InvalidCurrencyCode() {
        DealRequest request = new DealRequest("DEAL002", "ZZZ", "EUR",
                LocalDateTime.now(), new BigDecimal("100.00"));

        // ensure repo not queried until after validation
        when(dealRepository.existsByDealUniqueId(any())).thenReturn(false);

        assertThatThrownBy(() -> dealService.importDeal(request))
                .isInstanceOf(InvalidDealException.class)
                .hasMessageContaining("Invalid currency ISO code");
        verify(dealRepository, never()).save(any());
    }

    @Test
    @DisplayName("importDeal - future timestamp should throw InvalidDealException")
    void testImportDeal_FutureTimestamp() {
        DealRequest request = new DealRequest("DEAL003", "USD", "EUR",
                LocalDateTime.now().plusDays(2), new BigDecimal("100.00"));

        when(dealRepository.existsByDealUniqueId(any())).thenReturn(false);

        assertThatThrownBy(() -> dealService.importDeal(request))
                .isInstanceOf(InvalidDealException.class)
                .hasMessageContaining("cannot be in the future");
        verify(dealRepository, never()).save(any());
    }

    @Test
    @DisplayName("importDeal - negative amount should throw InvalidDealException")
    void testImportDeal_NegativeAmount() {
        DealRequest request = new DealRequest("DEAL004", "USD", "EUR",
                LocalDateTime.now(), new BigDecimal("-10"));

        when(dealRepository.existsByDealUniqueId(any())).thenReturn(false);

        assertThatThrownBy(() -> dealService.importDeal(request))
                .isInstanceOf(InvalidDealException.class)
                .hasMessageContaining("must be positive");
        verify(dealRepository, never()).save(any());
    }

    @Test
    @DisplayName("importDeal - same currencies should throw InvalidDealException")
    void testImportDeal_SameCurrencies() {
        DealRequest request = new DealRequest("DEAL005", "USD", "USD",
                LocalDateTime.now(), new BigDecimal("50"));

        when(dealRepository.existsByDealUniqueId(any())).thenReturn(false);

        assertThatThrownBy(() -> dealService.importDeal(request))
                .isInstanceOf(InvalidDealException.class)
                .hasMessageContaining("must be different");
        verify(dealRepository, never()).save(any());
    }

    @Test
    @DisplayName("getDealByUniqueId - not found should throw InvalidDealException")
    void testGetDealByUniqueId_NotFound() {
        when(dealRepository.findByDealUniqueId("NONEXISTENT")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> dealService.getDealByUniqueId("NONEXISTENT"))
                .isInstanceOf(InvalidDealException.class)
                .hasMessageContaining("Deal not found");
    }

    @Test
    @DisplayName("getDealByUniqueId - found")
    void testGetDealByUniqueId_Found() {
        Deal d = new Deal();
        d.setId(10L);
        d.setDealUniqueId("D10");
        d.setFromCurrencyIsoCode("USD");
        d.setToCurrencyIsoCode("EUR");
        d.setDealTimestamp(LocalDateTime.now());
        d.setDealAmount(new BigDecimal("123.45"));

        when(dealRepository.findByDealUniqueId("D10")).thenReturn(Optional.of(d));

        var resp = dealService.getDealByUniqueId("D10");
        assertThat(resp).isNotNull();
        assertThat(resp.getDealUniqueId()).isEqualTo("D10");
        assertThat(resp.getFromCurrencyIsoCode()).isEqualTo("USD");
    }
}
