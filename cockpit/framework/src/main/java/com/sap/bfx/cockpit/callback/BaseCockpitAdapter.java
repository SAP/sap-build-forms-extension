package com.sap.bfx.cockpit.callback;

import com.sap.bfx.cockpit.service.ProcessAbstract;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Base implementation of the CockpitAdapter interface.
 * Provides default (null) implementations for the methods.
 */
public class BaseCockpitAdapter implements CockpitAdapter {
    protected final JdbcTemplate jdbc;

    /** Columns allowed in findSuggestions to prevent SQL injection. */
    private static final java.util.Set<String> ALLOWED_COLUMNS =
            java.util.Set.of("description", "functional_id", "additional_information");

    /**
     * Constructor
     *
     * @param ds Datatsource to be used
     */
    protected BaseCockpitAdapter(final DataSource ds) {
        this.jdbc = new JdbcTemplate(ds);
    }

    @Override
    public void init(FrontendSettings settings, FrontendParams params) {
        final var rb = ResourceBundle.getBundle("cockpit", Locale.forLanguageTag(
                params.getLanguage() != null ? params.getLanguage() : "en"));
        settings.setLanguage(params.getLanguage() != null ? params.getLanguage() : "en");
        settings.getProfiles().add(new FrontendSettings.Profile("my_requests",
                rb.getString("profile_my_requests"), true));
        settings.getProfiles().add(new FrontendSettings.Profile("involved",
                rb.getString("profile_involved"), false));
        settings.getProfiles().add(new FrontendSettings.Profile("all",
                rb.getString("profile_all"), false));
    }

    @Override
    public int findProcesses(List<ProcessAbstract> processes, SearchParams params) {
        return 0;
    }

    @Override
    public List<String> findSuggestions(String column, String search) {
        return List.of();
    }

    @Override
    public List<String> findScenarios() {
        return List.of();
    }

    /**
     * Validates that the column name is in the allowed set.
     * Throws IllegalArgumentException if not, to prevent SQL injection.
     */
    protected void validateColumn(String column) {
        if (!ALLOWED_COLUMNS.contains(column)) {
            throw new IllegalArgumentException("Column not allowed for suggestions: " + column);
        }
    }
}
