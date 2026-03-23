package com.fglabs.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.fglabs.app.data.NotificationEntity
import com.fglabs.app.ui.MainViewModel
import com.fglabs.app.ui.NotificationViewModel
import com.fglabs.app.ui.components.AppGroupSummaryCard
import com.fglabs.app.ui.theme.AppTheme
import com.fglabs.app.ui.theme.ElectricBlue
import com.fglabs.app.ui.theme.NeonGreen
import com.fglabs.app.ui.theme.SurfaceGrey
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

enum class SortType { DATE, ID }
enum class SortOrder { ASC, DESC }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme {
                MainScreen(
                    onRequestNotificationAccess = {
                        startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                    }
                )
            }
        }
    }
}

sealed class Screen(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Recorder : Screen("recorder", "Gravador", Icons.Default.Mic)
    object Notifications : Screen("notifications", "Notificações", Icons.Default.Notifications)
}

@Composable
fun MainScreen(
    mainViewModel: MainViewModel = viewModel(),
    notificationViewModel: NotificationViewModel = viewModel(),
    onRequestNotificationAccess: () -> Unit
) {
    var selectedScreen by remember { mutableStateOf<Screen>(Screen.Home) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = SurfaceGrey,
                contentColor = Color.White
            ) {
                val screens = listOf(Screen.Home, Screen.Recorder, Screen.Notifications)
                screens.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = selectedScreen == screen,
                        onClick = { selectedScreen = screen },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ElectricBlue,
                            selectedTextColor = ElectricBlue,
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray,
                            indicatorColor = SurfaceGrey
                        )
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (selectedScreen) {
                Screen.Home -> HomeScreen(
                    mainViewModel, 
                    notificationViewModel, 
                    onRequestNotificationAccess,
                    onNavigateToNotifications = { selectedScreen = Screen.Notifications },
                    onNavigateToRecorder = { selectedScreen = Screen.Recorder }
                )
                Screen.Recorder -> RecorderScreen(mainViewModel)
                Screen.Notifications -> NotificationsScreen(notificationViewModel)
            }
        }
    }
}

@Composable
fun HomeScreen(
    mainViewModel: MainViewModel,
    notificationViewModel: NotificationViewModel,
    onRequestNotificationAccess: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToRecorder: () -> Unit
) {
    val unreadNotifCount by notificationViewModel.unreadCount.collectAsState()
    val audioCount by mainViewModel.recordingCount.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        Text(
            text = "Dashboard",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(32.dp))

        DashboardCard(
            title = "Notificações",
            count = unreadNotifCount,
            icon = Icons.Default.Notifications,
            color = ElectricBlue,
            onClick = onNavigateToNotifications
        )

        Spacer(modifier = Modifier.height(16.dp))

        DashboardCard(
            title = "Áudios Gravados",
            count = audioCount,
            icon = Icons.Default.Mic,
            color = NeonGreen,
            onClick = onNavigateToRecorder
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        Button(
            onClick = onRequestNotificationAccess,
            colors = ButtonDefaults.buttonColors(containerColor = SurfaceGrey),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Settings, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Acesso a Notificações")
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun DashboardCard(title: String, count: Int, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceGrey)
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(color.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(28.dp))
            }
            Spacer(modifier = Modifier.width(20.dp))
            Column {
                Text(text = title, color = Color.Gray, fontSize = 14.sp)
                Text(text = "$count", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = color)
            }
        }
    }
}

