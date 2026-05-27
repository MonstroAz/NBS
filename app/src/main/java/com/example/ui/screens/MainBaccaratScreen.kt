package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import com.example.data.HistoryShoe
import com.example.model.Decision
import com.example.model.Pattern
import com.example.model.BetType
import com.example.model.BaccaratEngine
import com.example.model.StrategyType
import com.example.ui.theme.*
import com.example.ui.viewmodel.BaccaratViewModel
import com.example.ui.viewmodel.RoundRecord
import com.example.ui.viewmodel.UiState
import com.example.model.BettingState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import java.text.SimpleDateFormat
import java.util.*

val PolishTextGold = Color(0xFFE5C158)

fun getLocalizedText(key: String, isPt: Boolean): String {
    val en = mapOf(
        "welcome_title" to "NEXT Baccarat System 3.0",
        "app_subtitle" to "FIVE-COUNT & MARTINGALE CONFIG",
        "system_onboarding_title" to "NBS 3.0 Onboarding Tutorial",
        "next_btn" to "NEXT",
        "back_btn" to "BACK",
        "get_started" to "GET STARTED",
        "observe_desc" to "Configure your active session base bet below. The system will automatically compute the attack sizing, recovery limits, and victory goals.",
        "strategy_label" to "Strategy Sizing",
        "five_count" to "Five-Count System",
        "martingale" to "Martingale System",
        "base_bet" to "Base Bet Unit",
        "custom_bet" to "Custom Base Bet",
        "game_bankroll" to "Game Bankroll",
        "trip_bankroll" to "Trip Pocket Bankroll",
        "banker_commission" to "Enable Banker 5% Commission (Standard)",
        "start_session" to "START NBS 3.0 SESSION",
        "carry_bankroll" to "Carry Active Pocket Bankroll",
        "reset_history" to "WIPE DATABASE",
        "active_recommendation" to "ACTIVE RECOMMENDATION",
        "observing_shoes" to "OBSERVING SHOE PROGRESS",
        "rhythm_bet_title" to "NEXT Baccarat System 3.0",
        "next_bet_amount" to "Net Recommended Size",
        "player" to "PLAYER",
        "banker" to "BANKER",
        "tie" to "TIE",
        "undo" to "UNDO",
        "leave_session" to "DISCARD SESSION",
        "save_session" to "SAVE & END SHOE",
        "shoes_logs" to "SHOES LOGS - ROUND BY ROUND",
        "expand_logs" to "Show Round Logs",
        "collapse_logs" to "Hide Round Logs"
    )
    val pt = mapOf(
        "welcome_title" to "NEXT Baccarat System 3.0",
        "app_subtitle" to "CONFIGURAÇÃO FIVE-COUNT E MARTINGALE",
        "system_onboarding_title" to "Tutorial de Integração NBS 3.0",
        "next_btn" to "PRÓXIMO",
        "back_btn" to "VOLTAR",
        "get_started" to "INICIAR",
        "observe_desc" to "Configure sua aposta base para a sessão ativa. O sistema calculará de forma automática as frações de ataque, limites de recuperação e metas de vitória.",
        "strategy_label" to "Estratégia de Aposta",
        "five_count" to "Sistema Five-Count",
        "martingale" to "Sistema Martingale",
        "base_bet" to "Unidade de Aposta Base",
        "custom_bet" to "Aposta Base Personalizada",
        "game_bankroll" to "Banca da Sessão (Game)",
        "trip_bankroll" to "Banca Geral (Trip)",
        "banker_commission" to "Ativar Comissão de 5% do Banker",
        "start_session" to "INICIAR SESSÃO",
        "carry_bankroll" to "Carregar Banca de Bolso Ativa",
        "reset_history" to "ZERAR HISTÓRICO",
        "active_recommendation" to "RECOMENDAÇÃO ATIVA",
        "observing_shoes" to "PROGRESSO DA SESSÃO",
        "rhythm_bet_title" to "NBS 3.0",
        "next_bet_amount" to "Aposta Recomendada",
        "player" to "PLAYER",
        "banker" to "BANKER",
        "tie" to "EMPATE",
        "undo" to "DESFAZER",
        "leave_session" to "DESCARTAR SESSÃO",
        "save_session" to "SALVAR E TERMINAR",
        "shoes_logs" to "HISTÓRICO DA RODADAS (ROUND BY ROUND)",
        "expand_logs" to "Mostrar Histórico de Rodadas",
        "collapse_logs" to "Esconder Histórico de Rodadas"
    )
    val map = if (isPt) pt else en
    return map[key] ?: (en[key] ?: "")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainBaccaratScreen(
    viewModel: BaccaratViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val historyShoes by viewModel.historyShoes.collectAsState()
    
    // Tab indicator state: 0 = Play, 1 = History, 2 = Help/Rules
    var selectedTab by remember { mutableIntStateOf(0) }
    var showCustomBaseBetDialog by remember { mutableStateOf(false) }
    var showSupportDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(MidnightBackground),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showSupportDialog = true },
                containerColor = PolishPrimaryBlue,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Favorite, contentDescription = "Support Me")
            }
        },
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF3F4759)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Casino,
                                    contentDescription = "Next Baccarat Icon",
                                    tint = Color(0xFFD7E3FF),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    "NEXT Baccarat System 3.0",
                                    fontWeight = FontWeight.Bold,
                                    color = PolishTextLight,
                                    fontSize = 17.sp,
                                    letterSpacing = (-0.2).sp
                                )
                                Text(
                                    if (uiState.strategyType == StrategyType.FiveCount) "FIVE-COUNT ACTIVE" else "MARTINGALE ACTIVE",
                                    fontWeight = FontWeight.Bold,
                                    color = PolishPrimaryBlue,
                                    fontSize = 10.sp,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = PolishBackground,
                        titleContentColor = PolishTextLight
                    ),
                    actions = {
                        if (uiState.isInShoe) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(30.dp))
                                    .background(PolishPlayerBg)
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    "LIVE SESSION",
                                    color = PolishPlayerText,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp,
                                    letterSpacing = 0.5.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        
                        IconButton(
                            onClick = { viewModel.toggleLanguage() },
                            modifier = Modifier.testTag("language_toggle_button")
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .border(1.dp, PolishBorder, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    if (uiState.isPortuguese) "PT" else "EN",
                                    color = PolishDarkGold,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        IconButton(
                            onClick = { showCustomBaseBetDialog = true },
                            modifier = Modifier.testTag("settings_button")
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = "Commission Settings", tint = PolishTextLight)
                        }
                    }
                )

                // Navigation Tabs
                Box(
                    modifier = Modifier.fillMaxWidth().background(PolishBackground),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier
                            .widthIn(max = 600.dp)
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(PolishCard)
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                    val tabs = listOf(
                        Triple(0, Icons.Default.PlayArrow, if (uiState.isPortuguese) "PREDIÇÃO" else "PREDICTOR"),
                        Triple(1, Icons.Default.History, if (uiState.isPortuguese) "HISTÓRICO" else "RECORDS"),
                        Triple(2, Icons.Default.MenuBook, if (uiState.isPortuguese) "SABER MAIS" else "RULES")
                    )
                    tabs.forEach { (index, icon, label) ->
                        val isSelected = selectedTab == index
                        val containerColor = if (isSelected) PolishCardLighter else Color.Transparent
                        val contentColor = if (isSelected) PolishPrimaryBlue else PolishTextGray

                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(containerColor)
                                .clickable { selectedTab = index }
                                .padding(vertical = 10.dp)
                                .testTag("tab_button_$index"),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                tint = contentColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = label,
                                color = contentColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
                }
            }
        },
        containerColor = MidnightBackground
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MidnightBackground
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                Box(modifier = Modifier.widthIn(max = 600.dp).fillMaxHeight()) {
                    when (selectedTab) {
                        0 -> {
                            if (!uiState.isInShoe) {
                                LobbyScreen(
                                    uiState = uiState,
                                    onStartShoe = { base, carry, bal -> viewModel.startNewShoe(base, carry, bal) },
                                    onStrategyChange = { strategy -> viewModel.setStrategyType(strategy) }
                                )
                            } else {
                                PlayScreen(
                                    uiState = uiState,
                                    onLogDecision = { dec -> viewModel.logDecision(dec) },
                                    onUndo = { viewModel.undoLastDecision() },
                                    onSaveEnd = { viewModel.saveAndEndShoe() },
                                    onDiscard = { viewModel.discardShoe() },
                                    onAdjustBankroll = { amt -> viewModel.adjustTotalBankroll(amt) }
                                )
                            }
                        }
                        1 -> HistoryScreen(
                            historyList = historyShoes,
                            onDelete = { id -> viewModel.deleteHistoryRecord(id) },
                            onClearAll = { viewModel.clearDatabaseHistory() }
                        )
                        2 -> HelpRulesScreen(onRestartOnboarding = { viewModel.forceShowOnboarding() })
                    }
                }
            }
        }
    }

    // Support Dialog
    if (showSupportDialog) {
        val clipboardManager = LocalClipboardManager.current
        val usdtAddress = "TXn8WCisQfaA2XDN7ZKX5zi3rfF54m796z"
        
        AlertDialog(
            onDismissRequest = { showSupportDialog = false },
            title = {
                Text(
                    "Support Me ☕",
                    color = PolishTextLight,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "If this app makes your day a bit easier, consider buying me a coffee! ☕ Your support helps and means a lot!",
                        color = PolishTextLight,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        usdtAddress,
                        color = PolishPrimaryBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(PolishCardLighter, RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        textAlign = TextAlign.Center
                    )
                    
                    Text(
                        "Only send Tether USDT (TRC20) assets to this address. Other assets will be lost forever.",
                        color = PolishBankerBg, // Red color
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(usdtAddress))
                        showSupportDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PolishPrimaryBlue, contentColor = PolishPlayerText)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copy Address", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSupportDialog = false }) {
                    Text("Close", color = PolishTextGray)
                }
            },
            containerColor = PolishCard,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Settings & Mode Configuration Dialog
    if (showCustomBaseBetDialog) {
        CommissionSettingsDialog(
            useCommission = uiState.useBankerCommission,
            onToggleCommission = { use -> viewModel.setUseBankerCommission(use) },
            onDismiss = { showCustomBaseBetDialog = false }
        )
    }

    if (uiState.showOnboarding) {
        OnboardingTutorial(
            isPt = uiState.isPortuguese,
            onDismiss = { viewModel.dismissOnboarding() }
        )
    }
}

