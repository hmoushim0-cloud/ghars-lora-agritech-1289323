package sa.factory.gharsloraagritech1.irrigation

import sa.factory.gharsloraagritech1.lora.CayenneLppDecoder
import sa.factory.gharsloraagritech1.model.IrrigationDecision
import sa.factory.gharsloraagritech1.model.SoilTelemetryPacket
import java.util.Locale

/**
 * Precision Date Palm Evapotranspiration & Irrigation Decision Engine.
 * Tailored for Saudi date palm varieties (Khalas, Sukkari, Ajwa).
 * Zero-Emoji Invariant strictly maintained.
 */
object DatePalmIrrigationEngine {

    // Crop Coefficients (Kc) for Saudi Date Palm varieties (FAO-56 Irrigation standard)
    const val KC_KHALAS = 0.85 // Al-Ahsa
    const val KC_SUKKARI = 0.90 // Al-Qassim
    const val KC_AJWA = 0.88 // Madinah

    // Soil Moisture Volumetric Water Content (VWC) Thresholds for sandy-loam soils
    const val CRITICAL_MOISTURE_THRESHOLD_PCT = 22.0
    const val FIELD_CAPACITY_TARGET_PCT = 42.0

    // Standard Drip Emitter Flow Rate (Liters per Hour)
    const val DRIP_FLOW_RATE_LPH = 60.0

    /**
     * Resolves crop coefficient Kc based on palm variety.
     */
    fun getCropCoefficient(variety: String): Double {
        return when (variety.toLowerCase(Locale.US)) {
            "sukkari", "سكري" -> KC_SUKKARI
            "ajwa", "عجوة" -> KC_AJWA
            else -> KC_KHALAS // Default Khalas
        }
    }

    /**
     * Evaluates live telemetry and computes automated irrigation decision.
     */
    fun evaluateIrrigation(
        telemetry: SoilTelemetryPacket,
        palmVariety: String = "Khalas"
    ): IrrigationDecision {
        val currentMoisture = telemetry.soilMoistureVwcPct
        val requiresIrrigation = currentMoisture < CRITICAL_MOISTURE_THRESHOLD_PCT

        var runtimeMinutes = 0
        var waterDeficitLiters = 0.0
        val downlinkHex: String

        if (requiresIrrigation) {
            val moistureDeficitPct = FIELD_CAPACITY_TARGET_PCT - currentMoisture
            val kc = getCropCoefficient(palmVariety)

            // Approximate root zone volume (1m radius, 0.8m depth = ~250 Liters soil volume)
            val soilVolumeLiters = 250.0
            waterDeficitLiters = (moistureDeficitPct / 100.0) * soilVolumeLiters * kc

            // Calculate runtime in minutes from drip emitter flow rate
            runtimeMinutes = ((waterDeficitLiters / DRIP_FLOW_RATE_LPH) * 60.0).toInt().coerceIn(15, 120)

            val downlinkBytes = CayenneLppDecoder.encodeValveDownlink(openValve = true)
            downlinkHex = downlinkBytes.joinToString("") { String.format(Locale.US, "%02X", it) }
        } else {
            val downlinkBytes = CayenneLppDecoder.encodeValveDownlink(openValve = false)
            downlinkHex = downlinkBytes.joinToString("") { String.format(Locale.US, "%02X", it) }
        }

        return IrrigationDecision(
            datePalmVariety = palmVariety,
            currentMoisturePct = currentMoisture,
            thresholdMoisturePct = CRITICAL_MOISTURE_THRESHOLD_PCT,
            requiresIrrigation = requiresIrrigation,
            recommendedRuntimeMinutes = runtimeMinutes,
            downlinkCommandHex = downlinkHex,
            waterEstimatedLiters = waterDeficitLiters
        )
    }
}
