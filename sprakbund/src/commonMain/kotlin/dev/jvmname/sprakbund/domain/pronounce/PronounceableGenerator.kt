// full generator in kotlin

package dev.jvmname.sprakbund.domain.pronounce

import dev.zacsweers.metro.Inject
import java.security.SecureRandom
import kotlin.random.Random

/** Original sources: https://www.multicians.org/thvv/gpw.js
 * Major changes:
 * 1. using secure random
 * 2. char arithmetic rather than indexing into an "abcd" string
 * 3. memoized cumulative sums into an array, using `binarySearch` for lookupv
 * */
@Inject
class PronounceableGenerator(private val trigram: Trigram) {
    private val random = SecureRandom()

    /*
    * the original sources had a 3-deep nested for-loop that each counted to 26. I achieved the same result (26^3= 17576)
    *  with much less unnecessary nesting. I've also memoized it, which uses a little bit more memory but saves
    * on repeatedly calculating the exact same value for each subsequent word generation
    */
    private val cumulativeSums: IntArray by lazy(LazyThreadSafetyMode.NONE) {
        var sum = 0
        IntArray(17576) { i ->
            val c1 = i / 676  // 26^2
            val c2 = (i / 26) % 26
            val c3 = i % 26
            sum += trigram[c1, c2, c3]
            sum
        }
    }

    //weight by sum of frequencies; TVV refers to this as "sigma" in the java code, which I liked
    private val sigma by lazy(LazyThreadSafetyMode.NONE) { cumulativeSums.last() }

    fun generate(length: Int): String {
        val threshold = (random.nextDouble() * sigma).toInt()
        val password = StringBuilder(length)
        /*
        * turning binary search into a boundary search; since the value never exists
        * the comparator will never return 0 so binsearch will return the "insertion point" `-(low+1)`,
        * which needs to be converted to a positive index
        */
        val index = cumulativeSums.asList()
            .binarySearch { if (it <= threshold) -1 else 1 }
            .let { if (it < 0) -(it + 1) else it }
        password.append('a' + index / 676) //c1
        password.append('a' + (index / 26) % 26) //c2
        val c3 = index % 26
        password.append('a' + c3)

        var passwordLength = 3
        while (passwordLength < length) {
            val c1 = password[passwordLength - 2] - 'a'
            val c2 = password[passwordLength - 1] - 'a'
            var sum = 0
            repeat(26) { i ->
                sum += trigram[c1, c2, i]
            }
            if (sum == 0) {
                break //exit while-loop
            }
//            val innerThreshold = random.nextDouble() * sum
            val innerThreshold = Random.nextDouble() * sum
            sum = 0
            for (i in 0..<26) {
                sum += trigram[c1, c2, i]
                if (sum > innerThreshold) {
                    password.append('a' + i)
                    break
                }
            }
            passwordLength++
        }
        return password.toString()
    }
}