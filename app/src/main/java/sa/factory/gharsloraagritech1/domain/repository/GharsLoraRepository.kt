package sa.factory.gharsloraagritech1.domain.repository

import sa.factory.gharsloraagritech1.model.GharsLoraItem
import sa.factory.gharsloraagritech1.model.GharsLoraOrder
import sa.factory.gharsloraagritech1.model.GharsLoraTelemetry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface GharsLoraRepository {
    val catalogFlow: Flow<List<GharsLoraItem>>
    val activeCart: StateFlow<List<GharsLoraItem>>
    val telemetryFlow: Flow<GharsLoraTelemetry>
    
    fun addToCart(item: GharsLoraItem)
    fun removeFromCart(item: GharsLoraItem)
    fun clearCart()
    suspend fun checkout(paymentMethod: String): GharsLoraOrder
    suspend fun refreshCatalog()
}
