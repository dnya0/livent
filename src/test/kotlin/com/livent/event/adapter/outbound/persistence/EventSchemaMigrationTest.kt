package com.livent.event.adapter.outbound.persistence

import java.nio.file.Files
import java.sql.DatabaseMetaData
import java.sql.DriverManager
import java.util.UUID
import kotlin.test.assertEquals
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.Test

class EventSchemaMigrationTest {
    @Test
    fun `chat rooms foreign key uses cascade delete after migrations`() {
        val databasePath = Files.createTempDirectory("livent-schema-${UUID.randomUUID()}").resolve("db")
        val jdbcUrl = "jdbc:h2:file:${databasePath.toAbsolutePath()};MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE"

        Flyway.configure()
            .dataSource(jdbcUrl, "sa", "")
            .locations("classpath:db/migration")
            .load()
            .migrate()

        DriverManager.getConnection(jdbcUrl, "sa", "").use { connection ->
            connection.metaData.getImportedKeys(null, "PUBLIC", "CHAT_ROOMS").use { resultSet ->
                val deleteRule = generateSequence {
                    if (resultSet.next()) resultSet else null
                }
                    .first { it.getString("FK_NAME").equals("CHAT_ROOMS_EVENT_ID_FKEY", ignoreCase = true) }
                    .getShort("DELETE_RULE")

                assertEquals(DatabaseMetaData.importedKeyCascade.toShort(), deleteRule)
            }
        }
    }
}
