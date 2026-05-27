package com.example.model

import kotlin.math.roundToInt

enum class Decision { Player, Banker, Tie }
enum class Pattern { R, C }
enum class BetType { Observe, Attack, Recovery, Profit }
enum class StrategyType { FiveCount, Martingale }

data class SystemConfig(
    val baseBet: Double,
    val attackBets: List<Double>,
    val recoveryBets: List<Double>,
    val winGoal: Double,
    val gameBankroll: Double,
    val totalBankroll: Double,
    val profitBetsTemplate: List<Double>? = null
)

data class BettingState(
    val betType: BetType = BetType.Observe,
    val level: Int = 1, // 1,2,3 for Attack; 4,5 for Recovery; 1 for Profit
    val profitIndex: Int = 0, // index in profit series
    val currentPattern: Pattern = Pattern.R,
    val consecutiveLossesR: Int = 0,
    val winLossHistory: List<Boolean> = emptyList() // true for Win, false for Loss
)

object BaccaratEngine {

    // Return the standard configuration if baseBet matches standard values, otherwise calculate proportionally.
    fun getConfigForBaseBet(baseBet: Double): SystemConfig {
        return when (baseBet.roundToInt()) {
            1 -> SystemConfig(1.0, listOf(1.0, 2.0, 3.0), listOf(4.0, 8.0), 8.0, 18.0, 90.0, listOf(1.0, 1.0, 2.0, 2.0, 3.0, 3.0, 4.0, 4.0))
            2 -> SystemConfig(2.0, listOf(2.0, 4.0, 6.0), listOf(8.0, 16.0), 16.0, 36.0, 180.0, listOf(3.0, 2.0, 2.0, 3.0, 4.0, 5.0))
            3 -> SystemConfig(3.0, listOf(3.0, 6.0, 9.0), listOf(12.0, 24.0), 24.0, 54.0, 270.0, listOf(5.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0))
            5 -> SystemConfig(5.0, listOf(5.0, 10.0, 15.0), listOf(20.0, 40.0), 40.0, 90.0, 450.0, listOf(7.0, 5.0, 6.0, 8.0, 10.0))
            8 -> SystemConfig(8.0, listOf(8.0, 16.0, 24.0), listOf(32.0, 64.0), 64.0, 144.0, 720.0, listOf(10.0, 8.0, 11.0, 13.0, 15.0))
            10 -> SystemConfig(10.0, listOf(10.0, 20.0, 30.0), listOf(40.0, 80.0), 80.0, 180.0, 900.0, listOf(14.0, 10.0, 12.0, 16.0, 20.0))
            15 -> SystemConfig(15.0, listOf(15.0, 30.0, 45.0), listOf(60.0, 120.0), 120.0, 270.0, 1350.0, listOf(21.0, 15.0, 18.0, 21.0, 24.0))
            20 -> SystemConfig(20.0, listOf(20.0, 40.0, 60.0), listOf(80.0, 160.0), 160.0, 360.0, 1800.0, listOf(28.0, 20.0, 24.0, 32.0, 40.0))
            25 -> SystemConfig(25.0, listOf(25.0, 50.0, 75.0), listOf(100.0, 200.0), 200.0, 450.0, 2250.0, listOf(35.0, 25.0, 30.0, 40.0, 50.0))
            35 -> SystemConfig(35.0, listOf(35.0, 70.0, 105.0), listOf(140.0, 280.0), 280.0, 630.0, 3150.0, listOf(50.0, 35.0, 42.0, 50.0, 58.0))
            50 -> SystemConfig(50.0, listOf(50.0, 100.0, 150.0), listOf(200.0, 400.0), 400.0, 900.0, 4500.0, listOf(70.0, 50.0, 60.0, 80.0, 100.0))
            75 -> SystemConfig(75.0, listOf(75.0, 150.0, 225.0), listOf(300.0, 600.0), 600.0, 1350.0, 6750.0, listOf(105.0, 75.0, 90.0, 120.0, 150.0))
            100 -> SystemConfig(100.0, listOf(100.0, 200.0, 300.0), listOf(400.0, 800.0), 800.0, 1800.0, 9000.0, listOf(140.0, 100.0, 120.0, 160.0, 200.0))
            200 -> SystemConfig(200.0, listOf(200.0, 400.0, 600.0), listOf(800.0, 1600.0), 1600.0, 3600.0, 18000.0, listOf(280.0, 200.0, 240.0, 320.0, 400.0))
            300 -> SystemConfig(300.0, listOf(300.0, 600.0, 900.0), listOf(1200.0, 2400.0), 2400.0, 5400.0, 27000.0, listOf(420.0, 300.0, 360.0, 480.0, 600.0))
            400 -> SystemConfig(400.0, listOf(400.0, 800.0, 1200.0), listOf(1600.0, 3200.0), 3200.0, 7200.0, 36000.0, listOf(560.0, 400.0, 480.0, 640.0, 800.0))
            500 -> SystemConfig(500.0, listOf(500.0, 1000.0, 1500.0), listOf(2000.0, 4000.0), 4000.0, 9000.0, 45000.0, listOf(700.0, 500.0, 600.0, 800.0, 1000.0))
            750 -> SystemConfig(750.0, listOf(750.0, 1500.0, 2250.0), listOf(3000.0, 6000.0), 6000.0, 13500.0, 67500.0, listOf(1050.0, 750.0, 900.0, 1200.0, 1500.0))
            1000 -> SystemConfig(1000.0, listOf(1000.0, 2000.0, 3000.0), listOf(4000.0, 8000.0), 8000.0, 18000.0, 90000.0, listOf(1400.0, 1000.0, 1200.0, 1600.0, 2000.0))
            else -> {
                // Formulaic calculation for non-standard base bets
                SystemConfig(
                    baseBet = baseBet,
                    attackBets = listOf(baseBet, baseBet * 2.0, baseBet * 3.0),
                    recoveryBets = listOf(baseBet * 4.0, baseBet * 8.0),
                    winGoal = baseBet * 8.0,
                    gameBankroll = baseBet * 18.0,
                    totalBankroll = baseBet * 90.0,
                    profitBetsTemplate = null
                )
            }
        }
    }