// ==========================================
// 1. LOBBY SCREEN (Setup shoe parameters)
// ==========================================
@Composable
fun LobbyScreen(
    uiState: UiState,
    onStartShoe: (Double, Boolean, Double) -> Unit,
    onStrategyChange: (StrategyType) -> Unit
) {
    var selectedBaseBet by remember { mutableDoubleStateOf(5.0) }
    var useCustomBaseBet by remember { mutableStateOf(false) }
    var customBaseBetText by remember { mutableStateOf("") }
    
    var customBankrollCheckbox by remember { mutableStateOf(false) }
    var customBankrollText by remember { mutableStateOf("") }

    val carryBankrollEnabled = uiState.totalBankroll > 0.0

    // Compute preview parameters dynamically based on selected bet
    val baseToUse = if (useCustomBaseBet) {
        customBaseBetText.toDoubleOrNull() ?: 5.0
    } else {
        selectedBaseBet
    }
    val config = BaccaratEngine.getConfigForBaseBet(baseToUse)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Card
        item {
            val isPt = uiState.isPortuguese
            Card(
                colors = CardDefaults.cardColors(containerColor = PolishCard),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, PolishBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        getLocalizedText("welcome_title", isPt),
                        fontWeight = FontWeight.Bold,
                        color = PolishDarkGold,
                        fontSize = 18.sp,
                        letterSpacing = (-0.2).sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        getLocalizedText("observe_desc", isPt),
                        color = PolishTextGray,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Strategy Sizing Selector Card
        item {
            val isPt = uiState.isPortuguese
            Card(
                colors = CardDefaults.cardColors(containerColor = PolishCard),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, PolishBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        getLocalizedText("strategy_label", isPt).uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = PolishTextLight,
                        fontSize = 13.sp,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val isFiveCount = uiState.strategyType == StrategyType.FiveCount
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isFiveCount) PolishPrimaryBlue else PolishCardLighter)
                                .border(1.dp, if (isFiveCount) PolishTextLight else PolishBorder, RoundedCornerShape(12.dp))
                                .clickable { onStrategyChange(StrategyType.FiveCount) }
                                .padding(vertical = 12.dp)
                                .testTag("strategy_option_fivecount"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                getLocalizedText("five_count", isPt),
                                color = if (isFiveCount) PolishPlayerText else PolishTextLight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        val isMartingale = uiState.strategyType == StrategyType.Martingale
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isMartingale) PolishPrimaryBlue else PolishCardLighter)
                                .border(1.dp, if (isMartingale) PolishTextLight else PolishBorder, RoundedCornerShape(12.dp))
                                .clickable { onStrategyChange(StrategyType.Martingale) }
                                .padding(vertical = 12.dp)
                                .testTag("strategy_option_martingale"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                getLocalizedText("martingale", isPt),
                                color = if (isMartingale) PolishPlayerText else PolishTextLight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }

        // Base Bet Sorter Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = PolishCard),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, PolishBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        "SELECT BASE BET CHIP",
                        fontWeight = FontWeight.Bold,
                        color = PolishTextLight,
                        fontSize = 13.sp,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (!useCustomBaseBet) {
                        // Quick pick chips
                        val chips = listOf(1.0, 2.0, 3.0, 5.0, 8.0, 10.0, 15.0, 20.0, 25.0, 50.0, 100.0)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            chips.forEach { valAmt ->
                                val isSelected = selectedBaseBet == valAmt
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) PolishPrimaryBlue else PolishCardLighter)
                                        .border(1.5.dp, if (isSelected) PolishTextLight else PolishBorder, CircleShape)
                                        .clickable { selectedBaseBet = valAmt }
                                        .testTag("base_chip_${valAmt.toInt()}"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "$${valAmt.toInt()}",
                                        color = if (isSelected) PolishPlayerText else PolishTextLight,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = useCustomBaseBet,
                            onCheckedChange = { useCustomBaseBet = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = PolishPrimaryBlue,
                                uncheckedColor = PolishTextGray
                            )
                        )
                        Text(
                            "Use custom Base Bet ($)",
                            color = PolishTextLight,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    if (useCustomBaseBet) {
                        OutlinedTextField(
                            value = customBaseBetText,
                            onValueChange = { customBaseBetText = it },
                            label = { Text("Base Bet ($)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = PolishTextLight,
                                unfocusedTextColor = PolishTextLight,
                                focusedBorderColor = PolishPrimaryBlue,
                                unfocusedBorderColor = PolishBorder
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                                .testTag("custom_base_input")
                        )
                    }
                }
            }
        }

        // Sizing & Target Calculations Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = PolishCard),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, PolishBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        "FORMULA PREVIEWS",
                        fontWeight = FontWeight.Bold,
                        color = PolishTextLight,
                        fontSize = 13.sp,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        ParameterCell(
                            label = "GAME BANKROLL",
                            value = "$${config.gameBankroll.toInt()}",
                            desc = "Session limits",
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        ParameterCell(
                            label = "TRIP BANKROLL",
                            value = "$${config.totalBankroll.toInt()}",
                            desc = "Total buffer",
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        ParameterCell(
                            label = "WIN GOAL",
                            value = "+$${config.winGoal.toInt()}",
                            desc = "Profit target",
                            colorValue = PolishGreen,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        ParameterCell(
                            label = "ATTACK MATRIX",
                            value = config.attackBets.joinToString("-") { "$${it.toInt()}" },
                            desc = "Levels 1-2-3",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        ParameterCell(
                            label = "RECOVERY MATRIX",
                            value = config.recoveryBets.joinToString("-") { "$${it.toInt()}" },
                            desc = "Levels 4-5",
                            colorValue = PolishBankerBg,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        ParameterCell(
                            label = "PROFIT ENTRY",
                            value = "${(config.baseBet * 1.4).toInt()} -> ${config.baseBet.toInt()} -> ${config.baseBet.toInt() + 1}",
                            desc = "Profit Series",
                            colorValue = PolishDarkGold,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Custom Bankroll Allocation Overrule
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = PolishCard),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, PolishBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = customBankrollCheckbox,
                            onCheckedChange = { customBankrollCheckbox = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = PolishPrimaryBlue,
                                uncheckedColor = PolishTextGray
                            )
                        )
                        Text(
                            "Override Starting Trip Bankroll",
                            color = PolishTextLight,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    if (customBankrollCheckbox) {
                        OutlinedTextField(
                            value = customBankrollText,
                            onValueChange = { customBankrollText = it },
                            label = { Text("Trip Bankroll ($)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = PolishTextLight,
                                unfocusedTextColor = PolishTextLight,
                                focusedBorderColor = PolishPrimaryBlue,
                                unfocusedBorderColor = PolishBorder
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                                .testTag("custom_bankroll_input")
                        )
                    }
                }
            }
        }

        // Launch Action Buttons
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (carryBankrollEnabled) {
                    Button(
                        onClick = { onStartShoe(baseToUse, true, 0.0) },
                        colors = ButtonDefaults.buttonColors(containerColor = PolishCardLighter),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("carry_bankroll_launch"),
                        border = BorderStroke(1.dp, PolishPrimaryBlue.copy(alpha = 0.5f))
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "START NEW SHOE - KEEP CURRENT BANKROLL",
                                fontWeight = FontWeight.Bold,
                                color = PolishTextLight,
                                fontSize = 12.sp,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                "Carrying existing total of $${uiState.totalBankroll.toInt()}",
                                fontSize = 10.sp,
                                color = PolishTextGray
                            )
                        }
                    }
                }

                Button(
                    onClick = {
                        val inputBankroll = if (customBankrollCheckbox) {
                            customBankrollText.toDoubleOrNull() ?: config.totalBankroll
                        } else {
                            config.totalBankroll
                        }
                        onStartShoe(baseToUse, false, inputBankroll)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PolishPrimaryBlue),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("fresh_bankroll_launch")
                ) {
                    Text(
                        "START FRESH SHOE SESSION",
                        fontWeight = FontWeight.Black,
                        color = PolishPlayerText,
                        fontSize = 14.sp,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ParameterCell(
    label: String,
    value: String,
    desc: String,
    colorValue: Color = PolishTextLight,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(PolishCardLighter)
            .border(1.dp, PolishBorder.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(10.dp)
    ) {
        Text(
            label,
            color = TextGray,
            fontWeight = FontWeight.Bold,
            fontSize = 9.sp,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            value,
            color = colorValue,
            fontWeight = FontWeight.Black,
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            desc,
            color = TextGrayDark,
            fontSize = 9.sp
        )
    }
}

// ==========================================
// 2. PLAY SCREEN (Prediction and Logger view)
// ==========================================
@Composable
fun PlayScreen(
    uiState: UiState,
    onLogDecision: (Decision) -> Unit,
    onUndo: () -> Unit,
    onSaveEnd: () -> Unit,
    onDiscard: () -> Unit,
    onAdjustBankroll: (Double) -> Unit
) {
    var showAdjustBankrollDialog by remember { mutableStateOf(false) }
    var adjustAmountText by remember { mutableStateOf("") }
    
    var showRoundLogs by remember { mutableStateOf(false) }

    val currentShoeProfit = uiState.currentShoeProfit
    val baseBet = uiState.baseBet
    val config = BaccaratEngine.getConfigForBaseBet(baseBet)
    val winGoal = config.winGoal

    var showVictoryDialog by remember { mutableStateOf(false) }
    var userAcknowledgedVictoryGoall by remember { mutableStateOf(false) }

    // Trigger Victory notification once win goal is reached
    LaunchedEffect(uiState.isWinGoalMet) {
        if (uiState.isWinGoalMet && !userAcknowledgedVictoryGoall) {
            showVictoryDialog = true
        }
    }

    // Prepare Prediction state variables
    val decisionsHistory = uiState.decisionsHistory
    // Filter out ties to count decisions
    val nonTieDecisions = decisionsHistory.filter { it != Decision.Tie }
    val isObservePhase = nonTieDecisions.size < 2

    // Bet Recommendation Values
    val predictedSide = BaccaratEngine.getBetSide(nonTieDecisions, uiState.currentPattern)
    
    // Construct BettingState
    val bettingState = BettingState(
        betType = uiState.betType,
        level = uiState.level,
        profitIndex = uiState.profitIndex,
        currentPattern = uiState.currentPattern,
        consecutiveLossesR = uiState.consecutiveLossesR,
        winLossHistory = uiState.roundRecords.filter { it.outcome == "Win" || it.outcome == "Loss" }.map { it.outcome == "Win" }
    )
    val betAmount = BaccaratEngine.getBetAmount(baseBet, config, bettingState, uiState.strategyType)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Core Statistics Cards (Bankroll / Shoe Profit)
        Card(
            colors = CardDefaults.cardColors(containerColor = PolishCard),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, PolishBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "TOTAL BANKROLL",
                            color = PolishTextGray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            "$${uiState.totalBankroll.toInt()}",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 22.sp
                        )
                    }

                    // Profit Badge
                    val profitColor = if (currentShoeProfit >= 0) PolishGreen else PolishBankerBg
                    val profitSign = if (currentShoeProfit >= 0) "+" else ""
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "SHOE PROFIT",
                            color = PolishTextGray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 0.5.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (currentShoeProfit >= 0) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                contentDescription = null,
                                tint = profitColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "$profitSign$${currentShoeProfit.toInt()}",
                                color = profitColor,
                                fontWeight = FontWeight.Black,
                                fontSize = 22.sp,
                                modifier = Modifier.testTag("shoe_profit_text")
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Stats Counters
                val totalWins = uiState.roundRecords.count { it.outcome == "Win" }
                val totalLosses = uiState.roundRecords.count { it.outcome == "Loss" }
                val totalGames = decisionsHistory.size

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(if (uiState.isPortuguese) "Total de Jogos: $totalGames" else "Total Games: $totalGames", color = PolishTextLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row {
                        Text(if (uiState.isPortuguese) "V: $totalWins" else "W: $totalWins", color = PolishGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(if (uiState.isPortuguese) "D: $totalLosses" else "L: $totalLosses", color = PolishBankerBg, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Win Goal Progress Bar
                val progress = if (winGoal > 0.0) (currentShoeProfit.coerceIn(0.0, winGoal) / winGoal).toFloat() else 0f
                val safeProgress = if (progress.isNaN()) 0f else progress.coerceIn(0f, 1f)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "GOAL",
                        color = PolishTextGray,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.width(40.dp)
                    )
                    LinearProgressIndicator(
                        progress = safeProgress,
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp)
                            .clip(CircleShape),
                        color = if (uiState.isWinGoalMet) PolishGreen else PolishPrimaryBlue,
                        trackColor = PolishCardLighter
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        "+$${winGoal.toInt()}",
                        color = if (uiState.isWinGoalMet) PolishGreen else Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // PREDICTION PANEL (Equivalent to web-app Prediction block!)
        Card(
            colors = CardDefaults.cardColors(containerColor = PolishCard),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.5.dp, if (isObservePhase) PolishBorder else if (predictedSide == Decision.Player) PolishPlayerBg.copy(alpha=0.4f) else PolishBankerBg.copy(alpha=0.4f))
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                // Gradient header stroke line
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    PolishPrimaryBlue,
                                    Color.Transparent
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "SUGGESTED ACTION",
                        color = PolishPrimaryBlue,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp,
                        letterSpacing = 2.sp
                    )
                    
                    Spacer(modifier = Modifier.height(14.dp))

                    if (isObservePhase) {
                        // Observe hands prompt
                        Text(
                            "OBSERVE HANDS",
                            color = PolishDarkGold,
                            fontWeight = FontWeight.Black,
                            fontSize = 32.sp,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            if (uiState.isPortuguese) {
                                "Insira mais ${2 - nonTieDecisions.size} resultados iniciais para ativar a recomendação do NBS 3.0."
                            } else {
                                "Input ${2 - nonTieDecisions.size} more initial outcomes to trigger NBS 3.0 calculation."
                            },
                            color = PolishTextGray,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    } else {
                        // Active Bet Recommendation
                        val predictionLabel = if (predictedSide == Decision.Player) getLocalizedText("player", uiState.isPortuguese) else getLocalizedText("banker", uiState.isPortuguese)
                        val predictionColor = Color.White // Elegant white display header as in HTML

                        Text(
                            predictionLabel,
                            color = predictionColor,
                            fontWeight = FontWeight.Black,
                            fontSize = 54.sp,
                            letterSpacing = (-1).sp,
                            modifier = Modifier.testTag("predicted_side_text")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(30.dp))
                                .background(PolishCardLighter)
                                .border(1.dp, PolishBorder, RoundedCornerShape(30.dp))
                                .padding(vertical = 8.dp, horizontal = 20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "BET: $${betAmount.toInt()}",
                                color = PolishPrimaryBlue,
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                modifier = Modifier.testTag("bet_amount_text")
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFF004A77))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    if (uiState.isPortuguese) "SISTEMA 3.0" else "NBS 3.0",
                                    color = Color(0xFFD1E4FF),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }

                    // Dynamic graph mock based on actual non-tie history rounds
                    val recentDecisions = nonTieDecisions.takeLast(7)
                    if (recentDecisions.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .padding(top = 20.dp)
                                .height(48.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            recentDecisions.forEachIndexed { i, dec ->
                                val col = if (dec == Decision.Player) PolishPrimaryBlue else PolishBankerBg
                                // Vary column heights slightly to give realistic look
                                val factor = when (i % 3) {
                                    0 -> 20.dp
                                    1 -> 36.dp
                                    else -> 28.dp
                                }
                                Box(
                                    modifier = Modifier
                                        .width(8.dp)
                                        .height(factor)
                                        .clip(CircleShape)
                                        .background(col)
                                )
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    HorizontalDivider(color = PolishBorder, thickness = 1.dp)

                    Spacer(modifier = Modifier.height(12.dp))

                    // Rhythm Betting Details
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("PATTERN", color = PolishTextGray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = if (isObservePhase) "Observe" else "${uiState.currentPattern.name} (Chop limit: ${if(uiState.currentPattern==Pattern.C) 1 else 2})",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("SIZING TYPE", color = PolishTextGray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = if (isObservePhase) " starter bet" else formatBetTypeFull(uiState),
                                color = if (uiState.betType == BetType.Profit) PolishDarkGold else if (uiState.betType == BetType.Recovery) PolishBankerBg else Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("R CONSEC LOSSES", color = PolishTextGray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = "${uiState.consecutiveLossesR} / 2",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Horizontal Bead Plate (The classic Retro Grid!)
        Card(
            colors = CardDefaults.cardColors(containerColor = PolishCard),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, PolishBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                Text(
                    "SHOE BEAD PLATE ROADMAP",
                    color = PolishTextGray,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                BeadPlateGrid(decisions = decisionsHistory)
            }
        }

        // WAGER LOGGER CHIPS ACTION BOARD (Casino style controls!)
        Card(
            colors = CardDefaults.cardColors(containerColor = PolishCard),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, PolishBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "LOG THIS ROUND'S OUTCOME",
                    color = PolishTextGray,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(14.dp))

                // Gridded input controls exactly styled from theme's HTML
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Column 1: P (Player) button (Light Blue style)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(72.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(PolishPlayerBg)
                            .clickable { onLogDecision(Decision.Player) }
                            .testTag("player_log_chip"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "P",
                                fontWeight = FontWeight.Black,
                                fontSize = 20.sp,
                                color = PolishPlayerText
                            )
                            Text(
                                getLocalizedText("player", uiState.isPortuguese),
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = PolishPlayerText
                            )
                        }
                    }

                    // Column 2: B (Banker) button (Pinkish Red style)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(72.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(PolishBankerBg)
                            .clickable { onLogDecision(Decision.Banker) }
                            .testTag("banker_log_chip"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "B",
                                fontWeight = FontWeight.Black,
                                fontSize = 20.sp,
                                color = PolishBankerText
                            )
                            Text(
                                getLocalizedText("banker", uiState.isPortuguese),
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = PolishBankerText
                            )
                        }
                    }

                    // Column 3: T (Tie) button (Grey/Midnight overlay style)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(72.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(PolishTieBg)
                            .clickable { onLogDecision(Decision.Tie) }
                            .testTag("tie_log_chip"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "T",
                                fontWeight = FontWeight.Black,
                                fontSize = 20.sp,
                                color = PolishTieText
                            )
                            Text(
                                getLocalizedText("tie", uiState.isPortuguese),
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = PolishTieText
                            )
                        }
                    }

                    // Column 4: Undo button (Bordered Dark style)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(72.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(PolishBackground)
                            .border(1.5.dp, PolishBorder, RoundedCornerShape(16.dp))
                            .clickable(enabled = decisionsHistory.isNotEmpty()) { onUndo() }
                            .testTag("undo_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.alpha(if (decisionsHistory.isNotEmpty()) 1.0f else 0.4f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Undo,
                                contentDescription = "Undo Last Log",
                                tint = PolishTextLight,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                "UNDO",
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = PolishTextLight
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Accessory action options below the grid: ADJUSTING pocket cash, and SAVING shoe
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { showAdjustBankrollDialog = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PolishDarkGold),
                        border = BorderStroke(1.dp, PolishBorder),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AttachMoney,
                            contentDescription = "Wallet Adjust",
                            tint = PolishDarkGold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "POCKET CASH",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = PolishTextLight
                        )
                    }

                    Button(
                        onClick = { onSaveEnd() },
                        colors = ButtonDefaults.buttonColors(containerColor = PolishGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1.2f)
                            .height(44.dp)
                            .testTag("save_shoe_button")
                    ) {
                        Text(
                            "SAVE SHOE SESSION",
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF003311),
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // ROUNDS PLAY-BY-PLAY DETAILED LOG TABLE
        if (uiState.roundRecords.isNotEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = PolishCard),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, PolishBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showRoundLogs = !showRoundLogs }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            getLocalizedText("shoes_logs", uiState.isPortuguese),
                            color = PolishTextGray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            letterSpacing = 0.5.sp
                        )
                        Icon(
                            imageVector = if (showRoundLogs) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = getLocalizedText(if (showRoundLogs) "collapse_logs" else "expand_logs", uiState.isPortuguese),
                            tint = PolishTextGray
                        )
                    }

                    if (showRoundLogs) {
                        Spacer(modifier = Modifier.height(10.dp))
// At usage site:
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            uiState.roundRecords.reversed().forEach { rec ->
                                RoundReportRow(rec, uiState.isPortuguese)
                            }
                        }
                    }
                }
            }
        }

        // Abort Game Button
        OutlinedButton(
            onClick = { onDiscard() },
            colors = ButtonDefaults.outlinedButtonColors(contentColor = PolishBankerBg),
            border = BorderStroke(1.dp, PolishBankerBg.copy(alpha = 0.4f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp)
                .testTag("discard_shoe_button")
        ) {
            Icon(Icons.Default.DeleteForever, contentDescription = "Aborted shoe", tint = PolishBankerBg)
            Spacer(modifier = Modifier.width(6.dp))
            Text("CANCEL SHOE (DISCARD)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }

    // Adjust Bankroll Overlay Dialog
    if (showAdjustBankrollDialog) {
        AlertDialog(
            onDismissRequest = { showAdjustBankrollDialog = false },
            title = { Text("Adjust Overall Bankroll", color = PolishTextLight) },
            text = {
                Column {
                    Text("Add dynamic pocket cash additions/deductions (e.g. cash outs, ATM visits). Use negative for cashouts.", color = PolishTextGray, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = adjustAmountText,
                        onValueChange = { adjustAmountText = it },
                        label = { Text("Amount ($)", color = PolishTextLight) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = PolishTextLight,
                            focusedBorderColor = PolishPrimaryBlue,
                            unfocusedBorderColor = PolishBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = adjustAmountText.toDoubleOrNull() ?: 0.0
                        onAdjustBankroll(amt)
                        showAdjustBankrollDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PolishPrimaryBlue)
                ) {
                    Text("APPLY", color = PolishPlayerText, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAdjustBankrollDialog = false }) {
                    Text("CANCEL", color = PolishTextGray)
                }
            },
            containerColor = PolishCard,
            textContentColor = PolishTextLight
        )
    }

    // Win Goal Met Victory Modal
    if (showVictoryDialog) {
        AlertDialog(
            onDismissRequest = { showVictoryDialog = false },
            icon = { Icon(Icons.Default.CardGiftcard, contentDescription = "Victory Info", tint = PolishDarkGold, modifier = Modifier.size(36.dp)) },
            title = { Text("Win Goal Reached!", color = PolishTextLight, fontWeight = FontWeight.Black, fontSize = 20.sp) },
            text = {
                Text(
                    "Congratulations! You have earned +$${currentShoeProfit.toInt()} profit, which successfully fulfills or exceeds your configured Baccarat session target of +$${winGoal.toInt()}.\n\nAccording to the Five-Count system, this is an optimal point to stop playing, save the shoe, and take a physical break before starting another session.",
                    color = PolishTextLight,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        userAcknowledgedVictoryGoall = true
                        showVictoryDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PolishGreen)
                ) {
                    Text("LOCK IN GAINS", color = Color(0xFF003311), fontWeight = FontWeight.Black)
                }
            },
            containerColor = PolishCardLighter
        )
    }
}

@Composable
fun ChipButton(
    label: String,
    subLabel: String,
    colorColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(colorColor)
                .clickable { onClick() }
                .drawBehind {
                    // Accent ring
                    drawCircle(
                        color = Color.White.copy(alpha = 0.3f),
                        radius = size.minDimension / 2.3f,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                label,
                color = TextWhite,
                fontWeight = FontWeight.Black,
                fontSize = 24.sp,
                fontFamily = FontFamily.Monospace
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            subLabel,
            color = TextWhite,
            fontWeight = FontWeight.Bold,
            fontSize = 9.sp,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun BeadPlateGrid(decisions: List<Decision>) {
    val colCount = if (decisions.isEmpty()) 11 else ((decisions.size + 5) / 6).coerceAtLeast(11)

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items((0 until colCount).toList()) { col ->
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                for (row in 0 until 6) {
                    val flatIndex = col * 6 + row
                    if (flatIndex < decisions.size) {
                        val dec = decisions[flatIndex]
                        BeadItem(decision = dec)
                    } else {
                        // Empty cell
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(PolishCardLighter)
                                .border(1.dp, PolishBorder.copy(alpha=0.3f), CircleShape)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BeadItem(decision: Decision) {
    val (bg, label, textCol) = when (decision) {
        Decision.Player -> Triple(PolishPlayerBg, "P", PolishPlayerText)
        Decision.Banker -> Triple(PolishBankerBg, "B", PolishBankerText)
        Decision.Tie -> Triple(PolishTieBg, "T", PolishTieText)
    }
    
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(bg),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            color = textCol,
            fontWeight = FontWeight.Black,
            fontSize = 11.sp
        )
    }
}

@Composable
fun RoundReportRow(rec: RoundRecord, isPt: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(PolishCardLighter)
            .border(1.dp, PolishBorder.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Outcome stamp circle
            val (markerColor, textCol, label) = when (rec.decision) {
                Decision.Player -> Triple(PolishPlayerBg, PolishPlayerText, "P")
                Decision.Banker -> Triple(PolishBankerBg, PolishBankerText, "B")
                Decision.Tie -> Triple(PolishTieBg, PolishTieText, "T")
            }
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(markerColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label,
                    color = textCol,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column {
                Text(
                    text = "Round #${rec.roundNumber}",
                    color = PolishTextLight,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                Text(
                    text = "Style: ${rec.patternName} | System: ${rec.betTypeName}",
                    color = PolishTextGray,
                    fontSize = 10.sp
                )
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            val isWin = rec.outcome == "Win"
            val isLoss = rec.outcome == "Loss"
            
            val stateColor = if (isWin) PolishGreen else if (isLoss) PolishBankerBg else PolishTextGray
            val symbol = if (isWin) "+$" else if (isLoss) "-$" else "$"
            val displayVal = if (rec.outcome == "Observe") "0" else (rec.betAmount).toInt().toString()

            val outcomeText = when {
                rec.outcome == "Observe" -> if (isPt) "Observar" else "Observe"
                rec.outcome == "Push (Tie)" -> if (isPt) "EMPATE (Push)" else "TIE (Push)"
                isWin -> if (isPt) "VITÓRIA" else "WIN"
                isLoss -> if (isPt) "DERROTA" else "LOSS"
                else -> rec.outcome.uppercase()
            }

            Text(
                text = outcomeText,
                color = stateColor,
                fontWeight = FontWeight.Black,
                fontSize = 10.sp,
                letterSpacing = 0.5.sp
            )
            Text(
                text = if (rec.outcome == "Observe") (if (isPt) "Aposta Inicial" else "Starter bet") else "$symbol$displayVal",
                color = stateColor,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
    }
}

private fun formatBetTypeFull(state: UiState): String {
    return when (state.betType) {
        BetType.Observe -> "Observe Cycle"
        BetType.Attack -> "Attack Lvl ${state.level}"
        BetType.Recovery -> "Recovery Lvl ${state.level}"
        BetType.Profit -> "Profit series Step ${state.profitIndex + 1}"
    }
}

// ==========================================
// 3. HISTORY SCREEN (Room SQLite Logs list)
// ==========================================
@Composable
fun HistoryScreen(
    historyList: List<HistoryShoe>,
    onDelete: (Int) -> Unit,
    onClearAll: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "HISTORIC SHOE SESSIONS",
                color = PolishTextLight,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                letterSpacing = 0.5.sp
            )

            if (historyList.isNotEmpty()) {
                TextButton(
                    onClick = { showDeleteConfirm = true }
                ) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = PolishBankerBg)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("WIPE DATA", color = PolishBankerBg, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (historyList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.AutoStories, contentDescription = null, tint = PolishTextGray, modifier = Modifier.size(54.dp))
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("No sessions recorded yet.", color = PolishTextLight, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Save shoes playing inside the Predictor tab.", color = PolishTextGray, fontSize = 12.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(historyList) { shoe ->
                    HistoryItemCard(shoe = shoe, onDelete = { onDelete(shoe.id) })
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Wipe Baccarat Database?", color = PolishTextLight) },
            text = { Text("Are you sure you want to delete all historic shoes permanently? This cannot be undone.", color = PolishTextGray) },
            confirmButton = {
                Button(
                    onClick = {
                        onClearAll()
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PolishBankerBg)
                ) {
                    Text("WIPE DATABASE", color = PolishBankerText, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("CANCEL", color = PolishTextGray)
                }
            },
            containerColor = PolishCard
        )
    }
}

@Composable
fun HistoryItemCard(shoe: HistoryShoe, onDelete: () -> Unit) {
    val formatter = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }
    val timeString = formatter.format(Date(shoe.timestamp))

    val isWinner = shoe.netWinLoss >= 0
    val statColor = if (isWinner) PolishGreen else PolishBankerBg
    val sign = if (isWinner) "+" else ""

    Card(
        colors = CardDefaults.cardColors(containerColor = PolishCard),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, PolishBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Shoe session #ID ${shoe.id}",
                        color = PolishTextLight,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(timeString, color = PolishTextGray, fontSize = 10.sp)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (shoe.isWinGoalMet) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(PolishGreen.copy(alpha=0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("GOAL MET", color = PolishGreen, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete record", tint = PolishTextGray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricItem(label = "BASE BET", value = "$${shoe.baseBet.toInt()}")
                MetricItem(label = "ROUNDS", value = "${shoe.roundsCount}")
                MetricItem(
                    label = "NET STATE",
                    value = "$sign$${shoe.netWinLoss.toInt()}",
                    color = statColor
                )
                MetricItem(label = "FINAL WALLET", value = "$${shoe.endingTotalBankroll.toInt()}")
            }

            if (shoe.decisionsHistory.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = PolishBorder, thickness = 1.dp)
                Spacer(modifier = Modifier.height(8.dp))
                Text("ROAD PERFORMANCE PREVIEW:", color = PolishTextGray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                
                val list = shoe.decisionsHistory.split(",").mapNotNull {
                    try { Decision.valueOf(it) } catch(e: Exception) { null }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    list.forEach { dec ->
                        val initial = dec.name.first().toString()
                        val (col, txt) = when(dec) {
                            Decision.Player -> Pair(PolishPlayerBg, PolishPlayerText)
                            Decision.Banker -> Pair(PolishBankerBg, PolishBankerText)
                            Decision.Tie -> Pair(PolishTieBg, PolishTieText)
                        }
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(col), 
                            contentAlignment = Alignment.Center
                        ) {
                            Text(initial, color = txt, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetricItem(label: String, value: String, color: Color = PolishTextLight) {
    Column {
        Text(label, color = PolishTextGray, fontSize = 8.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
        Text(value, color = color, fontSize = 13.sp, fontWeight = FontWeight.Black)
    }
}

// ==========================================
// 4. HELP SCREEN (Strategy guide viewer)
// ==========================================
@Composable
fun HelpRulesScreen(onRestartOnboarding: () -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                "SYSTEM DOCUMENTATION & RULES",
                color = PolishTextLight,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                letterSpacing = 0.5.sp
            )
        }

        item {
            OutlinedButton(
                onClick = onRestartOnboarding,
                border = BorderStroke(1.dp, PolishPrimaryBlue.copy(alpha = 0.5f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = PolishPrimaryBlue),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("onboarding_restart_button")
            ) {
                Icon(Icons.Default.HelpCenter, contentDescription = "Restart Guided Tour")
                Spacer(modifier = Modifier.width(8.dp))
                Text("REPLAY ONBOARDING TUTORIAL", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        // Baccarat Rhythm Betting (BRB)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = PolishCard),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, PolishBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        "1. NEXT BACCARAT SYSTEM 3.0",
                        fontWeight = FontWeight.Bold,
                        color = PolishPrimaryBlue,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        "THE SAUCE",
                        color = PolishTextLight,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Five-Bet Betting Formula
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = PolishCard),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, PolishBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        "2. FIVE-BET BETTING FORMULA",
                        fontWeight = FontWeight.Bold,
                        color = PolishBankerBg,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        "The system segregates your shoe stakes into three distinct layers depending on local performance:\n\n" +
                        "• Attack Bets (Levels 1 to 3): Consists of three multipliers: 1x, 2x, and 3x your Base Bet respectively. You increase 1 level on loss. If you win, you drop 1 level. If you win 2 wagers in a row or 2 out of 3, drop 2 levels.\n" +
                        "• Recovery Bets (Levels 4 and 5): Consists of 4x and 8x base bet. Activated once Attack level 3 loses. If you win either recovery bet, immediately drop back down to Attack Level 2 on the next round. If Level 5 loses, the current Game Bankroll is lost (session reset).\n" +
                        "• Profit Betting: Triggered upon winning two wagers in a row while at Level 1 base bet. The profit series multiplier starts at 1.4x, drops to 1x, rises to 1.2x, then increases by +0.4x with each concurrent victory (1.4 -> 1.0 -> 1.2 -> 1.6 -> 2.0). Any profit loss terminates Profit Betting instantly, returning to Attack Level 2.",
                        color = PolishTextLight,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Bankroll Architecture Table
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = PolishCard),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, PolishBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        "3. THE FIVE-COUNT BANKROLL MATRIX",
                        fontWeight = FontWeight.Bold,
                        color = PolishDarkGold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        "• Game Bankroll (Session limit): Equals $18X where X is the Base Bet (calculated as 1x+2x+3x+4x+8x = 18x). This is the maximum sum to buy-in at any individual shoe.\n" +
                        "• Total Trip Bankroll (Risk limit): Equals $90X (5 sessions). Represents the recommended full safety capital necessary to deploy the strategy securely.\n" +
                        "• Win Goal: Target profit is set to $8X per shoe. Once reached, players are highly advised to clear history, take a recess, and reset.",
                        color = PolishTextLight,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

// ==========================================
// 5. SETTINGS / APP OVERRIDES
// ==========================================
@Composable
fun CommissionSettingsDialog(
    useCommission: Boolean,
    onToggleCommission: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Game Rules Configuration", color = PolishTextLight) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Standard high-fidelity Baccarat models usually apply a 5% house commission on winning Banker bets. You can toggle this on or off below.",
                    color = PolishTextGray,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(PolishCardLighter)
                        .border(1.dp, PolishBorder, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Banker 5% Commission", color = PolishTextLight, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(
                            text = if (useCommission) "On (standard 0.95:1 payout)" else "Off (1:1 payout - No Commission/Super 6)",
                            color = if (useCommission) PolishPrimaryBlue else PolishTextGray,
                            fontSize = 10.sp
                        )
                    }

                    Switch(
                        checked = useCommission,
                        onCheckedChange = { onToggleCommission(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = PolishPlayerText,
                            checkedTrackColor = PolishPrimaryBlue,
                            uncheckedThumbColor = PolishTextGray,
                            uncheckedTrackColor = PolishCard
                        ),
                        modifier = Modifier.testTag("commission_switch")
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = PolishPrimaryBlue)
            ) {
                Text("DONE", color = PolishPlayerText, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = PolishCard,
        textContentColor = PolishTextLight
    )
}

@Composable
fun OnboardingTutorial(
    isPt: Boolean = false,
    onDismiss: () -> Unit
) {
    var currentStep by remember { mutableIntStateOf(0) }
    
    // Define slides
    val slides = if (isPt) {
        listOf(
            OnboardingSlide(
                title = "Domine a Mesa",
                headline = "Bem-vindo ao NBS 3.0",
                body = "Nosso motor de estratégia automatiza a rígida disciplina matemática do baccarat em tempo real.\n\nFaremos uma rápida visita guiada para dominar sua banca e as camadas estratégicas avançadas.",
                icon = Icons.Default.Casino,
                iconTint = PolishPrimaryBlue
            ),
            OnboardingSlide(
                title = "Alocação de Capital",
                headline = "Passo 1: Dimensionamento da Banca",
                body = "O dimensionamento é baseado na sua Aposta Base (X):\n\n• Limite por Sessão (Banca do Jogo) = 18X\n(Derivado de: 1x + 2x + 3x + 4x + 8x = 18x)\n\n• Unidade de Segurança do Bolso = 90X\nBuffer total sugerido para em média cinco sessões para proteger seu capital contra a variância.",
                icon = Icons.Default.MonetizationOn,
                iconTint = PolishDarkGold
            ),
            OnboardingSlide(
                title = "Progressão de Apostas",
                headline = "Passo 2: Attack & Recovery",
                body = "O Five-Count escala as apostas conforme os resultados das rodadas:\n\n• Apostas Attack (Nível 1 a 3: 1X, 2X, 3X). Aumenta 1 nível na derrota e diminui 1 nível na vitória, retornando para 1X em vitórias consecutivas.\n\n• Apostas Recovery (4X e 8X) são ativadas na derrota do Nível 3 como proteção. Ganhar qualquer uma de forma oportuna volta você instantaneamente ao Attack Nível 2.",
                icon = Icons.Default.TrendingUp,
                iconTint = PolishBankerBg
            ),
            OnboardingSlide(
                title = "Sequência de Lucro",
                headline = "Passo 3: Multiplicadores de Profit",
                body = "Ganhar duas rodadas seguidas no Nível 1 ativa nossa sequência de lucro Profit de alta rentabilidade:\n\n• A escala segue em 1.4X → 1.0X → 1.2X, depois ganha +0.4X com cada vitória combinada.\n\n• Captura lucros excelentes em sequências favoráveis. Qualquer derrota simples retorna você ao Nível 2.",
                icon = Icons.Default.FlashOn,
                iconTint = PolishGreen
            ),
            OnboardingSlide(
                title = "Meta Disciplinada",
                headline = "Passo 4: Travar Lucro",
                body = "Uma meta conservadora garante a vitória de forma sustentável:\n\n• A Meta de Lucro por Sessão é fixada em +8X do valor da aposta base.\n\n• Ao atingir +8X de lucro líquido, o sistema sugerirá encerrar e travar o caixa. Pare de jogar, salve estatísticas e descanse.",
                icon = Icons.Default.EmojiEvents,
                iconTint = PolishPlayerBg
            )
        )
    } else {
        listOf(
            OnboardingSlide(
                title = "Master the Table",
                headline = "Welcome to NBS 3.0",
                body = "Our strategy engine automates the strict mathematical discipline of baccarat betting in real-time.\n\nLet's take a quick guided tour to master your bankroll setup and the advanced strategy layers.",
                icon = Icons.Default.Casino,
                iconTint = PolishPrimaryBlue
            ),
            OnboardingSlide(
                title = "Capital Allocation",
                headline = "Step 1: Bankroll Sizing",
                body = "Sizing is based on your chosen Base Bet (X):\n\n• Session Buy-In (Game Bankroll) = 18X\n(Derived from: 1x + 2x + 3x + 4x + 8x = 18x)\n\n• Target Trip Bankroll = 90X\nRecommended five-session total buffer to safeguard your playing capital against variance.",
                icon = Icons.Default.MonetizationOn,
                iconTint = PolishDarkGold
            ),
            OnboardingSlide(
                title = "Sizing Formula",
                headline = "Step 2: Attack & Recovery",
                body = "The Five-Bet strategy scales bets depending on round outcomes:\n\n• Attack Level 1 to 3 wagers (1X, 2X, 3X). You increase one level on loss, and decrease one level on win, dropping to 1X on back-to-back victories.\n\n• Recovery Bets (4X & 8X) trigger on Level 3 loss as a cushion. Winning either instantly drops you back to Attack Level 2.",
                icon = Icons.Default.TrendingUp,
                iconTint = PolishBankerBg
            ),
            OnboardingSlide(
                title = "The Profit Run",
                headline = "Step 3: Profit Wave Multipliers",
                body = "Winning two wagers in a row at Level 1 triggers our high-return Profit sequence:\n\n• Bet structure scales as 1.4X → 1.0X → 1.2X, then gains +0.4X with each successive win.\n\n• This captures high-yield hot streaks. Any single defeat instantly safely returns you to Level 2.",
                icon = Icons.Default.FlashOn,
                iconTint = PolishGreen
            ),
            OnboardingSlide(
                title = "Discipline Goals",
                headline = "Step 4: Realize & Lock Profit",
                body = "A conservative target wins the war:\n\n• Session Win Goal is set to 8X of your base bet.\n\n• Upon reaching +8X net profit, the system prompts a session lock advisory. Stop playing, save your statistics, clear shoe logs, and rest.",
                icon = Icons.Default.EmojiEvents,
                iconTint = PolishPlayerBg
            )
        )
    }

    val slide = slides[currentStep]

    AlertDialog(
        onDismissRequest = {}, // Force user to use next/prev/dismiss to complete
        confirmButton = {
            if (currentStep < slides.size - 1) {
                Button(
                    onClick = { currentStep++ },
                    colors = ButtonDefaults.buttonColors(containerColor = PolishPrimaryBlue)
                ) {
                    Text(if (isPt) "PRÓXIMO" else "NEXT STEP", color = PolishPlayerText, fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = PolishGreen)
                ) {
                    Text(if (isPt) "INICIAR" else "GET STARTED (DONE)", color = Color(0xFF003311), fontWeight = FontWeight.Black)
                }
            }
        },
        dismissButton = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (currentStep > 0) {
                    TextButton(
                        onClick = { currentStep-- }
                    ) {
                        Text(if (isPt) "ANTERIOR" else "PREVIOUS", color = PolishTextLight)
                    }
                }
                TextButton(
                    onClick = onDismiss
                ) {
                    Text(if (isPt) "PULAR TUTORIAL" else "SKIP TOUR", color = PolishTextGray.copy(alpha = 0.6f))
                }
            }
        },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(PolishCardLighter),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = slide.icon,
                        contentDescription = null,
                        tint = slide.iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = slide.title.uppercase(),
                        fontWeight = FontWeight.ExtraBold,
                        color = slide.iconTint,
                        fontSize = 10.sp,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = slide.headline,
                        fontWeight = FontWeight.Bold,
                        color = PolishTextLight,
                        fontSize = 16.sp
                    )
                }
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = slide.body,
                    color = PolishTextLight,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )

                // Step dots indicator
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    slides.forEachIndexed { idx, _ ->
                        val isActive = idx == currentStep
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .size(if (isActive) 10.dp else 6.dp)
                                .clip(CircleShape)
                                .background(if (isActive) PolishPrimaryBlue else PolishBorder)
                        )
                    }
                }
            }
        },
        containerColor = PolishCard,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("onboarding_dialog")
    )
}

data class OnboardingSlide(
    val title: String,
    val headline: String,
    val body: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val iconTint: Color
)
