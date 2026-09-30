package com.example.tvremote.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.tvremote.data.local.dao.DeviceDao
import com.example.tvremote.data.local.dao.MacroDao
import com.example.tvremote.data.local.entity.DeviceEntity
import com.example.tvremote.data.local.entity.MacroEntity
import com.example.tvremote.domain.model.RemoteMacro
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [DeviceEntity::class, MacroEntity::class],
    version = 1,
    exportSchema = false
)
abstract class TvDatabase : RoomDatabase() {
    abstract fun deviceDao(): DeviceDao
    abstract fun macroDao(): MacroDao

    companion object {
        @Volatile
        private var INSTANCE: TvDatabase? = null

        fun getDatabase(context: Context): TvDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TvDatabase::class.java,
                    "tv_remote_database"
                )
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Seed default preset macros
                        CoroutineScope(Dispatchers.IO).launch {
                            val dao = getDatabase(context).macroDao()
                            val presets = RemoteMacro.defaultPresets().map { MacroEntity.fromDomain(it) }
                            dao.insertAll(presets)
                        }
                    }
                })
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
