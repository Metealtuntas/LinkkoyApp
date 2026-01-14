package com.example.linkkoy3.ui.folder

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.linkkoy3.data.Folder
import com.example.linkkoy3.data.Link
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class FolderViewModel() : ViewModel() {

    var currentFolder by mutableStateOf<Folder?>(null)
    var links by mutableStateOf<List<Link>>(emptyList())
    var subfolders by mutableStateOf<List<Folder>>(emptyList())
    var isLoading by mutableStateOf(false)

    private var recentlyDeletedLink: Link? = null
    private var recentlyDeletedFolder: Folder? = null

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    fun loadFolderData(folderId: String) {
        isLoading = true
        viewModelScope.launch {
            try {
                val folderDoc = firestore.collection("folders").document(folderId).get().await()
                currentFolder = folderDoc.toObject(Folder::class.java)

                val linksSnapshot = firestore.collection("links").whereEqualTo("folderId", folderId).get().await()
                links = linksSnapshot.toObjects(Link::class.java)

                val subfoldersSnapshot = firestore.collection("folders").whereEqualTo("parentId", folderId).get().await()
                subfolders = subfoldersSnapshot.toObjects(Folder::class.java)
            } finally {
                isLoading = false
            }
        }
    }

    fun addLink(title: String, url: String, folderId: String, onSuccess: () -> Unit) {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            val linkRef = firestore.collection("links").document()
            val link = Link(
                id = linkRef.id,
                title = title,
                url = url,
                folderId = folderId,
                userId = userId
            )
            linkRef.set(link).await()
            loadFolderData(folderId)
            onSuccess()
        }
    }

    fun deleteLinkWithUndo(link: Link) {
        recentlyDeletedLink = link
        links = links.filterNot { it.id == link.id }
    }

    fun undoDeleteLink() {
        recentlyDeletedLink?.let {
            links = (links + it).sortedBy { l -> l.title.lowercase() }
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

    fun createFolder(name: String, icon: String, color: String?, parentId: String, onSuccess: () -> Unit) {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            val folderRef = firestore.collection("folders").document()
            val folder = Folder(
                id = folderRef.id,
                name = name,
                icon = icon,
                color = color,
                userId = userId,
                parentId = parentId
            )
            folderRef.set(folder).await()
            loadFolderData(parentId)
            onSuccess()
        }
    }

    fun updateFolder(folderId: String, newName: String, newIcon: String, newColor: String?) {
        viewModelScope.launch {
            val updates = mapOf(
                "name" to newName,
                "icon" to newIcon,
                "color" to newColor
            )
            firestore.collection("folders").document(folderId).update(updates).await()
            loadFolderData(folderId)
        }
    }

    fun updateLink(linkId: String, newTitle: String, newUrl: String, folderId: String) {
        viewModelScope.launch {
            val updates = mapOf("title" to newTitle, "url" to newUrl)
            firestore.collection("links").document(linkId).update(updates).await()
            loadFolderData(folderId)
        }
    }

    fun moveLink(linkId: String, newFolderId: String) {
        viewModelScope.launch {
            firestore.collection("links").document(linkId).update("folderId", newFolderId).await()
            loadFolderData(currentFolder?.id ?: "")
        }
    }

    fun deleteFolderWithUndo(folder: Folder) {
        recentlyDeletedFolder = folder
        subfolders = subfolders.filterNot { it.id == folder.id }
    }

    fun undoDeleteFolder() {
        recentlyDeletedFolder?.let {
            subfolders = (subfolders + it).sortedBy { f -> f.name.lowercase() }
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
}