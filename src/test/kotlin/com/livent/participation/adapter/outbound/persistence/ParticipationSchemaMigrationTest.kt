package com.livent.participation.adapter.outbound.persistence

import java.nio.file.Path
import java.sql.DriverManager
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class ParticipationSchemaMigrationTest {

    @TempDir
    lateinit var tempDir: Path

    @Test
    fun `participation schema migration creates participations table`() {
        val databasePath = tempDir.resolve("db")
        val jdbcUrl =
            "jdbc:h2:file:${databasePath.toAbsolutePath()};MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE"

        Flyway.configure()
            .dataSource(jdbcUrl, "sa", "")
            .locations("classpath:db/migration")
            .load()
            .migrate()

        DriverManager.getConnection(jdbcUrl, "sa", "").use { connection ->
            connection.metaData.getColumns(null, "PUBLIC", "PARTICIPATIONS", "STATUS").use { resultSet ->
                check(resultSet.next()) { "participations.status column must exist after migrations." }
            }
        }
    }
}
