package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ConversationEntity
import com.example.data.local.NotificationEntity
import com.example.data.local.UserEntity
import com.example.data.security.BiometricHelper
import com.example.data.security.CryptoManager
import com.example.ui.components.UserAvatar
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    conversations: List<ConversationEntity>,
    notifications: List<NotificationEntity>,
    currentUser: UserEntity,
    allUsers: List<UserEntity>,
    isBiometricEnabled: Boolean,
    onToggleBiometric: (Boolean) -> Unit,
    encryptedPassword: String,
    onConversationSelected: (ConversationEntity) -> Unit,
    onCreateGroup: (title: String, avatarUrl: String, members: List<UserEntity>) -> Unit,
    onLogout: () -> Unit,
    onMarkNotifRead: (String) -> Unit,
    onClearNotif: (String) -> Unit,
    stories: List<com.example.viewmodel.StoryModel> = emptyList(),
    onPostStory: (String, String, String) -> Unit = { _, _, _ -> },
    onReplyToStory: (String, String) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) } // 0 = Chats, 1 = Groups, 2 = Notifications, 3 = DevOps Hub, 4 = Profile
    val unreadNotifications = notifications.filter { !it.isRead }.size

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = DarkSurface,
                tonalElevation = 8.dp,
                modifier = Modifier.border(width = 0.5.dp, color = DarkBorder, shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Chat, contentDescription = "Chats") },
                    label = { Text("Đoạn chat", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AccentCyan,
                        selectedTextColor = AccentCyan,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary,
                        indicatorColor = DarkSurfaceVariant
                    )
                )

                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.GroupAdd, contentDescription = "Groups") },
                    label = { Text("Tạo nhóm", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AccentPurple,
                        selectedTextColor = AccentPurple,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary,
                        indicatorColor = DarkSurfaceVariant
                    )
                )

                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (unreadNotifications > 0) {
                                    Badge(
                                        containerColor = ErrorRadical,
                                        contentColor = Color.White
                                    ) {
                                        Text(unreadNotifications.toString())
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                        }
                    },
                    label = { Text("Thông báo", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AccentCyan,
                        selectedTextColor = AccentCyan,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary,
                        indicatorColor = DarkSurfaceVariant
                    )
                )

                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.Terminal, contentDescription = "DevOps Hub") },
                    label = { Text("DevOps Hub", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AccentCyan,
                        selectedTextColor = AccentCyan,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary,
                        indicatorColor = DarkSurfaceVariant
                    )
                )

                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Cá nhân", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AccentPurple,
                        selectedTextColor = AccentPurple,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary,
                        indicatorColor = DarkSurfaceVariant
                    )
                )
            }
        },
        containerColor = DarkBackground,
        modifier = modifier
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
                },
                label = "home_tabs"
            ) { targetTab ->
                when (targetTab) {
                    0 -> ChatsTab(
                        conversations = conversations,
                        allUsers = allUsers,
                        currentUser = currentUser,
                        onConversationSelected = onConversationSelected,
                        stories = stories,
                        onPostStory = onPostStory,
                        onReplyToStory = onReplyToStory
                    )
                    1 -> CreateGroupTab(
                        allUsers = allUsers.filter { it.id != currentUser.id },
                        onCreateGroup = { name, avatarUrl, users ->
                            onCreateGroup(name, avatarUrl, users)
                            selectedTab = 0 // return to chats
                        }
                    )
                    2 -> NotificationsTab(
                        notifications = notifications,
                        onMarkNotifRead = onMarkNotifRead,
                        onClearNotif = onClearNotif
                    )
                    3 -> DevOpsHubScreen()
                    4 -> ProfileTab(
                        currentUser = currentUser,
                        isBiometricEnabled = isBiometricEnabled,
                        onToggleBiometric = onToggleBiometric,
                        encryptedPassword = encryptedPassword,
                        onLogout = onLogout
                    )
                }
            }
        }
    }
}

