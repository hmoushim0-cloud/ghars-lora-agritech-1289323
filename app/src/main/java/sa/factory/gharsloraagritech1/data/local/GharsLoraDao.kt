package sa.factory.gharsloraagritech1.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GharsLoraDao {
    @Query("SELECT * FROM agritech_items_cache ORDER BY id ASC")
    fun observeAllItems(): Flow<List<GharsLoraEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<GharsLoraEntity>)

    @Query("DELETE FROM agritech_items_cache")
    suspend fun clearAll()
}
