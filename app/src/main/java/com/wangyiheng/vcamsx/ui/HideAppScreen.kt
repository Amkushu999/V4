package com.wangyiheng.vcamsx.ui

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class HiddenApp(
    val packageName: String,
    val appName: String,
    val icon: android.graphics.drawable.Drawable?,
    var isHidden: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HideAppScreen() {
    val context = LocalContext.current
    var apps by remember { mutableStateOf<List<HiddenApp>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(Unit) {
        loadInstalledApps(context) { apps = it }
    }
    
    val filteredApps = remember(apps, searchQuery) {
        if (searchQuery.isBlank()) apps
        else apps.filter { it.appName.contains(searchQuery, ignoreCase = true) }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hide Apps", color = Color(0xFF00D4FF), fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = Color(0xFF0A0A0F)
                )
            )
        },
        containerColor = Color(0xFF0A0A0F)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search apps...", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF00D4FF)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    textColor = Color.White,
                    focusedBorderColor = Color(0xFF00D4FF),
                    unfocusedBorderColor = Color(0xFF1E2130)
                ),
                singleLine = true
            )
            
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredApps, key = { it.packageName }) { app ->
                    AppHideItem(
                        app = app,
                        onToggleHide = {
                            scope.launch {
                                app.isHidden = !app.isHidden
                                updateHiddenAppsList(context, app.packageName, app.isHidden)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AppHideItem(app: HiddenApp, onToggleHide: () -> Unit) {
    var isChecked by remember { mutableStateOf(app.isHidden) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF12141C)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (app.icon != null) {
                androidx.compose.foundation.Image(
                    bitmap = app.icon.toBitmap().asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Text(
                text = app.appName,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            
            Checkbox(
                checked = isChecked,
                onCheckedChange = { 
                    isChecked = it
                    onToggleHide()
                },
                colors = CheckboxDefaults.colors(
                    checkedColor = Color(0xFF00D4FF),
                    uncheckedColor = Color(0xFF6B7280)
                )
            )
        }
    }
}

suspend fun loadInstalledApps(context: Context, callback: (List<HiddenApp>) -> Unit) {
    withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        
        val prefs = context.getSharedPreferences("hidden_apps", Context.MODE_PRIVATE)
        val hiddenListString = prefs.getString("hidden_list", "") ?: ""
        val hiddenPackages = hiddenListString.split(",").filter { it.isNotBlank() }.toSet()
        
        val appList = packages
            .filter { it.flags and ApplicationInfo.FLAG_SYSTEM == 0 }
            .mapNotNull {
                try {
                    HiddenApp(
                        packageName = it.packageName,
                        appName = pm.getApplicationLabel(it).toString(),
                        icon = pm.getApplicationIcon(it.packageName),
                        isHidden = it.packageName in hiddenPackages
                    )
                } catch (e: Exception) { null }
            }
            .sortedBy { it.appName.lowercase() }
        
        withContext(Dispatchers.Main) {
            callback(appList)
        }
    }
}

fun updateHiddenAppsList(context: Context, packageName: String, isHidden: Boolean) {
    val prefs = context.getSharedPreferences("hidden_apps", Context.MODE_PRIVATE)
    val currentList = prefs.getString("hidden_list", "") ?: ""
    val packages = currentList.split(",").filter { it.isNotBlank() }.toMutableSet()

    if (isHidden) {
        packages.add(packageName)
    } else {
        packages.remove(packageName)
    }

    prefs.edit().putString("hidden_list", packages.joinToString(",")).apply()
}