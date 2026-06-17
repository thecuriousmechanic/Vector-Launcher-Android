package com.vector.launch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            VectorHomeScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VectorHomeScreen() {

    var showFocusSheet by remember {
        mutableStateOf(false)
    }

    var selectedPriority by remember {
        mutableStateOf<Priority?>(null)
    }

    var currentScreen by remember {
        mutableStateOf("home")
    }

    var currentProject by remember {
        mutableStateOf("Vector Launcher")
    }

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

    if (currentScreen == "workspace") {

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Black
        ) {

            ProjectWorkspace(
                projectName = currentProject,
                onBack = {
                    currentScreen = "home"
                }
            )
        }

        return
    }

    if (showFocusSheet) {

        ModalBottomSheet(
            containerColor = Color(0xFF121212),
            onDismissRequest = {
                showFocusSheet = false
            },
            sheetState = rememberModalBottomSheetState()
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {

                Text(
                    text = selectedPriority?.title ?: "",
                    color = Color.White,
                    fontSize = 22.sp
                )

                Spacer(
                    modifier = Modifier.height(20.dp)
                )

                Text(
                    text = "Project",
                    color = Color.White
                )

                Text(
                    text = "Vector Launcher",
                    color = Color.White
                )

                Spacer(
                    modifier = Modifier.height(20.dp)
                )

                Text(
                    text = "Knowledge",
                    color = Color.White
                )

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                when (selectedPriority?.title) {

                    "Build Vector Launcher" -> {
                        Text("Design Notes", color = Color.White)
                        Text("Architecture", color = Color.White)
                        Text("Android Tasks", color = Color.White)
                    }

                    "Review Research" -> {
                        Text("Research Papers", color = Color.White)
                        Text("Server Studies", color = Color.White)
                        Text("AI Reports", color = Color.White)
                    }

                    "Advance Renovation" -> {
                        Text("Blueprints", color = Color.White)
                        Text("Cost Analysis", color = Color.White)
                        Text("Material List", color = Color.White)
                    }
                }

                Spacer(
                    modifier = Modifier.height(20.dp)
                )

                Text(
                    text = "Tools",
                    color = Color.White
                )

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                Text(
                    text = "ChatGPT",
                    color = Color.White
                )

                Text(
                    text = "Obsidian",
                    color = Color.White
                )

                Text(
                    text = "Chrome",
                    color = Color.White
                )

                Spacer(
                    modifier = Modifier.height(24.dp)
                )

                Button(
                    onClick = {

                        currentProject =
                            selectedPriority?.title ?: "Workspace"

                        showFocusSheet = false

                        currentScreen = "workspace"
                    }
                ) {
                    Text("CONTINUE")
                }

                Spacer(
                    modifier = Modifier.height(32.dp)
                )
            }
        }
    }

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

            Spacer(
                modifier = Modifier.height(24.dp)
            )

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
                        selectedPriority = priority
                        showFocusSheet = true
                    }
                )

                Spacer(
                    modifier = Modifier.height(4.dp)
                )
            }

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            HorizontalDivider(
                color = Color.DarkGray
            )

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            Text(
                text = "PROJECTS",
                color = Color.Gray,
                fontSize = 14.sp
            )

            Spacer(
                modifier = Modifier.height(16.dp)
            )

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

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            HorizontalDivider(
                color = Color.DarkGray
            )

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            Text(
                text = "ChatGPT     Obsidian     Chrome     Notes",
                color = Color.White,
                fontSize = 16.sp
            )
        }
    }
}