package com.example.linkkoy3.ui.home

import android.net.Uri
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.linkkoy3.data.Folder
import com.example.linkkoy3.data.Link
import com.example.linkkoy3.ui.theme.*
import kotlinx.coroutines.launch

private fun getDomainFromUrl(url: String): String? {
    return try {
        val uri = Uri.parse(if (!url.startsWith("http")) "https://$url" else url)
        uri.host?.removePrefix("www.")
    } catch (e: Exception) {
        null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToFolder: (String) -> Unit,
    onLogout: () -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val onUndoAction: (Any) -> Unit = { item ->
        scope.launch {
            val message = when (item) {
                is Folder -> {
                    viewModel.deleteFolderWithUndo(item)
                    "Folder deleted"
                }
                is Link -> {
                    viewModel.deleteLinkWithUndo(item)
                    "Link deleted"
                }
                else -> ""
            }

            val result = snackbarHostState.showSnackbar(
                message = message,
                actionLabel = "Undo",
                withDismissAction = true,
                duration = SnackbarDuration.Short // Hata için eklendi
            )

            when (result) {
                SnackbarResult.ActionPerformed -> {
                    if (item is Folder) viewModel.undoDeleteFolder()
                    if (item is Link) viewModel.undoDeleteLink()
                }
                SnackbarResult.Dismissed -> {
                     if (item is Folder) viewModel.confirmDeleteFolder()
                     if (item is Link) viewModel.confirmDeleteLink()
                }
            }
        }
    }

    Scaffold(
        containerColor = Background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column(modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Link, contentDescription = "Linkkoy Logo", tint = Primary, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Linkkoy", color = TextWhite, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout", tint = TextSecondary)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                OutlinedTextField(
                    value = viewModel.searchQuery,
                    onValueChange = { viewModel.onSearchQueryChange(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search folders or links", color = TextSecondary) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CardBg,
                        unfocusedContainerColor = CardBg,
                        cursorColor = TextWhite,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )
            }
        },
        floatingActionButton = {
            Button(
                onClick = { showCreateDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("New Folder", modifier = Modifier.padding(vertical = 8.dp))
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp)
        ) {
             if (viewModel.isSearching) {
                item { Text("SEARCH RESULTS", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                if (viewModel.searchResultsFolders.isEmpty() && viewModel.searchResultsLinks.isEmpty()) {
                    item { Box(Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) { Text("No results found", color = TextSecondary) } }
                }
                items(viewModel.searchResultsFolders, key = { "folder-${it.id}" }) { folder ->
                    SwipeToDeleteContainer(onDelete = { onUndoAction(folder) }) {
                        FolderItem(folder = folder, onClick = { onNavigateToFolder(folder.id) })
                    }
                }
                items(viewModel.searchResultsLinks, key = { "link-${it.id}" }) { link ->
                    SwipeToDeleteContainer(onDelete = { onUndoAction(link) }) {
                        LinkSearchItem(link = link, onClick = { 
                            link.folderId?.let { onNavigateToFolder(it) } 
                        }, onCopy = {
                            scope.launch { snackbarHostState.showSnackbar("Link copied!") }
                        })
                    }
                }
            } else {
                item { Text("FOLDERS", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                if (viewModel.folders.isEmpty() && !viewModel.isLoading) {
                    item {
                        Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("No folders yet", color = TextWhite, fontSize = 18.sp)
                                Text("Create your first folder to get started", color = TextSecondary, fontSize = 14.sp)
                            }
                        }
                    }
                }
                items(viewModel.folders, key = { it.id }) { folder ->
                     SwipeToDeleteContainer(onDelete = { onUndoAction(folder) }) {
                        FolderItem(folder = folder, onClick = { onNavigateToFolder(folder.id) })
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateFolderDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { name, icon, color ->
                viewModel.createFolder(name, icon, color) {
                    showCreateDialog = false
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDeleteContainer(onDelete: () -> Unit, content: @Composable () -> Unit) {
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
                if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) Danger else Color.Transparent,
                label = ""
            )
            Box(
                Modifier
                    .fillMaxSize()
                    .background(color, RoundedCornerShape(12.dp))
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White)
            }
        }
    ) { content() }
}

@Composable
private fun FolderItem(folder: Folder, onClick: () -> Unit) {
    val iconColor = folder.color?.let { Color(android.graphics.Color.parseColor(it)) } ?: TextSecondary
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = getIconForName(folder.icon), contentDescription = null, tint = iconColor, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(16.dp))
            Text(folder.name, color = TextWhite, fontSize = 16.sp)
            Spacer(Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondary)
        }
    }
}

@Composable
private fun LinkSearchItem(link: Link, onClick: () -> Unit, onCopy: () -> Unit) {
    val clipboardManager = LocalClipboardManager.current
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = "https://www.google.com/s2/favicons?sz=64&domain_url=${getDomainFromUrl(link.url)}",
                contentDescription = "Favicon",
                error = rememberVectorPainter(Icons.Default.Link),
                modifier = Modifier.size(24.dp).clip(RoundedCornerShape(4.dp))
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(link.title, color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                Text(link.url, color = TextSecondary, fontSize = 14.sp, maxLines = 1)
            }
            IconButton(onClick = { 
                clipboardManager.setText(AnnotatedString(link.url))
                onCopy()
            }) {
                Icon(Icons.Default.ContentCopy, contentDescription = "Copy URL", tint = TextSecondary)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateFolderDialog(onDismiss: () -> Unit, onCreate: (String, String, String?) -> Unit) {
    var name by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf("folder") }
    val colors = listOf("#FFFFFF", "#F44336", "#E91E63", "#9C27B0", "#673AB7", "#3F51B5", "#2196F3", "#00BCD4", "#4CAF50", "#FFEB3B", "#FF9800")
    var selectedColor by remember { mutableStateOf<String?>(null) }

    val icons = listOf("folder", "book", "code", "fitness_center", "music_note", "restaurant", "shopping_cart", "gamepad", "movie", "work", "article", "favorite", "bookmark")

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = CardBg)) {
            Column(Modifier.padding(24.dp)) {
                Text("New Folder", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Folder name") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = InputBg, unfocusedContainerColor = InputBg, cursorColor = TextWhite, focusedBorderColor = Primary, unfocusedBorderColor = Color.Transparent, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                )
                Spacer(Modifier.height(16.dp))
                Text("Choose Color", color = TextSecondary)
                LazyRow(modifier = Modifier.padding(vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)){
                    items(colors) { color ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(android.graphics.Color.parseColor(color)))
                                .clickable { selectedColor = color }
                                .border(if (selectedColor == color) 2.dp else 0.dp, Color.White, CircleShape)
                        )
                    }
                }
                Text("Choose Icon", color = TextSecondary)
                LazyVerticalGrid(columns = GridCells.Fixed(4), modifier = Modifier.padding(vertical = 12.dp)) {
                    items(icons) { icon ->
                        Box(
                            Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selectedIcon == icon) Primary else InputBg)
                                .clickable { selectedIcon = icon }
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = getIconForName(icon), contentDescription = icon, tint = TextWhite)
                        }
                    }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = { if (name.isNotBlank()) onCreate(name, selectedIcon, selectedColor) }) { Text("Create") }
                }
            }
        }
    }
}

private fun getIconForName(name: String): ImageVector {
    return when (name) {
        "book" -> Icons.Default.Book
        "code" -> Icons.Default.Code
        "fitness_center" -> Icons.Default.FitnessCenter
        "music_note" -> Icons.Default.MusicNote
        "restaurant" -> Icons.Default.Restaurant
        "shopping_cart" -> Icons.Default.ShoppingCart
        "gamepad" -> Icons.Default.Gamepad
        "work" -> Icons.Default.Work
        "movie" -> Icons.Default.Movie
        "article" -> Icons.Default.Article
        "favorite" -> Icons.Default.Favorite
        "bookmark" -> Icons.Default.Bookmark
        else -> Icons.Default.Folder
    }
}
