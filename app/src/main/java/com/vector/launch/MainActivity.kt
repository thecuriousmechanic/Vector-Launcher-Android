package com.vector.launch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            VectorHomeScreen()
        }
    }
}

@Composable
fun VectorHomeScreen() {
    val context = LocalContext.current

    val priorities = listOf(
        Priority("Build Vector Launcher", true),
        Priority("Review Research"),
        Priority("Advance Renovation")
    )

    val projects = listOf(
        Project("Vector Launcher"),
        Project("Server Research"),
        Project("Renovation"),
        Project("Knowledge Base")
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 48.dp)
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                Image(
                    painter = painterResource(id = R.drawable.vector_logo),
                    contentDescription = "Vector Logo",
                    modifier = Modifier.size(36.dp)
                )

                Spacer(
                    modifier = Modifier.width(12.dp)
                )

                Text(
                    text = "TODAY",
                    color = Color.White,
                    fontSize = 20.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            priorities.forEach { priority ->

                Text(
                    text = if (priority.active)
                        "● ${priority.title}"
                    else
                        "○ ${priority.title}",

                    color = if (priority.active)
                        Color.White
                    else
                        Color.LightGray,

                    fontSize = if (priority.active)
                        20.sp
                    else
                        18.sp,

                    modifier = Modifier.clickable {

                        Toast.makeText(
                            context,
                            "Focus Sheet Coming Soon",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )

                Spacer(
                    modifier = Modifier.height(4.dp)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))

            HorizontalDivider(
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "PROJECTS",
                color = Color.Gray,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            projects.forEach { project ->

                Text(
                    text = project.name,
                    color = Color.White,
                    fontSize = 16.sp
                )

                Spacer(
                    modifier = Modifier.height(6.dp)
                )
            }


            HorizontalDivider(
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "⚡     📓     🌐     📝",
                color = Color.White,
                fontSize = 22.sp
            )
        }
    }
}