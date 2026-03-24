package com.fglabs.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.fglabs.app.ui.theme.GreenTeal
import com.fglabs.app.ui.theme.SurfaceGrey

@Composable
fun AppGroupSummaryCard(
    packageName: String,
    appName: String,
    count: Int,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val pm = remember { context.packageManager }
    
    val appIcon = remember(packageName) {
        try { pm.getApplicationIcon(packageName) } catch (e: Exception) { null }
    }

    val displayAppName = remember(packageName, appName) {
        if (appName.contains(".") || appName.isEmpty()) {
            try {
                pm.getApplicationLabel(pm.getApplicationInfo(packageName, 0)).toString()
            } catch (e: Exception) {
                packageName.split(".").lastOrNull()?.replaceFirstChar { it.uppercase() } ?: appName
            }
        } else {
            appName
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceGrey.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (appIcon != null) {
                AsyncImage(
                    model = appIcon,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp))
                )
            } else {
                Box(
                    modifier = Modifier.size(48.dp).background(GreenTeal.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(displayAppName.take(1), fontWeight = FontWeight.Bold, color = GreenTeal)
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(text = displayAppName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = GreenTeal)
                Text(text = packageName, fontSize = 10.sp, color = Color.Gray)
            }
            
            if (count > 0) {
                Badge(containerColor = GreenTeal, contentColor = Color.White) {
                    Text(count.toString(), modifier = Modifier.padding(4.dp))
                }
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
        }
    }
}
