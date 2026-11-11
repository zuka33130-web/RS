package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.MonetizationOn
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material.icons.outlined.Stars
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.AppDatabase
import com.example.data.model.ClientEntity
import com.example.data.model.TransactionType
import com.example.data.model.WorkOrderWithClient
import com.example.data.repository.WorkshopRepository
import com.example.ui.WorkshopViewModel
import com.example.ui.WorkshopViewModelFactory
import com.example.ui.daily.DailyManagementViewModel
import com.example.ui.daily.DailyManagementViewModelFactory
import com.example.ui.dialogs.NewCrmOrderDialog
import com.example.ui.dialogs.NewSupplyDialog
import com.example.ui.dialogs.NewTaskDialog
import com.example.ui.dialogs.NewTransactionDialog
import com.example.ui.finance.FinanceViewModel
import com.example.ui.finance.FinanceViewModelFactory
import com.example.ui.navigation.AppRoute
import com.example.ui.navigation.rememberAppRouter
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.FinanceScreen
import com.example.ui.screens.OrderDetailScreen
import com.example.ui.screens.QuoteEditorScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.ShoppingListScreen
import com.example.ui.screens.TaskScreen
import com.example.ui.settings.SettingsViewModel
import com.example.ui.settings.SettingsViewModelFactory
import com.example.ui.theme.ArcadeCardBorder
import com.example.ui.theme.ArcadeSurface
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.PixelRedHeart
import com.example.ui.theme.RetroGoldCoin
import com.example.ui.theme.RetroMuted
import com.example.util.RetroDocumentGenerator
import com.example.util.WhatsAppSender

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainApp()
            }
        }
    }
}

enum class NavigationScreen(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val route: AppRoute
) {
    WORKSHOP("Taller RS", Icons.Filled.SportsEsports, Icons.Outlined.SportsEsports, AppRoute.Dashboard),
    FINANCES("Billetera RS", Icons.Filled.MonetizationOn, Icons.Outlined.MonetizationOn, AppRoute.Finances),
    TASKS("Tareas", Icons.Filled.Stars, Icons.Outlined.Stars, AppRoute.Tasks),
    SUPPLIES("Compras", Icons.Filled.ShoppingCart, Icons.Outlined.ShoppingCart, AppRoute.Supplies),
    SETTINGS("Ajustes", Icons.Filled.Settings, Icons.Outlined.Settings, AppRoute.Settings)
}

