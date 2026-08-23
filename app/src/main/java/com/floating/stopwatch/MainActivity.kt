package com.floating.stopwatch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.floating.stopwatch.designsystem.theme.AlRayyashTheme
import com.floating.stopwatch.navigation.BottomNavItem
import com.floating.stopwatch.navigation.Screen
import com.floating.stopwatch.presentation.audio.AudioScreen
import com.floating.stopwatch.presentation.components.AlRayyashBottomNavigation
import com.floating.stopwatch.presentation.differences.DifferencesScreen
import com.floating.stopwatch.presentation.home.HomeScreen
import com.floating.stopwatch.presentation.learn.LearnScreen
import com.floating.stopwatch.presentation.more.MoreScreen
import com.floating.stopwatch.presentation.novel.NovelScreen
import com.floating.stopwatch.presentation.quiz.QuizScreen
import com.floating.stopwatch.presentation.settings.SettingsScreen
import com.floating.stopwatch.presentation.splash.SplashScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AlRayyashTheme {
                AlRayyashMainApp()
            }
        }
    }
}

@Composable
fun AlRayyashMainApp() {
    var currentRoute by remember { mutableStateOf<String>(Screen.Splash.route) }

    if (currentRoute == Screen.Splash.route) {
        SplashScreen(
            onSplashComplete = {
                currentRoute = Screen.Home.route
            }
        )
    } else {
        Scaffold(
            bottomBar = {
                // Show bottom nav on main tabs
                val isBottomBarVisible = currentRoute in listOf(
                    Screen.Home.route,
                    Screen.Learn.route,
                    Screen.Differences.route,
                    Screen.Audio.route,
                    Screen.More.route
                )

                if (isBottomBarVisible) {
                    AlRayyashBottomNavigation(
                        currentRoute = currentRoute,
                        onItemSelected = { item ->
                            currentRoute = item.route
                        }
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentRoute) {
                    Screen.Home.route -> HomeScreen(
                        onNavigateToNovel = { currentRoute = Screen.Novel.route },
                        onNavigateToDifferences = { currentRoute = Screen.Differences.route },
                        onNavigateToLearn = { currentRoute = Screen.Learn.route },
                        onNavigateToQuiz = { currentRoute = Screen.Quiz.route },
                        onNavigateToAudio = { currentRoute = Screen.Audio.route }
                    )
                    Screen.Novel.route -> NovelScreen()
                    Screen.Differences.route -> DifferencesScreen()
                    Screen.Learn.route -> LearnScreen()
                    Screen.Audio.route -> AudioScreen()
                    Screen.Quiz.route -> QuizScreen()
                    Screen.More.route -> MoreScreen(
                        onNavigateToSettings = { currentRoute = Screen.Settings.route }
                    )
                    Screen.Settings.route -> SettingsScreen(
                        onNavigateBack = { currentRoute = Screen.More.route }
                    )
                    else -> HomeScreen(
                        onNavigateToNovel = { currentRoute = Screen.Novel.route },
                        onNavigateToDifferences = { currentRoute = Screen.Differences.route },
                        onNavigateToLearn = { currentRoute = Screen.Learn.route },
                        onNavigateToQuiz = { currentRoute = Screen.Quiz.route },
                        onNavigateToAudio = { currentRoute = Screen.Audio.route }
                    )
                }
            }
        }
    }
}
