package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "active_shoe_state")
data class ActiveShoeState(
    @PrimaryKey val id: Int = 1, // Always 1
    val baseBet: Double = 5.0,
    val totalBankroll: Double = 450.0,
    val initialTotalBankroll: Double = 450.0,
    val betType: String = "Observe",
    val level: Int = 1,
    val profitIndex: Int = 0,
    val currentPattern: String = "R",
    val consecutiveLossesR: Int = 0,
    val decisionsHistory: String = "", // comma separated "Player", "Banker", "Tie"
    val winLossHistory: String = "", // comma separated "1" (true) or "0" (false)
    val gameProfit: Double = 0.0,
    val isActive: Boolean = false,
    val useBankerCommission: Boolean = true,
    val strategyType: String = "FiveCount"
)
