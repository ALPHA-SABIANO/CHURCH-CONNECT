@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.churchconnect

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

//SPLASH

@Composable
fun SplashScreen(onStart: () -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(ChurchBlueDark, ChurchBlue, ChurchBlueLight)))
    ) {
        Canvas(Modifier.fillMaxWidth().height(220.dp).align(Alignment.BottomCenter)) {
            val w = size.width
            val h = size.height
            val back = Path().apply {
                moveTo(0f, h * 0.45f)
                cubicTo(w * 0.25f, h * 0.10f, w * 0.50f, h * 0.80f, w, h * 0.35f)
                lineTo(w, h); lineTo(0f, h); close()
            }
            drawPath(back, ChurchGold.copy(alpha = 0.30f))
            val front = Path().apply {
                moveTo(0f, h * 0.65f)
                cubicTo(w * 0.30f, h * 0.35f, w * 0.60f, h * 0.95f, w, h * 0.60f)
                lineTo(w, h); lineTo(0f, h); close()
            }
            drawPath(front, Color.White.copy(alpha = 0.22f))
        }

        Column(
            Modifier.fillMaxSize().systemBarsPadding().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.weight(1f))
            Surface(shape = CircleShape, color = Color.White, shadowElevation = 8.dp, modifier = Modifier.size(150.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Image(
                        painter = painterResource(id = R.drawable.church_logo),
                        contentDescription = "Church Connect logo",
                        modifier = Modifier.size(100.dp)
                    )
                }
            }
            Spacer(Modifier.height(20.dp))
            Text("Church Connect", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text("Stay informed. Stay connected.", color = Color.White.copy(alpha = 0.9f), fontSize = 14.sp)
            Spacer(Modifier.weight(1f))
            Button(
                onClick = onStart,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ChurchGold, contentColor = ChurchBlueDark)
            ) { Text("Continue to Login", fontWeight = FontWeight.Bold) }
            Spacer(Modifier.height(16.dp))
        }
    }
}

// LOGIN AND REGISTER

@Composable
fun AuthScreen(onBack: (() -> Unit)?, onSuccess: () -> Unit) {
    var isLogin by remember { mutableStateOf(true) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") } // used as email or phone identifier for login/registration
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("member") } // "member", "pastor", or "admin"
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var info by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(Modifier.fillMaxSize().background(ScreenBg)) {
        ScreenHeader(
            title = if (isLogin) "Welcome to Church Connect" else "Create Account",
            subtitle = if (isLogin) "Login to access your church information" else "Register to join your church",
            onBack = if (!isLogin) ({ isLogin = true; error = null; info = null }) else onBack
        )
        Column(
            Modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                AppTabs(if (isLogin) 0 else 1, listOf("Login", "Register")) {
                    isLogin = (it == 0); error = null; info = null
                }
            }

            if (!isLogin) {
                // Role Selection (only during Registration)
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Select Account Type", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextDark)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val roles = listOf(
                                "member" to "Member",
                                "pastor" to "Pastor",
                                "admin" to "Admin"
                            )
                            roles.forEach { (roleKey, roleLabel) ->
                                val isSelected = selectedRole == roleKey
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedRole = roleKey },
                                    label = {
                                        Text(
                                            roleLabel,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = when (roleKey) {
                                            "admin" -> ChurchGold
                                            "pastor" -> ChurchGreen
                                            else -> ChurchBlue
                                        },
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }
                }

                // Full Name (required for registration)
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name *") },
                    placeholder = { Text("Enter your full name") },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            // Email Address or Phone Number (required for both login and registration)
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address or Phone Number *") },
                placeholder = { Text("Enter email or phone number") },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password *") },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                            tint = ChurchBlue
                        )
                    }
                },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            if (!isLogin) {
                OutlinedTextField(
                    value = confirm,
                    onValueChange = { confirm = it },
                    label = { Text("Confirm Password *") },
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(
                                imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password",
                                tint = ChurchBlue
                            )
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            error?.let { err ->
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(err, color = Color.Red, fontSize = 13.sp)
                }
            }
            info?.let { Text(it, color = ChurchGreen, fontSize = 13.sp) }

            PrimaryButton(
                text = if (loading) "Please wait..." else if (isLogin) "Log In" else "Create Account",
                enabled = !loading,
                onClick = {
                    error = null
                    info = null

                    if (isLogin) {
                        val identifier = email.trim()
                        val isValidId = isValidEmail(identifier) || isValidPhoneNumber(identifier)
                        if (identifier.isBlank()) {
                            error = "Please enter your email address or phone number."
                        } else if (!isValidId) {
                            error = "Please enter a valid email address or phone number."
                        } else if (password.length < 6) {
                            error = "Password must be at least 6 characters long."
                        } else {
                            loading = true
                            scope.launch {
                                try {
                                    ChurchRepository.login(identifier, password)
                                    onSuccess()
                                } catch (e: Exception) {
                                    error = e.localizedMessage ?: "Login failed. Check your credentials."
                                }
                                loading = false
                            }
                        }
                    } else {
                        val nameClean = name.trim()
                        val identifier = email.trim()
                        val isValidId = isValidEmail(identifier) || isValidPhoneNumber(identifier)

                        if (nameClean.isBlank()) {
                            error = "Please enter your full name."
                        } else if (identifier.isBlank()) {
                            error = "Please enter your email address or phone number."
                        } else if (!isValidId) {
                            error = "Please enter a valid email address or 10-digit phone number."
                        } else if (password.length < 6) {
                            error = "Password must be at least 6 characters long."
                        } else if (password != confirm) {
                            error = "Passwords do not match."
                        } else {
                            val phoneClean = if (isValidPhoneNumber(identifier)) identifier else ""
                            val emailClean = if (isValidEmail(identifier)) identifier else ""
                            loading = true
                            scope.launch {
                                try {
                                    ChurchRepository.register(nameClean, phoneClean, emailClean, password, selectedRole)
                                    onSuccess()
                                } catch (e: Exception) {
                                    error = e.localizedMessage ?: "Registration failed. Please try again."
                                }
                                loading = false
                            }
                        }
                    }
                }
            )

            if (isLogin) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            error = null
                            info = null
                            val identifier = email.trim()
                            if (identifier.isBlank() || (!isValidEmail(identifier) && !isValidPhoneNumber(identifier))) {
                                error = "Type your valid Email or 10-digit Phone Number above first, then tap Forgot password."
                            } else {
                                val resetEmail = if (isValidEmail(identifier)) {
                                    identifier
                                } else {
                                    "${identifier.filter { it.isDigit() }}@churchconnect.local"
                                }
                                scope.launch {
                                    try {
                                        ChurchRepository.resetPassword(resetEmail)
                                        info = "A password reset link was sent."
                                    } catch (e: Exception) {
                                        error = e.localizedMessage ?: "Could not send the reset link."
                                    }
                                }
                            }
                        }
                    ) { Text("Forgot password?", color = ChurchBlue) }
                }
            }

            // Explicit Logout / Exit button
            OutlinedButton(
                onClick = {
                    ChurchRepository.logout()
                    onBack?.invoke()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ChurchBlue)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = "Exit",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("Exit", fontWeight = FontWeight.Bold)
            }
        }
    }
}
