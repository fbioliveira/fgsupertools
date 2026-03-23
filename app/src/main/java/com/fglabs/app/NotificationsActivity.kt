package com.fglabs.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.fglabs.app.data.NotificationEntity
import com.fglabs.app.ui.NotificationViewModel
import com.fglabs.app.ui.components.AppGroupSummaryCard
import com.fglabs.app.ui.theme.AppTheme
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class NotificationsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme {
                NotificationsScreen(onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    viewModel: NotificationViewModel = viewModel(),
    onBack: () -> Unit
) {
    val notifications by viewModel.allNotifications.collectAsState()
    
    // Estado para controlar qual app está selecionado para ver os detalhes
    var selectedPackageName by remember { mutableStateOf<String?>(null) }

    // Interceptar o botão voltar do sistema se um app estiver selecionado
    BackHandler(enabled = selectedPackageName != null) {
        selectedPackageName = null
    }

    // Agrupamento das notificações
    val groupedNotifications = remember(notifications) {
        notifications.groupBy { it.packageName }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = if (selectedPackageName == null) "Notificações" else {
                            groupedNotifications[selectedPackageName]?.firstOrNull()?.appName ?: "Detalhes"
                        },
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { 
                        if (selectedPackageName == null) onBack() else selectedPackageName = null 
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    if (selectedPackageName == null) {
                        IconButton(onClick = { viewModel.clearAll() }) {
                            Icon(Icons.Default.Delete, contentDescription = "Limpar Tudo")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            AnimatedContent(targetState = selectedPackageName, label = "ScreenTransition") { pkg ->
                if (pkg == null) {
                    // LISTA DE GRUPOS (APPS)
                    AppGroupsList(
                        groupedNotifications = groupedNotifications,
                        onAppClick = { selectedPackageName = it }
                    )
                } else {
                    // LISTA DE NOTIFICAÇÕES DO APP SELECIONADO
                    AppNotificationDetail(
                        packageName = pkg,
                        notifications = groupedNotifications[pkg] ?: emptyList(),
                        onDelete = { viewModel.deleteNotification(it) }
                    )
                }
            }
        }
    }
}

@Composable
fun AppGroupsList(
    groupedNotifications: Map<String, List<NotificationEntity>>,
    onAppClick: (String) -> Unit
) {
    if (groupedNotifications.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Nenhuma notificação capturada", color = Color.Gray)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(groupedNotifications.keys.toList()) { pkgName ->
                val appsNotifs = groupedNotifications[pkgName] ?: emptyList()
                val appName = appsNotifs.firstOrNull()?.appName ?: "Desconhecido"
                val unreadCount = appsNotifs.count { !it.isRead }
                
                AppGroupSummaryCard(
                    packageName = pkgName,
                    appName = appName,
                    count = unreadCount,
                    onClick = { onAppClick(pkgName) }
                )
            }
        }
    }
}

@Composable
fun AppNotificationDetail(
    packageName: String,
    notifications: List<NotificationEntity>,
    onDelete: (NotificationEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(notifications) { notification ->
            NotificationDetailItem(notification = notification, onDelete = { onDelete(notification) })
        }
    }
}

@Composable
fun NotificationDetailItem(notification: NotificationEntity, onDelete: () -> Unit) {
    val sdf = remember { SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale.getDefault()) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = sdf.format(Date(notification.timestamp)),
                    fontSize = 11.sp,
                    color = Color.Gray
                )
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                }
            }
            
            Text(text = notification.title ?: "Sem título", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = notification.content ?: "", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
            
            if (notification.imagePath != null) {
                Spacer(modifier = Modifier.height(8.dp))
                AsyncImage(
                    model = File(notification.imagePath),
                    contentDescription = null,
                    modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            if (notification.bigPicturePath != null) {
                Spacer(modifier = Modifier.height(8.dp))
                AsyncImage(
                    model = File(notification.bigPicturePath),
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp).clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}
