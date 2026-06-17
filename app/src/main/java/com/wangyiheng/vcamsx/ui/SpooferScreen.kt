package com.wangyiheng.vcamsx.ui

import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wangyiheng.vcamsx.utils.DeviceSpoofer
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpooferScreen() {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    
    var currentBrand by remember { mutableStateOf(Build.BRAND) }
    var currentManufacturer by remember { mutableStateOf(Build.MANUFACTURER) }
    var currentModel by remember { mutableStateOf(Build.MODEL) }
    var currentAndroidVersion by remember { mutableStateOf(Build.VERSION.RELEASE) }
    var currentBuildId by remember { mutableStateOf(Build.ID) }
    var currentSecurityPatch by remember { mutableStateOf(Build.VERSION.SECURITY_PATCH) }
    
    var spoofBrand by remember { mutableStateOf("") }
    var spoofManufacturer by remember { mutableStateOf("") }
    var spoofModel by remember { mutableStateOf("") }
    var spoofAndroidVersion by remember { mutableStateOf("") }
    var spoofBuildId by remember { mutableStateOf("") }
    var spoofSecurityPatch by remember { mutableStateOf("") }
    
    var showBrandDialog by remember { mutableStateOf(false) }
    var showAndroidVersionDialog by remember { mutableStateOf(false) }
    
    val allFieldsFilled = spoofBrand.isNotBlank() && 
                         spoofManufacturer.isNotBlank() && 
                         spoofModel.isNotBlank() && 
                         spoofAndroidVersion.isNotBlank()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Device Spoofer", color = Color(0xFF00D4FF), fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF12141C)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("CURRENT DEVICE", color = Color(0xFF00D4FF), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Divider(color = Color(0xFF1E2130))
                    InfoRow("Model", currentModel)
                    InfoRow("Brand", currentBrand)
                    InfoRow("Manufacturer", currentManufacturer)
                    InfoRow("Android", currentAndroidVersion)
                    InfoRow("Build ID", currentBuildId)
                    InfoRow("Security Patch", currentSecurityPatch)
                }
            }
            
            Text("SPOOF CONFIGURATION", color = Color(0xFFFF2D7E), fontWeight = FontWeight.Bold, fontSize = 14.sp)
            
            SpoofField(
                label = "BRAND",
                value = spoofBrand,
                placeholder = "Select device brand",
                icon = Icons.Default.Business,
                onClick = { showBrandDialog = true },
                readOnly = true
            )
            
            SpoofField(
                label = "MANUFACTURER",
                value = spoofManufacturer,
                placeholder = "Enter manufacturer name",
                icon = Icons.Default.Factory,
                onValueChange = { spoofManufacturer = it }
            )
            
            SpoofField(
                label = "ANDROID VERSION",
                value = spoofAndroidVersion,
                placeholder = "Select Android version",
                icon = Icons.Default.Android,
                onClick = { showAndroidVersionDialog = true },
                readOnly = true
            )
            
            SpoofField(
                label = "DEVICE MODEL",
                value = spoofModel,
                placeholder = "e.g. SM-G998B or Pixel 8 Pro",
                icon = Icons.Default.PhoneAndroid,
                onValueChange = { spoofModel = it }
            )
            
            SpoofField(
                label = "BUILD ID",
                value = spoofBuildId,
                placeholder = "e.g. TP1A.220624.014",
                icon = Icons.Default.Code,
                onValueChange = { spoofBuildId = it }
            )
            
            SpoofField(
                label = "SECURITY PATCH",
                value = spoofSecurityPatch,
                placeholder = "e.g. 2024-11-01",
                icon = Icons.Default.Shield,
                onValueChange = { spoofSecurityPatch = it }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = {
                    scope.launch {
                        DeviceSpoofer.applySpoof(
                            context = context,
                            brand = spoofBrand,
                            manufacturer = spoofManufacturer,
                            model = spoofModel,
                            androidVersion = spoofAndroidVersion,
                            buildId = spoofBuildId,
                            securityPatch = spoofSecurityPatch
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = allFieldsFilled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (allFieldsFilled) Color(0xFF00D4FF) else Color(0xFF1E2130),
                    disabledContainerColor = Color(0xFF1E2130)
                )
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("APPLY SPOOF", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
    
    if (showBrandDialog) {
        BrandSelectionDialog(
            onBrandSelected = { 
                spoofBrand = it
                showBrandDialog = false
            },
            onDismiss = { showBrandDialog = false }
        )
    }
    
    if (showAndroidVersionDialog) {
        AndroidVersionDialog(
            onVersionSelected = {
                spoofAndroidVersion = it
                showAndroidVersionDialog = false
            },
            onDismiss = { showAndroidVersionDialog = false }
        )
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color(0xFF6B7280), fontSize = 13.sp)
        Text(value, color = Color.White, fontWeight = FontWeight.Medium, fontSize = 13.sp)
    }
}

@Composable
fun SpoofField(
    label: String,
    value: String,
    placeholder: String,
    icon: ImageVector,
    onClick: (() -> Unit)? = null,
    readOnly: Boolean = false,
    onValueChange: ((String) -> Unit)? = null
) {
    Column {
        Text(label, color = Color(0xFF6B7280), fontSize = 12.sp, modifier = Modifier.padding(bottom = 4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = { onValueChange?.invoke(it) },
            placeholder = { Text(placeholder, color = Color.Gray) },
            leadingIcon = { Icon(icon, contentDescription = null, tint = Color(0xFF00D4FF)) },
            modifier = Modifier
                .fillMaxWidth()
                .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
            shape = RoundedCornerShape(12.dp),
            readOnly = readOnly,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF00D4FF),
                unfocusedBorderColor = Color(0xFF1E2130),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                disabledBorderColor = Color(0xFF1E2130),
                disabledTextColor = Color.White
            ),
            singleLine = true
        )
    }
}

@Composable
fun BrandSelectionDialog(onBrandSelected: (String) -> Unit, onDismiss: () -> Unit) {
    val brands = listOf(
        "Samsung", "Google", "Xiaomi", "OnePlus", "Oppo", 
        "Vivo", "Realme", "Motorola", "Nothing", "Sony", 
        "Asus", "Honor", "Huawei", "Nokia"
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("SELECT BRAND", color = Color.White) },
        text = {
            Column {
                brands.forEach { brand ->
                    Text(
                        text = brand,
                        color = Color.White,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onBrandSelected(brand) }
                            .padding(12.dp)
                    )
                }
            }
        },
        confirmButton = {},
        containerColor = Color(0xFF12141C)
    )
}

@Composable
fun AndroidVersionDialog(onVersionSelected: (String) -> Unit, onDismiss: () -> Unit) {
    val versions = listOf("15", "14", "13", "12", "11", "10", "9", "8")
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("SELECT ANDROID VERSION", color = Color.White) },
        text = {
            Column {
                versions.forEach { version ->
                    Text(
                        text = version,
                        color = Color.White,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onVersionSelected(version) }
                            .padding(12.dp)
                    )
                }
            }
        },
        confirmButton = {},
        containerColor = Color(0xFF12141C)
    )
}