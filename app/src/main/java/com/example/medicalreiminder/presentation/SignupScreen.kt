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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.medicalreiminder.R
import com.example.medicalreiminder.viewModels.AuthenticationViewModel

@Composable
fun SignupScreen(
    modifier: Modifier = Modifier,
    viewModel: AuthenticationViewModel,
    onSignUp: () -> Unit,
    back: () -> Unit,
    goToLogin: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val isLoading by viewModel.isLoading.collectAsState()
    val textColor = if (isSystemInDarkTheme()) Color.White else Color.Black
    val isWide = LocalConfiguration.current.screenWidthDp >= 600

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        IconButton(onClick = back, enabled = !isLoading) {
            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
        }

        if (isWide) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SignupBrand(Modifier.weight(1f))
                SignupForm(
                    name = name,
                    email = email,
                    password = password,
                    passwordVisible = passwordVisible,
                    textColor = textColor,
                    isLoading = isLoading,
                    onNameChange = { name = it },
                    onEmailChange = { email = it },
                    onPasswordChange = { password = it },
                    onPasswordVisibilityChange = { passwordVisible = !passwordVisible },
                    onSubmit = {
                        viewModel.signUp(email, password, name, context) { state, message ->
                            if (state) onSignUp() else showSignupToast(context, message)
                        }
                    },
                    goToLogin = goToLogin,
                    modifier = Modifier.weight(1f)
                )
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SignupBrand()
                Spacer(modifier = Modifier.height(20.dp))
                SignupForm(
                    name = name,
                    email = email,
                    password = password,
                    passwordVisible = passwordVisible,
                    textColor = textColor,
                    isLoading = isLoading,
                    onNameChange = { name = it },
                    onEmailChange = { email = it },
                    onPasswordChange = { password = it },
                    onPasswordVisibilityChange = { passwordVisible = !passwordVisible },
                    onSubmit = {
                        viewModel.signUp(email, password, name, context) { state, message ->
                            if (state) onSignUp() else showSignupToast(context, message)
                        }
                    },
                    goToLogin = goToLogin
                )
            }
        }
    }
}

@Composable
private fun SignupBrand(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.jalees_logo_full),
            contentDescription = stringResource(R.string.app_name),
            modifier = Modifier.size(150.dp)
        )
        Text(
            text = stringResource(R.string.caregiver_signup),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0B4A73)
        )
        Text(
            text = stringResource(R.string.caregiver_signup_subtitle),
            fontSize = 14.sp,
            color = Color.Gray
        )
    }
}

@Composable
private fun SignupForm(
    name: String,
    email: String,
    password: String,
    passwordVisible: Boolean,
    textColor: Color,
    isLoading: Boolean,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordVisibilityChange: () -> Unit,
    onSubmit: () -> Unit,
    goToLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text(stringResource(R.string.caregiver_name), color = textColor) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text(stringResource(R.string.email), color = textColor) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text(stringResource(R.string.password), color = textColor) },
            placeholder = { Text(stringResource(R.string.password_hint), color = textColor) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = onPasswordVisibilityChange) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = null
                    )
                }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = onSubmit,
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
                Text(stringResource(R.string.signup), fontSize = 18.sp, color = Color.White)
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(stringResource(R.string.already_have_account), fontSize = 14.sp, color = textColor)
            TextButton(onClick = goToLogin, enabled = !isLoading) {
                Text(stringResource(R.string.login), fontSize = 14.sp, color = Color(0xFF0B4A73))
            }
        }
    }
}

private fun showSignupToast(context: android.content.Context, message: String?) {
    Toast.makeText(
        context,
        message ?: context.getString(R.string.generic_error),
        Toast.LENGTH_SHORT
    ).show()
}