// ---------------- SUB TAB VIEWS ----------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatsTab(
    conversations: List<ConversationEntity>,
    allUsers: List<UserEntity>,
    currentUser: UserEntity,
    onConversationSelected: (ConversationEntity) -> Unit,
    stories: List<com.example.viewmodel.StoryModel>,
    onPostStory: (String, String, String) -> Unit,
    onReplyToStory: (String, String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredConversations = conversations.filter {
        it.title.contains(searchQuery, ignoreCase = true) ||
                it.lastMessageText.contains(searchQuery, ignoreCase = true)
    }

    val onlineUsers = allUsers.filter { it.isOnline && it.id != "user_me" }
    
    val initials = remember(currentUser.displayName) {
        currentUser.displayName.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString("").uppercase()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp, start = 16.dp, end = 16.dp)
    ) {
        // App Title Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(AccentCyan, AccentIndigo)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF003062),
                        fontSize = 15.sp
                    )
                }
                Text(
                    text = "Chats",
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = Color.White
                )
            }
            
            // Header Settings icon of the immersive design
            IconButton(
                onClick = {},
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(DarkSurface)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Search Settings",
                    tint = TextPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Search text field
        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search", color = TextMuted, fontSize = 14.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = TextSecondary) },
            singleLine = true,
            shape = CircleShape,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = DarkSurface,
                unfocusedContainerColor = DarkSurface,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            modifier = Modifier
                .fillMaxWidth()
                .border(0.5.dp, DarkBorder, CircleShape)
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Active Online Users Slider (with customized Your Story layout)
        var showingStoryCreator by remember { mutableStateOf(false) }
        var viewerStoryStartIndex by remember { mutableStateOf<Int?>(null) }

        if (showingStoryCreator) {
            com.example.ui.components.StoryCreatorDialog(
                onDismiss = { showingStoryCreator = false },
                onPublish = onPostStory
            )
        }

        if (viewerStoryStartIndex != null) {
            com.example.ui.components.StoryViewerDialog(
                stories = stories,
                startIndex = viewerStoryStartIndex!!,
                onDismiss = { viewerStoryStartIndex = null },
                onReplyToStory = onReplyToStory
            )
        }

        Text(
            text = "Đang hoạt động (${onlineUsers.size})",
            color = AccentCyan,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Immersive dash styled "Your Story" circle container
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val myStories = stories.filter { it.userId == currentUser.id }
                    val myStoryIndex = if (myStories.isNotEmpty()) stories.indexOf(myStories.first()) else -1

                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .then(
                                if (myStoryIndex >= 0) {
                                    Modifier.border(width = 2.5.dp, brush = Brush.linearGradient(colors = listOf(AccentCyan, AccentPurple)), shape = CircleShape)
                                } else {
                                    Modifier.border(width = 1.5.dp, color = DarkBorder, shape = CircleShape)
                                }
                            )
                            .padding(if (myStoryIndex >= 0) 3.dp else 0.dp)
                            .clip(CircleShape)
                            .background(DarkSurface)
                            .clickable {
                                if (myStoryIndex >= 0) {
                                    viewerStoryStartIndex = myStoryIndex
                                } else {
                                    showingStoryCreator = true
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (myStoryIndex >= 0) {
                            UserAvatar(
                                avatarUrl = currentUser.avatarUrl,
                                size = 48.dp,
                                isOnline = false,
                                showStatus = false
                            )
                        } else {
                            Text(
                                "+",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentCyan
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tin của bạn",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1
                    )
                }
            }

            items(onlineUsers) { user ->
                val userStoryIndex = stories.indexOfFirst { it.userId == user.id }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable {
                        if (userStoryIndex >= 0) {
                            viewerStoryStartIndex = userStoryIndex
                        } else {
                            val existingChat = conversations.find { !it.isGroup && it.title == user.displayName }
                            if (existingChat != null) {
                                onConversationSelected(existingChat)
                            }
                        }
                    }
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .then(
                                if (userStoryIndex >= 0) {
                                    Modifier.border(width = 2.5.dp, brush = Brush.linearGradient(colors = listOf(AccentCyan, AccentPurple)), shape = CircleShape)
                                } else {
                                    Modifier.border(width = 0.dp, color = Color.Transparent, shape = CircleShape)
                                }
                            )
                            .padding(if (userStoryIndex >= 0) 3.dp else 0.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        UserAvatar(
                            avatarUrl = user.avatarUrl,
                            size = if (userStoryIndex >= 0) 48.dp else 54.dp,
                            isOnline = true,
                            showStatus = userStoryIndex < 0
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = user.displayName.split(" ").last(),
                        fontSize = 11.sp,
                        color = TextPrimary,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(18.dp))

        // Conversation items
        Text(
            text = "Hộp thư đến",
            color = TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
        
        Spacer(modifier = Modifier.height(8.dp))

        if (filteredConversations.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = "Empty",
                        tint = TextMuted,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Không tìm thấy đoạn chat nào",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(filteredConversations) { conv ->
                    val resolvedAvatar = if (conv.isGroup) {
                        conv.avatarUrl ?: "https://images.unsplash.com/photo-1582213782179-e0d53f98f2ca?auto=format&fit=crop&w=150&q=80"
                    } else {
                        allUsers.find { it.displayName == conv.title }?.avatarUrl ?: ""
                    }
                    val isUserOnline = allUsers.find { it.displayName == conv.title }?.isOnline ?: false

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onConversationSelected(conv) }
                            .padding(vertical = 10.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        UserAvatar(
                            avatarUrl = resolvedAvatar,
                            size = 52.dp,
                            isOnline = isUserOnline,
                            showStatus = !conv.isGroup
                        )

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = conv.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            
                            val lastMsgPreview = if (conv.lastMessageText.length > 35) conv.lastMessageText.take(32) + "..." else conv.lastMessageText
                            Text(
                                text = lastMsgPreview,
                                color = if (conv.lastMessageText.contains("đã gửi") || conv.lastMessageText.contains("họp")) TextPrimary else TextSecondary,
                                fontWeight = if (conv.lastMessageText.contains("đã gửi") || conv.lastMessageText.contains("họp")) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Time components & Pinned Indicator count badge
                        val format = SimpleDateFormat("HH:mm", Locale.getDefault())
                        val timeString = format.format(Date(conv.lastMessageTime))
                        Column(
                            horizontalAlignment = Alignment.End,
                            modifier = Modifier.padding(start = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = timeString,
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (conv.isGroup) {
                                    Box(
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clip(CircleShape)
                                            .background(AccentCyan),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            "3",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF003062)
                                        )
                                    }
                                } else {
                                    Icon(
                                        Icons.Default.DoneAll,
                                        contentDescription = "Seen",
                                        tint = AccentCyan,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CreateGroupTab(
    allUsers: List<UserEntity>,
    onCreateGroup: (String, String, List<UserEntity>) -> Unit
) {
    var groupName by remember { mutableStateOf("") }
    val avatarPresets = listOf(
        "https://images.unsplash.com/photo-1582213782179-e0d53f98f2ca?auto=format&fit=crop&w=150&q=80" to "Work",
        "https://images.unsplash.com/photo-1549692520-acc6669e2f0c?auto=format&fit=crop&w=150&q=80" to "Dev",
        "https://images.unsplash.com/photo-1517486808906-6ca8b3f04846?auto=format&fit=crop&w=150&q=80" to "Social",
        "https://images.unsplash.com/photo-1552664730-d307ca884978?auto=format&fit=crop&w=150&q=80" to "Creative"
    )
    var selectedAvatarUrl by remember { mutableStateOf(avatarPresets.first().first) }
    val selectedMembers = remember { mutableStateListOf<UserEntity>() }
    var errorMsg by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Tạo phòng chat nhóm",
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            color = Color.White
        )

        OutlinedTextField(
            value = groupName,
            onValueChange = { groupName = it },
            label = { Text("Tên cuộc thảo luận nhóm") },
            placeholder = { Text("e.g. Sắp bàn giao dự án 🚀") },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedBorderColor = AccentPurple,
                unfocusedBorderColor = DarkBorder,
                focusedLabelColor = AccentPurple
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = "Ảnh đại diện nhóm",
            fontSize = 12.sp,
            color = TextSecondary,
            fontWeight = FontWeight.Bold
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            avatarPresets.forEach { (url, label) ->
                val isSelected = selectedAvatarUrl == url
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .border(
                            width = if (isSelected) 3.dp else 1.dp,
                            color = if (isSelected) AccentPurple else DarkBorder,
                            shape = CircleShape
                        )
                        .clickable { selectedAvatarUrl = url }
                ) {
                    UserAvatar(
                        avatarUrl = url,
                        size = 54.dp,
                        isOnline = false,
                        showStatus = false
                    )
                }
            }
        }

        Text(
            text = "Thêm thành viên vào phòng (${selectedMembers.size})",
            fontSize = 12.sp,
            color = TextSecondary,
            fontWeight = FontWeight.Bold
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(allUsers) { user ->
                val isSelected = selectedMembers.contains(user)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            if (isSelected) selectedMembers.remove(user) else selectedMembers.add(user)
                        }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    UserAvatar(
                        avatarUrl = user.avatarUrl,
                        size = 44.dp,
                        isOnline = user.isOnline,
                        showStatus = true
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(user.displayName, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                        Text("@${user.username}", color = TextSecondary, fontSize = 11.sp)
                    }
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = {
                            if (isSelected) selectedMembers.remove(user) else selectedMembers.add(user)
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = AccentPurple,
                            uncheckedColor = DarkBorder
                        )
                    )
                }
            }
        }

        if (errorMsg.isNotEmpty()) {
            Text(errorMsg, color = ErrorRadical, fontSize = 12.sp)
        }

        Button(
            onClick = {
                if (groupName.isBlank()) {
                    errorMsg = "Vui lòng nhập tên nhóm!"
                } else if (selectedMembers.isEmpty()) {
                    errorMsg = "Chọn ít nhất 1 thành viên!"
                } else {
                    onCreateGroup(groupName, selectedAvatarUrl, selectedMembers.toList())
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(12.dp)),
            colors = ButtonDefaults.buttonColors(containerColor = AccentPurple)
        ) {
            Text("Tạo cuộc trò chuyện nhóm mới", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun NotificationsTab(
    notifications: List<NotificationEntity>,
    onMarkNotifRead: (String) -> Unit,
    onClearNotif: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Hệ thống thông báo",
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            color = Color.White,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (notifications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.CircleNotifications,
                        contentDescription = "Empty",
                        tint = TextMuted,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Không có thông báo mới nào", color = TextSecondary)
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(notifications) { notif ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = if (!notif.isRead) 1.dp else 0.dp,
                                brush = Brush.horizontalGradient(listOf(AccentCyan, AccentPurple)),
                                shape = RoundedCornerShape(14.dp)
                            ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (!notif.isRead) DarkSurfaceVariant else DarkSurface
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = notif.title,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary,
                                        fontSize = 14.sp
                                    )
                                    if (!notif.isRead) {
                                        Box(
                                            modifier = Modifier
                                                .padding(start = 8.dp)
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(AccentCyan)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = notif.content,
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(8.dp))

                            Row {
                                if (!notif.isRead) {
                                    IconButton(onClick = { onMarkNotifRead(notif.id) }) {
                                        Icon(Icons.Default.Done, contentDescription = "Read", tint = AccentCyan)
                                    }
                                }
                                IconButton(onClick = { onClearNotif(notif.id) }) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = ErrorRadical)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileTab(
    currentUser: UserEntity,
    isBiometricEnabled: Boolean,
    onToggleBiometric: (Boolean) -> Unit,
    encryptedPassword: String,
    onLogout: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val activity = context as? androidx.fragment.app.FragmentActivity
    
    var decryptedPasswordToShow by remember { mutableStateOf<String?>(null) }
    var showDecryptionResult by remember { mutableStateOf(false) }
    var securityExplanationMessage by remember { mutableStateOf("") }
    var showDialogAlert by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Thông tin cá nhân",
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            color = Color.White,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(2.dp))

        // Avatar with pulse frame
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(
                    Brush.sweepGradient(
                        colors = listOf(AccentCyan, AccentPurple, AccentCyan)
                    )
                )
                .padding(3.dp),
            contentAlignment = Alignment.Center
        ) {
            UserAvatar(
                avatarUrl = currentUser.avatarUrl,
                size = 94.dp,
                isOnline = true,
                showStatus = false
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = currentUser.displayName,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.White
            )
            Text(
                text = "@${currentUser.username}",
                fontSize = 13.sp,
                color = AccentCyan
            )
        }

        // Basic Info Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(0.5.dp, DarkBorder, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Info components
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Trạng thái", color = TextSecondary, fontSize = 12.sp)
                    Text("Đang trực tuyến 🟢", color = StatusOnline, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Divider(color = DarkBorder, thickness = 0.5.dp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Hệ thống bảo mật", color = TextSecondary, fontSize = 12.sp)
                    Text("Android Keystore API", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
        }

        // Advanced Security Controls Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(0.5.dp, DarkBorder, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Security",
                        tint = AccentCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Giải pháp bảo mật & Mã hóa",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Divider(color = DarkBorder, thickness = 0.5.dp)

                // 1. Biometric App Lock Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Khóa ứng dụng (Biometric)", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text("Mở bằng cảm biến vân tay/khuôn mặt", color = TextSecondary, fontSize = 11.sp)
                    }
                    Switch(
                        checked = isBiometricEnabled,
                        onCheckedChange = { checked ->
                            if (activity != null) {
                                val status = BiometricHelper.isBiometricAvailable(activity)
                                if (status == BiometricHelper.BiometricStatus.AVAILABLE) {
                                    BiometricHelper.showBiometricPrompt(
                                        activity = activity,
                                        title = "Xác nhận cấu hình",
                                        subtitle = if (checked) "Quét vân tay để bật khóa ứng dụng" else "Quét vân tay để hủy khóa ứng dụng",
                                        onSuccess = {
                                            onToggleBiometric(checked)
                                            android.widget.Toast.makeText(context, "Thay đổi cấu hình thành công!", android.widget.Toast.LENGTH_SHORT).show()
                                        },
                                        onError = { code, err ->
                                            android.widget.Toast.makeText(context, "Lỗi: $err", android.widget.Toast.LENGTH_SHORT).show()
                                        },
                                        onFailed = {
                                            android.widget.Toast.makeText(context, "Xác minh thất bại!", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                } else {
                                    val readableReason = when (status) {
                                        BiometricHelper.BiometricStatus.NO_HARDWARE -> "Thiết bị này không hỗ trợ phần cứng sinh trắc học."
                                        BiometricHelper.BiometricStatus.UNAVAILABLE -> "Cảm biến sinh trắc học hiện không khả dụng."
                                        BiometricHelper.BiometricStatus.NOT_ENROLLED -> "Chưa đăng ký vân tay/khuôn mặt trên thiết bị này. Hãy thêm trong Cài đặt Android."
                                        else -> "Xác thực sinh trắc học không được hỗ trợ."
                                    }
                                    securityExplanationMessage = readableReason
                                    showDialogAlert = true
                                }
                            } else {
                                android.widget.Toast.makeText(context, "Không tìm thấy FragmentActivity!", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = AccentCyan,
                            checkedTrackColor = AccentCyan.copy(alpha = 0.5f),
                            uncheckedThumbColor = TextSecondary,
                            uncheckedTrackColor = DarkBorder
                        )
                    )
                }

                Divider(color = DarkBorder, thickness = 0.5.dp)

                // 2. Hardware Keystore encrypted token
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("Bản ghi Keystore (AES-256 GCM)", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Text("Mật khẩu đã được mã hóa ẩn trong Hardware Enclave:", color = TextSecondary, fontSize = 11.sp)
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarkBackground, RoundedCornerShape(8.dp))
                            .border(0.5.dp, DarkBorder, RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = if (encryptedPassword.isEmpty()) "Trống (Chưa có mật khẩu)" else encryptedPassword,
                            color = AccentPurple,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            fontSize = 10.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = {
                            if (encryptedPassword.isEmpty()) {
                                android.widget.Toast.makeText(context, "Không có dữ liệu mật khẩu để giải mã!", android.widget.Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (activity != null) {
                                val status = BiometricHelper.isBiometricAvailable(activity)
                                if (status == BiometricHelper.BiometricStatus.AVAILABLE) {
                                    BiometricHelper.showBiometricPrompt(
                                        activity = activity,
                                        title = "Yêu cầu giải mã",
                                        subtitle = "Xác thực vân tay để giải mã mật khẩu từ Keystore",
                                        onSuccess = {
                                            val decrypted = CryptoManager.decryptText(encryptedPassword)
                                            decryptedPasswordToShow = decrypted
                                            showDecryptionResult = true
                                        },
                                        onError = { code, err ->
                                            android.widget.Toast.makeText(context, "Lỗi giải mã: $err", android.widget.Toast.LENGTH_SHORT).show()
                                        },
                                        onFailed = {
                                            android.widget.Toast.makeText(context, "Hủy giải mã!", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                } else {
                                    // Bypass check for testing if biometric is not supported by hardware/emulator
                                    val decrypted = CryptoManager.decryptText(encryptedPassword)
                                    decryptedPasswordToShow = decrypted
                                    showDecryptionResult = true
                                }
                            } else {
                                val decrypted = CryptoManager.decryptText(encryptedPassword)
                                decryptedPasswordToShow = decrypted
                                showDecryptionResult = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentPurple.copy(alpha = 0.15f), contentColor = AccentPurple),
                        modifier = Modifier.fillMaxWidth().height(36.dp),
                        contentPadding = PaddingValues()
                    ) {
                        Text("Giải mã mật khẩu bằng Keystore Key 🔓", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(12.dp)),
            colors = ButtonDefaults.buttonColors(containerColor = ErrorRadical)
        ) {
            Icon(Icons.Default.Logout, contentDescription = "Logout")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Đăng xuất tài khoản", fontWeight = FontWeight.Bold)
        }
    }

    if (showDecryptionResult && decryptedPasswordToShow != null) {
        AlertDialog(
            onDismissRequest = { 
                showDecryptionResult = false
                decryptedPasswordToShow = null
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LockOpen, contentDescription = "Unlock", tint = AccentCyan, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Giải mã Keystore Thành Công", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Toàn bộ thông tin nhạy cảm đã giải mã như sau:", color = TextSecondary, fontSize = 13.sp)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarkBackground, RoundedCornerShape(8.dp))
                            .border(0.5.dp, DarkBorder, RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = decryptedPasswordToShow ?: "",
                            color = AccentCyan,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                    }
                    Text("Dữ liệu được bảo mật bằng thuật toán AES/GCM/NoPadding bảo vệ phần cứng.", color = TextMuted, fontSize = 11.sp)
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { 
                        showDecryptionResult = false
                        decryptedPasswordToShow = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = AccentCyan)
                ) {
                    Text("Đóng bảo mật", fontWeight = FontWeight.Bold)
                }
            },
            containerColor = DarkSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showDialogAlert) {
        AlertDialog(
            onDismissRequest = { showDialogAlert = false },
            title = { Text("Thông báo cảm cảm biến", fontWeight = FontWeight.Bold, color = TextPrimary) },
            text = { Text(securityExplanationMessage, color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = { showDialogAlert = false }, colors = ButtonDefaults.textButtonColors(contentColor = AccentPurple)) {
                    Text("Đồng ý", fontWeight = FontWeight.Bold)
                }
            },
            containerColor = DarkSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}
