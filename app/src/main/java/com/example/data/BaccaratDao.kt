package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BaccaratDao {

    @Query("SELECT * FROM history_shoes ORDER BY timestamp DESC")
    fun getAllHistoryShoes(): Flow<List<HistoryShoe>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistoryShoe(shoe: HistoryShoe)

    @Query("DELETE FROM history_shoes WHERE id = :id")
    suspend fun deleteHistoryShoeById(id: Int)

    @Query("DELETE FROM history_shoes")
    suspend fun clearHistoryShoes()

    @Query("SELECT * FROM active_shoe_state WHERE id = 1 LIMIT 1")
    fun getActiveShoeStateFlow(): Flow<ActiveShoeState?>

    @Query("SELECT * FROM active_shoe_state WHERE id = 1 LIMIT 1")
    suspend fun getActiveShoeState(): ActiveShoeState?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActiveShoeState(state: ActiveShoeState)

    @Query("DELETE FROM active_shoe_state WHERE id = 1")
    suspend fun deleteActiveShoe()
}
