package com.example.linkkoy3.ui.common

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.linkkoy3.R
import com.example.linkkoy3.data.Folder
import com.example.linkkoy3.data.Link
import com.example.linkkoy3.ui.theme.*

// --- SHARED WRAPPER ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MySwipeToDeleteContainer(onDelete: () -> Unit, content: @Composable () -> Unit) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { direction ->
            if (direction == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else false
        }
    )
    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            val color by animateColorAsState(
                if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) Color.Red.copy(alpha = 0.8f) else Color.Transparent,
                label = "bg_color"
            )
            Box(Modifier.fillMaxSize().background(color, RoundedCornerShape(12.dp)).padding(horizontal = 20.dp), contentAlignment = Alignment.CenterEnd) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White)
            }
        }
    ) { content() }
}

// --- SHARED LIST ITEMS ---

@Composable
fun FolderItem(folder: Folder, onClick: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    var showMenu by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = getIconForName(folder.icon), contentDescription = null, tint = TextSecondary, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(folder.name, color = TextWhite, fontSize = 18.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
            Box {
                IconButton(onClick = { showMenu = true }) { Icon(Icons.Default.MoreVert, contentDescription = "More", tint = TextSecondary) }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }, modifier = Modifier.background(CardBg)) {
                    DropdownMenuItem(text = { Text(stringResource(R.string.edit)) }, onClick = { onEdit(); showMenu = false })
                    DropdownMenuItem(text = { Text(stringResource(R.string.delete), color = Color.Red) }, onClick = { onDelete(); showMenu = false })
                }
            }
        }
    }
}

// ... Other Composables ...

// --- PRIVATE UTILITY FUNCTION ---

private fun getIconForName(name: String): ImageVector { // HATA ÇÖZÜLDÜ: Fonksiyon private yapıldı
    return when (name) {
        "book" -> Icons.Default.Book
        "code" -> Icons.Default.Code
        "fitness" -> Icons.Default.FitnessCenter
        "music_note" -> Icons.Default.MusicNote
        "restaurant" -> Icons.Default.Restaurant
        "shopping_cart" -> Icons.Default.ShoppingCart
        "games" -> Icons.Default.Gamepad
        else -> Icons.Default.Folder
    }
}
