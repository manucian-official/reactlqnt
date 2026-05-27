package com.example.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.*
import com.example.data.repository.ChatRepository
import com.example.data.security.CryptoManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.UUID

class ChatViewModel(
    private val application: Application,
    private val repository: ChatRepository
) : AndroidViewModel(application) {

    // Current logged-in Session state
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    private val _isInitializing = MutableStateFlow(true)
    val isInitializing: StateFlow<Boolean> = _isInitializing.asStateFlow()

    // Security & Biometric states
    private val _isAppLocked = MutableStateFlow(false)
    val isAppLocked: StateFlow<Boolean> = _isAppLocked.asStateFlow()

    private val _isBiometricEnabled = MutableStateFlow(false)
    val isBiometricEnabled: StateFlow<Boolean> = _isBiometricEnabled.asStateFlow()

    private val _encryptedPassword = MutableStateFlow("")
    val encryptedPassword: StateFlow<String> = _encryptedPassword.asStateFlow()

    // Screen states
    val conversations: StateFlow<List<ConversationEntity>> = repository.conversations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notifications: StateFlow<List<NotificationEntity>> = repository.notifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allUsers: StateFlow<List<UserEntity>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _activeConversation = MutableStateFlow<ConversationEntity?>(null)
    val activeConversation: StateFlow<ConversationEntity?> = _activeConversation.asStateFlow()

    private val _activeMessages = MutableStateFlow<List<MessageEntity>>(emptyList())
    val activeMessages: StateFlow<List<MessageEntity>> = _activeMessages.asStateFlow()

    private val _activeMembers = MutableStateFlow<List<UserEntity>>(emptyList())
    val activeMembers: StateFlow<List<UserEntity>> = _activeMembers.asStateFlow()

    // Realtime UI states
    private val _isTyping = MutableStateFlow(false)
    val isTyping: StateFlow<Boolean> = _isTyping.asStateFlow()

    private val _replyingToMessage = MutableStateFlow<MessageEntity?>(null)
    val replyingToMessage: StateFlow<MessageEntity?> = _replyingToMessage.asStateFlow()

    private var messageObserveJob: Job? = null
    private var memberObserveJob: Job? = null
    private var simulatorJob: Job? = null
    private var callTimerJob: Job? = null

    // Call and Video Call states
    private val _activeCallState = MutableStateFlow(CallState())
    val activeCallState: StateFlow<CallState> = _activeCallState.asStateFlow()

    // Stories state
    private val _stories = MutableStateFlow<List<StoryModel>>(emptyList())
    val stories: StateFlow<List<StoryModel>> = _stories.asStateFlow()

    // Conversation custom themes: conversationId -> themeChoiceName
    private val _conversationThemes = MutableStateFlow<Map<String, String>>(emptyMap())
    val conversationThemes: StateFlow<Map<String, String>> = _conversationThemes.asStateFlow()

    init {
        // Automatically verify or set pre-saved user
        viewModelScope.launch {
            val prefs = application.getSharedPreferences("rtchat_secure_prefs", Context.MODE_PRIVATE)
            val savedUsername = prefs.getString("username", null)
            val savedDisplayName = prefs.getString("displayName", null)
            val savedAvatarUrl = prefs.getString("avatarUrl", null)
            val savedEncryptedPassword = prefs.getString("encrypted_password", "")
            val isBioEnabled = prefs.getBoolean("biometric_enabled", false)

            _isBiometricEnabled.value = isBioEnabled
            _encryptedPassword.value = savedEncryptedPassword ?: ""

            if (savedUsername != null && savedDisplayName != null) {
                // Pre-loaded user exists
                val user = UserEntity(
                    id = "user_me",
                    username = savedUsername,
                    displayName = savedDisplayName,
                    avatarUrl = savedAvatarUrl ?: "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=150&q=80",
                    isOnline = true,
                    lastSeen = System.currentTimeMillis()
                )
                repository.saveUser(user)
                _currentUser.value = user

                if (isBioEnabled) {
                    _isAppLocked.value = true
                }
                
                // Seed base values for an incredible UI experience
                repository.seedInitialDataIfEmpty(
                    currentUserId = user.id,
                    currentUserName = user.displayName,
                    currentUserAvatar = user.avatarUrl
                )
            } else {
                // No user saved yet — take them to LoginScreen
                _currentUser.value = null
                
                // Still seed initial data with fallback values so database is ready for testing
                repository.seedInitialDataIfEmpty(
                    currentUserId = "user_me",
                    currentUserName = "Khôi Plus",
                    currentUserAvatar = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=150&q=80"
                )
            }
            _isInitializing.value = false
            
            // Seed beautiful Stories list right away
            _stories.value = listOf(
                StoryModel(
                    id = "st_1",
                    userId = "user_tien",
                    userName = "Tiến Nguyễn",
                    userAvatar = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=150&q=80",
                    mediaUrl = "https://images.unsplash.com/photo-1507238691740-187a5b1d37b8?auto=format&fit=crop&w=400&q=80",
                    textContent = "Đang tối ưu PostgreSQL index... 🚀 Mượt quá!",
                    timestamp = System.currentTimeMillis() - 3600000,
                    bgType = "IMAGE"
                ),
                StoryModel(
                    id = "st_2",
                    userId = "user_mai",
                    userName = "Mai Hoa",
                    userAvatar = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=150&q=80",
                    mediaUrl = "",
                    textContent = "Chúc mọi người một ngày làm việc tràn đầy năng lượng! 🌸✨",
                    timestamp = System.currentTimeMillis() - 7200000,
                    bgType = "GRADIENT_SUNSET"
                ),
                StoryModel(
                    id = "st_3",
                    userId = "user_trung",
                    userName = "Trung Lê",
                    userAvatar = "https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?auto=format&fit=crop&w=150&q=80",
                    mediaUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?auto=format&fit=crop&w=400&q=80",
                    textContent = "Aesthetic setup workspace đợt này oke ghê 💻⌨️",
                    timestamp = System.currentTimeMillis() - 12000000,
                    bgType = "IMAGE"
                )
            )
        }

        // Start background socket emulation
        startSocketEmulation()
    }

    fun login(username: String, displayName: String, avatarUrl: String, passwordEntered: String) {
        viewModelScope.launch {
            val cleanUsername = username.lowercase().replace(" ", "")
            val cleanAvatar = avatarUrl.ifEmpty { "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=150&q=80" }
            
            // 1. Encrypt password using Android Keystore System
            val cipherTextWithIv = CryptoManager.encryptText(passwordEntered)

            // 2. Save securely to SharedPreferences
            val prefs = application.getSharedPreferences("rtchat_secure_prefs", Context.MODE_PRIVATE)
            prefs.edit()
                .putString("username", cleanUsername)
                .putString("displayName", displayName)
                .putString("avatarUrl", cleanAvatar)
                .putString("encrypted_password", cipherTextWithIv)
                .apply()

            _encryptedPassword.value = cipherTextWithIv

            // 3. Save User Entity
            val user = UserEntity(
                id = "user_me",
                username = cleanUsername,
                displayName = displayName,
                avatarUrl = cleanAvatar,
                isOnline = true,
                lastSeen = System.currentTimeMillis()
            )
            repository.saveUser(user)
            _currentUser.value = user

            // 4. Seed base values with current user representation
            repository.seedInitialDataIfEmpty(
                currentUserId = user.id,
                currentUserName = user.displayName,
                currentUserAvatar = user.avatarUrl
            )
        }
    }

    fun setBiometricEnabled(enabled: Boolean) {
        val prefs = application.getSharedPreferences("rtchat_secure_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("biometric_enabled", enabled).apply()
        _isBiometricEnabled.value = enabled
    }

    fun unlockApp() {
        _isAppLocked.value = false
    }

    fun logout() {
        val prefs = application.getSharedPreferences("rtchat_secure_prefs", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
        
        _currentUser.value = null
        _activeConversation.value = null
        _activeMessages.value = emptyList()
        _encryptedPassword.value = ""
        _isBiometricEnabled.value = false
        _isAppLocked.value = false
    }

    fun deselectConversation() {
        _activeConversation.value = null
        _activeMessages.value = emptyList()
        _activeMembers.value = emptyList()
    }

    fun selectConversation(conversation: ConversationEntity) {
        _activeConversation.value = conversation
        
        // Listen to Messages corresponding to selection
        messageObserveJob?.cancel()
        messageObserveJob = viewModelScope.launch {
            repository.getMessagesForConversation(conversation.id).collect { list ->
                _activeMessages.value = list
            }
        }

        // Listen to members list
        memberObserveJob?.cancel()
        memberObserveJob = viewModelScope.launch {
            repository.getMembersOfConversation(conversation.id).collect { list ->
                _activeMembers.value = list
            }
        }
    }

    fun sendMessage(text: String, replyTo: MessageEntity? = null, attachments: List<AttachmentEntity> = emptyList()) {
        val user = _currentUser.value ?: return
        val conv = _activeConversation.value ?: return

        viewModelScope.launch {
            val message = MessageEntity(
                id = UUID.randomUUID().toString(),
                conversationId = conv.id,
                senderId = user.id,
                senderName = user.displayName,
                senderAvatar = user.avatarUrl,
                text = text,
                timestamp = System.currentTimeMillis(),
                replyToMessageId = replyTo?.id,
                replyToMessageText = replyTo?.let { if (it.isDeleted) "Tin nhắn đã bị thu hồi" else it.text }
            )

            repository.insertMessage(message, attachments)
            _replyingToMessage.value = null

            // Trigger simulated "Socket Response" logic
            triggerSimulatedResponse(text)
        }
    }

    fun togglePinMessage(message: MessageEntity) {
        viewModelScope.launch {
            repository.pinMessage(message.id, !message.isPinned)
        }
    }

    fun editMessage(message: MessageEntity, newText: String) {
        viewModelScope.launch {
            repository.editMessage(message.id, newText)
        }
    }

    fun deleteMessage(message: MessageEntity) {
        viewModelScope.launch {
            repository.deleteMessage(message.id)
        }
    }

    fun setReplyingTo(message: MessageEntity?) {
        _replyingToMessage.value = message
    }

    fun addReaction(messageId: String, emoji: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.addReaction(messageId, user.id, user.displayName, emoji)
        }
    }

    fun removeReaction(messageId: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.removeReaction(messageId, user.id)
        }
    }

    fun markNotificationAsRead(id: String) {
        viewModelScope.launch {
            repository.markNotificationRead(id)
        }
    }

    fun clearNotification(id: String) {
        viewModelScope.launch {
            repository.deleteNotification(id)
        }
    }

    fun selectOrAddConversation(title: String, isGroup: Boolean, selectedUsers: List<UserEntity>, avatarUrl: String? = null) {
        viewModelScope.launch {
            val convId = "conv_" + UUID.randomUUID().toString().take(6)
            val newConv = ConversationEntity(
                id = convId,
                title = title,
                isGroup = isGroup,
                createdAt = System.currentTimeMillis(),
                lastMessageText = "Cuộc hội thoại mới đã được khởi tạo",
                lastMessageTime = System.currentTimeMillis(),
                avatarUrl = avatarUrl ?: if (isGroup) "https://images.unsplash.com/photo-1582213782179-e0d53f98f2ca?auto=format&fit=crop&w=150&q=80" else null
            )
            
            val memberIds = selectedUsers.map { it.id }.toMutableList()
            _currentUser.value?.id?.let { memberIds.add(it) }

            repository.createConversation(newConv, memberIds)
            _activeConversation.value = newConv
            selectConversation(newConv)
        }
    }

    fun leaveGroup(convId: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val announcement = MessageEntity(
                id = UUID.randomUUID().toString(),
                conversationId = convId,
                senderId = "system",
                senderName = "Hệ thống",
                senderAvatar = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?auto=format&fit=crop&w=150&q=80",
                text = "${user.displayName} đã rời khỏi nhóm chat",
                timestamp = System.currentTimeMillis()
            )
            repository.insertMessage(announcement)
            repository.removeMemberFromConversation(convId, user.id)

            _activeConversation.value = null
            _activeMessages.value = emptyList()
            _activeMembers.value = emptyList()
        }
    }

    fun inviteMembersToGroup(convId: String, invitedUsers: List<UserEntity>) {
        if (invitedUsers.isEmpty()) return
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val memberships = invitedUsers.map {
                ConversationMemberEntity(convId, it.id)
            }
            repository.insertMembers(memberships)

            val namesString = invitedUsers.joinToString(", ") { it.displayName }
            val announcement = MessageEntity(
                id = UUID.randomUUID().toString(),
                conversationId = convId,
                senderId = "system",
                senderName = "Hệ thống",
                senderAvatar = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?auto=format&fit=crop&w=150&q=80",
                text = "${user.displayName} đã thêm $namesString vào nhóm",
                timestamp = System.currentTimeMillis()
            )
            repository.insertMessage(announcement)
        }
    }

    fun updateGroupSettings(convId: String, newName: String, newAvatarUrl: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.updateGroupDetails(convId, newName, newAvatarUrl)
            
            val announcement = MessageEntity(
                id = UUID.randomUUID().toString(),
                conversationId = convId,
                senderId = "system",
                senderName = "Hệ thống",
                senderAvatar = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?auto=format&fit=crop&w=150&q=80",
                text = "${user.displayName} đã cập nhật thông tin nhóm thành \"$newName\"",
                timestamp = System.currentTimeMillis()
            )
            repository.insertMessage(announcement)

            // Reload exact active snapshot configuration in view state in real-time
            val updatedConfig = repository.conversations.firstOrNull()?.find { it.id == convId }
            if (updatedConfig != null) {
                _activeConversation.value = updatedConfig
            }
        }
    }

    // --- SOCKET.IO SIMULATION SUITE ---
    private fun startSocketEmulation() {
        simulatorJob?.cancel()
        simulatorJob = viewModelScope.launch {
            while (isActive) {
                delay(24000) // Sleep 24 seconds, periodically firing system alerts or notifications
                val currentConv = _activeConversation.value
                val notificationCount = notifications.value.filter { !it.isRead }.size
                
                // Randomly trigger typing indicator sometimes in active group or active chat
                if (currentConv != null && !_isTyping.value) {
                    val senderId = if (currentConv.isGroup) "user_tien" else currentConv.id.replace("conv_", "user_")
                    val sender = repository.getUser(senderId)
                    if (sender != null && sender.isOnline) {
                        _isTyping.value = true
                        delay(2500)
                        _isTyping.value = false
                        
                        // Pick a smart preset sentences depending on group / name
                        val mockSentence = listOf(
                            "Mọi người nghe tin gì chưa, dự án vừa test benchmark đạt hơn 10k TPS đấy!",
                            "Code clean chuẩn enterprise chạy sướng thật.",
                            "Tối nay làm cốc bia mừng app deploy thành công nha ae!",
                            "Vừa tối ưu xong API, load mượt nổ mắt."
                        ).random()

                        val autoMsg = MessageEntity(
                            id = UUID.randomUUID().toString(),
                            conversationId = currentConv.id,
                            senderId = sender.id,
                            senderName = sender.displayName,
                            senderAvatar = sender.avatarUrl,
                            text = mockSentence,
                            timestamp = System.currentTimeMillis()
                        )
                        repository.insertMessage(autoMsg)
                    }
                } else if (currentConv == null && notificationCount < 3) {
                    // Throw background system notification
                    val notifId = UUID.randomUUID().toString()
                    val sysNotif = NotificationEntity(
                        id = notifId,
                        title = "Tin nhắn mới từ Đội Ngũ Tech 🔥",
                        content = "Mai Hoa Lê: \"Em đã cập nhật UI hoàn thành rồi nha!\"",
                        timestamp = System.currentTimeMillis(),
                        isRead = false
                    )
                    repository.insertNotification(sysNotif)
                }
            }
        }
    }

    private fun triggerSimulatedResponse(triggerText: String) {
        val currentConv = _activeConversation.value ?: return
        viewModelScope.launch {
            // Wait 1.2s then flash typing bubble
            delay(1200)
            _isTyping.value = true
            delay(2000) // typing for 2 seconds
            _isTyping.value = false

            // Answer based on typing keyword triggers
            val answer = when {
                triggerText.contains("họp", ignoreCase = true) || triggerText.contains("meeting", ignoreCase = true) -> {
                    "Nhất trí ông ơi! Tầm 15h chiều mình gộp zoom làm quả sprint review nhé."
                }
                triggerText.contains("bug", ignoreCase = true) || triggerText.contains("lỗi", ignoreCase = true) -> {
                    "Lỗi ở đâu thế? Đã check logs của Prisma + PostgreSQL, kết nối DB vẫn cực kỳ ổn định nha."
                }
                triggerText.contains("logo", ignoreCase = true) || triggerText.contains("ảnh", ignoreCase = true) -> {
                    "Logo với assets tối nay em xuất bản vẽ gửi qua ổ S3 cho mọi người download luôn."
                }
                triggerText.contains("socket", ignoreCase = true) || triggerText.contains("realtime", ignoreCase = true) -> {
                    "Tối ưu adapter Socket IO xong xuôi rồi, ping giảm chỉ còn 8ms thôi nhé ae!"
                }
                else -> {
                    listOf(
                        "Rất chất lượng anh em ơi! 🚀",
                        "Quá dữ, code này rà soát kỹ lắm rồi, ko lo bug đâu.",
                        "Em đồng ý, thiết kế UI/UX đợt này sang xịn mịn hơn hẳn.",
                        "Ok luôn ông nhé, tôi đang tối ưu thêm index dưới PostgreSQL để tăng load.",
                        "Đã nhận thông tin nhé!"
                    ).random()
                }
            }

            // Assign who responds
            val senderId = if (currentConv.isGroup) {
                listOf("user_tien", "user_mai", "user_trung").random()
            } else {
                currentConv.id.replace("conv_", "user_")
            }
            val sender = repository.getUser(senderId) ?: UserEntity("user_tien", "tien_nguyen", "Kiều Tiến Nguyễn", "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=150&q=80", true, System.currentTimeMillis())

            val responseMsg = MessageEntity(
                id = UUID.randomUUID().toString(),
                conversationId = currentConv.id,
                senderId = sender.id,
                senderName = sender.displayName,
                senderAvatar = sender.avatarUrl,
                text = answer,
                timestamp = System.currentTimeMillis()
            )
            repository.insertMessage(responseMsg)

            // Trigger emoji reaction to your text automatically after 3 seconds for visual fun!
            delay(2500)
            val randomEmoji = listOf("❤️", "🔥", "👍", "😂").random()
            repository.addReaction(responseMsg.id, "user_me", "Khôi Plus", randomEmoji)
        }
    }

    override fun onCleared() {
        super.onCleared()
        messageObserveJob?.cancel()
        memberObserveJob?.cancel()
        simulatorJob?.cancel()
        callTimerJob?.cancel()
    }

    // --- IMMERSIVE CALL METHODS ---
    fun startCall(type: CallType) {
        val conv = _activeConversation.value ?: return
        val user = _currentUser.value ?: return
        
        val participantId = if (conv.isGroup) "group_members" else conv.id.replace("conv_", "user_")
        val participantName = conv.title
        val participantAvatar = conv.avatarUrl ?: if (conv.isGroup) {
            "https://images.unsplash.com/photo-1582213782179-e0d53f98f2ca?auto=format&fit=crop&w=150&q=80"
        } else {
            "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=150&q=80"
        }

        _activeCallState.value = CallState(
            type = type,
            status = CallStatus.RINGING,
            participantId = participantId,
            participantName = participantName,
            participantAvatar = participantAvatar,
            duration = 0
        )

        // Automatic response connection loop simulator after 2.5 seconds
        viewModelScope.launch {
            delay(2500)
            if (_activeCallState.value.status == CallStatus.RINGING) {
                _activeCallState.value = _activeCallState.value.copy(status = CallStatus.CONNECTED)
                
                // Start a timer counting seconds
                callTimerJob?.cancel()
                callTimerJob = viewModelScope.launch {
                    while (isActive) {
                        delay(1000)
                        _activeCallState.value = _activeCallState.value.copy(
                            duration = _activeCallState.value.duration + 1
                        )
                    }
                }
            }
        }
    }

    fun endCall() {
        val currentCall = _activeCallState.value
        if (currentCall.type == CallType.NONE) return

        callTimerJob?.cancel()
        _activeCallState.value = CallState() // reset to none

        val conv = _activeConversation.value ?: return

        // Insert historical record into chat
        val durationFormatted = String.format("%02d:%02d", currentCall.duration / 60, currentCall.duration % 60)
        val textMessage = if (currentCall.type == CallType.VIDEO) {
            "📹 Cuộc gọi video kết thúc. Thời lượng: $durationFormatted"
        } else {
            "📞 Cuộc gọi thoại kết thúc. Thời lượng: $durationFormatted"
        }

        viewModelScope.launch {
            val historyMsg = MessageEntity(
                id = UUID.randomUUID().toString(),
                conversationId = conv.id,
                senderId = "system",
                senderName = "Hệ thống",
                senderAvatar = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?auto=format&fit=crop&w=150&q=80",
                text = textMessage,
                timestamp = System.currentTimeMillis()
            )
            repository.insertMessage(historyMsg)
        }
    }

    fun toggleMute() {
        _activeCallState.value = _activeCallState.value.copy(isMuted = !_activeCallState.value.isMuted)
    }

    fun toggleCameraOff() {
        _activeCallState.value = _activeCallState.value.copy(isCameraOff = !_activeCallState.value.isCameraOff)
    }

    fun toggleSpeaker() {
        _activeCallState.value = _activeCallState.value.copy(isSpeakerOn = !_activeCallState.value.isSpeakerOn)
    }

    // --- DAILY STORIES SUITE ---
    fun postNewStory(textContent: String, mediaUrl: String, bgType: String) {
        val user = _currentUser.value ?: return
        val newStory = StoryModel(
            id = "st_" + UUID.randomUUID().toString().take(6),
            userId = user.id,
            userName = user.displayName,
            userAvatar = user.avatarUrl,
            mediaUrl = mediaUrl,
            textContent = textContent,
            timestamp = System.currentTimeMillis(),
            bgType = bgType
        )
        _stories.value = listOf(newStory) + _stories.value
    }

    fun sendStoryReactionMessage(userName: String, emoji: String) {
        val conv = _activeConversation.value
        if (conv != null) {
            sendMessage("Bày tỏ cảm xúc $emoji với tin của bạn!")
        }
    }

    // --- CUSTOM VISUAL THEMES ---
    fun setConversationTheme(conversationId: String, themeName: String) {
        val updated = _conversationThemes.value.toMutableMap()
        updated[conversationId] = themeName
        _conversationThemes.value = updated
    }
}

// --- SUPPORTING CHAT DATA TYPES ---
enum class CallType { NONE, AUDIO, VIDEO }
enum class CallStatus { IDLE, RINGING, CONNECTED }

data class CallState(
    val type: CallType = CallType.NONE,
    val status: CallStatus = CallStatus.IDLE,
    val duration: Int = 0,
    val isMuted: Boolean = false,
    val isCameraOff: Boolean = false,
    val isSpeakerOn: Boolean = false,
    val participantId: String = "",
    val participantName: String = "",
    val participantAvatar: String = ""
)

data class StoryModel(
    val id: String,
    val userId: String,
    val userName: String,
    val userAvatar: String,
    val mediaUrl: String,
    val isVideo: Boolean = false,
    val textContent: String = "",
    val timestamp: Long,
    val bgType: String = "" // "GRADIENT_SPACE", "GRADIENT_CYBER", "GRADIENT_SUNSET", "GRADIENT_EMERALD", or "IMAGE"
)
