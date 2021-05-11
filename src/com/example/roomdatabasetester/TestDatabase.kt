package com.example.roomdatabasetester

import android.content.Context
import androidx.room.*
import com.android.internal.annotations.GuardedBy

const val CURRENT_DB_VERSION = 1

@Database(
    entities = [AppInfo::class],
    version = CURRENT_DB_VERSION,
    exportSchema = false
)
abstract class TestDatabase : RoomDatabase() {
    abstract fun appInfoDao(): AppInfoDao

    override fun close() {
        synchronized(TestDatabase::class) {
            super.close()
            instance = null
        }
    }

    companion object {
        private const val DATABASE_NAME = "roomdatabasetest.db"
        @GuardedBy("TestDatabase::class")
        @Volatile
        private var instance: TestDatabase? = null

        fun getInstance(context: Context): TestDatabase =
            instance ?: synchronized(TestDatabase::class) {
                instance ?: buildDatabaseInstance(context).also { instance = it }
            }

        private fun buildDatabaseInstance(context: Context): TestDatabase =
            Room.databaseBuilder(context, TestDatabase::class.java, DATABASE_NAME)
                .fallbackToDestructiveMigrationOnDowngrade()
                .build()
    }
}

@Dao
interface AppInfoDao {
    @Transaction
    suspend fun updateOrInsert(appInfo: AppInfo) {
        if (update(appInfo) <= 0) {
            insert(appInfo)
        }
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(appInfo: AppInfo)

    /**
     * @return The number of rows updated.
     */
    @Update
    suspend fun update(appInfo: AppInfo): Int

    @Query("SELECT * FROM AppInfo WHERE packageName = :packageName")
    suspend fun getAppInfo(packageName: String): AppInfo?
}

@Entity
data class AppInfo(
    @ColumnInfo @PrimaryKey
    val packageName: String,
    @ColumnInfo
    val versionCode: Int
)
