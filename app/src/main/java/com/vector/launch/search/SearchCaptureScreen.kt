package com.vector.launch.search

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vector.launch.core.model.SearchResult
import com.vector.launch.core.model.SearchResultType
import com.vector.launch.core.ui.EmptyStateView
import com.vector.launch.core.ui.VectorCard

import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchCaptureScreen(
    viewModel: SearchCaptureViewModel,
    onBackClick: (() -> Unit)? = null,
    onResultSelected: (SearchResult) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    if (onBackClick != null) {
        BackHandler(onBack = onBackClick)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Search",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        letterSpacing = 1.5.sp
                    )
                },
                navigationIcon = {
                    if (onBackClick != null) {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Quick Capture Box
            VectorCard(
                borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (uiState.isCapturingNote) "Quick capture note" else "Quick capture task",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = if (uiState.isCapturingNote) "Switch to Task" else "Switch to Note",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.clickable { viewModel.toggleCaptureType() }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        BasicTextField(
                            value = uiState.quickCaptureInput,
                            onValueChange = { viewModel.updateCaptureInput(it) },
                            textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { viewModel.submitQuickCapture() }),
                            modifier = Modifier.weight(1f),
                            decorationBox = { innerTextField ->
                                if (uiState.quickCaptureInput.isBlank()) {
                                    Text(
                                        text = if (uiState.isCapturingNote) "Capture idea..." else "Add task (e.g. 'Buy milk')...",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                innerTextField()
                            }
                        )

                        Button(
                            onClick = { viewModel.submitQuickCapture() },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Capture", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Capture", style = MaterialTheme.typography.titleMedium)
                        }
                    }

                    uiState.isSuccessMessage?.let { successMessage ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = successMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Search Input Bar
            OutlinedTextField(
                value = uiState.query,
                onValueChange = { viewModel.updateQuery(it) },
                placeholder = { Text("Search tasks, projects, notes, apps...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Filter Pills
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = uiState.selectedFilter == null,
                        onClick = { viewModel.selectFilter(null) },
                        label = { Text("All") }
                    )
                }
                item {
                    FilterChip(
                        selected = uiState.selectedFilter == SearchResultType.APPLICATION,
                        onClick = { viewModel.selectFilter(SearchResultType.APPLICATION) },
                        label = { Text("Apps") }
                    )
                }
                item {
                    FilterChip(
                        selected = uiState.selectedFilter == SearchResultType.TASK,
                        onClick = { viewModel.selectFilter(SearchResultType.TASK) },
                        label = { Text("Tasks") }
                    )
                }
                item {
                    FilterChip(
                        selected = uiState.selectedFilter == SearchResultType.PROJECT,
                        onClick = { viewModel.selectFilter(SearchResultType.PROJECT) },
                        label = { Text("Projects") }
                    )
                }
                item {
                    FilterChip(
                        selected = uiState.selectedFilter == SearchResultType.NOTE,
                        onClick = { viewModel.selectFilter(SearchResultType.NOTE) },
                        label = { Text("Notes") }
                    )
                }
                item {
                    FilterChip(
                        selected = uiState.selectedFilter == SearchResultType.RESOURCE,
                        onClick = { viewModel.selectFilter(SearchResultType.RESOURCE) },
                        label = { Text("Resources") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Search Results List
            if (uiState.query.isBlank()) {
                EmptyStateView(message = "Type above to search across tasks, notes, projects & installed apps.")
            } else if (uiState.results.isEmpty()) {
                EmptyStateView(message = "No results found for '${uiState.query}'.")
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(uiState.results, key = { "${it.type}_${it.id}" }) { result ->
                        SearchResultCard(
                            result = result,
                            onClick = { onResultSelected(result) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SearchResultCard(
    result: SearchResult,
    onClick: () -> Unit
) {
    VectorCard(onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val icon = when (result.type) {
                SearchResultType.TASK -> Icons.Default.CheckCircle
                SearchResultType.PROJECT -> Icons.AutoMirrored.Filled.List
                SearchResultType.NOTE -> Icons.Default.Edit
                SearchResultType.RESOURCE -> Icons.Default.Info
                SearchResultType.APPLICATION -> Icons.Default.Home
            }
            Icon(
                imageVector = icon,
                contentDescription = result.type.name,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = result.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (result.subtitle.isNotBlank()) {
                    Text(
                        text = result.subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }

            Text(
                text = result.type.name,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
