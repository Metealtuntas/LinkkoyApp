package com.example.linkkoy3.ui.folder

import android.content.Intent
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
import androidx.compose.ui.platform.LocalContext
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
fun FolderScreen(
    folderId: String,
    viewModel: FolderViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToSubfolder: (String) -> Unit
) {
    var showAddLinkDialog by remember { mutableStateOf(false) }
    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var linkToEdit by remember { mutableStateOf<Link?>(null) }
    var folderToEdit by remember { mutableStateOf<Folder?>(null) }
    var linkToMove by remember { mutableStateOf<Link?>(null) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val onUndoLinkDelete: (Link) -> Unit = { link ->
        scope.launch {
            viewModel.deleteLinkWithUndo(link)
            val result = snackbarHostState.showSnackbar(
                message = "Link deleted",
                actionLabel = "Undo",
                withDismissAction = true,
                duration = SnackbarDuration.Short
            )
            when (result) {
                SnackbarResult.ActionPerformed -> viewModel.undoDeleteLink()
                SnackbarResult.Dismissed -> viewModel.confirmDeleteLink()
            }
        }
    }
    
    val onUndoFolderDelete: (Folder) -> Unit = { folder ->
        scope.launch {
            viewModel.deleteFolderWithUndo(folder)
            val result = snackbarHostState.showSnackbar(
                message = "Folder deleted",
                actionLabel = "Undo",
                withDismissAction = true,
                duration = SnackbarDuration.Short
            )
            when (result) {
                SnackbarResult.ActionPerformed -> viewModel.undoDeleteFolder()
                SnackbarResult.Dismissed -> viewModel.confirmDeleteFolder()
            }
        }
    }

    LaunchedEffect(folderId) {
        viewModel.loadFolderData(folderId)
    }

    Scaffold(
        containerColor = Background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        viewModel.currentFolder?.let {
                            val iconColor = it.color?.let { colorHex -> Color(android.graphics.Color.parseColor(colorHex)) } ?: Primary
                            Icon(getIconForName(it.icon), contentDescription = null, tint = iconColor, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(it.name, color = TextWhite)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextWhite)
                    }
                },
                actions = {
                    var showMenu by remember { mutableStateOf(false) }
                    Box {
                        IconButton(onClick = { showMenu = true }) { Icon(Icons.Default.MoreVert, contentDescription = "More", tint = TextWhite) }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }, modifier = Modifier.background(CardBg)) {
                            DropdownMenuItem(text = { Text("Edit Folder") }, onClick = {
                                folderToEdit = viewModel.currentFolder
                                showMenu = false
                            })
                            DropdownMenuItem(text = { Text("New Subfolder") }, onClick = {
                                showCreateFolderDialog = true
                                showMenu = false
                            })
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, bottom = 24.dp, top = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = { showAddLinkDialog = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Link")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Link", modifier = Modifier.padding(vertical = 8.dp))
                }
                FloatingActionButton(
                    onClick = { showCreateFolderDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    containerColor = CardBg,
                ) {
                    Icon(Icons.Default.CreateNewFolder, contentDescription = "Create Folder", tint = Primary)
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp)
        ) {
            if (viewModel.links.isEmpty() && viewModel.subfolders.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("No content yet", color = TextWhite, fontSize = 18.sp)
                            Text("Add a link or create a subfolder", color = TextSecondary, fontSize = 14.sp)
                        }
                    }
                }
            }
            items(viewModel.subfolders, key = { "folder-${it.id}" }) { subfolder ->
                SwipeToDeleteContainer(onDelete = { onUndoFolderDelete(subfolder) }) {
                    FolderItem(folder = subfolder, onClick = { onNavigateToSubfolder(subfolder.id) })
                }
            }
            items(viewModel.links, key = { "link-${it.id}" }) { link ->
                SwipeToDeleteContainer(onDelete = { onUndoLinkDelete(link) }) {
                    LinkItem(
                        link = link,
                        onOpen = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(if (link.url.startsWith("http")) link.url else "https://${link.url}"))
                            context.startActivity(intent)
                        },
                        onDelete = { onUndoLinkDelete(link) },
                        onCopy = { scope.launch { snackbarHostState.showSnackbar("Link copied!") } },
                        onEdit = { linkToEdit = link },
                        onMove = { linkToMove = link }
                    )
                }
            }
        }
    }

    if (showAddLinkDialog) {
        AddLinkDialog(onDismiss = { showAddLinkDialog = false }, onAdd = { title, url ->
            viewModel.addLink(title, url, folderId) { showAddLinkDialog = false }
        })
    }
    if (showCreateFolderDialog) {
        CreateFolderDialog(onDismiss = { showCreateFolderDialog = false }, onCreate = { name, icon, color ->
            viewModel.createFolder(name, icon, color, folderId) { showCreateFolderDialog = false }
        })
    }

    linkToEdit?.let {
        EditLinkDialog(link = it, onDismiss = { linkToEdit = null }, onConfirm = { newTitle, newUrl ->
            viewModel.updateLink(it.id, newTitle, newUrl, folderId)
            linkToEdit = null
        })
    }

    folderToEdit?.let {
        EditFolderDialog(folder = it, onDismiss = { folderToEdit = null }, onConfirm = { newName, newIcon, newColor ->
            viewModel.updateFolder(it.id, newName, newIcon, newColor)
            folderToEdit = null
        })
    }

    linkToMove?.let { link ->
        MoveLinkDialog(
            subfolders = viewModel.subfolders.filter { it.id != link.folderId },
            onDismiss = { linkToMove = null },
            onMove = { targetFolderId ->
                viewModel.moveLink(link.id, targetFolderId)
                scope.launch { snackbarHostState.showSnackbar("Link moved!") }
                linkToMove = null
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
            val color by animateColorAsState(if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) Danger else Color.Transparent, label = "")
            Box(
                Modifier.fillMaxSize().background(color, RoundedCornerShape(12.dp)).padding(horizontal = 20.dp),
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
private fun LinkItem(
    link: Link, 
    onOpen: () -> Unit, 
    onDelete: () -> Unit, 
    onCopy: () -> Unit, 
    onEdit: () -> Unit, 
    onMove: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    var showMenu by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onOpen() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg)
    ) {
        Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            val domain = getDomainFromUrl(link.url)
            AsyncImage(
                model = "https://www.google.com/s2/favicons?sz=64&domain=${domain}",
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
            Box {
                IconButton(onClick = { showMenu = true }) { Icon(Icons.Default.MoreVert, contentDescription = "More", tint = TextSecondary) }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }, modifier = Modifier.background(CardBg)) {
                    DropdownMenuItem(text = { Text("Open") }, onClick = { onOpen(); showMenu = false })
                    DropdownMenuItem(text = { Text("Copy") }, onClick = {
                        clipboardManager.setText(AnnotatedString(link.url))
                        onCopy()
                        showMenu = false
                    })
                    DropdownMenuItem(text = { Text("Move") }, onClick = { onMove(); showMenu = false })
                    DropdownMenuItem(text = { Text("Edit") }, onClick = { onEdit(); showMenu = false })
                    DropdownMenuItem(text = { Text("Delete", color = Danger) }, onClick = { onDelete(); showMenu = false })
                }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddLinkDialog(onDismiss: () -> Unit, onAdd: (String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Link") },
        text = {
            Column {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") })
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = url, onValueChange = { url = it }, label = { Text("URL") })
            }
        },
        confirmButton = { Button(onClick = { onAdd(title, url) }) { Text("Add") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun EditFolderDialog(folder: Folder, onDismiss: () -> Unit, onConfirm: (String, String, String?) -> Unit) {
    var name by remember { mutableStateOf(folder.name) }
    var selectedIcon by remember { mutableStateOf(folder.icon) }
    var selectedColor by remember { mutableStateOf(folder.color) }
    val colors = listOf("#FFFFFF", "#F44336", "#E91E63", "#9C27B0", "#673AB7", "#3F51B5", "#2196F3", "#00BCD4", "#4CAF50", "#FFEB3B", "#FF9800")
    val icons = listOf("folder", "book", "code", "fitness_center", "music_note", "restaurant", "shopping_cart", "gamepad", "movie", "work", "article", "favorite", "bookmark")

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = CardBg)) {
            Column(Modifier.padding(24.dp)) {
                Text("Edit Folder", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextWhite)
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
                    Button(onClick = { onConfirm(name, selectedIcon, selectedColor) }) { Text("Save") }
                }
            }
        }
    }
}

@Composable
private fun EditLinkDialog(link: Link, onDismiss: () -> Unit, onConfirm: (String, String) -> Unit) {
    var title by remember { mutableStateOf(link.title) }
    var url by remember { mutableStateOf(link.url) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Link") },
        text = {
            Column {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") })
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = url, onValueChange = { url = it }, label = { Text("URL") })
            }
        },
        confirmButton = { Button(onClick = { onConfirm(title, url) }) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun MoveLinkDialog(subfolders: List<Folder>, onDismiss: () -> Unit, onMove: (String) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Move link to...") },
        text = {
            if (subfolders.isEmpty()) {
                Text("No other subfolders to move to.")
            } else {
                LazyColumn {
                    items(subfolders) { folder ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onMove(folder.id) }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val iconColor = folder.color?.let { colorHex -> Color(android.graphics.Color.parseColor(colorHex)) } ?: TextSecondary
                            Icon(imageVector = getIconForName(folder.icon), contentDescription = null, tint = iconColor)
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(folder.name, color = TextWhite)
                        }
                    }
                }
            }
        },
        confirmButton = { 
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
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
