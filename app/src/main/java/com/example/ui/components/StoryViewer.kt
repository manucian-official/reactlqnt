package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.viewmodel.StoryModel
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun StoryCreatorDialog(
    onDismiss: () -> Unit,
    onPublish: (textContent: String, mediaUrl: String, bgType: String) -> Unit
) {
    var textContent by remember { mutableStateOf("") }
    var selectedBg by remember { mutableStateOf("GRADIENT_SPACE") }
    var selectedMediaUrl by remember { mutableStateOf("") }

    val bgOptions = listOf(
        Triple("GRADIENT_SPACE", "Vũ Trụ", Brush.linearGradient(colors = listOf(Color(0xFF0F172A), Color(0xFF1E1B4B)))),
        Triple("GRADIENT_CYBER", "Cyberpunk", Brush.linearGradient(colors = listOf(Color(0xFF1E1B4B), Color(0xFF4C1D95)))),
        Triple("GRADIENT_SUNSET", "Hoàng Hôn", Brush.linearGradient(colors = listOf(Color(0xFF881337), Color(0xFF9A3412)))),
        Triple("GRADIENT_EMERALD", "Ngọc Bích", Brush.linearGradient(colors = listOf(Color(0xFF022C22), Color(0xFF064E3B))))
    )

    // Unsplash preset illustrations
    val imagePresets = listOf(
        "https://images.unsplash.com/photo-1579546929518-9e396f3cc809?auto=format&fit=crop&w=400&q=80",
        "https://images.unsplash.com/photo-1534447677768-be436bb09401?auto=format&fit=crop&w=400&q=80",
        "https://images.unsplash.com/photo-1507238691740-187a5b1d37b8?auto=format&fit=crop&w=400&q=80"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground),
            color = DarkBackground
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Đóng", tint = Color.White)
                    }
                    Text(
                        text = "Tạo tin mới",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                    Button(
                        onClick = {
                            onPublish(textContent, selectedMediaUrl, selectedBg)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Chia sẻ", color = Color(0xFF003062), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                // Story Canvas Preview
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .border(1.dp, DarkBorder.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                        .then(
                            if (selectedMediaUrl.isEmpty()) {
                                Modifier.background(
                                    bgOptions.find { it.first == selectedBg }?.third ?: bgOptions[0].third
                                )
                            } else {
                                Modifier.background(Color.Black)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedMediaUrl.isNotEmpty()) {
                        AsyncImage(
                            model = selectedMediaUrl,
                            contentDescription = "Selected media",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.3f))
                        )
                    }

                    // Content text
                    OutlinedTextField(
                        value = textContent,
                        onValueChange = { if (it.length <= 120) textContent = it },
                        placeholder = { 
                            Text(
                                "Bắt đầu nhập tin nhắn của bạn ở đây...", 
                                color = Color.White.copy(alpha = 0.6f), 
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) 
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        textStyle = LocalTextStyle.current.copy(
                            fontSize = 20.sp, 
                            fontWeight = FontWeight.Bold, 
                            color = Color.White,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    )
                }

                // Stylized Customizations
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Background templates
                    Text("Mẫu phông nền màu sắc:", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        bgOptions.forEach { opt ->
                            val isChosen = selectedBg == opt.first && selectedMediaUrl.isEmpty()
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(opt.third)
                                    .border(2.dp, if (isChosen) AccentCyan else Color.Transparent, CircleShape)
                                    .clickable {
                                        selectedBg = opt.first
                                        selectedMediaUrl = ""
                                    }
                            )
                        }
                    }

                    // Preset images
                    Text("Hoặc chèn hình ảnh minh họa:", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        imagePresets.forEach { url ->
                            val isChosen = selectedMediaUrl == url
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(2.dp, if (isChosen) AccentCyan else Color.Transparent, RoundedCornerShape(10.dp))
                                    .clickable { selectedMediaUrl = url }
                            ) {
                                AsyncImage(
                                    model = url,
                                    contentDescription = "preset",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StoryViewerDialog(
    stories: List<StoryModel>,
    startIndex: Int,
    onDismiss: () -> Unit,
    onReplyToStory: (recipientName: String, replyText: String) -> Unit
) {
    var currentIndex by remember { mutableStateOf(startIndex) }
    if (stories.isEmpty() || currentIndex < 0 || currentIndex >= stories.size) {
        onDismiss()
        return
    }

    val story = stories[currentIndex]
    val context = LocalContext.current
    var replyText by remember { mutableStateOf("") }
    
    // Automatically fill Progress Bar animation (runs for 5 seconds)
    var progress by remember { mutableStateOf(0f) }
    val coroutineContext = rememberCoroutineScope()

    // Key to restart timer upon swipe/tap
    LaunchedEffect(currentIndex) {
        progress = 0f
        val tickMs = 50L
        val totalMs = 5000L
        val steps = totalMs / tickMs
        
        for (i in 1..steps) {
            delay(tickMs)
            progress = i.toFloat() / steps
        }
        
        // Auto-advance
        if (currentIndex < stories.size - 1) {
            currentIndex += 1
        } else {
            onDismiss()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            color = Color.Black
        ) {
            // Background Canvas based on Story templates
            val backgroundModifier = if (story.mediaUrl.isEmpty()) {
                val brush = when (story.bgType) {
                    "GRADIENT_SPACE" -> Brush.linearGradient(colors = listOf(Color(0xFF0F172A), Color(0xFF1E1B4B)))
                    "GRADIENT_CYBER" -> Brush.linearGradient(colors = listOf(Color(0xFF1E1B4B), Color(0xFF4C1D95)))
                    "GRADIENT_SUNSET" -> Brush.linearGradient(colors = listOf(Color(0xFF881337), Color(0xFF9A3412)))
                    "GRADIENT_EMERALD" -> Brush.linearGradient(colors = listOf(Color(0xFF022C22), Color(0xFF064E3B)))
                    else -> Brush.linearGradient(colors = listOf(Color(0xFF000000), Color(0xFF1E1B4B)))
                }
                Modifier.background(brush)
            } else {
                Modifier.background(Color.Black)
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(backgroundModifier)
            ) {
                // If contains image media, load it
                if (story.mediaUrl.isNotEmpty()) {
                    AsyncImage(
                        model = story.mediaUrl,
                        contentDescription = "Story Media background",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.35f))
                    )
                }

                // Interactive left (back) / right (forward) click sensors
                Row(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) {
                                if (currentIndex > 0) {
                                    currentIndex -= 1
                                } else {
                                    Toast.makeText(context, "Đây đã là tin đầu tiên", Toast.LENGTH_SHORT).show()
                                }
                            }
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) {
                                if (currentIndex < stories.size - 1) {
                                    currentIndex += 1
                                } else {
                                    onDismiss()
                                }
                            }
                    )
                }

                // Foreground components elements
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    
                    // TOP BAR: Stories Progress Indicator + Profile info
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = false) {} // block click intercepts
                    ) {
                        // Horizontal bar index
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .padding(horizontal = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            stories.forEachIndexed { idx, _ ->
                                val barProgress = when {
                                    idx < currentIndex -> 1f
                                    idx > currentIndex -> 0f
                                    else -> progress
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.3f))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .fillMaxWidth(barProgress)
                                            .clip(CircleShape)
                                            .background(AccentCyan)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Sender layout row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                AsyncImage(
                                    model = story.userAvatar,
                                    contentDescription = "avatar",
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .border(1.5.dp, AccentCyan, CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                                Column {
                                    Text(
                                        text = story.userName,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "vừa xong",
                                        fontSize = 10.sp,
                                        color = Color.White.copy(alpha = 0.6f)
                                    )
                                }
                            }

                            IconButton(onClick = onDismiss) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                            }
                        }
                    }

                    // CENTER BODY TEXT CONTENT
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = story.textContent,
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // BOTTOM ACTION BAR: Messenger Quick Reactions & Text Box reply
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Quick Reactions
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            listOf("❤️", "😂", "😮", "😢", "🔥", "👍").forEach { emoji ->
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.12f))
                                        .clickable {
                                            onReplyToStory(story.userName, emoji)
                                            Toast.makeText(context, "Đã gửi phản hồi $emoji đến ${story.userName}!", Toast.LENGTH_SHORT).show()
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(emoji, fontSize = 20.sp)
                                }
                            }
                        }

                        // Send message input
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TextField(
                                value = replyText,
                                onValueChange = { replyText = it },
                                placeholder = { Text("Gửi tin nhắn phản hồi...", color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp) },
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.White.copy(alpha = 0.15f),
                                    unfocusedContainerColor = Color.White.copy(alpha = 0.1f),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                ),
                                shape = CircleShape,
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(max = 50.dp)
                            )

                            val isFilled = replyText.isNotBlank()
                            IconButton(
                                onClick = {
                                    if (isFilled) {
                                        onReplyToStory(story.userName, replyText)
                                        Toast.makeText(context, "Đã gửi phản hồi đến ${story.userName}!", Toast.LENGTH_SHORT).show()
                                        replyText = ""
                                    }
                                },
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(if (isFilled) AccentCyan else Color.White.copy(alpha = 0.1f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = "Gửi",
                                    tint = if (isFilled) Color(0xFF003062) else Color.White.copy(alpha = 0.4f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
