package com.example.reply.ui.homepage

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.reply.network.ApiService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onBackClicked: () -> Unit,
    onLoginClicked: () -> Unit,
    onRegisterSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var nickname by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "注册") },
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("邮箱") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = nickname,
                onValueChange = { nickname = it },
                label = { Text("昵称") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("密码") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    val image = if (passwordVisible)
                        Icons.Filled.Visibility
                    else Icons.Filled.VisibilityOff

                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = "切换密码可见性")
                    }
                }
            )

            // 消息显示区域
            if (!errorMessage.isNullOrEmpty() || !successMessage.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage ?: successMessage ?: "",
                    color = if (errorMessage != null) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    // 替换全角 ＠ 为半角 @
                    val normalizedEmail = email.replace("＠", "@").trim()
                    val trimmedNickname = nickname.trim()

                    if (normalizedEmail.isEmpty() || password.isEmpty() || trimmedNickname.isEmpty()) {
                        errorMessage = "请填写所有字段"
                        successMessage = null
                        return@Button
                    }

//                    // 超宽松邮箱验证：只要有@符号就行
//                    if (!normalizedEmail.contains("@")) {
//                        errorMessage = "请输入有效的邮箱地址"
//                        successMessage = null
//                        return@Button
//                    }

                    // 密码长度验证
                    if (password.length < 6) {
                        errorMessage = "密码长度至少6位"
                        successMessage = null
                        return@Button
                    }

                    isLoading = true
                    errorMessage = null
                    successMessage = null

                    // 使用处理后的邮箱
                    ApiService.register(normalizedEmail, password, trimmedNickname) { success, message ->
                        isLoading = false
                        if (success) {
                            successMessage = "注册成功！"
                            // 自动跳转到登录或直接登录
                            onRegisterSuccess()
                        } else {
                            errorMessage = message
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(text = "注册")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 添加登录提示和链接
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "已有账户？",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.width(4.dp))
                TextButton(
                    onClick = onLoginClicked,
                    modifier = Modifier.padding(0.dp)
                ) {
                    Text(
                        text = "去登录",
                        style = MaterialTheme.typography.bodySmall.copy(
                            textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        }
    }
}