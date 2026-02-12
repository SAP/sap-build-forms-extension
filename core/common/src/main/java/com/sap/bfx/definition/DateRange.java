package com.sap.bfx.definition;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;

@Slf4j
@Data
@NoArgsConstructor
public class DateRange {
    LocalDate from;
    LocalDate to;

    /**
     * @param from
     * @param to
     */
    public DateRange(final String from, final String to) {
        this.setFrom(parse(from));
        this.setTo(parse(to));
    }

    /**
     * @param strDateTime
     * @return
     */
    private final LocalDate parse(final String strDateTime) {
        try {
            return LocalDate.parse(strDateTime);
        } catch (Exception e) {
            log.error("Error parsing data '" + strDateTime + "'", e);
        }
        return LocalDate.MIN;
    }
}
