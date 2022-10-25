package com.example.r2dbctransactionrollback.configuration

import io.r2dbc.spi.ConnectionFactory
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.r2dbc.ConnectionFactoryBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories
import org.springframework.r2dbc.connection.R2dbcTransactionManager
import org.springframework.r2dbc.connection.TransactionAwareConnectionFactoryProxy
import org.springframework.r2dbc.connection.lookup.AbstractRoutingConnectionFactory
import org.springframework.transaction.ReactiveTransactionManager
import org.springframework.transaction.annotation.EnableTransactionManagement
import org.springframework.transaction.reactive.TransactionSynchronizationManager
import reactor.core.publisher.Mono


private val log = KotlinLogging.logger {}

@Configuration
@EnableR2dbcAuditing
@EnableR2dbcRepositories(basePackages = ["com.example.r2dbctransactionrollback.repository"])
@EnableTransactionManagement
class DataR2dbcConfig : AbstractR2dbcConfiguration() {

//    @Bean
//    override fun connectionFactory(): ConnectionFactory {
//        return ConnectionFactoryBuilder.withUrl("r2dbc:pool:mysql://127.0.0.1:3306")
//            .database("testdb")
//            .username("root")
//            .password("root")
//            .build()
//    }

    @Primary
    @Bean("connectionFactory")
    override fun connectionFactory(): ConnectionFactory {

        val primaryConnectionFactory = ConnectionFactoryBuilder.withUrl("r2dbc:pool:mysql://localhost:3306/testdb")
            .username("root")
            .password("root")
            .build()

        val secondaryConnectionFactory = ConnectionFactoryBuilder.withUrl("r2dbc:pool:mysql://localhost:3306/testdb")
            .username("root")
            .password("root")
            .build()

        val routingConnectionFactory: AbstractRoutingConnectionFactory = object : AbstractRoutingConnectionFactory() {
            override fun determineCurrentLookupKey(): Mono<Any> {
                return TransactionSynchronizationManager.forCurrentTransaction().map {
                    var result = ""
                    if (it.isActualTransactionActive) {
                        result = if (it.isCurrentTransactionReadOnly) "secondary" else "primary"
                    } else {
                        result = "secondary"
                    }
                    log.info { "transactionActive =${it.isActualTransactionActive} readOnly=${it.isCurrentTransactionReadOnly} result = $result" }
                    result
                }
            }
        }
        val factories = mapOf("primary" to primaryConnectionFactory, "secondary" to secondaryConnectionFactory)

        routingConnectionFactory.setLenientFallback(true)
        routingConnectionFactory.setDefaultTargetConnectionFactory(primaryConnectionFactory)
        routingConnectionFactory.setTargetConnectionFactories(factories)

        return routingConnectionFactory
    }

    @Primary
    @Bean("reactiveTransactionManager")
    fun reactiveTransactionManager(@Qualifier("connectionFactory") connectionFactory: ConnectionFactory): ReactiveTransactionManager {
        return R2dbcTransactionManager(TransactionAwareConnectionFactoryProxy(connectionFactory))
    }
}