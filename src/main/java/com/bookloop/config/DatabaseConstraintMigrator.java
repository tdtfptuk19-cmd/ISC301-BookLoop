package com.bookloop.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;

/**
 * Idempotently updates database CHECK constraints (such as CK_orders_status)
 * so that new order lifecycle statuses (SHIPPED, PAID_HELD, etc.) and payment
 * methods (MARKETPLACE_HOLDING) are supported without constraint violation errors.
 */
@Component
@Order(1)
public class DatabaseConstraintMigrator implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConstraintMigrator.class);

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    public DatabaseConstraintMigrator(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.dataSource = dataSource;
    }

    @Override
    public void run(String... args) {
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            String dbProductName = metaData.getDatabaseProductName();
            if (dbProductName != null && dbProductName.toLowerCase().contains("microsoft")) {
                migrateSqlServerConstraints();
            }
        } catch (Exception e) {
            log.warn("DatabaseConstraintMigrator check skipped or encountered error: {}", e.getMessage());
        }
    }

    private void migrateSqlServerConstraints() {
        try {
            // Check if CK_orders_status needs updating
            String checkSql = "SELECT definition FROM sys.check_constraints WHERE name = 'CK_orders_status'";
            var definitions = jdbcTemplate.queryForList(checkSql, String.class);
            if (!definitions.isEmpty()) {
                String def = definitions.get(0);
                if (!def.contains("SHIPPED") || !def.contains("PAID_HELD")) {
                    log.info("Migrating CK_orders_status to include Phase 7 lifecycle statuses...");
                    jdbcTemplate.execute("ALTER TABLE orders DROP CONSTRAINT CK_orders_status;");
                    jdbcTemplate.execute("""
                        ALTER TABLE orders ADD CONSTRAINT CK_orders_status CHECK (status IN (
                            'PENDING', 'AWAITING_PAYMENT', 'PAID_HELD', 'CONFIRMED', 'SHIPPED', 
                            'DELIVERED', 'DISPUTED', 'REFUNDED', 'COMPLETED', 'CANCELLED'
                        ));
                    """);
                    log.info("Successfully updated CK_orders_status constraint.");
                }
            }

            // Check if CK_orders_payment needs updating
            String checkPaymentSql = "SELECT definition FROM sys.check_constraints WHERE name = 'CK_orders_payment'";
            var payDefs = jdbcTemplate.queryForList(checkPaymentSql, String.class);
            if (!payDefs.isEmpty()) {
                String def = payDefs.get(0);
                if (!def.contains("MARKETPLACE_HOLDING")) {
                    log.info("Migrating CK_orders_payment to include MARKETPLACE_HOLDING...");
                    jdbcTemplate.execute("ALTER TABLE orders DROP CONSTRAINT CK_orders_payment;");
                    jdbcTemplate.execute("""
                        ALTER TABLE orders ADD CONSTRAINT CK_orders_payment CHECK (payment_method IN (
                            'IN_PERSON_MEETUP', 'BANK_TRANSFER', 'MARKETPLACE_HOLDING'
                        ));
                    """);
                    log.info("Successfully updated CK_orders_payment constraint.");
                }
            }
        } catch (Exception ex) {
            log.error("Failed to migrate SQL Server constraints: {}", ex.getMessage());
        }
    }
}

