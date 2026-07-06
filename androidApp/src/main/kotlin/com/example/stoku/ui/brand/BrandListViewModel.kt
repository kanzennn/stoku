package com.example.stoku.ui.brand

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stoku.domain.model.Brand
import com.example.stoku.domain.usecase.brand.AddBrandUseCase
import com.example.stoku.domain.usecase.brand.DeleteBrandUseCase
import com.example.stoku.domain.usecase.brand.GetBrandsUseCase
import com.example.stoku.domain.usecase.brand.UpdateBrandUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface BrandDialog {
    data object None : BrandDialog
    data object Add : BrandDialog
    data class Edit(val brand: Brand) : BrandDialog
    data class Delete(val brand: Brand) : BrandDialog
}

data class BrandListUiState(
    val brands: List<Brand> = emptyList(),
    val dialog: BrandDialog = BrandDialog.None,
)

@HiltViewModel
class BrandListViewModel @Inject constructor(
    private val getBrandsUseCase: GetBrandsUseCase,
    private val addBrandUseCase: AddBrandUseCase,
    private val updateBrandUseCase: UpdateBrandUseCase,
    private val deleteBrandUseCase: DeleteBrandUseCase,
) : ViewModel() {

    private val _dialog = MutableStateFlow<BrandDialog>(BrandDialog.None)

    val uiState: StateFlow<BrandListUiState> = combine(
        getBrandsUseCase(),
        _dialog,
    ) { brands, dialog ->
        BrandListUiState(brands = brands, dialog = dialog)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BrandListUiState())

    fun showAddDialog() = _dialog.update { BrandDialog.Add }
    fun showEditDialog(brand: Brand) = _dialog.update { BrandDialog.Edit(brand) }
    fun showDeleteDialog(brand: Brand) = _dialog.update { BrandDialog.Delete(brand) }
    fun dismissDialog() = _dialog.update { BrandDialog.None }

    fun addBrand(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            addBrandUseCase(name.trim())
            dismissDialog()
        }
    }

    fun updateBrand(brand: Brand, newName: String) {
        if (newName.isBlank()) return
        viewModelScope.launch {
            updateBrandUseCase(brand.copy(name = newName.trim()))
            dismissDialog()
        }
    }

    fun deleteBrand(brand: Brand) {
        viewModelScope.launch {
            deleteBrandUseCase(brand)
            dismissDialog()
        }
    }
}
