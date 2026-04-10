package com.example.synapseai.ui.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.synapseai.R
import com.example.synapseai.ui.theme.*

data class BottomNavItem(
    val label: String,
    val iconRes: Int,
    val route: String
)

val bottomNavItems = listOf(
    BottomNavItem("Home", R.drawable.ic_dashboard, Screen.Dashboard.route),
    BottomNavItem("Contacts", R.drawable.ic_contacts, Screen.Contacts.route),
    BottomNavItem("History", R.drawable.ic_campaign, Screen.CallHistory.route),
    BottomNavItem("Dialer", R.drawable.ic_dialer, Screen.Dialer.route),
    BottomNavItem("Analytics", R.drawable.ic_analytics, Screen.Analytics.route)
)

@Composable
fun BottomNavBar(
    currentRoute: String?,
    onItemSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(DarkSurface)
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .navigationBarsPadding(),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        bottomNavItems.forEach { item ->
            val isSelected = currentRoute == item.route ||
                    (item.route == Screen.Dialer.route && currentRoute?.startsWith("dialer") == true)

            BottomNavItemView(
                item = item,
                isSelected = isSelected,
                onClick = { onItemSelected(item.route) }
            )
        }
    }
}

@Composable
private fun BottomNavItemView(
    item: BottomNavItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val tintColor by animateColorAsState(
        targetValue = if (isSelected) ElectricIndigo else TextTertiary,
        animationSpec = tween(300),
        label = "tint"
    )
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) ElectricIndigo.copy(alpha = 0.12f) else Color.Transparent,
        animationSpec = tween(300),
        label = "bg"
    )

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(id = item.iconRes),
            contentDescription = item.label,
            tint = tintColor,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = item.label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = tintColor,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}
