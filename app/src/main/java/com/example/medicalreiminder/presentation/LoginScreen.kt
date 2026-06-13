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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
    val context = LocalContext.current
    val isDarkTheme = isSystemInDarkTheme()
    var textColor by remember { mutableStateOf(Color.Black) }
    if (isDarkTheme) {
        textColor = Color.White
    } else {
        Color.Black
    }
    val config = LocalConfiguration.current.screenWidthDp
    LaunchedEffect(Unit) {
        if (authState.currentUser != null) {
            onUserExists()
        }
    }

    if (config < 600) {
        Column(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.ggk),
                contentDescription = "ggk",
                modifier = Modifier
                    .size(200.dp)
                    .offset(x = 1.dp, y = 4.dp)
            )
            Text(
                text = stringResource(R.string.caregiver_login),
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFDA6B53)
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(text = stringResource(R.string.email), color = textColor) },
                modifier = Modifier
                    .fillMaxWidth()  // Only added this line
                    .padding(horizontal = 16.dp)  // And this line
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(text = stringResource(R.string.password), color = textColor) },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password"
                        )
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {

                    authViewModel.login(email, password, context) { state, message ->
                        if (state) {
                            onLogIn()
                        } else {
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E6FFA))
            ) {
                Text(text = stringResource(R.string.login))
            }

            Spacer(modifier = Modifier.height(10.dp))

            TextButton(onClick = { onSignUp() }) {
                Text(text = stringResource(R.string.create_caregiver_account), color = textColor)
            }

            TextButton(onClick = {
                if (email.isNotBlank()) {
                    authViewModel.sendPasswordResetEmail(email, context)
                } else {
                    Toast.makeText(context, "Please enter your email", Toast.LENGTH_SHORT).show()
                }
            }) {
                Text(text = stringResource(R.string.forgot_password), color = textColor)
            }


        }
    }else{
        Row {
            Column(
                modifier = modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ggk),
                    contentDescription = "ggk",
                    modifier = Modifier
                        .size(200.dp)
                        .offset(x = 1.dp, y = 4.dp)
                )
                Text(
                    text = stringResource(R.string.caregiver_login),
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFDA6B53)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            Column(modifier = modifier.weight(1f).
            padding(top = 12.dp)) {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(text = stringResource(R.string.email), color = textColor) },
                    modifier = Modifier
                        .fillMaxWidth()  // Only added this line
                        .padding(horizontal = 16.dp)  // And this line
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(text = stringResource(R.string.password), color = textColor) },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password"
                            )
                        }
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {

                        authViewModel.login(email, password, context) { state, message ->
                            if (state) {
                                onLogIn()
                            } else {
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E6FFA))
                ) {
                    Text(text = stringResource(R.string.login))
                }

                Spacer(modifier = Modifier.height(10.dp))

                TextButton(onClick = { onSignUp() }) {
                    Text(text = stringResource(R.string.create_caregiver_account), color = textColor)
                }

                TextButton(onClick = {
                    if (email.isNotBlank()) {
                        authViewModel.sendPasswordResetEmail(email, context)
                    } else {
                        Toast.makeText(context, "Please enter your email", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Text(text = stringResource(R.string.forgot_password), color = textColor)
                }

            }

        }

        }
}
