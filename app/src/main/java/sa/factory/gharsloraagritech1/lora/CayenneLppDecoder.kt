package sa.factory.gharsloraagritech1.lora

import sa.factory.gharsloraagritech1.model.SoilTelemetryPacket
import java.util.Locale

/**
 * High-Precision LoRaWAN Cayenne LPP (Low Power Payload) Binary Decoder.
 * Decodes telemetry packets from smart agricultural IoT sensor nodes across Saudi date palm farms.
 * Zero-Emoji Invariant strictly maintained.
 */
object CayenneLppDecoder {

    // Standard Cayenne LPP Sensor Types
    const val LPP_DIGITAL_OUTPUT: Byte = 0x01
    const val LPP_ANALOG_INPUT: Byte = 0x02
    const val LPP_TEMPERATURE: Byte = 0x67
    const val LPP_HUMIDITY: Byte = 0x68
    const val LPP_BAROMETER: Byte = 0x73

    /**
     * Decodes a raw binary LoRaWAN uplink frame into structured soil telemetry.
     */
    fun decodeUplink(bytes: ByteArray): SoilTelemetryPacket {
        var temp = 25.0
        var humidity = 40.0
        var soilMoisture = 30.0
        var soilSalinity = 1.2
        var valveOpen = false
        var batteryV = 3.6

        var i = 0
        while (i < bytes.size) {
            val channel = bytes[i].toInt() and 0xFF
            if (i + 1 >= bytes.size) break
            val type = bytes[i + 1]
            i += 2

            when (type) {
                LPP_DIGITAL_OUTPUT -> {
                    if (i < bytes.size) {
                        val state = bytes[i].toInt() and 0xFF
                        if (channel == 5) valveOpen = (state != 0)
                        i += 1
                    }
                }
                LPP_ANALOG_INPUT -> {
                    if (i + 1 < bytes.size) {
                        val rawVal = ((bytes[i].toInt() shl 8) or (bytes[i + 1].toInt() and 0xFF)).toShort()
                        val value = rawVal.toDouble() * 0.01
                        when (channel) {
                            3 -> soilMoisture = value
                            6 -> soilSalinity = value
                            7 -> batteryV = value
                        }
                        i += 2
                    }
                }
                LPP_TEMPERATURE -> {
                    if (i + 1 < bytes.size) {
                        val rawTemp = ((bytes[i].toInt() shl 8) or (bytes[i + 1].toInt() and 0xFF)).toShort()
                        temp = rawTemp.toDouble() * 0.1
                        i += 2
                    }
                }
                LPP_HUMIDITY -> {
                    if (i < bytes.size) {
                        val rawHum = bytes[i].toInt() and 0xFF
                        humidity = rawHum.toDouble() * 0.5
                        i += 1
                    }
                }
                else -> {
                    i += 1
                }
            }
        }

        val hexBuilder = StringBuilder()
        for (b in bytes) {
            hexBuilder.append(String.format(Locale.US, "%02X", b))
        }

        return SoilTelemetryPacket(
            temperatureCelsius = temp,
            relativeHumidityPct = humidity,
            soilMoistureVwcPct = soilMoisture,
            soilSalinityDsm = soilSalinity,
            solenoidValveOpen = valveOpen,
            batteryVoltage = batteryV,
            rawHexPayload = hexBuilder.toString()
        )
    }

    /**
     * Decodes a hexadecimal string payload.
     */
    fun decodeHexPayload(hexString: String): SoilTelemetryPacket {
        val clean = hexString.replace(" ", "").trim()
        val len = clean.length
        val data = ByteArray(len / 2)
        var i = 0
        while (i < len) {
            data[i / 2] = ((Character.digit(clean[i], 16) shl 4) + Character.digit(clean[i + 1], 16)).toByte()
            i += 2
        }
        return decodeUplink(data)
    }

    /**
     * Encodes a LoRaWAN downlink command packet to actuate the solenoid valve relay.
     * Channel: 5, Type: 0x01 (Digital Output), Value: 0x01 (Open) / 0x00 (Close).
     */
    fun encodeValveDownlink(openValve: Boolean): ByteArray {
        val stateByte = if (openValve) 1.toByte() else 0.toByte()
        return byteArrayOf(0x05, LPP_DIGITAL_OUTPUT, stateByte)
    }
}
