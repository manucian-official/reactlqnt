package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Custom entity for live logs in simulator
data class ConsoleLog(
    val timestamp: String,
    val service: String,
    val type: String, // INFO, WARN, ERROR, SUCCESS
    val message: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevOpsHubScreen(
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    
    var currentSubTab by remember { mutableStateOf(0) } // 0 = Architecture, 1 = Backend, 2 = Database, 3 = DevOps/Docker, 4 = Live Console Simulator
    
    // Live simulator state managers
    var isSimulating by remember { mutableStateOf(true) }
    var userCountSimulated by remember { mutableStateOf(418) }
    var responseSpeedSimulated by remember { mutableStateOf(8) } // 8ms
    val simulatedLogs = remember { mutableStateListOf<ConsoleLog>() }
    var securityChecksPassed by remember { mutableStateOf(true) }
    var activeRpsByTime by remember { mutableStateOf(28) }

    // Seed initial logs to look highly premium and realistic on loading
    LaunchedEffect(Unit) {
        val sdf = SimpleDateFormat("HH:mm:ss.S", Locale.getDefault())
        simulatedLogs.add(ConsoleLog(sdf.format(Date()), "GATEWAY", "INFO", "Redis adapter listening on host:6379 for cross-pod socket propagation..."))
        simulatedLogs.add(ConsoleLog(sdf.format(Date()), "BACKEND", "SUCCESS", "NestJS microservice clustered dynamically using PM2 cluster mode (4 CPU cores)"))
        simulatedLogs.add(ConsoleLog(sdf.format(Date()), "DATABASE", "INFO", "Prisma Database connected successfully. Active pool connections: 18/20"))
        simulatedLogs.add(ConsoleLog(sdf.format(Date()), "NGINX", "SUCCESS", "Reverse proxy upstream active. Listening SSL on port :443 with TLS1.3 cipher suites"))
    }

    // Interactive simulator continuous logs thread
    LaunchedEffect(isSimulating) {
        if (!isSimulating) return@LaunchedEffect
        val sdf = SimpleDateFormat("HH:mm:ss.S", Locale.getDefault())
        while (isActive) {
            delay(1500 + (0..1500).random().toLong()) // periodic log cycles
            
            // Randomly pick a tech service log
            val servicePickerUser = listOf("GATEWAY", "BACKEND", "DATABASE", "NGINX", "BULLMQ", "WEBRTC").random()
            val logItem = when (servicePickerUser) {
                "GATEWAY" -> {
                    activeRpsByTime = (20..50).random()
                    val events = listOf(
                        "WS Handshake verified for user_client_${(100..999).random()}; JWT Token rotation successful.",
                        "Broadcasting message_on_channel room_id=group_tech to 4 connected socket gateways...",
                        "Client ping completed. Round-trip: ${responseSpeedSimulated}ms.",
                        "Typing event broadcast in progress to conversation_members key_sync:0.43s"
                    )
                    ConsoleLog(sdf.format(Date()), "GATEWAY", "INFO", events.random())
                }
                "BACKEND" -> {
                    val events = listOf(
                        "Resolved CASL authorization check: canReadMessage -> true for subject_user_me",
                        "Argon2 hashing verified for inbound login challenge, authorization grant set inside cookies",
                        "Audit log written: ACTION=AUTHENTICATION_LOGIN ip=192.168.1.189, geo=VN",
                        "Security header filter: XSS-Protection enabled; Frame-Options set to DENY."
                    )
                    ConsoleLog(sdf.format(Date()), "BACKEND", "SUCCESS", events.random())
                }
                "DATABASE" -> {
                    val events = listOf(
                        "Prisma executing lookup query SELECT FROM reactions WHERE messageId=msg_g4 [Time: 1.1ms; Indices: Composite_Idx_MsgUserEmoji]",
                        "DB connection health check payload resolved. DB status: OPTIMAL",
                        "Soft-delete Message CASCADE query triggered for message_id=${java.util.UUID.randomUUID().toString().take(6)}"
                    )
                    ConsoleLog(sdf.format(Date()), "DATABASE", "INFO", events.random())
                }
                "NGINX" -> {
                    val rps = (45..150).random()
                    val events = listOf(
                        "HTTP Rate Limit state check: SUCCESS for IP: client_${(1..10).random()}; limit bucket: 100/min",
                        "Nginx routing proxy_pass -> upstream NestGateway on internal_ip:3001",
                        "gzip compression compressed text/html stream by 67.4% (sent to client)"
                    )
                    ConsoleLog(sdf.format(Date()), "NGINX", "SUCCESS", events.random())
                }
                "BULLMQ" -> {
                    val events = listOf(
                        "Dequeued JobID_Attachment_${(1000..9999).random()} for background virus scan & S3 compression sync",
                        "Attachment scan complete: status=CLEAN (ClamAV verified), bucket=production_chat_assets_s3",
                        "Pushing ScheduledMessageJob to global Redis sorted set timeline queue"
                    )
                    ConsoleLog(sdf.format(Date()), "BULLMQ", "INFO", events.random())
                }
                else -> {
                    val events = listOf(
                        "WebRTC Signaling: SDPOffer matched from PeerA to PeerB. Generating STUN binding requests...",
                        "Signaling gateway binding: peer_call_uuid established successfully with ICE candidates verified",
                        "CoTURN Server log: Relay candidates requested for secure TURN relay voice channel stream"
                    )
                    ConsoleLog(sdf.format(Date()), "WEBRTC", "WARN", events.random())
                }
            }
            
            simulatedLogs.add(logItem)
            // Cap at 25 logs for mobile memory constraints
            if (simulatedLogs.size > 25) {
                simulatedLogs.removeAt(0)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Brush.linearGradient(colors = listOf(AccentCyan, AccentPurple))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Terminal,
                                contentDescription = "Terminal",
                                tint = Color(0xFF003062),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                "DevOps & Systems Architect",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                "Kiến trúc hệ thống phân tán và bảo mật",
                                fontSize = 10.sp,
                                color = AccentCyan
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface),
                modifier = Modifier.border(0.5.dp, DarkBorder)
            )
        },
        containerColor = DarkBackground,
        modifier = modifier
    ) { paddingVal ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingVal)
        ) {
            // Horizontal row selectors for tabs
            ScrollableSubTabs(
                selectedTab = currentSubTab,
                onTabSelected = { currentSubTab = it }
            )

            // Dynamic view based on tab selection
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                AnimatedContent(
                    targetState = currentSubTab,
                    transitionSpec = {
                        fadeIn() togetherWith fadeOut()
                    },
                    label = "devops_panels"
                ) { targetTab ->
                    when (targetTab) {
                        0 -> CorePlatformBlueprintView()
                        1 -> CodeViewer(
                            title = "NestJS WebSocket Gateway",
                            subtitle = "Thực thi kết nối thời gian thực, đồng bộ Redis, xác thực JWT, RBAC.",
                            code = NestJSSourceCode,
                            onCopy = { clipboardManager.setText(AnnotatedString(NestJSSourceCode)) }
                        )
                        2 -> CodeViewer(
                            title = "Prisma Schema (PostgreSQL DB)",
                            subtitle = "Full Schema quan hệ người dùng, tin nhắn, index tối ưu hóa lookups 100k+ TPS.",
                            code = PrismaSchemaCode,
                            onCopy = { clipboardManager.setText(AnnotatedString(PrismaSchemaCode)) }
                        )
                        3 -> Column {
                            ScrollableDockerTabs(
                                onCopyDockerCompose = { clipboardManager.setText(AnnotatedString(DockerComposeCode)) },
                                onCopyNginx = { clipboardManager.setText(AnnotatedString(NginxConfigCode)) },
                                onCopyCI = { clipboardManager.setText(AnnotatedString(GitHubActionsCode)) }
                            )
                        }
                        4 -> ConsoleSimulatorView(
                            isSimulating = isSimulating,
                            onSimulationToggled = { isSimulating = it },
                            userCount = userCountSimulated,
                            onUserCountChange = { userCountSimulated = it },
                            speed = responseSpeedSimulated,
                            onSpeedChange = { responseSpeedSimulated = it },
                            rps = activeRpsByTime,
                            securityChecks = securityChecksPassed,
                            onSecurityToggle = { securityChecksPassed = it },
                            logs = simulatedLogs,
                            onClearLogs = { simulatedLogs.clear() }
                        )
                    }
                }
            }
        }
    }
}

