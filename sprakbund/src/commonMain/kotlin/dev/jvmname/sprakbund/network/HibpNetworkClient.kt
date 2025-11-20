package dev.jvmname.sprakbund.network

import dev.zacsweers.metro.Inject
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

@Inject
class HibpNetworkClient(private val client: HttpClient) {
    suspend fun getPwnedPassword(passwordHash: String): List<HibpLine> {
        val hash = passwordHash.take(5)
        return client.get("https://api.pwnedpasswords.com/range/$hash").body()
    }
}