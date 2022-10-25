see [DataR2dbcConfig](https://github.com/stonegyu/r2dbc-transactionrollback/blob/main/src/main/kotlin/com/example/r2dbctransactionrollback/configuration/DataR2dbcConfig.kt)

### Transaction rollback works fine when using this code.
```kotlin
@Configuration
@EnableR2dbcAuditing
@EnableR2dbcRepositories(basePackages = ["com.example.r2dbctransactionrollback.repository"])
@EnableTransactionManagement
class DataR2dbcConfig : AbstractR2dbcConfiguration() {

    @Bean
    override fun connectionFactory(): ConnectionFactory {
        return ConnectionFactoryBuilder.withUrl("r2dbc:pool:mysql://127.0.0.1:3306")
            .database("testdb")
            .username("root")
            .password("root")
            .build()
    }
}
```

### However, when the code below works, the transaction rollback does not work.
```kotlin
@Configuration
@EnableR2dbcAuditing
@EnableR2dbcRepositories(basePackages = ["com.example.r2dbctransactionrollback.repository"])
@EnableTransactionManagement
class DataR2dbcConfig : AbstractR2dbcConfiguration() {
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
```

you can test [request.http](https://github.com/stonegyu/r2dbc-transactionrollback/blob/main/src/test/kotlin/com/example/r2dbctransactionrollback/request.http)
