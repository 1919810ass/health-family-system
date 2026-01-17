package com.healthfamily.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DatabaseSchemaRepair implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseSchemaRepair.class);

    private final JdbcTemplate jdbcTemplate;

    public DatabaseSchemaRepair(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        repairFamilyMembersTable();
    }

    private void repairFamilyMembersTable() {
        if (!tableExists("family_members")) {
            return;
        }

        ensureColumn("family_members", "relation",
                "alter table family_members add column relation varchar(32) null");

        ensureColumn("family_members", "is_admin",
                "alter table family_members add column is_admin tinyint(1) not null default 0");

        ensureColumn("family_members", "created_at",
                "alter table family_members add column created_at datetime not null default current_timestamp");
    }

    private boolean tableExists(String tableName) {
        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from information_schema.tables where table_schema = database() and table_name = ?",
                Integer.class,
                tableName
        );
        return count != null && count > 0;
    }

    private void ensureColumn(String tableName, String columnName, String ddl) {
        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from information_schema.columns where table_schema = database() and table_name = ? and column_name = ?",
                Integer.class,
                tableName,
                columnName
        );
        if (count != null && count > 0) {
            return;
        }
        log.warn("Repairing schema: {} missing column {}, applying DDL: {}", tableName, columnName, ddl);
        jdbcTemplate.execute(ddl);
    }
}