@Composable
fun MainApp() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val database = remember { AppDatabase.getDatabase(context, scope) }
    val repository = remember { WorkshopRepository(database) }

    val workshopViewModel: WorkshopViewModel = viewModel(
        factory = WorkshopViewModelFactory(repository)
    )
    val financeViewModel: FinanceViewModel = viewModel(
        factory = FinanceViewModelFactory(repository)
    )
    val settingsViewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModelFactory(repository)
    )
    val dailyManagementViewModel: DailyManagementViewModel = viewModel(
        factory = DailyManagementViewModelFactory(repository)
    )
    val analyticsViewModel: com.example.ui.analytics.AnalyticsViewModel = viewModel(
        factory = com.example.ui.analytics.AnalyticsViewModelFactory(repository)
    )

    // Centralized state-based App Router
    val router = rememberAppRouter(AppRoute.Dashboard)
    val navScreens = NavigationScreen.values()

    // Dialog & overlay states
    var showNewCrmOrderDialog by remember { mutableStateOf(false) }
    var showNewTransactionDialog by remember { mutableStateOf(false) }
    var transactionDialogInitialType by remember { mutableStateOf(TransactionType.INCOME) }
    var showNewTaskDialog by remember { mutableStateOf(false) }
    var showNewSupplyDialog by remember { mutableStateOf(false) }

    // Counts for navigation badges
    val tasks by dailyManagementViewModel.tasks.collectAsStateWithLifecycle()
    val pendingTasksCount = tasks.count { !it.isCompleted }

    val supplies by dailyManagementViewModel.supplies.collectAsStateWithLifecycle()
    val neededSuppliesCount = supplies.count { it.isNeeded }
    val allCrmOrders by workshopViewModel.allCrmOrders.collectAsStateWithLifecycle()

    // Centralized BackHandler pop logic
    BackHandler(enabled = !router.isTopTab || router.currentRoute !is AppRoute.Dashboard) {
        router.popBackStack()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (router.isTopTab) {
                NavigationBar(
                    containerColor = ArcadeSurface,
                    tonalElevation = 8.dp,
                    modifier = Modifier
                        .border(androidx.compose.foundation.BorderStroke(1.dp, ArcadeCardBorder))
                        .testTag("main_bottom_nav")
                ) {
                    navScreens.forEach { screen ->
                        val isSelected = router.currentRoute == screen.route
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { router.navigateToTab(screen.route) },
                            modifier = Modifier.testTag("nav_tab_${screen.name.lowercase()}"),
                            icon = {
                                when (screen) {
                                    NavigationScreen.TASKS -> {
                                        if (pendingTasksCount > 0) {
                                            BadgedBox(badge = {
                                                Badge(containerColor = PixelRedHeart, contentColor = Color.White) {
                                                    Text("$pendingTasksCount", fontWeight = FontWeight.Black)
                                                }
                                            }) {
                                                Icon(
                                                    imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                                    contentDescription = screen.title
                                                )
                                            }
                                        } else {
                                            Icon(
                                                imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                                contentDescription = screen.title
                                            )
                                        }
                                    }
                                    NavigationScreen.SUPPLIES -> {
                                        if (neededSuppliesCount > 0) {
                                            BadgedBox(badge = {
                                                Badge(containerColor = RetroGoldCoin, contentColor = Color.Black) {
                                                    Text("$neededSuppliesCount", fontWeight = FontWeight.Black)
                                                }
                                            }) {
                                                Icon(
                                                    imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                                    contentDescription = screen.title
                                                )
                                            }
                                        } else {
                                            Icon(
                                                imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                                contentDescription = screen.title
                                            )
                                        }
                                    }
                                    else -> {
                                        Icon(
                                            imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                            contentDescription = screen.title
                                        )
                                    }
                                }
                            },
                            label = {
                                Text(
                                    text = screen.title,
                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = NeonCyan,
                                selectedTextColor = NeonCyan,
                                unselectedIconColor = RetroMuted,
                                unselectedTextColor = RetroMuted,
                                indicatorColor = NeonCyan.copy(alpha = 0.15f)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val route = router.currentRoute) {
                is AppRoute.Dashboard -> {
                    DashboardScreen(
                        viewModel = workshopViewModel,
                        onOpenNewOrder = { showNewCrmOrderDialog = true },
                        onOpenCrmOrderDetail = { crmOrder ->
                            router.navigateTo(AppRoute.OrderDetail(crmOrder))
                        },
                        onOpenSettings = { router.navigateToTab(AppRoute.Settings) },
                        onOpenAnalytics = { router.navigateTo(AppRoute.Analytics) }
                    )
                }
                is AppRoute.Finances -> {
                    FinanceScreen(
                        viewModel = financeViewModel,
                        onOpenIncomeDialog = {
                            transactionDialogInitialType = TransactionType.INCOME
                            showNewTransactionDialog = true
                        },
                        onOpenExpenseDialog = {
                            transactionDialogInitialType = TransactionType.EXPENSE
                            showNewTransactionDialog = true
                        }
                    )
                }
                is AppRoute.Tasks -> {
                    TaskScreen(
                        viewModel = dailyManagementViewModel,
                        onOpenNewTask = { showNewTaskDialog = true }
                    )
                }
                is AppRoute.Supplies -> {
                    ShoppingListScreen(
                        viewModel = dailyManagementViewModel,
                        onOpenDetailedDialog = { showNewSupplyDialog = true }
                    )
                }
                is AppRoute.Settings -> {
                    SettingsScreen(
                        viewModel = settingsViewModel
                    )
                }
                is AppRoute.OrderDetail -> {
                    val currentOrder = allCrmOrders.firstOrNull {
                        it.order.id == route.orderWithClient.order.id
                    } ?: route.orderWithClient
                    OrderDetailScreen(
                        orderWithClient = currentOrder,
                        onBack = { router.popBackStack() },
                        onUpdateStatus = { newStatus ->
                            workshopViewModel.updateCrmOrderStatus(currentOrder.order, newStatus)
                        },
                        onUpdateCosts = { partsCost, laborCost, diagnosis ->
                            workshopViewModel.updateCrmOrderCosts(currentOrder.order, partsCost, laborCost, diagnosis)
                        },
                        onDeleteOrder = {
                            workshopViewModel.deleteCrmOrder(currentOrder.order)
                            router.popBackStack()
                        },
                        onOpenQuoteEditor = { orderToQuote ->
                            router.navigateTo(AppRoute.QuoteEditor(orderToQuote))
                        }
                    )
                }
                is AppRoute.QuoteEditor -> {
                    val currentOrder = route.orderWithClient?.let { routeOrder ->
                        allCrmOrders.firstOrNull { it.order.id == routeOrder.order.id } ?: routeOrder
                    }
                    QuoteEditorScreen(
                        initialOrderWithClient = currentOrder,
                        onBack = { router.popBackStack() }
                    )
                }
                is AppRoute.Analytics -> {
                    com.example.ui.screens.AnalyticsScreen(
                        viewModel = analyticsViewModel,
                        onBack = { router.popBackStack() }
                    )
                }
            }
        }
    }

    // --- Active Dialogs ---
    if (showNewCrmOrderDialog) {
        NewCrmOrderDialog(
            onDismiss = { showNewCrmOrderDialog = false },
            onSave = { firstName, lastName, address, phone, clientNotes, deviceModel, serialNumber, reportedFault, internalNotes, photosUris, signatureUri ->
                workshopViewModel.createCrmOrder(
                    firstName = firstName,
                    lastName = lastName,
                    address = address,
                    phone = phone,
                    clientNotes = clientNotes,
                    deviceModel = deviceModel,
                    serialNumber = serialNumber,
                    reportedFault = reportedFault,
                    internalNotes = internalNotes,
                    photosUris = photosUris,
                    signatureUri = signatureUri
                ) { createdOrder ->
                    if (phone.isNotBlank()) {
                        val tempClient = ClientEntity(
                            firstName = firstName,
                            lastName = lastName,
                            address = address,
                            phone = phone
                        )
                        val receiptMsg = RetroDocumentGenerator.generateIntakeReceipt(createdOrder, tempClient)
                        WhatsAppSender.sendToWhatsApp(context, phone, receiptMsg)
                    }
                }
                showNewCrmOrderDialog = false
            }
        )
    }



    if (showNewTransactionDialog) {
        NewTransactionDialog(
            initialType = transactionDialogInitialType,
            onDismiss = { showNewTransactionDialog = false },
            onSave = { type, amount, category, description ->
                financeViewModel.addTransaction(
                    type = type,
                    amount = amount,
                    category = category,
                    description = description
                )
                showNewTransactionDialog = false
            }
        )
    }

    if (showNewTaskDialog) {
        NewTaskDialog(
            onDismiss = { showNewTaskDialog = false },
            onSave = { title, priority, relatedConsole ->
                dailyManagementViewModel.addTask(
                    title = title,
                    priority = priority,
                    relatedConsole = relatedConsole
                )
                showNewTaskDialog = false
            }
        )
    }

    if (showNewSupplyDialog) {
        NewSupplyDialog(
            onDismiss = { showNewSupplyDialog = false },
            onSave = { name, category, quantity, unit, isNeeded, estimatedPrice, supplier ->
                dailyManagementViewModel.addShoppingItem(
                    name = name,
                    category = category,
                    quantity = quantity,
                    unit = unit,
                    isNeeded = isNeeded,
                    estimatedPrice = estimatedPrice,
                    supplier = supplier
                )
                showNewSupplyDialog = false
            }
        )
    }
}

