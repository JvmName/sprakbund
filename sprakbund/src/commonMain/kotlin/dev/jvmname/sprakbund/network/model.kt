package dev.jvmname.sprakbund.network

import kotlinx.serialization.Serializable


@Serializable(with = HibpLineSerializer::class)
data class HibpLine(
    val hash: String,
    val count: Int
)