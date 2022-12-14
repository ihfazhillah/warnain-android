package com.ihfazh.warnain.categories

import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ihfazh.warnain.common.PreferenceManager
import com.ihfazh.warnain.domain.Category
import com.ihfazh.warnain.domain.CategorySorter
import com.ihfazh.warnain.repositories.CategoryRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import java.io.FilterInputStream

@KoinViewModel
class CategoriesViewModel(
    private val categoryRepository: CategoryRepository,
    private val preferenceManager: PreferenceManager
): ViewModel() {

    private var _searchState = MutableStateFlow("")
    val searchState: StateFlow<String> = _searchState

    fun updateSearch(value: String){
        _searchState.value = value
    }

    private var _sorterState = MutableStateFlow(CategorySorter.TITLE)
    val sorterState: StateFlow<CategorySorter> = _sorterState
    fun updateSorterState(state: CategorySorter){
        _sorterState.value = state
    }


    private var _latestCategories = MutableStateFlow(listOf<Category>())
    val latestCategories: StateFlow<List<Category>> = _latestCategories

    private fun getLatestCategory() = viewModelScope.launch {
        _latestCategories.value = categoryRepository.getLastAccess()
    }

    fun hasToken(): Boolean {
        return preferenceManager.getToken() != null
    }

//    @OptIn(ExperimentalCoroutinesApi::class)
//    val categories = searchState.flatMapLatest { query ->
//        categoryRepository.getCategories(query)
//    }.shareIn(viewModelScope, SharingStarted.Lazily)

    @OptIn(ExperimentalCoroutinesApi::class)
    val categories = searchState.combine(sorterState){ search, sort ->
        categoryRepository.getCategories(search, sort)
    }.flatMapLatest {
        it
    }.shareIn(viewModelScope, SharingStarted.Lazily)

//    init {
//        viewModelScope.launch{
//            filterState.collectLatest {
//                if (it == CategoryFilter.LATEST){
//                    getLatestCategory()
//                }
//            }
//        }
//    }

    // used for saving scroll state
    var firstVisibleItemOffset = 0
    var firstVisibleItemIndex = 0

}