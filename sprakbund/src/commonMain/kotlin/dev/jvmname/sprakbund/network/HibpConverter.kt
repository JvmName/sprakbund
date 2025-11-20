package dev.jvmname.sprakbund.network

import io.ktor.http.ContentType
import io.ktor.http.content.OutgoingContent
import io.ktor.serialization.Configuration
import io.ktor.serialization.ContentConverter
import io.ktor.util.reflect.TypeInfo
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.charsets.Charset
import io.ktor.utils.io.readRemaining
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.readString
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.StringFormat
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.AbstractDecoder
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import java.util.Locale

fun Configuration.hibp(
    hibp: HibpFormat = HibpFormat.Default,
    contentType: ContentType = ContentType.Text.Plain

) {
    register(contentType, HibpContentConverter(hibp))
}

class HibpContentConverter(private val hibp: HibpFormat) : ContentConverter {
    override suspend fun serialize(
        contentType: ContentType,
        charset: Charset,
        typeInfo: TypeInfo,
        value: Any?
    ): OutgoingContent = TODO("Not yet implemented")

    override suspend fun deserialize(
        charset: Charset,
        typeInfo: TypeInfo,
        content: ByteReadChannel
    ): Any {
        return withContext(Dispatchers.IO) {
            val input = content.readRemaining().readString()
            hibp.decodeFromString(HibpListSerializer(HibpLineSerializer()), input)
        }
    }
}

class HibpFormat : StringFormat {
    companion object {
        val Default = HibpFormat()
    }

    override val serializersModule: SerializersModule = SerializersModule {}

    override fun <T> encodeToString(
        serializer: SerializationStrategy<T>,
        value: T
    ): String = TODO("Not yet implemented")

    override fun <T> decodeFromString(
        deserializer: DeserializationStrategy<T>,
        string: String
    ): T = deserializer.deserialize(HibpDecoder(string.trim().lines()))
}

/**
 * Decodes a collection of HIBP lines
 * Filters out empty lines and creates HibpLineDecoder for each valid line
 */
@OptIn(ExperimentalSerializationApi::class)
class HibpDecoder(
    private val lines: List<String>
) : AbstractDecoder() {
    private var currentIndex = 0

    override val serializersModule: SerializersModule = SerializersModule {}

    override fun decodeSequentially() = true

    override fun decodeCollectionSize(descriptor: SerialDescriptor): Int = lines.size

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        return if (currentIndex < lines.size) currentIndex++ else CompositeDecoder.DECODE_DONE
    }

    override fun <T> decodeSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        deserializer: DeserializationStrategy<T>,
        previousValue: T?
    ): T = deserializer.deserialize(HibpLineDecoder(lines[index]))
}

/**
 * Decodes a single line in the format "HASH:COUNT"
 * Used by HibpSerializer to extract hash and count values
 */
class HibpLineDecoder(
    private val line: String
) : AbstractDecoder() {
    private companion object {
        val colon = Regex(":")
    }

    private val parts: List<String> = line.split(colon)
        .also { require(it.size == 2) { "Malformed line: expected 'HASH:COUNT' format, got '$line'" } }

    private var elementIndex = -1

    override fun decodeSequentially(): Boolean = true

    override val serializersModule = EmptySerializersModule()

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        if (elementIndex >= descriptor.elementsCount) return CompositeDecoder.DECODE_DONE
        return elementIndex++
    }

    override fun decodeString(): String = parts[0].trim().uppercase(Locale.US)

    override fun decodeInt(): Int = try {
        parts[1].toInt()
    } catch (e: NumberFormatException) {
        throw IllegalArgumentException("Invalid count value in HIBP line: '${parts[1]}'", e)
    }
}


@OptIn(ExperimentalSerializationApi::class)
class HibpListSerializer(private val lineSerializer: HibpLineSerializer) : KSerializer<List<HibpLine>> {
    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("dev.jvmname.hibp", lineSerializer.descriptor) {
            element("lines", ListSerializer(lineSerializer).descriptor)
        }

    override fun serialize(encoder: Encoder, value: List<HibpLine>) = TODO("Not yet implemented")

    override fun deserialize(decoder: Decoder): List<HibpLine> {
        return decoder.decodeStructure(descriptor) {
            require(decodeSequentially())
            val size = decodeCollectionSize(descriptor)
            require(size >= 0) { "Size must be known in advance" }
            List(size) { i ->
                decodeSerializableElement(descriptor, i, lineSerializer)
            }
        }
    }
}

class HibpLineSerializer : KSerializer<HibpLine> {
    override val descriptor = buildClassSerialDescriptor("dev.jvmname.hibp.line") {
        element<String>("name")
        element<Int>("count")
    }

    override fun serialize(
        encoder: Encoder,
        value: HibpLine
    ) = TODO("Not yet implemented")

    override fun deserialize(decoder: Decoder): HibpLine {
        return decoder.decodeStructure(descriptor) {
            require(decodeSequentially())
            HibpLine(
                hash = decodeStringElement(descriptor, 0),
                count = decodeIntElement(descriptor, 1)
            )
        }

    }
}