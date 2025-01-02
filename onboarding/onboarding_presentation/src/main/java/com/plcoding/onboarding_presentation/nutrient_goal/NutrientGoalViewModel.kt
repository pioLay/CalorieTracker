package com.plcoding.onboarding_presentation.nutrient_goal

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plcoding.core.domain.preferences.Preferences
import com.plcoding.core.domain.use_case.FilterOutDigits
import com.plcoding.core.util.UiEvent
import com.plcoding.onboarding_domain.use_case.ValidateNutrients
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NutrientGoalViewModel @Inject constructor(
    private val preferences: Preferences,
    private val filterOutDigits: FilterOutDigits,
    private val validateNutrients: ValidateNutrients
) : ViewModel() {

    var state by mutableStateOf(NutrientGoalState())
        private set

    init {
        val userInfo = preferences.loadUserInfo()
        state = state.copy(
            carbsRatio = filterOutDigits((userInfo.carbRatio * 10f).toString()),
            proteinRatio = filterOutDigits((userInfo.proteinRatio * 10f).toString()),
            fatRatio = filterOutDigits((userInfo.fatRatio * 10f).toString()),
        )
    }

    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    fun onEvent(event: NutrientGoalEvent) {
        when (event) {
            is NutrientGoalEvent.OnCarbRatioEnter -> {
                validateCurrentNutrients {
                    preferences.saveCarbRatio(it.carbsRatio)
                }
            }

            is NutrientGoalEvent.OnProteinRatioEnter -> {
                validateCurrentNutrients {
                    preferences.saveCarbRatio(it.proteinRatio)
                }
            }

            is NutrientGoalEvent.OnFatRatioEnter -> {
                validateCurrentNutrients {
                    preferences.saveFatRatio(it.fatRatio)
                }
            }


            is NutrientGoalEvent.OnCarbRatioChanged -> {
                state = state.copy(
                    carbsRatio = filterOutDigits(event.ratio)
                )
            }

            is NutrientGoalEvent.OnFatRatioChanged -> {
                state = state.copy(
                    proteinRatio = filterOutDigits(event.ratio)
                )
            }

            is NutrientGoalEvent.OnProteinRatioChanged -> {
                state = state.copy(
                    fatRatio = filterOutDigits(event.ratio)
                )
            }


            is NutrientGoalEvent.OnNextClick -> {
                val result = validateNutrients(
                    carbsRatioText = state.carbsRatio,
                    proteinRatioText = state.proteinRatio,
                    fatRatioText = state.fatRatio
                )
                when (result) {
                    is ValidateNutrients.Result.Success -> {
                        preferences.saveCarbRatio(result.carbsRatio)
                        preferences.saveProteinRatio(result.proteinRatio)
                        preferences.saveFatRatio(result.fatRatio)
                        viewModelScope.launch {
                            _uiEvent.send(UiEvent.Success)
                        }
                    }

                    is ValidateNutrients.Result.Error -> {
                        viewModelScope.launch {
                            _uiEvent.send(UiEvent.ShowSnackbar(result.message))
                        }
                    }
                }
            }
        }
    }

    private fun validateCurrentNutrients(saveRatio: (ValidateNutrients.Result.Success) -> Unit) {
        val result = validateNutrients(
            carbsRatioText = state.carbsRatio,
            proteinRatioText = state.proteinRatio,
            fatRatioText = state.fatRatio
        )
        when (result) {
            is ValidateNutrients.Result.Success -> {
                saveRatio(result)
            }

            is ValidateNutrients.Result.Error -> {
                viewModelScope.launch {
                    _uiEvent.send(UiEvent.ShowSnackbar(result.message))
                }
            }
        }
    }
}