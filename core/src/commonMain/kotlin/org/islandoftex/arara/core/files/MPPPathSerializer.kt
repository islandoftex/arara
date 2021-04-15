// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.files

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.islandoftex.arara.api.files.MPPPath

@Serializer(forClass = MPPPath::class)
internal object MPPPathSerializer : KSerializer<MPPPath> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("MPPPath", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: MPPPath) = encoder.encodeString(value.toString())
    override fun deserialize(decoder: Decoder): MPPPath = MPPPath(decoder.decodeString())
}
