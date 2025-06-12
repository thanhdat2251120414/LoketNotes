package com.example.locketnotes.presentation.components


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.locketnotes.R // đổi theo tên package thật
import androidx.compose.ui.unit.dp


@Composable
fun StoryGrid(modifier: Modifier = Modifier) {
    val stories = remember {
        listOf(
            R.drawable.story1,
            R.drawable.story2,
            R.drawable.story3,
            R.drawable.story4,
            null,
            R.drawable.story5
        )
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier.padding(8.dp),
        contentPadding = PaddingValues(4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(stories.size) { index ->
            StoryCard(imageRes = stories[index])
        }
    }
}
