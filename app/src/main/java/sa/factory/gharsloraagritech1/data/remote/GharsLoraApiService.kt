package sa.factory.gharsloraagritech1.data.remote

import sa.factory.gharsloraagritech1.model.GharsLoraItem
import sa.factory.gharsloraagritech1.model.GharsLoraOrder

interface GharsLoraApiService {
    suspend fun fetchCatalog(): List<GharsLoraItem>
    suspend fun submitOrder(order: GharsLoraOrder): Boolean
}
