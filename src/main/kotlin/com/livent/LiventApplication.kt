package com.livent

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class LiventApplication

fun main(args: Array<String>) {
    runApplication<LiventApplication>(*args)
}
