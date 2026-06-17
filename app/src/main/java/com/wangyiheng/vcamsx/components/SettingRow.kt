package com.wangyiheng.vcamsx.components

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingRow(
    label: String,
    checkedState: MutableState<Boolean>,
    onCheckedChange: (Boolean) -> Unit,
    context: Context
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF12141C)) // Dark card background
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = label, 
            color = Color(0xFFE8EAF0), 
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checkedState.value,
            onCheckedChange = { isChecked ->
                // 1. Update the UI state immediately
                checkedState.value = isChecked
                
                // 2. Trigger the save function passed from HomeScreen
                onCheckedChange(isChecked)
                
                // 3. Show a Toast representing what was toggled
                val statusText = if (isChecked) "ENABLED" else "DISABLED"
                Toast.makeText(context, "$label: $statusText", Toast.LENGTH_SHORT).show()
            },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFF00D4FF), // Neon Cyan
                checkedTrackColor = Color(0xFF00D4FF).copy(alpha = 0.4f),
                uncheckedThumbColor = Color.Gray,
                uncheckedTrackColor = Color(0xFF1E2130)
            )
        )
    }
}