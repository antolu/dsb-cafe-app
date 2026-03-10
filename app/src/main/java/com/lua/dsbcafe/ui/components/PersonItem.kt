package com.lua.dsbcafe.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lua.dsbcafe.data.model.Person
import com.lua.dsbcafe.ui.theme.RankBronze
import com.lua.dsbcafe.ui.theme.RankGold
import com.lua.dsbcafe.ui.theme.RankSilver

@Composable
fun PersonItem(person: Person, rank: Int) {
    ListItem(
        leadingContent = { RankBadge(rank) },
        headlineContent = {
            Text(
                text = person.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (rank <= 3) FontWeight.SemiBold else FontWeight.Normal,
            )
        },
        trailingContent = {
            androidx.compose.foundation.layout.Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = person.coffeeCount.toString(),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Icon(
                    imageVector = Icons.Filled.Coffee,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        },
    )
}

@Composable
private fun RankBadge(rank: Int) {
    val badgeColor = when (rank) {
        1 -> RankGold
        2 -> RankSilver
        3 -> RankBronze
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val textColor = when (rank) {
        1, 2, 3 -> MaterialTheme.colorScheme.onPrimary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(badgeColor),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = rank.toString(),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
        )
    }
}
