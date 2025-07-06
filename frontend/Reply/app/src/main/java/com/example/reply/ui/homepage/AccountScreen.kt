package com.example.reply.ui.homepage

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.reply.R
import com.example.reply.data.UserData

@Composable
fun AccountScreen(userData: UserData, onLogout: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        LoggedInUserInfo(userData = userData, onLogout = onLogout)
        Spacer(modifier = Modifier.height(24.dp))


    }
}

@Composable
private fun LoggedInUserInfo(
    userData: UserData,
    onLogout: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.profile_animation),
            contentDescription = "用户头像",
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.size(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = userData.nickname,
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "ID: ${userData.id}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "邮箱: ${userData.email}",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Button(onClick = onLogout) {
            Text("退出")
        }
    }
}
