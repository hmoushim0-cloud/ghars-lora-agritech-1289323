package sa.factory.gharsloraagritech1.data.repository

import sa.factory.gharsloraagritech1.domain.repository.GharsLoraRepository
import sa.factory.gharsloraagritech1.hardware.HardwareSensorDriver
import sa.factory.gharsloraagritech1.model.GharsLoraItem
import sa.factory.gharsloraagritech1.model.GharsLoraOrder
import sa.factory.gharsloraagritech1.model.GharsLoraTelemetry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class GharsLoraRepositoryImpl(
    private val sensorDriver: HardwareSensorDriver
) : GharsLoraRepository {

    private val _catalog = MutableStateFlow<List<GharsLoraItem>>(listOf(
        GharsLoraItem("1", "تصميم شبكة ري متكاملة", "تصميم نظام ري متكامل مع خرائط توزيع الرشاشات والتنقيط باستخدام خوارزميات توفير المياه", 150.0, "معتمد", "تصميم شبكات الري"),
        GharsLoraItem("2", "توريد وتركيب صمامات Solenoid", "صمامات كهرومغناطيسية ذكية مع التحكم عن بعد عبر LoRaWAN لضبط تدفق المياه", 280.0, "فوري", "اختيار الأشجار والنباتات"),
        GharsLoraItem("3", "تحليل رطوبة التربة عبر LoRaWAN", "أجهزة استشعار رطوبة التربة تعمل بتقنية LoRaWAN مع تقارير فورية عبر التطبيق", 95.0, "جديد", "أنظمة الإنارة الذكية")
    ))
    override val catalogFlow: Flow<List<GharsLoraItem>> = _catalog.asStateFlow()

    private val _cart = MutableStateFlow<List<GharsLoraItem>>(emptyList())
    override val activeCart: StateFlow<List<GharsLoraItem>> = _cart.asStateFlow()

    override val telemetryFlow: Flow<GharsLoraTelemetry> = sensorDriver.observePeripheralStream()
        .map { frame ->
            GharsLoraTelemetry(
                peripheralName = frame.peripheralName,
                status = frame.connectionState,
                liveReading = frame.livePayload,
                batteryPct = 96,
                signalDbm = -54
            )
        }

    override fun addToCart(item: GharsLoraItem) {
        val current = _cart.value.toMutableList()
        current.add(item)
        _cart.value = current
    }

    override fun removeFromCart(item: GharsLoraItem) {
        val current = _cart.value.toMutableList()
        current.remove(item)
        _cart.value = current
    }

    override fun clearCart() {
        _cart.value = emptyList()
    }

    override suspend fun checkout(paymentMethod: String): GharsLoraOrder {
        val currentItems = _cart.value.toList()
        val subtotal = currentItems.sumOf { it.priceSar }
        val vat = subtotal * 0.15
        val total = subtotal + vat
        val order = GharsLoraOrder(
            orderId = "ORD-${System.currentTimeMillis() % 100000}",
            items = currentItems,
            subtotalSar = subtotal,
            vatSar = vat,
            totalSar = total,
            paymentMethod = paymentMethod
        )
        clearCart()
        return order
    }

    override suspend fun refreshCatalog() {
        // Offline-first sync logic
    }
}
