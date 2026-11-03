package sa.factory.gharsloraagritech1.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import sa.factory.gharsloraagritech1.model.GharsLoraItem

@Entity(tableName = "agritech_items_cache")
data class GharsLoraEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val priceSar: Double,
    val badge: String,
    val category: String,
    val status: String
) {
    fun toDomain(): GharsLoraItem = GharsLoraItem(
        id = id,
        title = title,
        description = description,
        priceSar = priceSar,
        badge = badge,
        category = category,
        status = status
    )
}
