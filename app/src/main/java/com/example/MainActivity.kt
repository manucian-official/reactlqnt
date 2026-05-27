package com.example

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.AppDatabase
import com.example.data.local.*
import com.example.data.repository.ChatRepository
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.*
import com.example.viewmodel.ChatViewModel
import com.example.ui.components.CallOverlay

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 1. Initialize persistent Room DB and Repository layer
        val database = AppDatabase.getDatabase(this)
        val repository = ChatRepository(database)

        // 2. Setup ViewModel Factory for simple Constructor Injection
        val viewModelFactory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return ChatViewModel(this@MainActivity.application, repository) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }

        setContent {
            MyApplicationTheme {
                val chatViewModel: ChatViewModel = viewModel(factory = viewModelFactory)

                // 3. Observe the core States reactively with Lifecycle-safety
                val currentUser by chatViewModel.currentUser.collectAsStateWithLifecycle()
                val isInitializing by chatViewModel.isInitializing.collectAsStateWithLifecycle()
                val conversations by chatViewModel.conversations.collectAsStateWithLifecycle()
                val activeConversation by chatViewModel.activeConversation.collectAsStateWithLifecycle()
                val activeMessages by chatViewModel.activeMessages.collectAsStateWithLifecycle()
                val activeMembers by chatViewModel.activeMembers.collectAsStateWithLifecycle()
                val allUsers by chatViewModel.allUsers.collectAsStateWithLifecycle()
                val notifications by chatViewModel.notifications.collectAsStateWithLifecycle()
                val isTyping by chatViewModel.isTyping.collectAsStateWithLifecycle()
                val replyingToMessage by chatViewModel.replyingToMessage.collectAsStateWithLifecycle()
                val stories by chatViewModel.stories.collectAsStateWithLifecycle()
                val callState by chatViewModel.activeCallState.collectAsStateWithLifecycle()
                val conversationThemes by chatViewModel.conversationThemes.collectAsStateWithLifecycle()

                val isAppLocked by chatViewModel.isAppLocked.collectAsStateWithLifecycle()
                val isBiometricEnabled by chatViewModel.isBiometricEnabled.collectAsStateWithLifecycle()
                val encryptedPassword by chatViewModel.encryptedPassword.collectAsStateWithLifecycle()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val appModifier = Modifier.padding(innerPadding)

                    val currentUserSnapshot = currentUser
                    val activeConversationSnapshot = activeConversation

                    when {
                        isInitializing -> {
                            // Immersive loading splash screen prevents screen swapping artifacts
                            SplashScreen(modifier = appModifier)
                        }
                        isAppLocked -> {
                            AppLockOverlay(
                                activity = this@MainActivity,
                                encryptedPassword = encryptedPassword,
                                onUnlockSuccess = { chatViewModel.unlockApp() }
                            )
                        }
                        currentUserSnapshot == null -> {
                            // User is forced to register/log in to set up username and avatar
                            LoginScreen(
                                onLoginSuccess = { username, name, avatar, password ->
                                    chatViewModel.login(username, name, avatar, password)
                                },
                                modifier = appModifier
                            )
                        }
                        activeConversationSnapshot != null -> {
                            // Deep Chat Window portal active
                            ChatScreen(
                                conversation = activeConversationSnapshot,
                                messages = activeMessages,
                                currentUser = currentUserSnapshot ?: UserEntity("user_me", "khoiplus", "Khôi", "", true, System.currentTimeMillis()),
                                isTyping = isTyping,
                                replyingTo = replyingToMessage,
                                members = activeMembers,
                                allUsers = allUsers,
                                activeTheme = conversationThemes[activeConversationSnapshot.id] ?: "BLUE",
                                onThemeChange = { themeName -> chatViewModel.setConversationTheme(activeConversationSnapshot.id, themeName) },
                                onStartVoiceCall = { chatViewModel.startCall(com.example.viewmodel.CallType.AUDIO) },
                                onStartVideoCall = { chatViewModel.startCall(com.example.viewmodel.CallType.VIDEO) },
                                onBack = { chatViewModel.deselectConversation() }, // goes back beautifully
                                onSendMessage = { text, reply, attachments ->
                                    chatViewModel.sendMessage(text, reply, attachments)
                                },
                                onPinMessage = { msg -> chatViewModel.togglePinMessage(msg) },
                                onEditMessage = { msg, newText -> chatViewModel.editMessage(msg, newText) },
                                onDeleteMessage = { msg -> chatViewModel.deleteMessage(msg) },
                                onAddReaction = { id, emoji -> chatViewModel.addReaction(id, emoji) },
                                onCancelReply = { chatViewModel.setReplyingTo(null) },
                                onUpdateGroup = { name, avatar -> chatViewModel.updateGroupSettings(activeConversationSnapshot.id, name, avatar) },
                                onInviteMembers = { users -> chatViewModel.inviteMembersToGroup(activeConversationSnapshot.id, users) },
                                onLeaveGroup = { chatViewModel.leaveGroup(activeConversationSnapshot.id) },
                                modifier = appModifier
                            )
                        }
                        else -> {
                            // Master Dashboard navigation list
                            HomeScreen(
                                conversations = conversations,
                                notifications = notifications,
                                currentUser = currentUserSnapshot ?: UserEntity("user_me", "khoiplus", "Khôi", "", true, System.currentTimeMillis()),
                                allUsers = allUsers,
                                isBiometricEnabled = isBiometricEnabled,
                                onToggleBiometric = { enabled -> chatViewModel.setBiometricEnabled(enabled) },
                                encryptedPassword = encryptedPassword,
                                onConversationSelected = { conv -> chatViewModel.selectConversation(conv) },
                                onCreateGroup = { title, avatarUrl, selectedMembers ->
                                    chatViewModel.selectOrAddConversation(title, true, selectedMembers, avatarUrl)
                                },
                                onLogout = { chatViewModel.logout() },
                                onMarkNotifRead = { id -> chatViewModel.markNotificationAsRead(id) },
                                onClearNotif = { id -> chatViewModel.clearNotification(id) },
                                stories = stories,
                                onPostStory = { text, media, bg -> chatViewModel.postNewStory(text, media, bg) },
                                onReplyToStory = { username, reply -> chatViewModel.sendStoryReactionMessage(username, reply) },
                                modifier = appModifier
                            )
                        }
                    }
                }

                if (callState.type != com.example.viewmodel.CallType.NONE) {
                    CallOverlay(
                        callState = callState,
                        onMuteToggle = { chatViewModel.toggleMute() },
                        onCameraToggle = { chatViewModel.toggleCameraOff() },
                        onSpeakerToggle = { chatViewModel.toggleSpeaker() },
                        onHangUp = { chatViewModel.endCall() }
                    )
                }
            }
        }
    }
}

