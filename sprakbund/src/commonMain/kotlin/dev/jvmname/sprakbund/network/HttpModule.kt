package dev.jvmname.sprakbund.network

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation

@ContributesTo(AppScope::class)
interface HttpModule {
    @[Provides SingleIn(AppScope::class)]
    fun httpClient(converter: HibpContentConverter): HttpClient {
        return HttpClient(createHttpEngine()) {
            install(ContentNegotiation) {
                hibp()
            }
        }
    }

    @Provides
    fun hibpConverter(): HibpContentConverter = HibpContentConverter(HibpFormat.Default)
}

expect fun createHttpEngine(): HttpClientEngineFactory<*>


