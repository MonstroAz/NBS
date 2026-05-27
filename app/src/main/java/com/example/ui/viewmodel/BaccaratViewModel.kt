package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ActiveShoeState
import com.example.data.AppDatabase
import com.example.data.BaccaratRepository
import com.example.data.HistoryShoe
import com.example.model.BaccaratEngine
import com.example.model.StrategyType
import com.example.model.BettingState
import com.example.model.Decision
import com.example.model.Pattern
import com.example.model.BetType
import com.example.model.SystemConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

data class RoundRecord(
    val roundNumber: Int,
    val decision: Decision,
    val prediction: Decision?,
    val betAmount: Double,
    val outcome: String, // "Win", "Loss", "Push (Tie)", "Observe"
    val netProfit: Double,
    val runningTotal: Double,
    val betTypeName: String,
    val patternName: String
)

data class UiState(
    val isLoading: Boolean = true,
    val isInShoe: Boolean = false,
    val baseBet: Double = 5.0,
    val totalBankroll: Double = 450.0,
    val initialTotalBankroll: Double = 450.0,
    val currentShoeProfit: Double = 0.0,
    val currentPattern: Pattern = Pattern.R,
    val consecutiveLossesR: Int = 0,
    val betType: BetType = BetType.Observe,
    val level: Int = 1,
    val profitIndex: Int = 0,
    val decisionsHistory: List<Decision> = emptyList(),
    val roundRecords: List<RoundRecord> = emptyList(),
    val isWinGoalMet: Boolean = false,
    val useBankerCommission: Boolean = true,
    val showOnboarding: Boolean = false,
    val strategyType: StrategyType = StrategyType.FiveCount,
    val isPortuguese: Boolean = false
)

class BaccaratViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: BaccaratRepository
    private val sharedPrefs = application.getSharedPreferences("baccarat_prefs", android.content.Context.MODE_PRIVATE)
    
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _historyShoes = MutableStateFlow<List<HistoryShoe>>(emptyList())
    val historyShoes: StateFlow<List<HistoryShoe>> = _historyShoes.asStateFlow()

    init {
        val db = AppDatabase.getDatabase(application)
        repository = BaccaratRepository(db.baccaratDao())
        
        // Listen to history shoes changes
        viewModelScope.launch {
            repository.allHistoryShoes.collect { list ->
                _historyShoes.value = list
            }
        }

        // Initialize and load any active in-game shoe state on startup
        viewModelScope.launch {
            val active = repository.getActiveShoeState()
            val hasSeenOnboarding = sharedPrefs.getBoolean("has_seen_onboarding", false)
            val isPortuguese = sharedPrefs.getBoolean("is_portuguese", false)
            val preferredStrategyName = sharedPrefs.getString("preferred_strategy", "FiveCount") ?: "FiveCount"
            val strategy = if (preferredStrategyName == "Martingale") StrategyType.Martingale else StrategyType.FiveCount

            if (active != null && active.isActive) {
                restoreActiveShoe(active)
                _uiState.value = _uiState.value.copy(
                    showOnboarding = !hasSeenOnboarding,
                    isPortuguese = isPortuguese,
                    strategyType = strategy
                )
            } else {
                _uiState.value = UiState(
                    isLoading = false,
                    isInShoe = false,
                    showOnboarding = !hasSeenOnboarding,
                    isPortuguese = isPortuguese,
                    strategyType = strategy
                )
            }
        }
    }

    fun dismissOnboarding() {
        sharedPrefs.edit().putBoolean("has_seen_onboarding", true).apply()
        _uiState.value = _uiState.value.copy(showOnboarding = false)
    }

    fun forceShowOnboarding() {
        _uiState.value = _uiState.value.copy(showOnboarding = true)
    }

    fun toggleLanguage() {
        val next = !_uiState.value.isPortuguese
        sharedPrefs.edit().putBoolean("is_portuguese", next).apply()
        _uiState.value = _uiState.value.copy(isPortuguese = next)
    }

    fun setStrategyType(strategy: StrategyType) {
        sharedPrefs.edit().putString("preferred_strategy", strategy.name).apply()
        _uiState.value = _uiState.value.copy(strategyType = strategy)
        if (_uiState.value.isInShoe) {
            rebuildCurrentShoeWithNewStrategy(strategy)
        }
    }

    // Restore active shoe from saved parameters
    private fun restoreActiveShoe(active: ActiveShoeState) {
        val decisions = parseDecisionsList(active.decisionsHistory)
        val baseBet = active.baseBet
        val totalBankroll = active.totalBankroll
        val initialTotalBankroll = active.initialTotalBankroll
        val useCommission = active.useBankerCommission
        val strategy = try { StrategyType.valueOf(active.strategyType) } catch (e: Exception) { StrategyType.FiveCount }

        rebuildShoeState(
            baseBet = baseBet,
            startTotalBankroll = initialTotalBankroll,
            decisions = decisions,
            currentTotalBankroll = totalBankroll,
            useCommission = useCommission,
            strategy = strategy
        )
    }

    // Toggle the standard 5% banker commission (95% payout for winning banker bets)
    fun setUseBankerCommission(use: Boolean) {
        _uiState.value = _uiState.value.copy(useBankerCommission = use)
        // If we are currently in a shoe, rebuild the state of the rounds with the new commission rules
        if (_uiState.value.isInShoe) {
            rebuildCurrentShoeWithNewCommission(use)
        }
    }

    private fun rebuildCurrentShoeWithNewCommission(useCommission: Boolean) {
        val current = _uiState.value
        rebuildShoeState(
            baseBet = current.baseBet,
            startTotalBankroll = current.initialTotalBankroll,
            decisions = current.decisionsHistory,
            currentTotalBankroll = current.totalBankroll, // Will be calculated dynamically
            useCommission = useCommission,
            strategy = current.strategyType
        )
    }

    private fun rebuildCurrentShoeWithNewStrategy(strategy: StrategyType) {
        val current = _uiState.value
        rebuildShoeState(
            baseBet = current.baseBet,
            startTotalBankroll = current.initialTotalBankroll,
            decisions = current.decisionsHistory,
            currentTotalBankroll = current.totalBankroll,
            useCommission = current.useBankerCommission,
            strategy = strategy
        )
    }

    // Initializing a new shoe session
    fun startNewShoe(baseBet: Double = 5.0, carryBankroll: Boolean = false, customBankroll: Double = 0.0) {
        viewModelScope.launch {
            val config = BaccaratEngine.getConfigForBaseBet(baseBet)
            val startingBankroll = if (carryBankroll) {
                // Carry existing total bankroll
                _uiState.value.totalBankroll
            } else {
                if (customBankroll > 0.0) customBankroll else config.totalBankroll
            }

            _uiState.value = UiState(
                isLoading = false,
                isInShoe = true,
                baseBet = baseBet,
                totalBankroll = startingBankroll,
                initialTotalBankroll = startingBankroll,
                currentShoeProfit = 0.0,
                currentPattern = Pattern.R,
                consecutiveLossesR = 0,
                betType = BetType.Observe,
                level = 1,
                profitIndex = 0,
                decisionsHistory = emptyList(),
                roundRecords = emptyList(),
                isWinGoalMet = false,
                useBankerCommission = _uiState.value.useBankerCommission,
                showOnboarding = _uiState.value.showOnboarding,
                strategyType = _uiState.value.strategyType,
                isPortuguese = _uiState.value.isPortuguese
            )

            saveCurrentStateToDb()
        }
    }

    // Add winning decision: Player, Banker, or Tie
    fun logDecision(decision: Decision) {
        val current = _uiState.value
        if (!current.isInShoe) return

        val newDecisions = current.decisionsHistory + decision
        rebuildShoeState(
            baseBet = current.baseBet,
            startTotalBankroll = current.initialTotalBankroll,
            decisions = newDecisions,
            currentTotalBankroll = current.totalBankroll,
            useCommission = current.useBankerCommission,
            strategy = current.strategyType
        )
    }

    // Revert last logged calculation
    fun undoLastDecision() {
        val current = _uiState.value
        if (!current.isInShoe || current.decisionsHistory.isEmpty()) return

        val newDecisions = current.decisionsHistory.dropLast(1)
        rebuildShoeState(
            baseBet = current.baseBet,
            startTotalBankroll = current.initialTotalBankroll,
            decisions = newDecisions,
            currentTotalBankroll = current.totalBankroll,
            useCommission = current.useBankerCommission,
            strategy = current.strategyType
        )
    }

    // Complete the shoe and save statistics permanently
    fun saveAndEndShoe() {
        val current = _uiState.value
        if (!current.isInShoe) return

        viewModelScope.launch {
            // Save shoe history
            val entity = HistoryShoe(
                baseBet = current.baseBet,
                initialTotalBankroll = current.initialTotalBankroll,
                endingTotalBankroll = current.totalBankroll,
                netWinLoss = current.currentShoeProfit,
                roundsCount = current.decisionsHistory.size,
                decisionsHistory = current.decisionsHistory.joinToString(",") { it.name },
                isWinGoalMet = current.isWinGoalMet,
                strategyType = current.strategyType.name
            )
            repository.saveHistoryShoe(entity)

            // Delete active shoe state
            repository.deleteActiveShoeState()

            // Update UI state back to main lobby but keeping the updated bankroll count
            _uiState.value = UiState(
                isLoading = false,
                isInShoe = false,
                totalBankroll = current.totalBankroll,
                initialTotalBankroll = current.totalBankroll,
                useBankerCommission = current.useBankerCommission,
                showOnboarding = current.showOnboarding,
                strategyType = current.strategyType,
                isPortuguese = current.isPortuguese
            )
        }
    }

    // Force clear/disconnect in-play shoe without saving stats
    fun discardShoe() {
         viewModelScope.launch {
             val current = _uiState.value
             repository.deleteActiveShoeState()
             _uiState.value = UiState(
                 isLoading = false,
                 isInShoe = false,
                 totalBankroll = current.initialTotalBankroll, // Revert overall bankroll back to start
                 initialTotalBankroll = current.initialTotalBankroll,
                 useBankerCommission = current.useBankerCommission,
                 showOnboarding = current.showOnboarding,
                 strategyType = current.strategyType,
                 isPortuguese = current.isPortuguese
             )
         }
    }

    // Dynamic adjustment of overall Trip pocket bankroll (top up/withdraw)
    fun adjustTotalBankroll(amount: Double) {
        val current = _uiState.value
        val newBankroll = maxOf(0.0, current.totalBankroll + amount)
        val newInitial = maxOf(0.0, current.initialTotalBankroll + amount)
        
        _uiState.value = current.copy(
            totalBankroll = newBankroll,
            initialTotalBankroll = newInitial
        )

        if (current.isInShoe) {
            viewModelScope.launch {
                saveCurrentStateToDb()
            }
        }
    }

    // Delete history record
    fun deleteHistoryRecord(id: Int) {
        viewModelScope.launch {
            repository.deleteHistoryShoeById(id)
        }
    }

    // Reset everything
    fun clearDatabaseHistory() {
        viewModelScope.launch {
            repository.clearAllHistoryShoes()
        }
    }

    // HELPER: Core engine re-evaluator. This processes instructions step by step!
    private fun rebuildShoeState(
        baseBet: Double,
        startTotalBankroll: Double,
        decisions: List<Decision>,
        currentTotalBankroll: Double,
        useCommission: Boolean,
        strategy: StrategyType = _uiState.value.strategyType
    ) {
        val config = BaccaratEngine.getConfigForBaseBet(baseBet)
        val targetWinGoal = if (strategy == StrategyType.Martingale) baseBet * 5.0 else config.winGoal // simplified standard target for martingale

        // Re-initialize state structures
        var bettingState = BettingState(
            betType = BetType.Observe,
            level = 1,
            profitIndex = 0,
            currentPattern = Pattern.R,
            consecutiveLossesR = 0,
            winLossHistory = emptyList()
        )

        val nonTieDecisionsHistory = mutableListOf<Decision>()
        val records = mutableListOf<RoundRecord>()
        var shoeProfit = 0.0
        var runningBankroll = startTotalBankroll

        decisions.forEachIndexed { idx, dec ->
            val roundNum = idx + 1
            
            // 1. Fetch bet recommendation based on history up to this point
            val recommendedSide = BaccaratEngine.getBetSide(nonTieDecisionsHistory, bettingState.currentPattern)
            val recommendedAmt = BaccaratEngine.getBetAmount(baseBet, config, bettingState, strategy)

            if (dec == Decision.Tie) {
                // Tie Hand: Push player / banker stakes, no strategy change
                records.add(
                    RoundRecord(
                        roundNumber = roundNum,
                        decision = dec,
                        prediction = recommendedSide,
                        betAmount = recommendedAmt,
                        outcome = "Push (Tie)",
                        netProfit = 0.0,
                        runningTotal = shoeProfit,
                        betTypeName = if (recommendedSide == null) "Observe" else formatBetTypeString(bettingState, strategy),
                        patternName = bettingState.currentPattern.name
                    )
                )
            } else {
                // Non-Tie Hand
                if (recommendedSide == null) {
                    // Observation phase Starter Bet (neutral)
                    records.add(
                        RoundRecord(
                            roundNumber = roundNum,
                            decision = dec,
                            prediction = null,
                            betAmount = baseBet,
                            outcome = "Observe",
                            netProfit = 0.0,
                            runningTotal = shoeProfit,
                            betTypeName = "Observe",
                            patternName = bettingState.currentPattern.name
                        )
                    )
                    
                    nonTieDecisionsHistory.add(dec)
                    // Trigger observation transition
                    bettingState = BaccaratEngine.calculateNextState(bettingState, true, strategy)
                } else {
                    // Formal wagering
                    val betWon = dec == recommendedSide
                    val netChangeForRound = if (betWon) {
                        if (dec == Decision.Banker && useCommission) {
                            recommendedAmt * 0.95 // Banker 5% commission standard
                        } else {
                            recommendedAmt // Player or No Commission Banker
                        }
                    } else {
                        -recommendedAmt
                    }

                    shoeProfit += netChangeForRound
                    runningBankroll += netChangeForRound

                    // Advance system sizing state
                    bettingState = BaccaratEngine.calculateNextState(bettingState, betWon, strategy)
                    nonTieDecisionsHistory.add(dec)

                    records.add(
                        RoundRecord(
                            roundNumber = roundNum,
                            decision = dec,
                            prediction = recommendedSide,
                            betAmount = recommendedAmt,
                            outcome = if (betWon) "Win" else "Loss",
                            netProfit = netChangeForRound,
                            runningTotal = shoeProfit,
                            betTypeName = formatBetTypeString(bettingState, strategy), // Capture updated or previous
                            patternName = bettingState.currentPattern.name
                        )
                    )
                }
            }
        }

        // Calculate dynamic properties for next active bet
        val nextPredictedSide = BaccaratEngine.getBetSide(nonTieDecisionsHistory, bettingState.currentPattern)
        val nextBetAmount = BaccaratEngine.getBetAmount(baseBet, config, bettingState, strategy)
        val isWinGoalMet = shoeProfit >= targetWinGoal

        // Save computed configurations to state flow
        _uiState.value = UiState(
            isLoading = false,
            isInShoe = true,
            baseBet = baseBet,
            totalBankroll = runningBankroll,
            initialTotalBankroll = startTotalBankroll,
            currentShoeProfit = shoeProfit,
            currentPattern = bettingState.currentPattern,
            consecutiveLossesR = bettingState.consecutiveLossesR,
            betType = bettingState.betType,
            level = bettingState.level,
            profitIndex = bettingState.profitIndex,
            decisionsHistory = decisions,
            roundRecords = records,
            isWinGoalMet = isWinGoalMet,
            useBankerCommission = useCommission,
            showOnboarding = _uiState.value.showOnboarding,
            strategyType = strategy,
            isPortuguese = _uiState.value.isPortuguese
        )

        // Save progress immediately to DB for auto-resume
        viewModelScope.launch {
            saveCurrentStateToDb()
        }
    }

    private fun formatBetTypeString(state: BettingState, strategy: StrategyType = _uiState.value.strategyType): String {
        if (strategy == StrategyType.Martingale) return "M-${state.level}"
        return when (state.betType) {
            BetType.Observe -> "Observe"
            BetType.Attack -> "A-${state.level}"
            BetType.Recovery -> "R-${state.level}"
            BetType.Profit -> "P-${state.profitIndex + 1}"
        }
    }

    // Serialize states to Room database
    private suspend fun saveCurrentStateToDb() {
        val current = _uiState.value
        val serialDecisions = current.decisionsHistory.joinToString(",") { it.name }
        
        // Reconstruct winLossHistory representation
        val serialWinLoss = getBettingStateFromCurrent().winLossHistory.joinToString(",") { if (it) "1" else "0" }

        val activeEntity = ActiveShoeState(
            id = 1,
            baseBet = current.baseBet,
            totalBankroll = current.totalBankroll,
            initialTotalBankroll = current.initialTotalBankroll,
            betType = current.betType.name,
            level = current.level,
            profitIndex = current.profitIndex,
            currentPattern = current.currentPattern.name,
            consecutiveLossesR = current.consecutiveLossesR,
            decisionsHistory = serialDecisions,
            winLossHistory = serialWinLoss,
            gameProfit = current.currentShoeProfit,
            isActive = current.isInShoe,
            useBankerCommission = current.useBankerCommission,
            strategyType = current.strategyType.name
        )
        repository.saveActiveShoeState(activeEntity)
    }

    // Get BettingState helper
    private fun getBettingStateFromCurrent(): BettingState {
        val current = _uiState.value
        // Re-construct the list of true/false based on outcomes
        val list = current.roundRecords
            .filter { it.outcome == "Win" || it.outcome == "Loss" }
            .map { it.outcome == "Win" }
        return BettingState(
            betType = current.betType,
            level = current.level,
            profitIndex = current.profitIndex,
            currentPattern = current.currentPattern,
            consecutiveLossesR = current.consecutiveLossesR,
            winLossHistory = list
        )
    }

    // Parses string representation back into Decision enum
    private fun parseDecisionsList(serialStr: String): List<Decision> {
        if (serialStr.isBlank()) return emptyList()
        return serialStr.split(",").mapNotNull {
            try { Decision.valueOf(it) } catch (e: Exception) { null }
        }
    }
}
