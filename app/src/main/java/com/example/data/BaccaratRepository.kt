package com.example.data

import kotlinx.coroutines.flow.Flow

class BaccaratRepository(private val dao: BaccaratDao) {

    val allHistoryShoes: Flow<List<HistoryShoe>> = dao.getAllHistoryShoes()
    val activeShoeStateFlow: Flow<ActiveShoeState?> = dao.getActiveShoeStateFlow()

    suspend fun getActiveShoeState(): ActiveShoeState? = dao.getActiveShoeState()

    suspend fun saveHistoryShoe(shoe: HistoryShoe) = dao.insertHistoryShoe(shoe)

    suspend fun deleteHistoryShoeById(id: Int) = dao.deleteHistoryShoeById(id)

    suspend fun clearAllHistoryShoes() = dao.clearHistoryShoes()

    suspend fun saveActiveShoeState(state: ActiveShoeState) = dao.insertActiveShoeState(state)

    suspend fun deleteActiveShoeState() = dao.deleteActiveShoe()
}