@Composable
fun SplashScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(DarkBackground, DarkSurface)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Glow App Logo
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(AccentCyan, AccentPurple)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "M",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 38.sp
                )
            }

            Text(
                text = "Remix: Messenger",
                color = TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            CircularProgressIndicator(
                color = AccentCyan,
                strokeWidth = 3.dp,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
fun AppLockOverlay(
    activity: androidx.fragment.app.FragmentActivity,
    encryptedPassword: String,
    onUnlockSuccess: () -> Unit
) {
    var passwordInput by remember { androidx.compose.runtime.mutableStateOf("") }
    var passwordVisible by remember { androidx.compose.runtime.mutableStateOf(false) }
    var errorMsg by remember { androidx.compose.runtime.mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(com.example.ui.theme.DarkBackground),
        contentAlignment = Alignment.Center
    ) {
        // Neon radial glow for high modernism aesthetic
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    androidx.compose.ui.graphics.Brush.radialGradient(
                        colors = listOf(com.example.ui.theme.AccentCyan.copy(alpha = 0.1f), androidx.compose.ui.graphics.Color.Transparent),
                        radius = 1000f
                    )
                )
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .border(0.5.dp, com.example.ui.theme.DarkBorder, androidx.compose.foundation.shape.RoundedCornerShape(24.dp)),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.DarkSurface.copy(alpha = 0.95f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(
                            androidx.compose.ui.graphics.Brush.linearGradient(
                                colors = listOf(com.example.ui.theme.AccentCyan, com.example.ui.theme.AccentPurple)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = "Lock icon",
                        tint = androidx.compose.ui.graphics.Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Text(
                    text = "Ứng dụng đang khóa 🔒",
                    color = com.example.ui.theme.TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Text(
                    text = "Yêu cầu xác nhận sinh trắc học hoặc nhập mật khẩu để tiếp tục thảo luận.",
                    color = com.example.ui.theme.TextSecondary,
                    fontSize = 12.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Button(
                    onClick = {
                        val status = com.example.data.security.BiometricHelper.isBiometricAvailable(activity)
                        if (status == com.example.data.security.BiometricHelper.BiometricStatus.AVAILABLE) {
                            com.example.data.security.BiometricHelper.showBiometricPrompt(
                                activity = activity,
                                title = "Xác thực bảo mật",
                                subtitle = "Truy cập Realtime Tech Chat",
                                onSuccess = { onUnlockSuccess() },
                                onError = { _, err ->
                                    android.widget.Toast.makeText(activity, "Lỗi: $err", android.widget.Toast.LENGTH_SHORT).show()
                                },
                                onFailed = {
                                    android.widget.Toast.makeText(activity, "Xác minh sinh trắc học thất bại!", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            )
                        } else {
                            android.widget.Toast.makeText(activity, "Cảm biến không khả dụng! Vui lòng dùng mật khẩu.", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp)),
                    colors = ButtonDefaults.buttonColors(containerColor = com.example.ui.theme.AccentCyan)
                ) {
                    Text("Xác thực Sinh trắc học Handshake", color = androidx.compose.ui.graphics.Color.White, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, fontSize = 13.sp)
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Divider(color = com.example.ui.theme.DarkBorder, thickness = 0.5.dp)
                    Text(
                        text = "HOẶC NHẬP MẬT KHẨU KOYSTORE",
                        color = com.example.ui.theme.TextMuted,
                        fontSize = 9.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        modifier = Modifier
                            .background(com.example.ui.theme.DarkSurface)
                            .padding(horizontal = 10.dp)
                    )
                }

                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = {
                        passwordInput = it
                        errorMsg = ""
                    },
                    label = { Text("Mật khẩu mã hóa bypass") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Lock", tint = com.example.ui.theme.AccentPurple) },
                    trailingIcon = {
                        val image = if (passwordVisible) {
                            Icons.Default.Visibility
                        } else {
                            Icons.Default.VisibilityOff
                        }
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = image, contentDescription = "Password toggle", tint = com.example.ui.theme.TextSecondary)
                        }
                    },
                    visualTransformation = if (passwordVisible) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = com.example.ui.theme.TextPrimary,
                        unfocusedTextColor = com.example.ui.theme.TextPrimary,
                        focusedBorderColor = com.example.ui.theme.AccentPurple,
                        unfocusedBorderColor = com.example.ui.theme.DarkBorder,
                        focusedLabelColor = com.example.ui.theme.AccentPurple,
                        unfocusedLabelColor = com.example.ui.theme.TextSecondary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMsg.isNotEmpty()) {
                    Text(errorMsg, color = com.example.ui.theme.ErrorRadical, fontSize = 11.sp)
                }

                Button(
                    onClick = {
                        if (passwordInput.isBlank()) {
                            errorMsg = "Xin hãy nhập mật khẩu thiết lập!"
                            return@Button
                        }
                        val decrypted = com.example.data.security.CryptoManager.decryptText(encryptedPassword)
                        if (decrypted == passwordInput) {
                            onUnlockSuccess()
                            android.widget.Toast.makeText(activity, "Bypass mở khóa Keystore thành công!", android.widget.Toast.LENGTH_SHORT).show()
                        } else {
                            errorMsg = "Mật khẩu không trùng khớp bản ghi mã hóa!"
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp)),
                    colors = ButtonDefaults.buttonColors(containerColor = com.example.ui.theme.DarkSurfaceVariant)
                ) {
                    Text("Xác minh & Giải mã Key", color = com.example.ui.theme.TextPrimary, fontSize = 13.sp)
                }
            }
        }
    }
}
