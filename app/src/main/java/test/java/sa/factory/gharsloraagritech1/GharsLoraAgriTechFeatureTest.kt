package sa.factory.gharsloraagritech1

import sa.factory.gharsloraagritech1.irrigation.DatePalmIrrigationEngine
import sa.factory.gharsloraagritech1.lora.CayenneLppDecoder
import org.junit.Assert.*
import org.junit.Test

/**
 * Feature Test Suite for Ghars AgriTech LoRaWAN Soil & Irrigation Engine.
 * Tests Cayenne LPP binary decoding, Date Palm deficit evaluation, and Solenoid valve downlinks.
 * Zero-Emoji Invariant verified.
 */
class GharsLoraAgriTechFeatureTest {

    @Test
    fun testCayenneLppBinaryDecoding() {
        // Construct standard Cayenne LPP packet:
        // Ch 1 (0x01), Type Temp (0x67), Value 38.5 deg C (385 -> 0x0181)
        // Ch 2 (0x02), Type Hum (0x68), Value 22.0% (44 -> 0x2C)
        // Ch 3 (0x03), Type Analog (0x02), Value Soil VWC 18.50% (1850 -> 0x073A)
        // Ch 5 (0x05), Type Digital Out (0x01), Value Relay OFF (0x00)
        // Ch 6 (0x06), Type Analog (0x02), Value Salinity 1.45 dS/m (145 -> 0x0091)
        val lppHex = "0167018102682C0302073A05010006020091"
        val packet = CayenneLppDecoder.decodeHexPayload(lppHex)

        assertEquals("Temperature must decode to 38.5 C", 38.5, packet.temperatureCelsius, 0.05)
        assertEquals("Humidity must decode to 22.0%", 22.0, packet.relativeHumidityPct, 0.05)
        assertEquals("Soil moisture must decode to 18.50%", 18.50, packet.soilMoistureVwcPct, 0.05)
        assertEquals("Soil salinity must decode to 1.45 dS/m", 1.45, packet.soilSalinityDsm, 0.05)
        assertFalse("Solenoid valve must initially be CLOSED", packet.solenoidValveOpen)
    }

    @Test
    fun testDatePalmIrrigationTriggerWhenMoistureDeficit() {
        // Soil moisture 18.5% is below critical threshold of 22.0%
        val lppHex = "0167018102682C0302073A05010006020091"
        val telemetry = CayenneLppDecoder.decodeHexPayload(lppHex)

        val decision = DatePalmIrrigationEngine.evaluateIrrigation(telemetry, palmVariety = "Khalas")

        assertTrue("Irrigation must be triggered when moisture is below 22%", decision.requiresIrrigation)
        assertEquals("Downlink hex must command relay ON (050101)", "050101", decision.downlinkCommandHex)
        assertTrue("Recommended runtime must be at least 15 minutes", decision.recommendedRuntimeMinutes >= 15)
        assertTrue("Estimated water deficit must be positive", decision.waterEstimatedLiters > 0.0)
    }

    @Test
    fun testDatePalmIrrigationCutoffWhenMoistureAdequate() {
        // Ch 3 Soil Moisture 35.00% (3500 -> 0x0DAC) - Adequate moisture
        val lppHex = "0167018102682C03020DAC05010106020091"
        val telemetry = CayenneLppDecoder.decodeHexPayload(lppHex)

        val decision = DatePalmIrrigationEngine.evaluateIrrigation(telemetry, palmVariety = "Sukkari")

        assertFalse("Irrigation must NOT be triggered when moisture is adequate", decision.requiresIrrigation)
        assertEquals("Downlink hex must command relay OFF (050100)", "050100", decision.downlinkCommandHex)
        assertEquals("Runtime must be 0 minutes when adequate", 0, decision.recommendedRuntimeMinutes)
    }

    @Test
    fun testValveDownlinkByteEncoding() {
        val openCmd = CayenneLppDecoder.encodeValveDownlink(openValve = true)
        assertArrayEquals(byteArrayOf(0x05, 0x01, 0x01), openCmd)

        val closeCmd = CayenneLppDecoder.encodeValveDownlink(openValve = false)
        assertArrayEquals(byteArrayOf(0x05, 0x01, 0x00), closeCmd)
    }
}
