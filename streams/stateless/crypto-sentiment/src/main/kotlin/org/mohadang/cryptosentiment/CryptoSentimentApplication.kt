package org.mohadang.cryptosentiment

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class CryptoSentimentApplication

fun main(args: Array<String>) {
    runApplication<CryptoSentimentApplication>(*args)
}
