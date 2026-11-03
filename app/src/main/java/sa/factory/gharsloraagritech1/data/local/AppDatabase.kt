package sa.factory.gharsloraagritech1.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [GharsLoraEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun agritechDao(): GharsLoraDao
}
