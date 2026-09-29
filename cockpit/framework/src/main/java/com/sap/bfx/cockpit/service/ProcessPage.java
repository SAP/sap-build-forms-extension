package com.sap.bfx.cockpit.service;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * Paged response wrapper for process queries.
 */
@Data
@AllArgsConstructor
public class ProcessPage {
    private List<ProcessAbstract> items;
    private int totalCount;
}
