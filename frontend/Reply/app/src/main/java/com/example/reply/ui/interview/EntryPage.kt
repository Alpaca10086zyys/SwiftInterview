package com.example.reply.ui.interview
import androidx.compose.foundation.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.reply.R
import com.example.reply.ui.EmptyComingSoon

@Composable
fun EntryPage(onStartClicked: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(1.dp))

        // 中间偏上的图片
        Image(
            painter = painterResource(id = R.drawable.begin),
            contentDescription = "面试开始图",
            modifier = Modifier
                .size(240.dp)
                .align(Alignment.CenterHorizontally)
        )

        // 最下方按钮
        Button(
            onClick = onStartClicked,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("开始面试")
        }
    }
}

