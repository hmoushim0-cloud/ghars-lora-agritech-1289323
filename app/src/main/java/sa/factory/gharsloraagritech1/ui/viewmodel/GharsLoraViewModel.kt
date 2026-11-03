package sa.factory.gharsloraagritech1.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import sa.factory.gharsloraagritech1.domain.usecase.*
import sa.factory.gharsloraagritech1.model.GharsLoraItem
import sa.factory.gharsloraagritech1.model.GharsLoraOrder
import sa.factory.gharsloraagritech1.model.GharsLoraTelemetry
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class GharsLoraUiState(
    val catalog: List<GharsLoraItem> = emptyList(),
    val filteredCatalog: List<GharsLoraItem> = emptyList(),
    val selectedCategory: String = "الكل",
    val cart: List<GharsLoraItem> = emptyList(),
    val telemetry: GharsLoraTelemetry? = null,
    val lastOrder: GharsLoraOrder? = null,
    val isCheckingOut: Boolean = false,
    val activeTab: String = "home"
)

class GharsLoraViewModel(
    private val getCatalogUseCase: GetCatalogUseCase,
    private val manageCartUseCase: ManageCartUseCase,
    private val processCheckoutUseCase: ProcessCheckoutUseCase,
    private val observeHardwareTelemetryUseCase: ObserveHardwareTelemetryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(GharsLoraUiState())
    val uiState: StateFlow<GharsLoraUiState> = _uiState.asStateFlow()

    init {
        // Observe Catalog
        getCatalogUseCase().onEach { list ->
            _uiState.update { it.copy(catalog = list, filteredCatalog = list) }
        }.launchIn(viewModelScope)

        // Observe Cart
        manageCartUseCase.cart.onEach { cartItems ->
            _uiState.update { it.copy(cart = cartItems) }
        }.launchIn(viewModelScope)

        // Observe Telemetry
        observeHardwareTelemetryUseCase().onEach { telem ->
            _uiState.update { it.copy(telemetry = telem) }
        }.launchIn(viewModelScope)
    }

    fun selectCategory(category: String) {
        _uiState.update { state ->
            val filtered = if (category == "الكل") state.catalog else state.catalog.filter { it.category == category }
            state.copy(selectedCategory = category, filteredCatalog = filtered)
        }
    }

    fun switchTab(tab: String) {
        _uiState.update { it.copy(activeTab = tab) }
    }

    fun addToCart(item: GharsLoraItem) = manageCartUseCase.add(item)
    fun removeFromCart(item: GharsLoraItem) = manageCartUseCase.remove(item)

    fun checkout() {
        viewModelScope.launch {
            _uiState.update { it.copy(isCheckingOut = true) }
            val order = processCheckoutUseCase()
            _uiState.update { it.copy(isCheckingOut = false, lastOrder = order, activeTab = "checkout") }
        }
    }
}