    // Determine who to bet on: PLAYER, BANKER, or NULL (Observe)
    fun getBetSide(decisionHistory: List<Decision>, pattern: Pattern): Decision? {
        // We need at least 2 non-Tie decisions to bet
        if (decisionHistory.size < 2) return null

        val secondPreceding = decisionHistory[decisionHistory.size - 2]
        return when (pattern) {
            Pattern.R -> secondPreceding // Repeat
            Pattern.C -> {
                // Chop: opposite of second preceding
                if (secondPreceding == Decision.Player) Decision.Banker else Decision.Player
            }
        }
    }

    // Determine next recommendation sizing amount in dollars
    fun getBetAmount(baseBet: Double, config: SystemConfig, state: BettingState, strategy: StrategyType = StrategyType.FiveCount): Double {
        if (strategy == StrategyType.Martingale) {
            if (state.betType == BetType.Observe) return baseBet
            val exponent = (state.level - 1).coerceIn(0, 4)
            val multiplier = when (exponent) {
                0 -> 1.0
                1 -> 2.0
                2 -> 4.0
                3 -> 8.0
                4 -> 16.0
                else -> 1.0
            }
            return baseBet * multiplier
        }

        return when (state.betType) {
            BetType.Observe -> baseBet // Neutral Starter Bet
            BetType.Attack -> {
                val idx = (state.level - 1).coerceIn(0, 2)
                config.attackBets[idx]
            }
            BetType.Recovery -> {
                val idx = (state.level - 4).coerceIn(0, 1)
                config.recoveryBets[idx]
            }
            BetType.Profit -> {
                val template = config.profitBetsTemplate
                if (template != null && state.profitIndex < template.size) {
                    template[state.profitIndex]
                } else {
                    // Formulaic Profit Series mapping:
                    // Index 0: 1.4 * X (e.g. 7.0 for 5.0 base bet)
                    // Index 1: 1.0 * X (e.g. 5.0)
                    // Index 2: 1.2 * X (e.g. 6.0)
                    // Index k >= 3: (1.2 + (k - 2) * 0.4) * X
                    val mult = when (state.profitIndex) {
                        0 -> 1.4
                        1 -> 1.0
                        2 -> 1.2
                        else -> 1.2 + (state.profitIndex - 2) * 0.4
                    }
                    (mult * baseBet * 10.0).roundToInt() / 10.0 // Round to 1 decimal place
                }
            }
        }
    }

