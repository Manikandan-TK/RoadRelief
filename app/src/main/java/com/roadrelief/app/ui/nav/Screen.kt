package com.roadrelief.app.ui.nav

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Profile : Screen("profile")
    data object NewCase : Screen("new_case")
    data object CaseDetail : Screen("case_detail/{caseId}") {
        fun createRoute(caseId: Long) = "case_detail/$caseId"
    }
    data object Camera : Screen("camera")
    data object SubmissionGuide : Screen("submission_guide")
}
