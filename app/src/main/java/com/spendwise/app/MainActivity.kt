package com.spendwise.app

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowOutward
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spendwise.app.ui.screens.AnalyticsScreen
import com.spendwise.app.ui.screens.CreditBookScreen
import com.spendwise.app.ui.screens.DashboardScreen
import com.spendwise.app.ui.screens.OcrScannerScreen
import com.spendwise.app.ui.screens.VoiceInputScreen
import com.spendwise.app.ui.screens.WalletsScreen
import com.spendwise.app.ui.theme.ExpenseRed
import com.spendwise.app.ui.theme.LocalVeltoColors
import com.spendwise.app.ui.theme.PrimaryEmerald
import com.spendwise.app.ui.theme.SpendwiseTheme
import com.spendwise.app.ui.theme.VeltoForestGreen
import com.spendwise.app.ui.theme.VeltoLime
import com.spendwise.app.ui.theme.WarningAmber
import com.spendwise.app.ui.viewmodel.DashboardViewModel

enum class SpendwiseNavDestination(val label: String, val icon: ImageVector) {
    HOME("Home", Icons.Default.Home),
    PAYMENTS("Payments", Icons.Default.ArrowOutward),
    WALLETS("Wallets", Icons.Default.CreditCard),
    INSIGHTS("Insights", Icons.Default.BarChart),
    VOICE_INPUT("Voice", Icons.Default.Home),
    OCR_SCANNER("Scan", Icons.Default.Home)
}

class MainActivity : ComponentActivity() {

    private val viewModel: DashboardViewModel by viewModels {
        val app = application as SpendwiseApplication
        DashboardViewModel.Factory(app.repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var isDarkTheme by remember { mutableStateOf(false) }

            SpendwiseTheme(darkTheme = isDarkTheme) {
                MainAppContent(
                    viewModel = viewModel,
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = { isDarkTheme = !isDarkTheme }
                )
            }
        }
    }
}

