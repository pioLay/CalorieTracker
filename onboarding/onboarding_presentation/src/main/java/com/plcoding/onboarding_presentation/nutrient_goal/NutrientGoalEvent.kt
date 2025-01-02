package com.plcoding.onboarding_presentation.nutrient_goal

sealed class NutrientGoalEvent {
    data class OnCarbRatioEnter(val ratio: String): NutrientGoalEvent()
    data class OnCarbRatioChanged(val ratio: String): NutrientGoalEvent()
    data class OnProteinRatioEnter(val ratio: String): NutrientGoalEvent()
    data class OnProteinRatioChanged(val ratio: String): NutrientGoalEvent()
    data class OnFatRatioEnter(val ratio: String): NutrientGoalEvent()
    data class OnFatRatioChanged(val ratio: String): NutrientGoalEvent()
    object OnNextClick: NutrientGoalEvent()
}
