package com.example.medicalreiminder.presentation

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.medicalreiminder.model.utils.navigateAndDontComeBack
import com.example.medicalreiminder.viewModels.AlertViewModel
import com.example.medicalreiminder.viewModels.AuthenticationViewModel
import com.example.medicalreiminder.viewModels.ReminderViewModel
import kotlinx.serialization.Serializable


@Serializable
object SignIn

@Serializable
object SignUp

@Serializable
object Main

@Serializable
object Alerts

@Serializable
data class EditReminder(
    val id: Int = 0,
    val name: String,
    val time: Long,
    val timeOffset: Long,
    val dose: String
)

@Serializable
data class AddReminder(
    val name: String,
    val time: Long,
    val timeOffset: Long,
    val dose: String
)

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun Navigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    appContext: Application
) {

    NavHost(navController = navController, startDestination = SignIn) {
        val authenticationViewModel = AuthenticationViewModel(appContext)
        val reminderViewModel = ReminderViewModel(appContext)
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
                ReminderViewModel = reminderViewModel,
                authenticationViewModel = authenticationViewModel,
                alertViewModel = alertViewModel,
                modifier = modifier,
                onAddMed = { name, ft, tf, dose ->
                    navController.navigate(route = AddReminder(name, ft, tf, dose))
                },
                onEditMed = { id, name, ft, tf, dose ->
                    navController.navigate(route = EditReminder(id, name, ft, tf, dose))
                },
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

        composable<EditReminder> {
            val args = it.toRoute<EditReminder>()
            EditMedicationScreen(
                modifier = modifier,
                reminderViewModel = reminderViewModel,
                authenticationViewModel = authenticationViewModel,
                id = args.id,
                medName = args.name,
                medFirstTime = args.time,
                timeOffset = args.timeOffset,
                medDose = args.dose
            ) {
                navController.popBackStack()
            }
        }
        composable<AddReminder> {
            val args = it.toRoute<EditReminder>()
            AddMedicationScreen(
                modifier = modifier,
                reminderViewModel = reminderViewModel,
                authenticationViewModel = authenticationViewModel,
                medName = args.name,
                medFirstTime = args.time,
                timeOffset = args.timeOffset,
                medDose = args.dose
            ) {
                navController.popBackStack()
            }
        }
    }

}




