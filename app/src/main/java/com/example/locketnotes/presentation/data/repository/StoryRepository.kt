package com.example.locketnotes.presentation.data.repository

import android.util.Log
import com.example.locketnotes.presentation.domain.model.Story
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class StoryRepository {

    private val databaseRef = FirebaseDatabase.getInstance().getReference("user")
    private val auth = FirebaseAuth.getInstance()

    fun getAllStories(): Flow<List<Story>> = callbackFlow {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            close(Exception("Người dùng chưa đăng nhập"))
            return@callbackFlow
        }

        val userPhotoRef = databaseRef.child(userId).child("photos")

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val stories = mutableListOf<Story>()
                Log.d("FirebaseStory", "snapshot children count: ${snapshot.childrenCount}")

                for (child in snapshot.children) {
                    val story = child.getValue(Story::class.java)
                    val idFromKey = child.key ?: "unknown_key"

                    if (story != null) {
                        val fixedStory = story.copy(id = idFromKey)
                        Log.d("FirebaseStory", "Fetched: $idFromKey -> $fixedStory")
                        stories.add(fixedStory)
                    } else {
                        Log.w("FirebaseStory", "Cannot parse story from: $idFromKey, raw data: ${child.value}")
                    }
                }

                trySend(stories).isSuccess
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseStory", "Cancelled: ${error.message}")
                close(error.toException())
            }
        }

        userPhotoRef.addValueEventListener(listener)
        awaitClose { userPhotoRef.removeEventListener(listener) }
    }
    fun updateStory(story: Story) {
        val userId = auth.currentUser?.uid ?: return
        val storyId = story.id
        if (storyId.isBlank()) return

        databaseRef.child(userId)
            .child("photos")
            .child(storyId)
            .setValue(story)
            .addOnSuccessListener {
                Log.d("FirebaseStory", "Updated story: $story")
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseStory", "Failed to update story: ${e.message}")
            }
    }


    fun addStory(story: Story) {
        val userId = auth.currentUser?.uid ?: return
        val photoKey = databaseRef.child(userId).child("photos").push().key ?: return
        val newStory = story.copy(id = photoKey)

        databaseRef.child(userId)
            .child("photos")
            .child(photoKey)
            .setValue(newStory)

        Log.d("FirebaseStory", "Added story: $newStory")
    }

    fun deleteStory(storyId: String) {
        val userId = auth.currentUser?.uid ?: return

        databaseRef.child(userId)
            .child("photos")
            .child(storyId)
            .removeValue()

        Log.d("FirebaseStory", "Deleted story with id = $storyId")
    }

}
