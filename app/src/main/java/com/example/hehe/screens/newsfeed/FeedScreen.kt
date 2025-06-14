package com.example.hehe.screens.newsfeed

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import com.example.hehe.model.PostData
import com.example.hehe.ui.components.PostItem

@Composable
fun FeedScreen(
    listState: LazyListState,
    posts: List<PostData>
) {
    LazyColumn(state = listState) {
        items(posts) { post ->
            PostItem(
                avatarId = post.avatarId,
                name = post.name,
                content = post.content,
                imageId = post.imageId,
                likesCount = post.likesCount,
                onLike = {},
                onComment = {},
                onShare = {},
                onSave = {}
            )
        }
    }
}