@Composable
fun RecorderScreen(mainViewModel: MainViewModel) {
    val context = LocalContext.current
    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasAudioPermission = isGranted
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "Gravador de Voz",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        RecorderComponent(
            mainViewModel = mainViewModel,
            hasPermission = hasAudioPermission,
            onRequestPermission = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) }
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        RecentRecordings(mainViewModel)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(viewModel: NotificationViewModel) {
    val notifications by viewModel.allNotifications.collectAsState()
    var selectedPackageName by remember { mutableStateOf<String?>(null) }
    
    // Estados para ordenação
    var sortType by remember { mutableStateOf(SortType.DATE) }
    var sortOrder by remember { mutableStateOf(SortOrder.DESC) }

    // Interceptar o botão voltar se um grupo estiver aberto
    BackHandler(enabled = selectedPackageName != null) {
        selectedPackageName = null
    }

    val groupedNotifications = remember(notifications) {
        notifications.groupBy { it.packageName }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (selectedPackageName != null) {
                IconButton(onClick = { selectedPackageName = null }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                }
            }
            Text(
                text = if (selectedPackageName == null) "Notificações" else {
                    groupedNotifications[selectedPackageName]?.firstOrNull()?.appName ?: "Detalhes"
                },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        }
        
        AnimatedContent(targetState = selectedPackageName, label = "NotificationNav") { pkg ->
            if (pkg == null) {
                // Lista de Grupos
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    groupedNotifications.forEach { (packageName, appNotifications) ->
                        val unreadCount = appNotifications.count { !it.isRead }
                        item(key = packageName) {
                            AppGroupSummaryCard(
                                packageName = packageName,
                                appName = appNotifications.firstOrNull()?.appName ?: "Desconhecido",
                                count = unreadCount,
                                onClick = { selectedPackageName = packageName }
                            )
                        }
                    }
                }
            } else {
                // Lista de Notificações do Grupo
                val appNotifications = groupedNotifications[pkg] ?: emptyList()
                
                // Aplicar Ordenação
                val sortedNotifications = remember(appNotifications, sortType, sortOrder) {
                    when (sortType) {
                        SortType.DATE -> if (sortOrder == SortOrder.ASC) {
                            appNotifications.sortedBy { it.timestamp }
                        } else {
                            appNotifications.sortedByDescending { it.timestamp }
                        }
                        SortType.ID -> if (sortOrder == SortOrder.ASC) {
                            appNotifications.sortedBy { it.notificationId }
                        } else {
                            appNotifications.sortedByDescending { it.notificationId }
                        }
                    }
                }

                Column {
                    // Controles de Filtro/Ordenação
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilterChip(
                            selected = sortType == SortType.DATE,
                            onClick = { 
                                if (sortType == SortType.DATE) {
                                    sortOrder = if (sortOrder == SortOrder.ASC) SortOrder.DESC else SortOrder.ASC
                                } else {
                                    sortType = SortType.DATE
                                }
                            },
                            label = { Text("Data") },
                            leadingIcon = if (sortType == SortType.DATE) {
                                { Icon(if (sortOrder == SortOrder.ASC) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ElectricBlue.copy(alpha = 0.2f),
                                selectedLabelColor = ElectricBlue
                            )
                        )

                        FilterChip(
                            selected = sortType == SortType.ID,
                            onClick = { 
                                if (sortType == SortType.ID) {
                                    sortOrder = if (sortOrder == SortOrder.ASC) SortOrder.DESC else SortOrder.ASC
                                } else {
                                    sortType = SortType.ID
                                }
                            },
                            label = { Text("ID") },
                            leadingIcon = if (sortType == SortType.ID) {
                                { Icon(if (sortOrder == SortOrder.ASC) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ElectricBlue.copy(alpha = 0.2f),
                                selectedLabelColor = ElectricBlue
                            )
                        )
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(sortedNotifications) { notification ->
                            NotificationListItem(
                                notification = notification, 
                                onMarkAsRead = { viewModel.markAsRead(notification) },
                                onDelete = { viewModel.deleteNotification(notification) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationListItem(notification: NotificationEntity, onMarkAsRead: () -> Unit, onDelete: () -> Unit) {
    val sdf = remember { SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale.getDefault()) }
    var expanded by remember { mutableStateOf(false) }
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable { 
                expanded = !expanded 
                if (expanded) onMarkAsRead()
            },
        shape = RoundedCornerShape(12.dp),
        color = SurfaceGrey.copy(alpha = 0.5f)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Text(
                                text = sdf.format(Date(notification.timestamp)),
                                fontSize = 10.sp,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ID: ${notification.notificationId}",
                                fontSize = 10.sp,
                                color = Color.Gray
                            )
                        }
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (!notification.isRead) {
                                Box(modifier = Modifier.size(6.dp).background(NeonGreen, CircleShape))
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            
                            IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Delete, contentDescription = "Deletar", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                    Text(text = notification.title ?: "Sem título", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
            }

            Text(
                text = notification.content ?: "",
                fontSize = 13.sp,
                maxLines = if (expanded) Int.MAX_VALUE else 2,
                overflow = TextOverflow.Ellipsis,
                color = if (expanded) Color.White else Color.Gray
            )

            if (expanded && notification.bigPicturePath != null) {
                Spacer(modifier = Modifier.height(8.dp))
                AsyncImage(
                    model = File(notification.bigPicturePath),
                    contentDescription = "Big Picture",
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

@Composable
fun RecorderComponent(
    mainViewModel: MainViewModel,
    hasPermission: Boolean,
    onRequestPermission: () -> Unit
) {
    val isRecording by mainViewModel.isRecording.collectAsState()
    val amplitude by mainViewModel.amplitude.collectAsState()
    
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isRecording) 1.2f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(28.dp),
        color = SurfaceGrey
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(SurfaceGrey, Color.Black.copy(alpha = 0.3f))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (isRecording) {
                    WaveformCanvas(amplitude = amplitude)
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .scale(scale)
                        .clip(CircleShape)
                        .background(if (isRecording) NeonGreen else ElectricBlue)
                        .clickable {
                            if (hasPermission) {
                                if (isRecording) mainViewModel.stopRecording() else mainViewModel.startRecording()
                            } else {
                                onRequestPermission()
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = if (isRecording) "Parar" else "Gravar",
                        tint = Color.Black,
                        modifier = Modifier.size(40.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = if (isRecording) "Gravando..." else "Toque para gravar",
                    color = if (isRecording) NeonGreen else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun WaveformCanvas(amplitude: Float) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .padding(horizontal = 40.dp)
    ) {
        val barCount = 20
        val barWidth = size.width / (barCount * 1.5f)
        val centerY = size.height / 2
        
        for (i in 0 until barCount) {
            val barHeight = (amplitude * size.height * (0.5f + Math.random().toFloat() * 0.5f)).coerceAtLeast(4.dp.toPx())
            drawRect(
                brush = Brush.verticalGradient(listOf(ElectricBlue, NeonGreen)),
                topLeft = Offset(
                    x = i * barWidth * 1.5f,
                    y = centerY - barHeight / 2
                ),
                size = Size(barWidth, barHeight)
            )
        }
    }
}

@Composable
fun RecentRecordings(viewModel: MainViewModel) {
    val recordings by viewModel.recentRecordings.collectAsState()

    Column {
        Text(
            text = "Últimas Gravações",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))
        recordings.forEach { file ->
            RecordingItem(
                file = file, 
                onPlay = { viewModel.playRecording(file) },
                onDelete = { viewModel.deleteRecording(file) }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun RecordingItem(file: File, onPlay: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPlay() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceGrey.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = ElectricBlue)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = file.name,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                color = Color.White
            )
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Deletar", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
