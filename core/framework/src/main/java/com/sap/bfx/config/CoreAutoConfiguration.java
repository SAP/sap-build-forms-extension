package com.sap.bfx.config;

import com.sap.bfx.callback.FeedService;
import com.sap.bfx.callback.FeedTypeConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Spring Boot auto-configuration for core/framework.
 * Provides a default PostgreSQL-backed {@link FeedService} bean for any app that has
 * core/framework on its classpath but not cockpit/framework.
 * When cockpit/framework is present its {@code CockpitAutoConfiguration} registers its own
 * {@link FeedService} bean first (via {@code @ConditionalOnMissingBean}), which takes priority.
 */
@AutoConfiguration
public class CoreAutoConfiguration {

    /**
     * Default {@link FeedService} backed directly by the form database.
     * Superseded by the cockpit module's bean when cockpit/framework is on the classpath.
     * PostgreSQL large objects require an active transaction, so writes use {@link TransactionTemplate}.
     */
    @Bean
    @ConditionalOnMissingBean
    public FeedService feedService(
            @Qualifier("formDatabaseDataSource") DataSource dataSource,
            @Qualifier("formDatabaseDataSourceTransactionManager") PlatformTransactionManager transactionManager) {
        final var jdbc = new JdbcTemplate(dataSource);
        final var tx = new TransactionTemplate(transactionManager);
        return (formId, userNm, text, type, parentId) -> {
            final String id = UUID.randomUUID().toString();
            final byte[] textBytes = text.getBytes(StandardCharsets.UTF_8);
            tx.executeWithoutResult(status -> {
                final Integer max;
                if (parentId == null) {
                    max = jdbc.queryForObject(
                            "SELECT MAX(pos) FROM forms_feeds WHERE form_id = ? AND parent_id IS NULL",
                            Integer.class, formId);
                } else {
                    max = jdbc.queryForObject(
                            "SELECT MAX(pos) FROM forms_feeds WHERE form_id = ? AND parent_id = ?",
                            Integer.class, formId, parentId);
                }
                final int nextPos = (max != null ? max : 0) + 1;
                jdbc.update(con -> {
                    final var ps = con.prepareStatement(
                            "INSERT INTO forms_feeds (id, parent_id, user_nm, type, pos, form_id, ts, text)"
                                    + " VALUES (?, ?, ?, ?, ?, ?, NOW(), ?)");
                    ps.setString(1, id);
                    ps.setString(2, parentId);
                    ps.setString(3, userNm);
                    ps.setString(4, FeedTypeConverter.toChar(type));
                    ps.setInt(5, nextPos);
                    ps.setString(6, formId);
                    ps.setBlob(7, new ByteArrayInputStream(textBytes), textBytes.length);
                    return ps;
                });
            });
        };
    }
}
