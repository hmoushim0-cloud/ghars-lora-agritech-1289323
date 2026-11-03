package sa.factory.gharsloraagritech1.model

import java.io.Serializable

data class GharsLoraItem(
    val id: String,
    val title: String,
    val description: String,
    val priceSar: Double,
    val badge: String = "معتمد",
    val category: String = "عام",
    val status: String = "AVAILABLE"
) : Serializable

data class GharsLoraOrder(
    val orderId: String,
    val items: List<GharsLoraItem>,
    val subtotalSar: Double,
    val vatSar: Double,
    val totalSar: Double,
    val paymentMethod: String = "Mada Contactless EMV",
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "CONFIRMED"
) : Serializable

data class GharsLoraTelemetry(
    val peripheralName: String,
    val status: String,
    val liveReading: String,
    val batteryPct: Int = 98,
    val signalDbm: Int = -58,
    val timestamp: Long = System.currentTimeMillis()
) : Serializable

sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
    object Empty : UiState<Nothing>()
}

data class SoilTelemetryPacket(
    val temperatureCelsius: Double,
    val relativeHumidityPct: Double,
    val soilMoistureVwcPct: Double,
    val soilSalinityDsm: Double,
    val solenoidValveOpen: Boolean,
    val batteryVoltage: Double,
    val rawHexPayload: String
) : Serializable

data class IrrigationDecision(
    val datePalmVariety: String,
    val currentMoisturePct: Double,
    val thresholdMoisturePct: Double,
    val requiresIrrigation: Boolean,
    val recommendedRuntimeMinutes: Int,
    val downlinkCommandHex: String,
    val waterEstimatedLiters: Double
) : Serializable
