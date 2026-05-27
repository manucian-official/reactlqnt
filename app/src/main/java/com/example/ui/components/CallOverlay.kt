package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.viewmodel.CallState
import com.example.viewmodel.CallStatus
import com.example.viewmodel.CallType
import com.example.ui.theme.*

@Composable
fun CallOverlay(
    callState: CallState,
    onMuteToggle: () -> Unit,
    onCameraToggle: () -> Unit,
    onSpeakerToggle: () -> Unit,
    onHangUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (callState.type == CallType.NONE) return

    val isVideo = callState.type == CallType.VIDEO
    val isRinging = callState.status == CallStatus.RINGING

    // Animate background color transition depending on connection status
    val backgroundColor by animateColorAsState(
        targetValue = if (isRinging) Color(0xFF0F172A) else Color(0xFF020617),
        animationSpec = tween(1000)
    )

    // Pulsing circle scale animation for Ringing
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale1 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse1"
    )
    val pulseScale2 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 2.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing, delayMillis = 500),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse2"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {

        // --- BACKGROUND SIMULATED VIDEO STREAM ---
        if (isVideo && !isRinging && !callState.isCameraOff) {
            // Simulated feed of user or participant
            AsyncImage(
                model = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=800&q=80",
                contentDescription = "Simulated Video Stream",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            // Darkness overlay to see actions better
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
            )
        }

        // Main info column
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            // Top Header: Video vs Voice layout
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 40.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = AccentIndigo.copy(alpha = 0.8f),
                    modifier = Modifier.border(0.5.dp, AccentCyan, RoundedCornerShape(24.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = if (isVideo) Icons.Default.Videocam else Icons.Default.Call,
                            contentDescription = "Call Mode",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = if (isVideo) "CUỘC GỌI VIDEO" else "CUỘC GỌI THOẠI",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Ringing Pulse animations vs Connection details
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(200.dp)
                ) {
                    if (isRinging) {
                        // Pulse circle 1
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .background(AccentCyan.copy(alpha = 0.25f * (2.2f - pulseScale1)))
                                .border(1.dp, AccentCyan.copy(alpha = 0.5f * (2.2f - pulseScale1)), CircleShape)
                                .align(Alignment.Center)
                                .fillMaxSize(pulseScale1)
                        )
                        // Pulse circle 2
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .background(AccentPurple.copy(alpha = 0.2f * (2.2f - pulseScale2)))
                                .border(1.dp, AccentPurple.copy(alpha = 0.4f * (2.2f - pulseScale2)), CircleShape)
                                .align(Alignment.Center)
                                .fillMaxSize(pulseScale2)
                        )
                    }

                    // Central Avatar Circle representation
                    AsyncImage(
                        model = callState.participantAvatar,
                        contentDescription = "Participant Avatar",
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape)
                            .border(2.dp, AccentCyan, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Name & Connection State Labels
                Text(
                    text = callState.participantName,
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (isRinging) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CircularProgressIndicator(
                            color = AccentCyan,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "Đang đổ chuông...",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    val minutes = callState.duration / 60
                    val seconds = callState.duration % 60
                    val formattedDuration = String.format("%02d:%02d", minutes, seconds)
                    Text(
                        text = "Đang kết nối • $formattedDuration",
                        color = StatusOnline,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
            }

            // --- BOTTOM AREA: CONTROL WHEEL PANELS ---
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(30.dp),
                modifier = Modifier.padding(bottom = 20.dp)
            ) {

                // Call features panel
                Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Mute Audio trigger
                    IconButtonWithLabel(
                        imageVector = if (callState.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                        label = "Mắt mic",
                        isActive = callState.isMuted,
                        onClick = onMuteToggle,
                        activeColor = ErrorRadical
                    )

                    if (isVideo) {
                        // Camera toggle switch
                        IconButtonWithLabel(
                            imageVector = if (callState.isCameraOff) Icons.Default.VideocamOff else Icons.Default.Videocam,
                            label = "Tắt Cam",
                            isActive = callState.isCameraOff,
                            onClick = onCameraToggle,
                            activeColor = ErrorRadical
                        )
                    }

                    // Speakerphone trigger
                    IconButtonWithLabel(
                        imageVector = if (callState.isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                        label = "Loa ngoài",
                        isActive = callState.isSpeakerOn,
                        onClick = onSpeakerToggle,
                        activeColor = AccentCyan
                    )
                }

                // Call hang up trigger
                IconButton(
                    onClick = onHangUp,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(ErrorRadical)
                        .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape),
                    colors = IconButtonDefaults.iconButtonColors(containerColor = ErrorRadical)
                ) {
                    Icon(
                        imageVector = Icons.Default.CallEnd,
                        contentDescription = "Hang up",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun IconButtonWithLabel(
    imageVector: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    activeColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        val buttonBackground = if (isActive) activeColor else Color.White.copy(alpha = 0.15f)
        val iconColor = if (isActive) Color.White else TextPrimary

        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(buttonBackground)
                .border(width = 0.5.dp, color = Color.White.copy(alpha = 0.1f), shape = CircleShape)
        ) {
            Icon(
                imageVector = imageVector,
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
        }
        Text(
            text = label,
            fontSize = 11.sp,
            color = TextSecondary,
            fontWeight = FontWeight.Medium
        )
    }
}
