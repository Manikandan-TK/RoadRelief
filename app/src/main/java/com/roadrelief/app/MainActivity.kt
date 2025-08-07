package com.roadrelief.app

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.roadrelief.app.ui.nav.Screen
import com.roadrelief.app.ui.screens.camera.CameraScreen
import com.roadrelief.app.ui.screens.casedetail.CaseDetailScreen
import com.roadrelief.app.ui.screens.home.HomeScreen
import com.roadrelief.app.ui.screens.newcase.NewCaseScreen
import com.roadrelief.app.ui.screens.profile.ProfileScreen
import com.roadrelief.app.ui.screens.submission.SubmissionGuideScreen
import com.roadrelief.app.ui.theme.RoadReliefTheme
import dagger.hilt.android.AndroidEntryPoint

const val PREFS_NAME = "RoadReliefPrefs"
const val KEY_FIRST_LAUNCH_COMPLETE = "firstLaunchComplete"

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RoadReliefTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val isFirstLaunchComplete = sharedPreferences.getBoolean(KEY_FIRST_LAUNCH_COMPLETE, false)

    val startDestination = if (isFirstLaunchComplete) {
        Screen.Home.route
    } else {
        Screen.Profile.route
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }
        composable(Screen.Profile.route) {
            // Pass navController and SharedPreferences to ProfileScreen
            ProfileScreen(navController = navController, sharedPreferences = sharedPreferences)
        }
        composable(Screen.NewCase.route) {
            NewCaseScreen(navController = navController)
        }
        composable(
            route = Screen.CaseDetail.route,
            arguments = listOf(navArgument("caseId") { type = NavType.LongType })
        ) {
            // val caseId = it.arguments?.getLong("caseId")
            CaseDetailScreen(onBackClicked = { navController.navigateUp() })
        }
        composable(Screen.Camera.route) {
            CameraScreen(navController = navController)
        }
        composable(Screen.SubmissionGuide.route) {
            SubmissionGuideScreen()
        }
    }
}