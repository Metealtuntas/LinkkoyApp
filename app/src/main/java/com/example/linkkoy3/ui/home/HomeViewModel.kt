package com.example.linkkoy3.ui.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.linkkoy3.data.AuthManager
import com.example.linkkoy3.data.Folder
import com.example.linkkoy3.data.Link
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class HomeViewModel(
    private val authManager: AuthManager
) : ViewModel() {

    var folders by mutableStateOf<List<Folder>>(emptyList())
    var isLoading by mutableStateOf(false)
    var searchQuery by mutableStateOf("")
    var isSearching by mutableStateOf(false)
    var searchResultsFolders by mutableStateOf<List<Folder>>(emptyList())
    var searchResultsLinks by mutableStateOf<List<Link>>(emptyList())
    private var searchJob: Job? = null

    private var recentlyDeletedLink: Link? = null
    private var recentlyDeletedFolder: Folder? = null

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    init {
        loadFolders()
    }

    fun loadFolders() {
        val userId = auth.currentUser?.uid ?: return
        isLoading = true
        viewModelScope.launch {
            try {
                val snapshot = firestore.collection("folders").whereEqualTo("userId", userId).whereEqualTo("parentId", null).get().await()
                folders = snapshot.toObjects(Folder::class.java)
            } finally {
                isLoading = false
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        searchQuery = query
        searchJob?.cancel()

        if (query.isBlank()) {
            isSearching = false
            return
        }

        isSearching = true
        searchJob = viewModelScope.launch {
            delay(300) 
            val userId = auth.currentUser?.uid ?: return@launch
            val foldersSnapshot = firestore.collection("folders").whereEqualTo("userId", userId).get().await()
            searchResultsFolders = foldersSnapshot.toObjects(Folder::class.java).filter { it.name.contains(query, ignoreCase = true) }

            val linksSnapshot = firestore.collection("links").whereEqualTo("userId", userId).get().await()
            searchResultsLinks = linksSnapshot.toObjects(Link::class.java).filter { it.title.contains(query, ignoreCase = true) || it.url.contains(query, ignoreCase = true) }
        }
    }

    fun deleteLinkWithUndo(link: Link) {
        recentlyDeletedLink = link
        searchResultsLinks = searchResultsLinks.filterNot { it.id == link.id }
    }

    fun undoDeleteLink() {
        recentlyDeletedLink?.let { linkToRestore ->
            searchResultsLinks = (searchResultsLinks + linkToRestore).sortedBy { it.title.lowercase() }
            recentlyDeletedLink = null
        }
    }

    fun confirmDeleteLink() {
        recentlyDeletedLink?.let { linkToDelete ->
            viewModelScope.launch {
                firestore.collection("links").document(linkToDelete.id).delete().await()
                recentlyDeletedLink = null
            }
        }
    }

    fun deleteFolderWithUndo(folder: Folder) {
        recentlyDeletedFolder = folder
        folders = folders.filterNot { it.id == folder.id }
        if (isSearching) {
            searchResultsFolders = searchResultsFolders.filterNot { it.id == folder.id }
        }
    }

    fun undoDeleteFolder() {
        recentlyDeletedFolder?.let { folderToRestore ->
            folders = (folders + folderToRestore).sortedBy { it.name.lowercase() }
            if (isSearching) {
                searchResultsFolders = (searchResultsFolders + folderToRestore).sortedBy { it.name.lowercase() }
            }
            recentlyDeletedFolder = null
        }
    }

    fun confirmDeleteFolder() {
        recentlyDeletedFolder?.let { folderToDelete ->
            viewModelScope.launch {
                deleteFolderAndContents(folderToDelete.id)
                recentlyDeletedFolder = null
            }
        }
    }

    private suspend fun deleteFolderAndContents(folderId: String) {
        val subfoldersQuery = firestore.collection("folders").whereEqualTo("parentId", folderId).get().await()
        subfoldersQuery.documents.forEach { subfolderDoc ->
            deleteFolderAndContents(subfolderDoc.id)
        }
        val linksQuery = firestore.collection("links").whereEqualTo("folderId", folderId).get().await()
        linksQuery.documents.forEach { linkDoc ->
            linkDoc.reference.delete().await()
        }
        firestore.collection("folders").document(folderId).delete().await()
    }

    fun createFolder(name: String, icon: String, color: String?, onSuccess: () -> Unit) {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            val folderRef = firestore.collection("folders").document()
            val folder = Folder(id = folderRef.id, name = name, icon = icon, color = color, userId = userId)
            folderRef.set(folder).await()
            loadFolders()
            onSuccess()
        }
    }

    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            auth.signOut()
            authManager.clear()
            onSuccess()
        }
    }
}