// ---------------- DESIGN COMPILATION SUBCOMPONENTS ----------------

@Composable
fun ScrollableSubTabs(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    val items = listOf("Kiến Trúc", "NestJS Gateway", "Database Schema", "Docker & Nginx", "Giả Lập Live")
    val icons = listOf(Icons.Default.Layers, Icons.Default.Code, Icons.Default.Dns, Icons.Default.Settings, Icons.Default.Terminal)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurface)
            .border(0.5.dp, DarkBorder)
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 8.dp, horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items.forEachIndexed { idx, label ->
            val isSelected = selectedTab == idx
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) DarkSurfaceVariant else Color.Transparent)
                    .border(
                        width = 0.5.dp,
                        color = if (isSelected) AccentCyan else Color.Transparent,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable { onTabSelected(idx) }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = icons[idx],
                    contentDescription = label,
                    tint = if (isSelected) AccentCyan else TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = label,
                    color = if (isSelected) TextPrimary else TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun CorePlatformBlueprintView() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.border(0.5.dp, DarkBorder, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Waves, contentDescription = "Blueprint", tint = AccentPurple)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Kiến Trúc Tổng Thể (Enterprise Architecture)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        "Hệ thống vận hành theo mô hình Clean Architecture kết hợp Domain-Driven Design (DDD) & CQRS. Đảm bảo khả năng chịu tải cao, mở rộng ngang dễ dàng với Redis Pub/Sub và an toàn tuyệt đối với cơ chế mã hóa Argon2 + 2FA TOTP + RBAC chặt chẽ.",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        item {
            Text(
                "Các Thành Phần Trọng Yếu Của Hệ Thống:",
                color = AccentCyan,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        val componentsInfo = listOf(
            Triple(
                "1. Socket.IO Cluster & Redis sync",
                "Mọi Socket Node kết nối chéo với nhau thông qua Redis Pub/Sub Adapter. Khi người dùng gửi tin nhắn trên Node 1, Redis lập tức propagate tín hiệu tới toàn bộ các Node khác, đảm bảo độ trễ nhắn tin < 15ms cho 10M+ kết nối đồng thời.",
                Icons.Default.Shuffle
            ),
            Triple(
                "2. DB Shielding & Optimization",
                "Prisma ORM điều phối kết nối PostgreSQL với pooling được bảo vệ bằng Nginx Reverse Proxy. Hệ thống tận hành Composite Index đa tầng trên (messageId, userId, emoji) và (conversationId, createdAt) để truy vấn tin nhắn cuộn mượt cực hạn.",
                Icons.Default.Security
            ),
            Triple(
                "3. BullMQ Worker Queue",
                "Giải phóng tải CPU của Main Rest API bằng hàng chờ asynchronous BullMQ. Các nghiệp vụ nặng như quét virus ClamAV cho attachment, nén ảnh, xử lý Scheduled Message hay đẩy FCM Push Notification đều chạy nền bảo vệ Node.",
                Icons.Default.HourglassEmpty
            ),
            Triple(
                "4. WebRTC Signaling & TURN Network",
                "Máy chủ tín hiệu tích hợp trung chuyển SDP handshakes trực tiếp vào Websocket Gateway. Kết hợp máy chủ TURN/STUN CoTURN tự động bypass Symmetric NAT giúp cuộc gọi thoại và video rõ nét.",
                Icons.Default.Call
            ),
            Triple(
                "5. Bảo Mật Hardened Security Layer",
                "Helmet API che giấu nhân hệ điều hành, chống SQL Injection tự động thông qua Prisma parameterized query. JWT Token xoay vòng (AccessToken 15m trong HTTP-only cookie, RefreshToken 7d lưu Redis mã hóa) mang lại bảo mật ngân hàng.",
                Icons.Default.Lock
            )
        )

        items(componentsInfo) { (title, desc, icon) ->
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(DarkSurfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = title, tint = AccentCyan, modifier = Modifier.size(18.dp))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(desc, color = TextSecondary, fontSize = 11.sp, lineHeight = 16.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun CodeViewer(
    title: String,
    subtitle: String,
    code: String,
    onCopy: () -> Unit
) {
    var copied by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                Text(subtitle, color = AccentCyan, fontSize = 11.sp)
            }
            Button(
                onClick = {
                    onCopy()
                    copied = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = if (copied) StatusOnline else AccentPurple),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.height(34.dp)
            ) {
                Icon(
                    imageVector = if (copied) Icons.Default.Check else Icons.Default.ContentCopy,
                    contentDescription = "Copy",
                    tint = if (copied) Color.Black else Color.White,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (copied) "Đã sao chép!" else "Copy code", fontSize = 11.sp, color = if (copied) Color.Black else Color.White)
            }
        }

        // Reset display code sync
        LaunchedEffect(copied) {
            if (copied) {
                delay(2000)
                copied = false
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF07090C))
                .border(0.5.dp, DarkBorder, RoundedCornerShape(12.dp))
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                item {
                    Text(
                        text = code,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFFC5CBD3),
                        fontSize = 10.5.sp,
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ScrollableDockerTabs(
    onCopyDockerCompose: () -> Unit,
    onCopyNginx: () -> Unit,
    onCopyCI: () -> Unit
) {
    var selectedSubTab by remember { mutableStateOf(0) } // 0 = docker-compose, 1 = nginx.conf, 2 = ci-cd.yml
    
    val codes = listOf(DockerComposeCode, NginxConfigCode, GitHubActionsCode)
    val copyActions = listOf(onCopyDockerCompose, onCopyNginx, onCopyCI)
    val names = listOf("docker-compose.yml", "nginx.conf", "github-actions.yml")
    val subtitles = listOf(
        "Khai báo cluster NestJS, Redis, PostgreSQL với healthchecks chặt chẽ.",
        "Cấu hình reverse proxy bảo mật cứng, tải ngược websocket, rate limiter.",
        "GitHub Actions Pipeline tự động verify test, build image và CD."
    )

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = selectedSubTab,
            containerColor = DarkSurface,
            contentColor = AccentPurple,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = selectedSubTab == 0,
                onClick = { selectedSubTab = 0 },
                text = { Text("Docker Compose", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedSubTab == 1,
                onClick = { selectedSubTab = 1 },
                text = { Text("Nginx Proxy", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedSubTab == 2,
                onClick = { selectedSubTab = 2 },
                text = { Text("CI/CD YAML", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            )
        }

        Box(modifier = Modifier.weight(1f)) {
            CodeViewer(
                title = names[selectedSubTab],
                subtitle = subtitles[selectedSubTab],
                code = codes[selectedSubTab],
                onCopy = copyActions[selectedSubTab]
            )
        }
    }
}

@Composable
fun ConsoleSimulatorView(
    isSimulating: Boolean,
    onSimulationToggled: (Boolean) -> Unit,
    userCount: Int,
    onUserCountChange: (Int) -> Unit,
    speed: Int,
    onSpeedChange: (Int) -> Unit,
    rps: Int,
    securityChecks: Boolean,
    onSecurityToggle: (Boolean) -> Unit,
    logs: List<ConsoleLog>,
    onClearLogs: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Quick interactive knobs card
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.border(0.5.dp, DarkBorder, RoundedCornerShape(14.dp))
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Trung Tâm Giả Lập Hệ Thống", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onSimulationToggled(!isSimulating) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSimulating) ErrorRadical else StatusOnline
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(28.dp),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Icon(
                                imageVector = if (isSimulating) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Sim",
                                tint = if (!isSimulating) Color.Black else Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isSimulating) "Dừng Giả Lập" else "Bắt Đầu", fontSize = 11.sp, color = if (!isSimulating) Color.Black else Color.White)
                        }
                        IconButton(onClick = onClearLogs, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Refresh, contentDescription = "Clear", tint = TextSecondary, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                Divider(color = DarkBorder, thickness = 0.5.dp)

                // Sim counters row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("ACTIVE SOCKET CONNS", fontSize = 9.sp, color = TextSecondary)
                        Text("${userCount} conns", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AccentCyan)
                    }
                    Column {
                        Text("AVG END-TO-END LATENCY", fontSize = 9.sp, color = TextSecondary)
                        Text("${speed} ms", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = StatusOnline)
                    }
                    Column {
                        Text("ACTIVE DB RPS", fontSize = 9.sp, color = TextSecondary)
                        Text("${rps} rps", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AccentPurple)
                    }
                }

                Divider(color = DarkBorder, thickness = 0.5.dp)

                // Adjustable controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { onUserCountChange(userCount + (50..250).random()) },
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("+ Tăng tải (Scale Connections)", fontSize = 10.sp, color = Color.White)
                    }

                    Button(
                        onClick = { onSecurityToggle(!securityChecks) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (securityChecks) StatusOnline.copy(alpha = 0.2f) else ErrorRadical.copy(alpha = 0.2f)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                            .border(0.5.dp, if (securityChecks) StatusOnline else ErrorRadical, RoundedCornerShape(8.dp)),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = if (securityChecks) "Security Shields: ACTIVE" else "Security Shields: SUSPENDED",
                            fontSize = 10.sp,
                            color = if (securityChecks) StatusOnline else ErrorRadical,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Terminal Console Output Log
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(Icons.Default.Terminal, contentDescription = "CLI", tint = StatusOnline, modifier = Modifier.size(16.dp))
            Text("Console Stream logs (PM2 / Kubernetes cluster sync):", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF040608))
                .border(0.5.dp, DarkBorder, RoundedCornerShape(12.dp))
                .padding(10.dp)
        ) {
            if (logs.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Đang chờ logs mới...", color = TextMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(logs) { log ->
                        val colorText = when (log.service) {
                            "GATEWAY" -> AccentCyan
                            "BACKEND" -> AccentPurple
                            "DATABASE" -> Color(0xFFE2E3FF)
                            "NGINX" -> StatusOnline
                            "BULLMQ" -> Color(0xFFFBC02D)
                            else -> ErrorRadical
                        }
                        
                        val indicatorSymbol = when (log.type) {
                            "SUCCESS" -> "✔️"
                            "WARN" -> "⚠️"
                            "ERROR" -> "❌"
                            else -> "ℹ️"
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "[${log.timestamp}] ",
                                color = TextMuted,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.width(68.dp)
                            )
                            Text(
                                text = "${log.service} ",
                                color = colorText,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.width(66.dp)
                            )
                            Text(
                                text = "$indicatorSymbol ${log.message}",
                                color = if (log.type == "ERROR") ErrorRadical else TextPrimary,
                                fontSize = 9.5.sp,
                                fontFamily = FontFamily.Monospace,
                                lineHeight = 13.sp,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ---------------- VERBATIM FULL CONFIG/SOURCE CODES ----------------

val PrismaSchemaCode = """datasource db {
  provider = "postgresql"
  url      = env("DATABASE_URL")
}

generator client {
  provider = "prisma-client-js"
}

model User {
  id                String               @id @default(uuid())
  email             String               @unique
  username          String               @unique
  passwordHash      String
  displayName       String
  avatarUrl         String?
  isOnline          Boolean              @default(false)
  lastSeen          DateTime             @default(now())
  createdAt         DateTime             @default(now())
  updatedAt         DateTime             @updatedAt
  twoFactorSecret   String?
  isTwoFactorActive Boolean              @default(false)

  sessions          Session[]
  devices           Device[]
  refreshTokens     RefreshToken[]
  memberships       ConversationMember[]
  messages          Message[]
  messageReads      MessageRead[]
  notifications     Notification[]
  friendshipsSource Friendship[]         @relation("SourceUser")
  friendshipsTarget Friendship[]         @relation("TargetUser")
  blocksSource      Block[]              @relation("SourceBlock")
  blocksTarget      Block[]              @relation("TargetBlock")
  reportsSource     Report[]             @relation("Reporter")
  reportsTarget     Report[]             @relation("Reported")
  auditLogs         AuditLog[]

  @@index([email])
  @@index([username])
}

model Session {
  id        String   @id @default(uuid())
  userId    String
  user      User     @relation(fields: [userId], references: [id], onDelete: Cascade)
  token     String   @unique
  ipAddress String
  createdAt DateTime @default(now())
}

model Device {
  id          String   @id @default(uuid())
  userId      String
  user        User     @relation(fields: [userId], references: [id], onDelete: Cascade)
  deviceToken String   @unique
  deviceType  String   // ios, android, desktop
  lastIp      String
  createdAt   DateTime @default(now())
}

model RefreshToken {
  id        String   @id @default(uuid())
  userId    String
  user      User     @relation(fields: [userId], references: [id], onDelete: Cascade)
  token     String   @unique
  expiresAt DateTime
  createdAt DateTime @default(now())
}

model Conversation {
  id        String               @id @default(uuid())
  title     String?
  isGroup   Boolean              @default(false)
  avatarUrl String?
  createdAt DateTime             @default(now())
  updatedAt DateTime             @updatedAt
  members   ConversationMember[]
  messages  Message[]
}

model ConversationMember {
  id             String       @id @default(uuid())
  conversationId String
  conversation   Conversation @relation(fields: [conversationId], references: [id], onDelete: Cascade)
  userId         String
  user           User         @relation(fields: [userId], references: [id], onDelete: Cascade)
  role           String       @default("MEMBER") // ADMIN, MEMBER
  createdAt      DateTime     @default(now())

  @@unique([conversationId, userId])
}

model Message {
  id             String        @id @default(uuid())
  conversationId String
  conversation   Conversation  @relation(fields: [conversationId], references: [id], onDelete: Cascade)
  senderId       String
  sender         User          @relation(fields: [senderId], references: [id], onDelete: Cascade)
  text           String
  isPinned       Boolean       @default(false)
  isEdited       Boolean       @default(false)
  isDeleted      Boolean       @default(false)
  replyToId      String?
  replyTo        Message?      @relation("Replies", fields: [replyToId], references: [id], onDelete: SetNull)
  replies        Message[]     @relation("Replies")
  createdAt      DateTime      @default(now())
  updatedAt      DateTime      @updatedAt
  attachments    Attachment[]
  reactions      Reaction[]
  reads          MessageRead[]

  @@index([conversationId, createdAt])
}

model MessageRead {
  id        String   @id @default(uuid())
  messageId String
  message   Message  @relation(fields: [messageId], references: [id], onDelete: Cascade)
  userId    String
  user      User     @relation(fields: [userId], references: [id], onDelete: Cascade)
  readAt    DateTime @default(now())

  @@unique([messageId, userId])
}

model Reaction {
  id        String   @id @default(uuid())
  messageId String
  message   Message  @relation(fields: [messageId], references: [id], onDelete: Cascade)
  userId    String
  emoji     String
  createdAt DateTime @default(now())

  @@unique([messageId, userId, emoji])
}

model Attachment {
  id        String   @id @default(uuid())
  messageId String
  message   Message  @relation(fields: [messageId], references: [id], onDelete: Cascade)
  fileUrl   String
  fileName  String
  fileType  String
  fileSize  Int
  createdAt DateTime @default(now())
}

model Notification {
  id        String   @id @default(uuid())
  userId    String
  user      User     @relation(fields: [userId], references: [id], onDelete: Cascade)
  title     String
  content   String
  isRead    Boolean  @default(false)
  createdAt DateTime @default(now())
}

model Friendship {
  id        String   @id @default(uuid())
  senderId  String
  sender    User     @relation("SourceUser", fields: [senderId], references: [id], onDelete: Cascade)
  receiverId String
  receiver  User     @relation("TargetUser", fields: [receiverId], references: [id], onDelete: Cascade)
  status    String   @default("PENDING") // PENDING, ACCEPTED
  createdAt DateTime @default(now())

  @@unique([senderId, receiverId])
}

model Block {
  id         String   @id @default(uuid())
  blockerId  String
  blocker    User     @relation("SourceBlock", fields: [blockerId], references: [id], onDelete: Cascade)
  blockedId  String
  blocked    User     @relation("TargetBlock", fields: [blockedId], references: [id], onDelete: Cascade)
  createdAt  DateTime @default(now())

  @@unique([blockerId, blockedId])
}

model Report {
  id         String   @id @default(uuid())
  reporterId String
  reporter   User     @relation("Reporter", fields: [reporterId], references: [id], onDelete: Cascade)
  reportedId String
  reported   User     @relation("Reported", fields: [reportedId], references: [id], onDelete: Cascade)
  reason     String
  createdAt  DateTime @default(now())
}

model AuditLog {
  id        String   @id @default(uuid())
  userId    String
  user      User     @relation(fields: [userId], references: [id], onDelete: Cascade)
  action    String   // LOGIN, UPDATE_PASSWORD, CREATE_CHANNEL
  ipAddress String
  createdAt DateTime @default(now())
}"""

val NestJSSourceCode = """import {
  WebSocketGateway,
  WebSocketServer,
  SubscribeMessage,
  OnGatewayConnection,
  OnGatewayDisconnect,
  ConnectedSocket,
  MessageBody,
} from '@nestjs/websockets';
import { Server, Socket } from 'socket.io';
import { JwtService } from '@nestjs/jwt';
import { RedisService } from './redis.service';
import { UseFilters, UseGuards } from '@nestjs/common';
import { WsJwtAuthGuard } from './ws-jwt.guard';
import { MessageDto } from './dto/message.dto';
import { CASLAbilityFactory } from '../auth/casl-ability.factory';

@WebSocketGateway({
  cors: { origin: '*', credentials: true },
  transports: ['websocket', 'polling'],
})
export class ChatGateway implements OnGatewayConnection, OnGatewayDisconnect {
  @WebSocketServer() server: Server;

  constructor(
    private readonly jwtService: JwtService,
    private readonly redisService: RedisService,
    private readonly caslFactory: CASLAbilityFactory
  ) {}

  async handleConnection(client: Socket) {
    try {
      const authHeader = client.handshake.headers.authorization;
      if (!authHeader || !authHeader.startsWith('Bearer ')) {
        client.disconnect();
        return;
      }
      const token = authHeader.split(' ')[1];
      const payload = await this.jwtService.verifyAsync(token);
      client.data = { userId: payload.sub, email: payload.email };

      // Bind user to local Redis presence state
      await this.redisService.setUserOnline(payload.sub, client.id);
      client.broadcast.emit('user_presence_changed', { userId: payload.sub, isOnline: true });

      // Joins auto personal room to multi-device routing
      await client.join('user_id_' + payload.sub);
    } catch (err) {
      client.disconnect();
    }
  }

  async handleDisconnect(client: Socket) {
    const userId = client.data?.userId;
    if (userId) {
      await this.redisService.setUserOffline(userId);
      this.server.emit('user_presence_changed', { userId, isOnline: false, lastSeen: Date.now() });
    }
  }

  @UseGuards(WsJwtAuthGuard)
  @SubscribeMessage('join_conversation')
  async handleJoinConversation(
    @ConnectedSocket() client: Socket,
    @MessageBody('conversationId') convId: string
  ) {
    const userId = client.data.userId;
    await client.join('conv_' + convId);
    return { status: 'success', room: 'conv_' + convId };
  }

  @UseGuards(WsJwtAuthGuard)
  @SubscribeMessage('send_msg')
  async handleSendMessage(
    @ConnectedSocket() client: Socket,
    @MessageBody() payload: MessageDto
  ) {
    const userId = client.data.userId;
    
    // CASL permissions dynamic enforcement
    const ability = this.caslFactory.createForUser(userId);
    if (!ability.can('create', 'Message')) {
       throw new Error('Forbidden: Cannot write messages inside this group.');
    }

    const payloadWithMeta = {
      ...payload,
      senderId: userId,
      timestamp: Date.now(),
    };

    // Propagate message event cleanly to everyone inside group conversation room
    this.server.to('conv_' + payload.conversationId).emit('message_received', payloadWithMeta);

    // Queue S3 storage & ClamAV virus scanner tasks safely off main thread via BullMQ
    await this.redisService.enqueueAttachmentProcessing(payloadWithMeta);
  }

  @UseGuards(WsJwtAuthGuard)
  @SubscribeMessage('typing_state')
  async handleTypingIndicator(
    @ConnectedSocket() client: Socket,
    @MessageBody() data: { conversationId: string, isTyping: boolean }
  ) {
    client.to('conv_' + data.conversationId).emit('typing_status_changed', {
      senderId: client.data.userId,
      isTyping: data.isTyping,
    });
  }
}"""

val DockerComposeCode = """version: '3.8'

services:
  pg_db:
    image: postgres:15-alpine
    container_name: remix_messenger_db
    environment:
      POSTGRES_DB: remix_messenger
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: production_secure_pwd_102
    ports:
      - "5432:5432"
    volumes:
      - pg_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres -d remix_messenger"]
      interval: 10s
      timeout: 5.dp
      retries: 5

  redis_pubsub:
    image: redis:7-alpine
    container_name: remix_messenger_redis
    command: redis-server --appendonly yes --requirepass strong_redis_pwd_6379
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data
    healthcheck:
      test: ["CMD", "redis-cli", "-a", "strong_redis_pwd_6379", "ping"]
      interval: 10s
      timeout: 5.dp
      retries: 5

  nest_backend:
    build:
      context: ./backend
      dockerfile: Dockerfile.prod
    container_name: remix_messenger_backend
    environment:
      DATABASE_URL: "postgresql://postgres:production_secure_pwd_102@pg_db:5432/remix_messenger?schema=public"
      REDIS_URL: "redis://default:strong_redis_pwd_6379@redis_pubsub:6379"
      JWT_SECRET: "FAANG_enterprise_secure_token_key_777"
      PORT: 3001
    ports:
      - "3001:3001"
    depends_on:
      pg_db:
        condition: service_healthy
      redis_pubsub:
        condition: service_healthy

  next_frontend:
    build:
      context: ./frontend
      dockerfile: Dockerfile.prod
    container_name: remix_messenger_frontend
    environment:
      NEXT_PUBLIC_API_URL: "https://your_domain.com/api"
      NEXT_PUBLIC_WS_URL: "wss://your_domain.com"
    ports:
      - "3000:3000"
    depends_on:
      - nest_backend

  nginx_proxy:
    image: nginx:1.24-alpine
    container_name: remix_messenger_nginx
    volumes:
      - ./nginx/nginx.conf:/etc/nginx/nginx.conf:ro
      - ./nginx/certs:/etc/nginx/certs:ro
    ports:
      - "80:80"
      - "443:443"
    depends_on:
      - next_frontend
      - nest_backend

volumes:
  pg_data:
  redis_data:"""

val NginxConfigCode = """user nginx;
worker_processes auto;
error_log /var/log/nginx/error.log warn;
pid       /var/run/nginx.pid;

events {
    worker_connections 1024;
}

http {
    include       /etc/nginx/mime.types;
    default_type  application/octet-stream;

    # Rate limiting zone for protection against DDoS brute-force attacks
    limit_req_zone ${'$'}binary_remote_addr zone=api_limit_zone:10m rate=10r/s;

    # Compression optimizations
    gzip on;
    gzip_types text/plain text/css application/json application/javascript text/xml;

    upstream frontend_server {
        server next_frontend:3000;
    }

    upstream backend_server {
        server nest_backend:3001;
    }

    server {
        listen 80;
        server_name your_domain.com;
        return 301 https://${'$'}host${'$'}request_uri;
    }

    server {
        listen 443 ssl http2;
        server_name your_domain.com;

        ssl_certificate     /etc/nginx/certs/fullchain.pem;
        ssl_certificate_key /etc/nginx/certs/privkey.pem;
        ssl_protocols       TLSv1.2 TLSv1.3;
        ssl_ciphers         ECDHE-ECDSA-AES128-GCM-SHA256:ECDHE-RSA-AES128-GCM-SHA256;

        # Hardened Security headers
        add_header X-Frame-Options "DENY" always;
        add_header X-Content-Type-Options "nosniff" always;
        add_header Content-Security-Policy "default-src 'self' http: https: data: blob: 'unsafe-inline'" always;
        add_header Strict-Transport-Security "max-age=63072000; includeSubDomains; preload" always;

        # Static assets and Web Frontend routing
        location / {
            proxy_pass http://frontend_server;
            proxy_http_version 1.1;
            proxy_set_header Upgrade ${'$'}http_upgrade;
            proxy_set_header Connection 'upgrade';
            proxy_set_header Host ${'$'}host;
        }

        # Rest API routes
        location /api {
            limit_req zone=api_limit_zone burst=20 nodelay;
            proxy_pass http://backend_server;
            proxy_set_header Host ${'$'}host;
            proxy_set_header X-Real-IP ${'$'}remote_addr;
        }

        # WebSockets (Socket.IO Upgrade proxy config)
        location /socket.io {
            proxy_pass http://backend_server;
            proxy_http_version 1.1;
            proxy_set_header Upgrade ${'$'}http_upgrade;
            proxy_set_header Connection "Upgrade";
            proxy_set_header Host ${'$'}host;
            proxy_set_header X-Real-IP ${'$'}remote_addr;
            proxy_read_timeout 600s;
            proxy_send_timeout 600s;
        }
    }
}"""

val GitHubActionsCode = """name: Enterprise DevOps Deployment Pipeline

on:
  push:
    branches: [ "main", "production" ]
  pull_request:
    branches: [ "main" ]

jobs:
  static_validation:
    name: Build & Code Verification
    runs-on: ubuntu-latest

    services:
      postgres:
        image: postgres:15-alpine
        env:
          POSTGRES_DB: test_db
          POSTGRES_PASSWORD: test_password
        ports:
          - 5432:5432
        options: >-
          --health-cmd pg_isready
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5

    steps:
    - name: Checkout Source Code
      uses: actions/checkout@v3

    - name: Setup Node.js Runtime (v18)
      uses: actions/setup-node@v3
      with:
        node-version: 18
        cache: 'npm'

    - name: Install Monorepo Dependencies
      run: npm ci

    - name: Generate Prisma ORM client
      run: npx prisma generate

    - name: Execute Prisma Integrity Migrations
      run: npx prisma migrate dev --name init
      env:
        DATABASE_URL: "postgresql://postgres:test_password@localhost:5432/test_db?schema=public"

    - name: Execute Security Vulnerability Audit
      run: npm audit --audit-level=high

    - name: Execute ESLint & TypeScript validation
      run: npm run lint

    - name: Execute Jest Unit and Integrations tests
      run: npm run test

  deploy_to_cloud:
    name: Build Docker Images and CD Deploy
    needs: [static_validation]
    if: github.ref == 'refs/heads/production'
    runs-on: ubuntu-latest

    steps:
    - name: Checkout code
      uses: actions/checkout@v3

    - name: Set up QEMU for Multi-arch compilation
      uses: docker/setup-qemu-action@v2

    - name: Set up Docker Buildx
      uses: docker/setup-buildx-action@v2

    - name: Log in to Google Artifact Registry / DockerHub
      uses: docker/login-action@v2
      with:
        username: ${'{'} secrets.DOCKER_CONTAINER_REGISTRY_USER }
        password: ${'{'} secrets.DOCKER_CONTAINER_REGISTRY_PASSWORD }

    - name: Build and Push Backend Image
      uses: docker/build-push-action@v4
      with:
        context: ./backend
        push: true
        tags: |
          org/remix-backend:latest
          org/remix-backend:${'{'} github.sha }

    - name: Build and Push Frontend Web Bundle
      uses: docker/build-push-action@v4
      with:
        context: ./frontend
        push: true
        tags: |
          org/remix-frontend:latest
          org/remix-frontend:${'{'} github.sha }

    - name: Deploy to Kubernetes Cluster / SSH webhook Hook
      run: |
        curl -X POST -d "token=${'{'} secrets.WEBHOOK_DEPLOY_CD_TOKEN }" https://deploy-agent.your_domain.com/redeploy
"""
