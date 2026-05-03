package com.livent.user.adapter.outbound.persistence

import java.nio.file.Files
import java.sql.DriverManager
import java.util.UUID
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.Test

class UserSchemaMigrationTest {
    @Test
    fun `user schema migration creates users table`() {
        val databasePath = Files.createTempDirectory("livent-user-schema-${UUID.randomUUID()}").resolve("db")
        val jdbcUrl = "jdbc:h2:file:${databasePath.toAbsolutePath()};MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE"

        Flyway.configure()
            .dataSource(jdbcUrl, "sa", "")
            .locations("classpath:db/migration")
            .load()
            .migrate()

        DriverManager.getConnection(jdbcUrl, "sa", "").use { connection ->
            connection.metaData.getColumns(null, "PUBLIC", "USERS", "NICKNAME").use { resultSet ->
                check(resultSet.next()) { "users.nickname column must exist after migrations." }
            }
        }
    }
}
