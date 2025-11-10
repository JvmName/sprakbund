package dev.jvmname.pronounce

import kotlin.random.Random

class Pronounceable {
    private val trigram = Trigram()
    private val letters = "abcdefghijklmnopqrstuvwxyz"

    fun generate(length: Int): String {
        val threshold = Random.nextDouble() * 125729 //weight by sum of frequencies
        var sum = 0
        val password = StringBuilder(length)
        /*
        * the original sources had a 3-deep nested for-loop that each counted to 26. This achieves the same result with
        * much less unnecessary nesting.
        */
        repeat(17576) { i ->
            val c1 = i / 676  // 26^2
            val c2 = (i / 26) % 26
            val c3 = i % 26
            sum += trigram[c1, c2, c3]
            if (sum > threshold) { // Found start. Break.
                password.append(letters[c1])
                password.append(letters[c2])
                password.append(letters[c3])
                return@repeat
            }
        }
        var passwordLength = 3
        while (passwordLength < length) {
            val c1 = letters.indexOf(password[passwordLength - 2])
            val c2 = letters.indexOf(password[passwordLength - 1])
            sum = 0
            repeat(26) { i ->
                sum += trigram[c1, c2, i]
            }
            if (sum == 0) {
                break //exit while-loop
            }
            val innerThreshold = Random.nextDouble() * sum
            sum = 0
            repeat(26) { i ->
                sum += trigram[c1, c2, i]
                if (sum > innerThreshold) {
                    password.append(letters[i])
                    return@repeat
                }
            }
            passwordLength++

        }
        return password.toString()
    }
}