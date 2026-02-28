package de.codevoid.gpxmanager.ui.common

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Reusable card layout with 1/3 left area (preview placeholder) and 2/3 right area (details).
 * Per specification: left 1/3 is reserved for preview (to be added later).
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ItemCard(
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
    preview: @Composable () -> Unit = {
        Box(
            modifier = Modifier.padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            // Preview placeholder
        }
    },
    details: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                preview()
            }
            Box(
                modifier = Modifier
                    .weight(2f)
                    .padding(12.dp)
            ) {
                details()
            }
        }
    }
}
