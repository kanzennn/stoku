package com.example.stoku.ui.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stoku.domain.model.Category
import com.example.stoku.domain.usecase.category.AddCategoryUseCase
import com.example.stoku.domain.usecase.category.DeleteCategoryUseCase
import com.example.stoku.domain.usecase.category.GetCategoriesUseCase
import com.example.stoku.domain.usecase.category.UpdateCategoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface CategoryDialog {
    data object None : CategoryDialog
    data object Add : CategoryDialog
    data class Edit(val category: Category) : CategoryDialog
    data class Delete(val category: Category) : CategoryDialog
}

data class CategoryListUiState(
    val categories: List<Category> = emptyList(),
    val dialog: CategoryDialog = CategoryDialog.None,
)

@HiltViewModel
class CategoryListViewModel @Inject constructor(
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val addCategoryUseCase: AddCategoryUseCase,
    private val updateCategoryUseCase: UpdateCategoryUseCase,
    private val deleteCategoryUseCase: DeleteCategoryUseCase,
) : ViewModel() {

    private val _dialog = MutableStateFlow<CategoryDialog>(CategoryDialog.None)

    val uiState: StateFlow<CategoryListUiState> = combine(
        getCategoriesUseCase(),
        _dialog,
    ) { categories, dialog ->
        CategoryListUiState(categories = categories, dialog = dialog)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CategoryListUiState())

    fun showAddDialog() = _dialog.update { CategoryDialog.Add }
    fun showEditDialog(category: Category) = _dialog.update { CategoryDialog.Edit(category) }
    fun showDeleteDialog(category: Category) = _dialog.update { CategoryDialog.Delete(category) }
    fun dismissDialog() = _dialog.update { CategoryDialog.None }

    fun addCategory(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            addCategoryUseCase(name.trim())
            dismissDialog()
        }
    }

    fun updateCategory(category: Category, newName: String) {
        if (newName.isBlank()) return
        viewModelScope.launch {
            updateCategoryUseCase(category.copy(name = newName.trim()))
            dismissDialog()
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            deleteCategoryUseCase(category)
            dismissDialog()
        }
    }
}
