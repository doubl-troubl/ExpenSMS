package com.dagimg.expensms.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dagimg.expensms.ui.theme.AppColors
import com.dagimg.expensms.ui.theme.Spacing

@Composable
fun SettingItem(
    icon: @Composable () -> Unit,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    action: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .then(
                    if (onClick != null) {
                        Modifier.clickable { onClick() }
                    } else {
                        Modifier
                    },
                ).padding(Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Icon
        Box(
            modifier = Modifier.size(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            icon()
        }

        Spacer(modifier = Modifier.width(Spacing.md))

        // Content
        Column(
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = AppColors.Foreground,
                fontSize = 16.sp,
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.MutedForeground,
                fontSize = 14.sp,
            )
        }

        // Action or Chevron
        if (action != null) {
            action()
        } else if (onClick != null) {
            Icon(
                painter =
                    androidx.compose.ui.res
                        .painterResource(id = android.R.drawable.ic_media_next),
                contentDescription = "Navigate",
                tint = AppColors.MutedForeground,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}
