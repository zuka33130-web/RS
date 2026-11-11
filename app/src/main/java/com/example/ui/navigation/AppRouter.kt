package com.example.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.data.model.WorkOrderWithClient

/**
 * Rutas de la aplicación Retro State RS.
 */
sealed class AppRoute(val key: String) {
    // Tab de Navegación Principal
    object Dashboard : AppRoute("dashboard")
    object Finances : AppRoute("finances")
    object Tasks : AppRoute("tasks")
    object Supplies : AppRoute("supplies")
    object Settings : AppRoute("settings")

    // Subpantallas secundarias
    object Analytics : AppRoute("analytics")
    data class OrderDetail(val orderWithClient: WorkOrderWithClient) : AppRoute("order_detail_${orderWithClient.order.id}")
    data class QuoteEditor(val orderWithClient: WorkOrderWithClient? = null) : AppRoute("quote_editor")
}

/**
 * Estado del Router centralizado para navegación reactiva sin librerías externas.
 */
class AppRouterState(initialRoute: AppRoute = AppRoute.Dashboard) {

    var currentRoute by mutableStateOf<AppRoute>(initialRoute)
        private set

    private val backStack = mutableListOf<AppRoute>()

    /**
     * Navega hacia una nueva pantalla guardando la anterior en el backstack.
     */
    fun navigateTo(route: AppRoute) {
        if (currentRoute != route) {
            backStack.add(currentRoute)
            currentRoute = route
        }
    }

    /**
     * Navega directamente a un Tab principal limpiando las pantallas secundarias del historial.
     */
    fun navigateToTab(tabRoute: AppRoute) {
        backStack.clear()
        currentRoute = tabRoute
    }

    /**
     * Retrocede a la pantalla anterior o vuelve al Dashboard principal.
     */
    fun popBackStack(): Boolean {
        return if (backStack.isNotEmpty()) {
            currentRoute = backStack.removeAt(backStack.size - 1)
            true
        } else if (currentRoute !is AppRoute.Dashboard) {
            currentRoute = AppRoute.Dashboard
            true
        } else {
            false
        }
    }

    val isTopTab: Boolean
        get() = currentRoute is AppRoute.Dashboard ||
                currentRoute is AppRoute.Finances ||
                currentRoute is AppRoute.Tasks ||
                currentRoute is AppRoute.Supplies ||
                currentRoute is AppRoute.Settings
}

@Composable
fun rememberAppRouter(initialRoute: AppRoute = AppRoute.Dashboard): AppRouterState {
    return remember { AppRouterState(initialRoute) }
}
