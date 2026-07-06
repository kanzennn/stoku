package com.example.stoku.ui.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stoku.domain.model.Brand
import com.example.stoku.domain.model.Category
import com.example.stoku.domain.usecase.brand.AddBrandUseCase
import com.example.stoku.domain.usecase.brand.GetBrandsUseCase
import com.example.stoku.domain.usecase.category.GetCategoriesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Feeds the Brand/Category pickers used by product entry & edit forms, sourced from the managed tables (Settings > Manajemen Data). */
@HiltViewModel
class BrandCategoryOptionsViewModel @Inject constructor(
    getBrandsUseCase: GetBrandsUseCase,
    getCategoriesUseCase: GetCategoriesUseCase,
    private val addBrandUseCase: AddBrandUseCase,
) : ViewModel() {

    val brands: StateFlow<List<Brand>> = getBrandsUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val categories: StateFlow<List<Category>> = getCategoriesUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Registers a brand name typed by the user if it isn't already in the table, so it's pickable next time. */
    fun ensureBrandExists(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        if (brands.value.any { it.name.equals(trimmed, ignoreCase = true) }) return
        viewModelScope.launch { addBrandUseCase(trimmed) }
    }
}
