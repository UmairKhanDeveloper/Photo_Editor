package com.example.photo_editor.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.photo_editor.presentation.screen.HomeScreen
import com.example.photo_editor.presentation.screen.PhotoEditor

@Composable
fun Navigation(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screens.HomeScreen.route) {
        composable(Screens.HomeScreen.route) { HomeScreen(navController) }
        composable(
            route = Screens.PhotoEditor.route + "/{imageUri}",
            arguments = listOf(navArgument("imageUri") { type = NavType.StringType })
        ) { backStackEntry ->
            val imageUri = backStackEntry.arguments?.getString("imageUri")
            PhotoEditor(navController, imageUri)
        }
    }


}
sealed class Screens(val title:String,val route:String){
    object HomeScreen:Screens("HomeScreen","HomeScreen")
    object PhotoEditor:Screens("PhotoEditor","PhotoEditor")

}