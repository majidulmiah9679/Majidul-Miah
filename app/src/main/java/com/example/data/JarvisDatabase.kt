package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sender: String, // "USER" or "JARVIS"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val protocol: String = "GENERAL",
    val webhookInfo: String? = null
)

@Entity(tableName = "webhook_logs")
data class WebhookLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val url: String,
    val method: String,
    val payload: String,
    val statusCode: Int,
    val responseBody: String,
    val latencyMs: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val isSuccess: Boolean
)

@Dao
interface JarvisDao {
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage): Long

    @Query("DELETE FROM chat_messages")
    suspend fun clearAllMessages()

    @Query("SELECT * FROM webhook_logs ORDER BY timestamp DESC LIMIT 50")
    fun getWebhookLogs(): Flow<List<WebhookLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWebhookLog(log: WebhookLog): Long

    @Query("DELETE FROM webhook_logs")
    suspend fun clearWebhookLogs()
}

@Database(
    entities = [ChatMessage::class, WebhookLog::class],
    version = 1,
    exportSchema = false
)
abstract class JarvisDatabase : RoomDatabase() {
    abstract fun jarvisDao(): JarvisDao

    companion object {
        @Volatile
        private var INSTANCE: JarvisDatabase? = null

        fun getDatabase(context: Context): JarvisDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    JarvisDatabase::class.java,
                    "jarvis_assistant_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class JarvisRepository(private val dao: JarvisDao) {
    val messages: Flow<List<ChatMessage>> = dao.getAllMessages()
    val webhookLogs: Flow<List<WebhookLog>> = dao.getWebhookLogs()

    suspend fun addMessage(message: ChatMessage): Long = dao.insertMessage(message)
    suspend fun clearMessages() = dao.clearAllMessages()

    suspend fun logWebhook(log: WebhookLog): Long = dao.insertWebhookLog(log)
    suspend fun clearLogs() = dao.clearWebhookLogs()
}
