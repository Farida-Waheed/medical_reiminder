package com.example.medicalreiminder.presentation

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.medicalreiminder.R
import com.example.medicalreiminder.viewModels.AuthenticationViewModel

@Composable
fun LoginPage(
    modifier: Modifier = Modifier,
    authViewModel: AuthenticationViewModel,
    onUserExists: () -> Unit,
    onLogIn: () -> Unit,
    onSignUp: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val authState = authViewModel.auth
    val isLoading by authViewModel.isLoading.collectAsState()
    val context = LocalContext.current
    val textColor = if (isSystemInDarkTheme()) Color.White else Color.Black
    val isWide = LocalConfiguration.current.screenWidthDp >= 600

    LaunchedEffect(Unit) {
        if (authState.currentUser != null) {
            onUserExists()
        }
    }

    if (isWide) {
        Row(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LoginBrand(Modifier.weight(1f))
            LoginForm(
                email = email,
                password = password,
                passwordVisible = passwordVisible,
                textColor = textColor,
                isLoading = isLoading,
                onEmailChange = { email = it },
                onPasswordChange = { password = it },
                onPasswordVisibilityChange = { passwordVisible = !passwordVisible },
                onLogin = {
                    authViewModel.login(email, password, context) { state, message ->
                        if (state) onLogIn() else showAuthToast(context, message)
                    }
                },
                onSignUp = onSignUp,
                onForgotPassword = {
                    authViewModel.sendPasswordResetEmail(email, context)
                },
                modifier = Modifier.weight(1f)
            )
        }
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LoginBrand()
            Spacer(modifier = Modifier.height(16.dp))
            LoginForm(
                email = email,
                password = password,
                passwordVisible = passwordVisible,
                textColor = textColor,
                isLoading = isLoading,
                onEmailChange = { email = it },
                onPasswordChange = { password = it },
                onPasswordVisibilityChange = { passwordVisible = !passwordVisible },
                onLogin = {
                    authViewModel.login(email, password, context) { state, message ->
                        if (state) onLogIn() else showAuthToast(context, message)
                    }
                },
                onSignUp = onSignUp,
                onForgotPassword = {
                    authViewModel.sendPasswordResetEmail(email, context)
                }
            )
        }
    }
}

@Composable
private fun LoginBrand(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.jalees_logo_full),
            contentDescription = stringResource(R.string.app_name),
            modifier = Modifier.size(210.dp)
        )
        Text(
            text = stringResource(R.string.caregiver_login),
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0B4A73)
        )
    }
}

@Composable
private fun LoginForm(
    email: String,
    password: String,
    passwordVisible: Boolean,
    textColor: Color,
    isLoading: Boolean,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordVisibilityChange: () -> Unit,
    onLogin: () -> Unit,
    onSignUp: () -> Unit,
    onForgotPassword: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text(text = stringResource(R.string.email), color = textColor) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text(text = stringResource(R.string.password), color = textColor) },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = onPasswordVisibilityChange) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = null
                    )
                }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onLogin,
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0B4A73))
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = Color.White
                )
                Text(
                    text = stringResource(R.string.auth_loading),
                    modifier = Modifier.padding(start = 8.dp)
                )
            } else {
                Text(text = stringResource(R.string.login))
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        TextButton(onClick = onSignUp, enabled = !isLoading) {
            Text(text = stringResource(R.string.create_caregiver_account), color = textColor)
        }
        TextButton(onClick = onForgotPassword, enabled = !isLoading) {
            Text(text = stringResource(R.string.forgot_password), color = textColor)
        }
    }
}

private fun showAuthToast(context: android.content.Context, message: String?) {
    Toast.makeText(
        context,
        message ?: context.getString(R.string.generic_error),
        Toast.LENGTH_SHORT
    ).show()
}
