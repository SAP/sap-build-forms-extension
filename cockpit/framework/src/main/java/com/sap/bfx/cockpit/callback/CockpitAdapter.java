package com.sap.bfx.cockpit.callback;

import com.sap.bfx.callback.Adapter;
import com.sap.bfx.cockpit.service.FeedEntry;
import com.sap.bfx.cockpit.service.ProcessAbstract;

import java.util.List;

/**
 * Adapter interface for Cockpit operations.
 */
public interface CockpitAdapter extends Adapter {

    /**
     * Initialize frontend settings based on provided parameters.
     *
     * @param settings the data structure to be filled
     * @param params   Frontend parameters
     */
    void init(final FrontendSettings settings, final FrontendParams params);

    /**
     * Query process instances based on provided attributes and locale.
     * Appends the page of matching processes to {@code processes} and returns
     * the total (unpaged) count so the caller can build a {@code ProcessPage}.
     *
     * @param processes List to append the current page of results to
     * @param params    Search params from the frontend (includes page / pageSize)
     * @return total count of matching processes (before paging)
     */
    int findProcesses(final List<ProcessAbstract> processes, SearchParams params);

    /**
     * Returns distinct non-blank values for the given column that contain
     * {@code search} (case-insensitive), up to 100 results, ordered alphabetically.
     * Allowed column names: {@code description}, {@code functional_id}.
     *
     * @param column the DB column name to query
     * @param search substring to filter by; empty string returns all distinct values
     * @return sorted list of distinct matching values
     */
    List<String> findSuggestions(String column, String search);

    /**
     * Returns distinct non-blank scenario names, ordered alphabetically.
     *
     * @return sorted list of scenario names
     */
    List<String> findScenarios();

    /**
     * Returns all feed entries for the given process (form_id), ordered by pos ASC.
     * Returns an empty list by default for adapters that do not support feeds.
     *
     * @param formId the process ID ({@code forms_forms.id})
     * @return ordered list of feed entries, or an empty list if not supported
     */
    default List<FeedEntry> findFeeds(String formId) {
        return List.of();
    }

    /**
     * Inserts a new feed entry for the given process.
     *
     * @param formId   the process ID ({@code forms_forms.id})
     * @param userNm   author resolved server-side from the security context
     * @param text     feed text
     * @param type     one of COMMENT, INFO, QUESTION, ANSWER
     * @param parentId parent feed entry ID for threading, or {@code null} for top-level
     * @return the persisted {@link FeedEntry}, or {@code null} if not supported
     */
    default FeedEntry addFeed(String formId, String userNm, String text, String type, String parentId) {
        return null;
    }
}