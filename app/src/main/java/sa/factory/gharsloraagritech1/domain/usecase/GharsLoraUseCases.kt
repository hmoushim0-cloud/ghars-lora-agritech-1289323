package sa.factory.gharsloraagritech1.domain.usecase

import sa.factory.gharsloraagritech1.domain.repository.GharsLoraRepository
import sa.factory.gharsloraagritech1.model.GharsLoraItem
import sa.factory.gharsloraagritech1.model.GharsLoraOrder
import sa.factory.gharsloraagritech1.model.GharsLoraTelemetry
import kotlinx.coroutines.flow.Flow

class GetCatalogUseCase(private val repository: GharsLoraRepository) {
    operator fun invoke(): Flow<List<GharsLoraItem>> = repository.catalogFlow
}

class ManageCartUseCase(private val repository: GharsLoraRepository) {
    val cart = repository.activeCart
    fun add(item: GharsLoraItem) = repository.addToCart(item)
    fun remove(item: GharsLoraItem) = repository.removeFromCart(item)
    fun clear() = repository.clearCart()
}

class ProcessCheckoutUseCase(private val repository: GharsLoraRepository) {
    suspend operator fun invoke(paymentMethod: String = "Mada Contactless EMV"): GharsLoraOrder =
        repository.checkout(paymentMethod)
}

class ObserveHardwareTelemetryUseCase(private val repository: GharsLoraRepository) {
    operator fun invoke(): Flow<GharsLoraTelemetry> = repository.telemetryFlow
}
