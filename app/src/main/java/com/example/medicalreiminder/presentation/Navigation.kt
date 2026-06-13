package com.example.medicalreiminder.presentation

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.medicalreiminder.model.utils.navigateAndDontComeBack
import com.example.medicalreiminder.viewModels.AlertViewModel
import com.example.medicalreiminder.viewModels.AuthenticationViewModel
import kotlinx.serialization.Serializable


@Serializable
object SignIn

@Serializable
object SignUp

@Serializable
object Main

@Serializable
object Alerts

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun Navigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    appContext: Application
) {

    NavHost(navController = navController, startDestination = SignIn) {
        val authenticationViewModel = AuthenticationViewModel()
        val alertViewModel = AlertViewModel()
        composable<SignIn> {
            LoginPage(modifier, authenticationViewModel, onLogIn =  {
                navController.navigateAndDontComeBack(Main)
            }, onUserExists = {
                navController.navigateAndDontComeBack(Main)
            }) {
                navController.navigate(route = SignUp)
            }
        }
        composable<SignUp> {
            SignupScreen(modifier, authenticationViewModel, back = {
                navController.popBackStack()
            }, onSignUp = {
                navController.navigate(route = SignIn)
            }) {
                navController.navigate(route = SignIn)
            }
        }
        composable<Main> {
            MainScreen(
                authenticationViewModel = authenticationViewModel,
                alertViewModel = alertViewModel,
                modifier = modifier,
                onLogout = {
                    navController.navigateAndDontComeBack(SignIn)
                },
                onAlerts = {
                    navController.navigate(route = Alerts)
                }
            )
        }
        composable<Alerts> {
            AlertsScreen(
                modifier = modifier,
                alertViewModel = alertViewModel,
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }

}