    // Calculate state progression for both BRB Pattern and Betting Sizing
    fun calculateNextState(
        state: BettingState,
        betOutcome: Boolean, // true = Win, false = Loss
        strategy: StrategyType = StrategyType.FiveCount
    ): BettingState {
        // 1. Calculate next BRB Pattern and consecutive losses in R
        val (nextPattern, nextConsecutiveLossesR) = when (state.currentPattern) {
            Pattern.R -> {
                if (betOutcome) {
                    Pair(Pattern.R, 0)
                } else {
                    val losses = state.consecutiveLossesR + 1
                    if (losses == 2) {
                        Pair(Pattern.C, 0) // Shift to Chop after 2 losses in R
                    } else {
                        Pair(Pattern.R, losses)
                    }
                }
            }
            Pattern.C -> {
                if (betOutcome) {
                    Pair(Pattern.C, 0) // Stay in Chop on wins
                } else {
                    Pair(Pattern.R, 0) // Shift back to Repeat on 1 loss in Chop
                }
            }
        }

        // 2. Accumulate outcome history
        val updatedHistory = state.winLossHistory + betOutcome

        if (strategy == StrategyType.Martingale) {
            val nextLvl = if (state.betType == BetType.Observe) {
                1
            } else {
                if (betOutcome) {
                    1 // Reset to 1x multiplier on win
                } else {
                    if (state.level >= 5) 1 else state.level + 1 // Wipe/Cap at Level 5, cycle back to 1
                }
            }
            return BettingState(
                betType = BetType.Attack,
                level = nextLvl,
                profitIndex = 0,
                currentPattern = nextPattern,
                consecutiveLossesR = nextConsecutiveLossesR,
                winLossHistory = updatedHistory
            )
        }

        // Check patterns in last outcomes
        val twoInARow = updatedHistory.size >= 2 && updatedHistory.takeLast(2) == listOf(true, true)
        val twoOutOfThree = updatedHistory.size >= 2 && updatedHistory.takeLast(3).count { it } >= 2

        var nextBetType = state.betType
        var nextLevel = state.level
        var nextProfitIndex = state.profitIndex

        // 3. Process Sizing logic based on current BetType
        when (state.betType) {
            BetType.Observe -> {
                // First bet after observation
                nextBetType = BetType.Attack
                nextLevel = 1
                nextProfitIndex = 0
            }
            BetType.Attack -> {
                if (!betOutcome) {
                    // Loss: raise 1 level
                    val proposedLevel = state.level + 1
                    if (proposedLevel > 3) {
                        // Loss of highest attack bet -> move to Recovery
                        nextBetType = BetType.Recovery
                        nextLevel = 4
                    } else {
                        nextBetType = BetType.Attack
                        nextLevel = proposedLevel
                    }
                    nextProfitIndex = 0
                } else {
                    // Win: drop level
                    if (twoInARow) {
                        // Drop 2 levels
                        val normalNext = maxOf(1, state.level - 2)
                        if (normalNext == 1) {
                            // Won two in a row and next bet falls to Level 1 -> Enter Profit betting!
                            nextBetType = BetType.Profit
                            nextLevel = 1
                            nextProfitIndex = 0
                        } else {
                            nextBetType = BetType.Attack
                            nextLevel = normalNext
                        }
                    } else if (twoOutOfThree) {
                        // Drop 2 levels
                        nextBetType = BetType.Attack
                        nextLevel = maxOf(1, state.level - 2)
                        nextProfitIndex = 0
                    } else {
                        // Ordinary win: drop 1 level
                        nextBetType = BetType.Attack
                        nextLevel = maxOf(1, state.level - 1)
                        nextProfitIndex = 0
                    }
                }
            }
            BetType.Recovery -> {
                if (betOutcome) {
                    // Win either recovery bet: drop to Level 2 Attack Bet on next round
                    nextBetType = BetType.Attack
                    nextLevel = 2
                    nextProfitIndex = 0
                } else {
                    // Loss
                    if (state.level == 4) {
                        // Move to second recovery bet
                        nextBetType = BetType.Recovery
                        nextLevel = 5
                    } else {
                        // Level 5 Loss: Tapped out (wiped out Game Bankroll)
                        // This resets to Level 1 Attack, starting fresh
                        nextBetType = BetType.Attack
                        nextLevel = 1
                        nextProfitIndex = 0
                    }
                }
            }
            BetType.Profit -> {
                if (betOutcome) {
                    // Win: move to next profit step
                    nextBetType = BetType.Profit
                    nextLevel = 1
                    nextProfitIndex = state.profitIndex + 1
                } else {
                    // Loss: ends profit betting, counts as loss of a Level 1 bet -> next is Level 2 Attack
                    nextBetType = BetType.Attack
                    nextLevel = 2
                    nextProfitIndex = 0
                }
            }
        }

        return BettingState(
            betType = nextBetType,
            level = nextLevel,
            profitIndex = nextProfitIndex,
            currentPattern = nextPattern,
            consecutiveLossesR = nextConsecutiveLossesR,
            winLossHistory = updatedHistory
        )
    }
}
