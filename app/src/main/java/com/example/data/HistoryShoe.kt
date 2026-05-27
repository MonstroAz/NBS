package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history_shoes")
data class HistoryShoe(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val baseBet: Double,
    val initialTotalBankroll: Double,
    val endingTotalBankroll: Double,
    val netWinLoss: Double,
    val roundsCount: Int,
    val decisionsHistory: String, // comma-separated values, e.g., "P,B,T,P,P"
    val isWinGoalMet: Boolean,
    val strategyType: String = "FiveCount"
)
