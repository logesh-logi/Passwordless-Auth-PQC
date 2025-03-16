package com.fidoauth.pqcclient.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SecurityDetailItem(
    title: String,
    value: String,
    contentColor: Color = Color.Black,
    labelColor: Color = Color.Gray
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = labelColor,
                fontWeight = FontWeight.Normal
            )

            Text(
                text = value,
                color = contentColor,
                fontWeight = FontWeight.Medium
            )
        }

        Divider(
            color = Color.LightGray.copy(alpha = 0.3f),
            thickness = 0.5.dp
        )
    }
}