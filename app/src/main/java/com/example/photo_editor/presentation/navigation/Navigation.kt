package com.example.photo_editor.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.photo_editor.presentation.screen.HomeScreen

@Composable
fun Navigation(navController: NavHostController) {
    NavHost(navController = navController, startDestination =Screens.HomeScreen.route ) {
        composable(Screens.HomeScreen.route){ HomeScreen() }

    }

}
sealed class Screens(val title:String,val route:String){
    object HomeScreen:Screens("Home","Home")
}