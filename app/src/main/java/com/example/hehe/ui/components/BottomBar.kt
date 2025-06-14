package com.example.hehe.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.hehe.R

@Composable
fun BottomBar(modifier: Modifier = Modifier) {
    NavigationBar(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp),
        tonalElevation = 4.dp,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        // Home
        NavigationBarItem(
            selected = false,
            onClick = { /* TODO: Reload Feed */ },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_home),
                    contentDescription = "Home",
                    modifier = Modifier
                        .size(24.dp)
                        .padding(top = 6.dp, bottom = 6.dp)
                )
            },
            label = { Text("Home") },
            alwaysShowLabel = false
        )

        // Add
        NavigationBarItem(
            selected = false,
            onClick = { /* TODO: Add */ },
            icon = {
                Box(
                    modifier = Modifier
                        .padding(top = 6.dp, bottom = 6.dp)
                        .size(44.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_add),
                        contentDescription = "Add",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            },
            label = null,
            alwaysShowLabel = false
        )

        // Search
        NavigationBarItem(
            selected = false,
            onClick = { /* TODO: Search */ },
            icon = {
                Box(
                    modifier = Modifier
                        .padding(top = 8.dp, bottom = 8.dp)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_search),
                        contentDescription = "Search",
                        modifier = Modifier.size(24.dp)
                    )
                }
            },
            label = { Text("Search") },
            alwaysShowLabel = false
        )
    }
}
