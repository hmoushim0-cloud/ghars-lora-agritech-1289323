package sa.factory.gharsloraagritech1.hardware

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import sa.factory.gharsloraagritech1.irrigation.DatePalmIrrigationEngine
import sa.factory.gharsloraagritech1.lora.CayenneLppDecoder
import java.util.Locale
import kotlin.random.Random

data class HardwareTelemetryFrame(
    val peripheralName: String,
    val connectionState: String,
    val livePayload: String,
    val timestamp: Long = System.currentTimeMillis()
)

class HardwareSensorDriver {
    private val peripheral = "LoRaWAN Soil Moisture Node + Solenoid Valve Relays"

    // Real Cayenne LPP hex frames simulating typical Saudi date palm orchard conditions
    private val simulatedHexFrames = listOf(
        "0167018102682C0302073A05010006020091", // 38.5C, 22% Hum, 18.5% VWC (Deficit -> Valve open)
        "01670177026832030208340501010602008C", // 37.5C, 25% Hum, 21.0% VWC (Deficit -> Valve open)
        "0167015E02683C03020BB805010106020082", // 35.0C, 30% Hum, 30.0% VWC (Adequate -> Valve close)
        "0167014002684603020FA00501000602007D"  // 32.0C, 35% Hum, 40.0% VWC (Field capacity -> Valve close)
    )

    fun observePeripheralStream(): Flow<HardwareTelemetryFrame> = flow {
        emit(HardwareTelemetryFrame(peripheral, "CONNECTING", "LoRaWAN OTAA Handshake initializing..."))
        delay(300)
        emit(HardwareTelemetryFrame(peripheral, "ONLINE", "LoRaWAN Join Accept received | DevEUI: 70B3D57ED005A123 | Channel: 868.1 MHz"))

        var frameIndex = 0
        while (true) {
            delay(1500)
            val hex = simulatedHexFrames[frameIndex % simulatedHexFrames.size]
            val packet = CayenneLppDecoder.decodeHexPayload(hex)
            val decision = DatePalmIrrigationEngine.evaluateIrrigation(packet, palmVariety = "Khalas")

            val valveStatus = if (decision.requiresIrrigation) "OPEN (${decision.recommendedRuntimeMinutes}m)" else "CLOSED"
            val formattedTelemetry = String.format(
                Locale.US,
                "Cayenne LPP: %s | Temp: %.1fC | VWC: %.1f%% | Salinity: %.2f dS/m | Valve: %s",
                hex.substring(0, 16),
                packet.temperatureCelsius,
                packet.soilMoistureVwcPct,
                packet.soilSalinityDsm,
                valveStatus
            )

            emit(HardwareTelemetryFrame(peripheral, "ACTIVE_STREAMING", formattedTelemetry))
            frameIndex++
        }
    }
}
