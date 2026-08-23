package com.floating.stopwatch.navigation

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Home : Screen("home")
    data object Novel : Screen("novel")
    data object Differences : Screen("differences")
    data object Learn : Screen("learn")
    data object Audio : Screen("audio")
    data object Quiz : Screen("quiz")
    data object More : Screen("more")
    data object Settings : Screen("settings")
    data object About : Screen("about")
    data object Book : Screen("book")
    data object Saved : Screen("saved")
    data object DailyReview : Screen("daily_review")
    data object Progress : Screen("progress")
    data object Search : Screen("search")
}

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val activeIconName: String,
    val inactiveIconName: String
) {
    data object Home : BottomNavItem(Screen.Home.route, "الرئيسية", "home_filled", "home_outlined")
    data object Learn : BottomNavItem(Screen.Learn.route, "التعلّم", "book_filled", "book_outlined")
    data object Differences : BottomNavItem(Screen.Differences.route, "الفروق", "swap_filled", "swap_outlined")
    data object Audio : BottomNavItem(Screen.Audio.route, "الاستماع", "audio_filled", "audio_outlined")
    data object More : BottomNavItem(Screen.More.route, "المزيد", "more_filled", "more_outlined")
}

val bottomNavItems = listOf(
    BottomNavItem.Home,
    BottomNavItem.Learn,
    BottomNavItem.Differences,
    BottomNavItem.Audio,
    BottomNavItem.More
)
