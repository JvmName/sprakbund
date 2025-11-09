package dev.jvmname.sprakbund

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform