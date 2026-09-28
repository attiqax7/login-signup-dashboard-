package com.example.composeauth

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ==============================================================================
// 1. MAIN ACTIVITY: Single entry point of the app
// ==============================================================================
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

// Simple enum to track which screen is currently visible
enum class AppScreen {
    LOGIN,
    SIGNUP,
    DASHBOARD
}

// ==============================================================================
// 2. APP NAVIGATION: Switches between Login, Signup, and Dashboard
// ==============================================================================
@Composable
fun AppNavigation() {
    // Current screen state (starts strictly on LOGIN)
    var currentScreen by remember { mutableStateOf(AppScreen.LOGIN) }
    // Currently logged-in username to display on the Dashboard
    var loggedInUsername by remember { mutableStateOf("") }

    when (currentScreen) {
        AppScreen.LOGIN -> {
            LoginScreen(
                onNavigateToSignup = { currentScreen = AppScreen.SIGNUP },
                onLoginSuccess = { user ->
                    loggedInUsername = user
                    currentScreen = AppScreen.DASHBOARD
                }
            )
        }
        AppScreen.SIGNUP -> {
            SignupScreen(
                onNavigateToLogin = { currentScreen = AppScreen.LOGIN },
                onSignupSuccess = {
                    currentScreen = AppScreen.LOGIN
                }
            )
        }
        AppScreen.DASHBOARD -> {
            DashboardScreen(
                username = loggedInUsername,
                onLogout = {
                    loggedInUsername = ""
                    currentScreen = AppScreen.LOGIN
                }
            )
        }
    }
}

// ==============================================================================
// 3. LOGIN SCREEN
// ==============================================================================
@Composable
fun LoginScreen(
    onNavigateToSignup: () -> Unit,
    onLoginSuccess: (String) -> Unit
) {
    val context = LocalContext.current
    val sharedPrefs = remember {
        context.getSharedPreferences("app_user_prefs", Context.MODE_PRIVATE)
    }

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Login",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Error message if fields are empty or invalid
        if (errorMessage != null) {
            Text(
                text = errorMessage ?: "",
                color = MaterialTheme.colorScheme.error,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        // Username Field
        OutlinedTextField(
            value = username,
            onValueChange = {
                username = it
                errorMessage = null
            },
            label = { Text("Username") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Password Field (Hidden with PasswordVisualTransformation)
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                errorMessage = null
            },
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Login Button
        Button(
            onClick = {
                val inputUser = username.trim()
                val inputPass = password.trim()

                if (inputUser.isEmpty() || inputPass.isEmpty()) {
                    errorMessage = "Please enter both username and password"
                    return@Button
                }

                // Check stored credentials in SharedPreferences
                val savedUser = sharedPrefs.getString("saved_username", null)
                val savedPass = sharedPrefs.getString("saved_password", null)

                if (savedUser == null || savedPass == null) {
                    errorMessage = "No account found. Please sign up first."
                } else if (savedUser.equals(inputUser, ignoreCase = true) && savedPass == inputPass) {
                    errorMessage = null
                    onLoginSuccess(savedUser)
                } else {
                    errorMessage = "Invalid username or password"
                }
            },
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Login", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Navigate to Signup
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text("Don't have an account?")
            TextButton(onClick = onNavigateToSignup) {
                Text("Sign Up", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ==============================================================================
// 4. SIGNUP SCREEN
// ==============================================================================
@Composable
fun SignupScreen(
    onNavigateToLogin: () -> Unit,
    onSignupSuccess: () -> Unit
) {
    val context = LocalContext.current
    val sharedPrefs = remember {
        context.getSharedPreferences("app_user_prefs", Context.MODE_PRIVATE)
    }

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Sign Up",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Error message display
        if (errorMessage != null) {
            Text(
                text = errorMessage ?: "",
                color = MaterialTheme.colorScheme.error,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        // Username Field
        OutlinedTextField(
            value = username,
            onValueChange = {
                username = it
                errorMessage = null
            },
            label = { Text("Username") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Password Field
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                errorMessage = null
            },
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Confirm Password Field
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = {
                confirmPassword = it
                errorMessage = null
            },
            label = { Text("Confirm Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Sign Up Button
        Button(
            onClick = {
                val inputUser = username.trim()
                val inputPass = password.trim()
                val inputConfirm = confirmPassword.trim()

                // 1. Check for empty fields
                if (inputUser.isEmpty() || inputPass.isEmpty() || inputConfirm.isEmpty()) {
                    errorMessage = "Please fill in all fields"
                    return@Button
                }

                // 2. Check if password and confirm password match
                if (inputPass != inputConfirm) {
                    errorMessage = "Passwords do not match"
                    return@Button
                }

                // 3. Save locally in SharedPreferences
                sharedPrefs.edit().apply {
                    putString("saved_username", inputUser)
                    putString("saved_password", inputPass)
                    apply()
                }

                Toast.makeText(context, "Account created successfully", Toast.LENGTH_SHORT).show()
                errorMessage = null
                onSignupSuccess()
            },
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Sign Up", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Navigate to Login
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text("Already have an account?")
            TextButton(onClick = onNavigateToLogin) {
                Text("Login", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ==============================================================================
// 5. DASHBOARD SCREEN
// ==============================================================================
@Composable
fun DashboardScreen(
    username: String,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Welcome to Dashboard",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Display the logged-in username
        Text(
            text = username,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(36.dp))

        // Logout Button
        Button(
            onClick = onLogout,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Logout", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}