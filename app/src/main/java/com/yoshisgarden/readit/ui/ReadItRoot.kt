package com.yoshisgarden.readit.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.yoshisgarden.readit.ui.navigation.BottomTab
import com.yoshisgarden.readit.ui.navigation.Routes
import com.yoshisgarden.readit.ui.screens.dictionary.DictionaryScreen
import com.yoshisgarden.readit.ui.screens.dictionary.DictionaryViewModel
import com.yoshisgarden.readit.ui.screens.dictionary.PhraseDetailScreen
import com.yoshisgarden.readit.ui.screens.flashcard.FlashcardScreen
import com.yoshisgarden.readit.ui.screens.help.HelpScreen
import com.yoshisgarden.readit.ui.screens.home.HomeScreen
import com.yoshisgarden.readit.ui.screens.progress.ProgressScreen
import com.yoshisgarden.readit.ui.screens.quiz.QuizScreen
import com.yoshisgarden.readit.ui.screens.quiz.QuizSelectScreen
import com.yoshisgarden.readit.ui.screens.settings.SettingsScreen
import com.yoshisgarden.readit.ui.screens.version.VersionScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadItRoot() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val tabRoutes = BottomTab.entries.map { it.route }
    val showBars = currentRoute in tabRoutes

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = showBars,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.height(20.dp))
                Text(
                    "  ReadIT",
                    style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(16.dp),
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Filled.Insights, null) },
                    label = { Text("進捗グラフ") },
                    selected = currentRoute == Routes.PROGRESS,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate(Routes.PROGRESS)
                    },
                    modifier = Modifier.padding(horizontal = 12.dp),
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Filled.Settings, null) },
                    label = { Text("設定") },
                    selected = currentRoute == Routes.SETTINGS,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate(Routes.SETTINGS)
                    },
                    modifier = Modifier.padding(horizontal = 12.dp),
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.AutoMirrored.Filled.HelpOutline, null) },
                    label = { Text("ヘルプ") },
                    selected = currentRoute == Routes.HELP,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate(Routes.HELP)
                    },
                    modifier = Modifier.padding(horizontal = 12.dp),
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Filled.Info, null) },
                    label = { Text("バージョン情報") },
                    selected = currentRoute == Routes.VERSION,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate(Routes.VERSION)
                    },
                    modifier = Modifier.padding(horizontal = 12.dp),
                )
            }
        },
    ) {
        Scaffold(
            // Lift the whole scaffold above the soft keyboard so the bottom
            // navigation (Home etc.) stays tappable while searching. Fixes
            // the keyboard covering the nav bar on some devices (e.g. AQUOS sense7).
            modifier = Modifier.imePadding(),
            topBar = {
                if (showBars) {
                    TopAppBar(
                        title = { Text(currentTitle(currentRoute)) },
                        navigationIcon = {
                            androidx.compose.material3.IconButton(
                                onClick = { scope.launch { drawerState.open() } },
                            ) { Icon(Icons.Filled.Menu, contentDescription = "メニュー") }
                        },
                    )
                }
            },
            bottomBar = {
                if (showBars) {
                    NavigationBar {
                        BottomTab.entries.forEach { tab ->
                            NavigationBarItem(
                                selected = currentRoute == tab.route,
                                onClick = {
                                    navController.navigate(tab.route) {
                                        popUpTo(Routes.HOME) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = { Icon(tab.icon, contentDescription = tab.label) },
                                label = { Text(tab.label) },
                            )
                        }
                    }
                }
            },
            floatingActionButton = {
                if (currentRoute == Routes.HOME) {
                    ExtendedFloatingActionButton(
                        onClick = { navController.navigate(Routes.FLASHCARD) },
                        icon = { Icon(Icons.Filled.PlayArrow, null) },
                        text = { Text("今日の学習") },
                    )
                }
            },
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = Routes.HOME,
                modifier = Modifier.fillMaxSize().padding(padding),
            ) {
                composable(Routes.HOME) {
                    HomeScreen(
                        vm = viewModel(factory = ReadItViewModels.Factory),
                        onOpenFlashcards = { navController.navigate(Routes.FLASHCARD) },
                        onOpenQuiz = { navController.navigate(Routes.QUIZ_SELECT) },
                        onOpenDictionary = { navController.navigate(Routes.DICTIONARY) },
                        onOpenPhrase = { navController.navigate(Routes.detail(it)) },
                        onOpenProgress = { navController.navigate(Routes.PROGRESS) },
                    )
                }
                composable(Routes.DICTIONARY) {
                    val vm: DictionaryViewModel = viewModel(factory = ReadItViewModels.Factory)
                    DictionaryScreen(
                        vm = vm,
                        onOpenPhrase = { navController.navigate(Routes.detail(it)) },
                    )
                }
                composable(
                    route = "${Routes.DETAIL}/{phraseId}",
                    arguments = listOf(navArgument("phraseId") { type = NavType.LongType }),
                ) { entry ->
                    val id = entry.arguments?.getLong("phraseId") ?: 0L
                    PhraseDetailScreen(
                        phraseId = id,
                        vm = viewModel(factory = ReadItViewModels.Factory),
                        onBack = { navController.popBackStack() },
                    )
                }
                composable(Routes.QUIZ_SELECT) {
                    QuizSelectScreen(
                        onStart = { navController.navigate(Routes.quiz(it.id)) },
                    )
                }
                composable(
                    route = "${Routes.QUIZ}/{mode}",
                    arguments = listOf(navArgument("mode") { type = NavType.StringType }),
                ) { entry ->
                    QuizScreen(
                        modeId = entry.arguments?.getString("mode") ?: "fill_blank",
                        vm = viewModel(factory = ReadItViewModels.Factory),
                        onExit = { navController.popBackStack(Routes.QUIZ_SELECT, false) },
                    )
                }
                composable(Routes.FLASHCARD) {
                    FlashcardScreen(
                        vm = viewModel(factory = ReadItViewModels.Factory),
                        onExit = { navController.popBackStack(Routes.HOME, false) },
                    )
                }
                composable(Routes.PROGRESS) {
                    ProgressScreen(
                        vm = viewModel(factory = ReadItViewModels.Factory),
                        onBack = { navController.popBackStack() },
                    )
                }
                composable(Routes.SETTINGS) {
                    SettingsScreen(
                        vm = viewModel(factory = ReadItViewModels.Factory),
                        onBack = { navController.popBackStack() },
                    )
                }
                composable(Routes.HELP) {
                    HelpScreen(onBack = { navController.popBackStack() })
                }
                composable(Routes.VERSION) {
                    VersionScreen(onBack = { navController.popBackStack() })
                }
            }
        }
    }
}

private fun currentTitle(route: String?): String = when (route) {
    Routes.HOME -> "ReadIT"
    Routes.DICTIONARY -> "フレーズ辞書"
    Routes.QUIZ_SELECT -> "クイズ"
    Routes.FLASHCARD -> "単語帳"
    else -> "ReadIT"
}
