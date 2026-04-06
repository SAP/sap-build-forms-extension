package com.sap.bfx.session;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MoneyAmount {
    public final static String EUR = "EUR";
    public final static String USD = "USD";
    public final static String GBP = "GBP";
    public final static String CAD = "CAD";
    public final static String CHF = "CHF";

    private BigDecimal amount;
    // Currency code according to ISO 4217, see https://de.iban.com/currency-codes
    private String currency;
}
