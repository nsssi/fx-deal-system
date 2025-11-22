package com.progressoft.fxdealsystem.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class DealRequest {

    @NotBlank(message = "dealUniqueId is required")
    private String dealUniqueId;

    @NotBlank(message = "fromCurrencyIsoCode is required")
    @Size(min = 3, max = 3, message = "fromCurrencyIsoCode must be 3 letters")
    private String fromCurrencyIsoCode;

    @NotBlank(message = "toCurrencyIsoCode is required")
    @Size(min = 3, max = 3, message = "toCurrencyIsoCode must be 3 letters")
    private String toCurrencyIsoCode;

    @NotNull(message = "dealTimestamp is required")
    @PastOrPresent(message = "dealTimestamp cannot be in the future")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dealTimestamp;

    @NotNull(message = "dealAmount is required")
    @DecimalMin(value = "0.01", message = "dealAmount must be greater than 0")
    private BigDecimal dealAmount;

    public DealRequest() {}

    public DealRequest(String dealUniqueId, String fromCurrencyIsoCode, String toCurrencyIsoCode,
                       LocalDateTime dealTimestamp, BigDecimal dealAmount) {
        this.dealUniqueId = dealUniqueId;
        this.fromCurrencyIsoCode = fromCurrencyIsoCode;
        this.toCurrencyIsoCode = toCurrencyIsoCode;
        this.dealTimestamp = dealTimestamp;
        this.dealAmount = dealAmount;
    }

    public String getDealUniqueId() { return dealUniqueId; }
    public void setDealUniqueId(String dealUniqueId) { this.dealUniqueId = dealUniqueId; }

    public String getFromCurrencyIsoCode() { return fromCurrencyIsoCode; }
    public void setFromCurrencyIsoCode(String fromCurrencyIsoCode) { this.fromCurrencyIsoCode = fromCurrencyIsoCode; }

    public String getToCurrencyIsoCode() { return toCurrencyIsoCode; }
    public void setToCurrencyIsoCode(String toCurrencyIsoCode) { this.toCurrencyIsoCode = toCurrencyIsoCode; }

    public LocalDateTime getDealTimestamp() { return dealTimestamp; }
    public void setDealTimestamp(LocalDateTime dealTimestamp) { this.dealTimestamp = dealTimestamp; }

    public BigDecimal getDealAmount() { return dealAmount; }
    public void setDealAmount(BigDecimal dealAmount) { this.dealAmount = dealAmount; }
}
