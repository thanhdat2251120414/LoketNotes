package com.example.hehe.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hehe.R

@Composable
fun Header() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_react),
            contentDescription = null,
            modifier = Modifier.size(30.dp)
        )
        Text(
            text = "News feed",
            fontSize = 20.sp,
            modifier = Modifier
                .background(Color.LightGray, CircleShape)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )
        Image(
            painter = painterResource(id = R.drawable.ic_comment),
            contentDescription = null,
            modifier = Modifier.size(30.dp)
        )
    }
}
