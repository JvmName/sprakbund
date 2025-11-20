package dev.jvmname.sprakbund.domain

import androidx.compose.ui.util.fastAny
import dev.jvmname.sprakbund.network.HibpNetworkClient
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import org.kotlincrypto.hash.sha1.SHA1

@Inject
class HibpChecker(
    private val client: HibpNetworkClient,
    private val scope: CoroutineScope
) {

    suspend fun checkPasswords(passwords: List<String>): Map<String, Boolean> {
        val results = passwords.map { checkPwned(scope, it) }.awaitAll()
        return buildMap(passwords.size) {
            for (i in passwords.indices) {
                put(passwords[i], results[i])
            }
        }
    }

    private fun checkPwned(scope: CoroutineScope, password: String) = scope.async(Dispatchers.IO) {
        val hashed = with(SHA1()) {
            digest(password.encodeToByteArray()).toHexString()
        }
        val lastHash = hashed.drop(5)
        val lines = client.getPwnedPassword(hashed)
        lines.fastAny { it.hash == lastHash && it.count >= 1 }
    }
}