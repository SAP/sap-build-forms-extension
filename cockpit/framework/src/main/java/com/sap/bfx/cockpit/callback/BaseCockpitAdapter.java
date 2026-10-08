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
        settings.setLanguage(params.getLanguage() != null ? params.getLanguage() : "en");
        final var rb = getResourceBundle(params);
        settings.getProfiles().add(new FrontendSettings.Profile("my_requests",
                rb.getString("profile_my_requests"), true));
    }

    /**
     * Returns the i18n ResourceBundle for the cockpit, resolved from the request language.
     * Can be used in custom {@link #init} overrides to translate additional profile labels.
     */
    protected ResourceBundle getResourceBundle(FrontendParams params) {
        return ResourceBundle.getBundle("cockpit", Locale.forLanguageTag(
                params.getLanguage() != null ? params.getLanguage() : "en"));
    }

    /**
     * Adds the "I'm involved" profile to the frontend settings.
     * Call this from a custom {@link #init} override to include this profile.
     */
    protected void addInvolvedProfile(FrontendSettings settings, FrontendParams params) {
        settings.getProfiles().add(new FrontendSettings.Profile("involved",
                getResourceBundle(params).getString("profile_involved"), false));
    }

    /**
     * Adds the "All" profile to the frontend settings.
     * Call this from a custom {@link #init} override to include this profile.
     */
    protected void addAllProfile(FrontendSettings settings, FrontendParams params) {
        settings.getProfiles().add(new FrontendSettings.Profile("all",
                getResourceBundle(params).getString("profile_all"), false));
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

    /**
     * Appends a SQL condition for a string filter field based on the operator type.
     *
     * @param condition   The SQL condition builder to append to
     * @param params      The parameter list to append bind values to
     * @param column      SQL column name
     * @param type        Operator: "equals", "contains", "begins_with", "ends_with"
     * @param values      Filter values; must be non-null and non-empty
     */
    protected static void appendStringFilter(StringBuilder condition, java.util.List<Object> params,
                                             String column, String type, String[] values) {
        if (values.length == 1) {
            final String v = values[0];
            switch (type == null ? "contains" : type) {
                case "equals":
                    condition.append(" AND ").append(column).append(" = ?");
                    params.add(v);
                    break;
                case "begins_with":
                    condition.append(" AND ").append(column).append(" LIKE CONCAT(?,'%')");
                    params.add(v);
                    break;
                case "ends_with":
                    condition.append(" AND ").append(column).append(" LIKE CONCAT('%',?)");
                    params.add(v);
                    break;
                default: // "contains"
                    condition.append(" AND ").append(column).append(" LIKE CONCAT('%',?,'%')");
                    params.add(v);
                    break;
            }
        } else {
            final var placeholders = "?,".repeat(values.length);
            condition.append(" AND ").append(column).append(" IN (")
                    .append(placeholders, 0, placeholders.length() - 1).append(")");
            params.addAll(java.util.Arrays.asList(values));
        }
    }
}