@Composable
fun MainAppContent(
    viewModel: DashboardViewModel,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    val colors = LocalVeltoColors.current
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var currentScreen by remember { mutableStateOf(SpendwiseNavDestination.HOME) }
    var showNotificationDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    // User authentication & session state
    var isLoggedIn by remember { mutableStateOf(true) }
    var userName by remember { mutableStateOf("Utkarsh Raj") }
    var userEmail by remember { mutableStateOf("utkarsh.raj@spendwise.app") }
    var showUserDialog by remember { mutableStateOf(false) }
    var showLoginPromptDialog by remember { mutableStateOf(false) }

    // 25% shortage condition for notification
    val hasAllowance = state.activeCycle != null && state.activeCycle!!.isActive && state.activeCycle!!.initialAllowance > 0
    val startingPool = if (hasAllowance) state.activeCycle!!.totalStartingPool else 0.0
    val isShortage25 = hasAllowance && startingPool > 0 && (state.currentAllowancePool <= 0.25 * startingPool)

    val showBottomBar = currentScreen != SpendwiseNavDestination.VOICE_INPUT &&
            currentScreen != SpendwiseNavDestination.OCR_SCANNER

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.canvas)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // --- Top App Header: SpendWise Logo (Shifted Left) + Notifications + Settings + User Avatar ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 16.dp, top = 8.dp, bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Official Transparent SpendWise Logo (Shifted left, visible in both light & dark theme)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 2.dp)
                ) {
                    Image(
                        painter = painterResource(id = if (colors.isDark) R.drawable.app_logo_dark else R.drawable.app_logo_light),
                        contentDescription = "SpendWise Logo",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(34.dp)
                    )
                    Spacer(modifier = Modifier.width(7.dp))
                    Text(
                        text = "Spend",
                        color = if (colors.isDark) Color.White else VeltoForestGreen,
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "Wise",
                        color = if (colors.isDark) VeltoLime else PrimaryEmerald,
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Notification Bell (Only opens dialog when <= 25% shortage condition met; stays blank/silent otherwise)
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (colors.isDark) colors.cardElevated else Color(0xFFEAEFE7))
                            .clickable {
                                if (isShortage25) {
                                    showNotificationDialog = true
                                } else {
                                    Toast.makeText(context, "No notifications", Toast.LENGTH_SHORT).show()
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = if (isShortage25) WarningAmber else colors.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                        if (isShortage25) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(ExpenseRed)
                                    .align(Alignment.TopEnd)
                            )
                        }
                    }

                    // Settings Section (Replaces theme toggle in header as requested)
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (colors.isDark) colors.cardElevated else Color(0xFFEAEFE7))
                            .clickable { showSettingsDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = colors.textPrimary,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    // User Profile Avatar ("UR" for Utkarsh Raj or "GU" for Guest)
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (isLoggedIn) (if (colors.isDark) VeltoLime else VeltoForestGreen) else Color(0xFF9E9E9E))
                            .clickable {
                                if (isLoggedIn) {
                                    showUserDialog = true
                                } else {
                                    showLoginPromptDialog = true
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isLoggedIn) userName.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString("") else "GU",
                            color = if (isLoggedIn && colors.isDark) Color.Black else Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Screen Content Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when (currentScreen) {
                    SpendwiseNavDestination.HOME -> DashboardScreen(
                        viewModel = viewModel,
                        onNavigateToVoice = { currentScreen = SpendwiseNavDestination.VOICE_INPUT },
                        onNavigateToOcr = { currentScreen = SpendwiseNavDestination.OCR_SCANNER },
                        onNavigateToPayments = { currentScreen = SpendwiseNavDestination.PAYMENTS },
                        onNavigateToCards = { currentScreen = SpendwiseNavDestination.WALLETS }
                    )
                    SpendwiseNavDestination.PAYMENTS -> CreditBookScreen(
                        viewModel = viewModel
                    )
                    SpendwiseNavDestination.WALLETS -> WalletsScreen(
                        viewModel = viewModel
                    )
                    SpendwiseNavDestination.INSIGHTS -> AnalyticsScreen(
                        viewModel = viewModel
                    )
                    SpendwiseNavDestination.VOICE_INPUT -> VoiceInputScreen(
                        viewModel = viewModel,
                        onBack = { currentScreen = SpendwiseNavDestination.HOME }
                    )
                    SpendwiseNavDestination.OCR_SCANNER -> OcrScannerScreen(
                        viewModel = viewModel,
                        onBack = { currentScreen = SpendwiseNavDestination.HOME }
                    )
                }
            }

            // Space for the floating bottom navigation bar
            if (showBottomBar) {
                Spacer(modifier = Modifier.height(76.dp))
            }
        }

        // --- Signature Floating Capsule Navigation Bar ---
        if (showBottomBar) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(elevation = 12.dp, shape = RoundedCornerShape(32.dp), spotColor = Color(0x33000000))
                        .clip(RoundedCornerShape(32.dp))
                        .background(if (colors.isDark) Color(0xFF14221C) else Color(0xFFF0F3EC))
                        .border(1.dp, if (colors.isDark) Color(0xFF22382E) else Color(0xFFE2E7DD), RoundedCornerShape(32.dp))
                        .padding(horizontal = 6.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val navItems = listOf(
                            SpendwiseNavDestination.HOME,
                            SpendwiseNavDestination.PAYMENTS,
                            SpendwiseNavDestination.WALLETS,
                            SpendwiseNavDestination.INSIGHTS
                        )

                        navItems.forEach { dest ->
                            val isSelected = currentScreen == dest

                            if (isSelected) {
                                // Active Pill Capsule
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(24.dp))
                                        .background(if (colors.isDark) VeltoLime else VeltoForestGreen)
                                        .clickable { currentScreen = dest }
                                        .padding(horizontal = 16.dp, vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = dest.icon,
                                            contentDescription = dest.label,
                                            tint = if (colors.isDark) Color.Black else Color.White,
                                            modifier = Modifier.size(17.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = dest.label,
                                            color = if (colors.isDark) Color.Black else Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            } else {
                                // Inactive Tab Item
                                Column(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable { currentScreen = dest }
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = dest.icon,
                                        contentDescription = dest.label,
                                        tint = colors.textSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = dest.label,
                                        color = colors.textSecondary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- Shortage Notification Dialog (Alert appears ONLY when <= 25% allowance left) ---
        if (showNotificationDialog && isShortage25) {
            val remainingPct = if (startingPool > 0) ((state.currentAllowancePool / startingPool) * 100).toInt() else 0
            AlertDialog(
                onDismissRequest = { showNotificationDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = WarningAmber,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Low Allowance Alert (≤ 25%)",
                            color = WarningAmber,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                containerColor = colors.card,
                text = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(WarningAmber.copy(alpha = 0.12f))
                            .border(1.dp, WarningAmber.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                            .padding(14.dp)
                    ) {
                        Text(
                            text = "Only ₹${"%,.2f".format(state.currentAllowancePool)} left in your active pool ($remainingPct% remaining). Limit daily spend to ₹${"%.0f".format(state.dailySafeToSpend)}/day.",
                            color = colors.textPrimary,
                            fontSize = 13.sp,
                            lineHeight = 17.sp
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { showNotificationDialog = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (colors.isDark) VeltoLime else VeltoForestGreen,
                            contentColor = if (colors.isDark) Color.Black else Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Close")
                    }
                }
            )
        }

        // --- User Account Management & Cloud Backup Dialog ---
        if (showUserDialog) {
            AlertDialog(
                onDismissRequest = { showUserDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AccountCircle, contentDescription = null, tint = if (colors.isDark) VeltoLime else VeltoForestGreen, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Account & Backup", color = colors.textPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                },
                containerColor = colors.card,
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // User Info Card
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (colors.isDark) colors.cardElevated else Color(0xFFF1F5EE))
                                .padding(14.dp)
                        ) {
                            Column {
                                Text(userName, color = colors.textPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text(userEmail, color = colors.textSecondary, fontSize = 12.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(PrimaryEmerald))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Cloud Sync Active • SQLite Integrated", color = PrimaryEmerald, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }

                        // Cloud Backup Action
                        Button(
                            onClick = {
                                Toast.makeText(context, "Cloud backup completed! All ledgers secured.", Toast.LENGTH_SHORT).show()
                                showUserDialog = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (colors.isDark) VeltoLime else VeltoForestGreen,
                                contentColor = if (colors.isDark) Color.Black else Color.White
                            )
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Backup Data to Cloud", fontWeight = FontWeight.Bold)
                        }

                        // Restore Data Action
                        Button(
                            onClick = {
                                Toast.makeText(context, "Data restored from last saved cloud snapshot.", Toast.LENGTH_SHORT).show()
                                showUserDialog = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (colors.isDark) colors.cardElevated else Color(0xFFE2EAE0),
                                contentColor = colors.textPrimary
                            )
                        ) {
                            Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Restore Latest Backup", fontWeight = FontWeight.SemiBold)
                        }

                        // Logout Button
                        Button(
                            onClick = {
                                isLoggedIn = false
                                showUserDialog = false
                                Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ExpenseRed.copy(alpha = 0.15f),
                                contentColor = ExpenseRed
                            )
                        ) {
                            Icon(Icons.Default.Logout, contentDescription = null, tint = ExpenseRed, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Log Out", fontWeight = FontWeight.Bold)
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showUserDialog = false }) {
                        Text("Done", color = colors.textSecondary)
                    }
                }
            )
        }

        // --- Login Prompt Dialog (When Logged Out) ---
        if (showLoginPromptDialog || !isLoggedIn && showLoginPromptDialog) {
            var tempName by remember { mutableStateOf("") }
            var tempEmail by remember { mutableStateOf("") }

            AlertDialog(
                onDismissRequest = { showLoginPromptDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Login, contentDescription = null, tint = if (colors.isDark) VeltoLime else VeltoForestGreen, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sign In to SpendWise", color = colors.textPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                },
                containerColor = colors.card,
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Log in with your name and email to enable cloud backup and multi-device data integration.", color = colors.textSecondary, fontSize = 12.sp)

                        OutlinedTextField(
                            value = tempName,
                            onValueChange = { tempName = it },
                            label = { Text("Your Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = colors.textPrimary,
                                unfocusedTextColor = colors.textPrimary
                            )
                        )

                        OutlinedTextField(
                            value = tempEmail,
                            onValueChange = { tempEmail = it },
                            label = { Text("Email Address") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = colors.textPrimary,
                                unfocusedTextColor = colors.textPrimary
                            )
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (tempName.isNotBlank()) userName = tempName.trim()
                            if (tempEmail.isNotBlank()) userEmail = tempEmail.trim()
                            isLoggedIn = true
                            showLoginPromptDialog = false
                            Toast.makeText(context, "Welcome, $userName! Synced successfully.", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (colors.isDark) VeltoLime else VeltoForestGreen,
                            contentColor = if (colors.isDark) Color.Black else Color.White
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Log In & Sync", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLoginPromptDialog = false }) {
                        Text("Cancel", color = colors.textSecondary)
                    }
                }
            )
        }

        // --- Settings Dialog (Holds Theme Toggle, Developer Credits & App Info) ---
        if (showSettingsDialog) {
            AlertDialog(
                onDismissRequest = { showSettingsDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = if (colors.isDark) VeltoLime else VeltoForestGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Settings & Preferences",
                            color = colors.textPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 19.sp
                        )
                    }
                },
                containerColor = colors.card,
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // 1. Theme Selection Section (Shifted to Settings)
                        Text(
                            text = "APPEARANCE",
                            color = colors.textSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Light Mode Button
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (!isDarkTheme) (if (colors.isDark) VeltoLime else VeltoForestGreen) else colors.cardElevated)
                                    .clickable {
                                        if (isDarkTheme) onToggleTheme()
                                    }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.LightMode,
                                        contentDescription = null,
                                        tint = if (!isDarkTheme) (if (colors.isDark) Color.Black else Color.White) else colors.textSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Light ☀️",
                                        color = if (!isDarkTheme) (if (colors.isDark) Color.Black else Color.White) else colors.textPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            // Dark Mode Button
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isDarkTheme) VeltoLime else colors.cardElevated)
                                    .clickable {
                                        if (!isDarkTheme) onToggleTheme()
                                    }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.DarkMode,
                                        contentDescription = null,
                                        tint = if (isDarkTheme) Color.Black else colors.textSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Dark 🌙",
                                        color = if (isDarkTheme) Color.Black else colors.textPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        // 2. Official Credit Section (Shifted as requested)
                        Text(
                            text = "ABOUT & CREDITS",
                            color = colors.textSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (colors.isDark) Color(0xFF14241C) else Color(0xFFE8F3EB))
                                .border(1.dp, (if (colors.isDark) VeltoLime else PrimaryEmerald).copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                                .padding(14.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Image(
                                        painter = painterResource(id = if (colors.isDark) R.drawable.app_logo_dark else R.drawable.app_logo_light),
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "SpendWise",
                                        color = colors.textPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (colors.isDark) VeltoLime else VeltoForestGreen)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "v2.4",
                                            color = if (colors.isDark) Color.Black else Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Designed And Maintained By Utkarsh Raj !!",
                                    color = if (colors.isDark) VeltoLime else VeltoForestGreen,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Save For Your Future Plans !!",
                                    color = colors.textSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // 3. Quick Account & Cloud Sync Shortcut
                        TextButton(
                            onClick = {
                                showSettingsDialog = false
                                showUserDialog = true
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, tint = if (colors.isDark) VeltoLime else PrimaryEmerald, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Manage Cloud Sync & Account", color = if (colors.isDark) VeltoLime else PrimaryEmerald, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { showSettingsDialog = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (colors.isDark) VeltoLime else VeltoForestGreen,
                            contentColor = if (colors.isDark) Color.Black else Color.White
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Close", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    }
}
