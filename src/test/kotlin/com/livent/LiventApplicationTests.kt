package com.livent

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(
    classes = [LiventApplication::class],
    properties = [
        "spring.r2dbc.url=r2dbc:h2:mem:///livent-test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.r2dbc.username=sa",
        "spring.r2dbc.password=",
        "spring.flyway.enabled=false",
    ],
)
class LiventApplicationTests {

    @Test
    fun contextLoads() {}
}
