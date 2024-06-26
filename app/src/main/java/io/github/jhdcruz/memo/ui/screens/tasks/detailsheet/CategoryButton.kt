package io.github.jhdcruz.memo.ui.screens.tasks.detailsheet

import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import io.github.jhdcruz.memo.R
import io.github.jhdcruz.memo.ui.screens.container.ContainerViewModel
import io.github.jhdcruz.memo.ui.components.EmptyState
import io.github.jhdcruz.memo.ui.components.LoadingState
import io.github.jhdcruz.memo.ui.components.PickerDialog
import io.github.jhdcruz.memo.ui.screens.tasks.TasksViewModel
import kotlinx.coroutines.launch

@Composable
fun CategoryButton(
    modifier: Modifier = Modifier,
    containerViewModel: ContainerViewModel,
    tasksViewModel: TasksViewModel,
) {
    val scope = rememberCoroutineScope()

    val categories = containerViewModel.categories.collectAsState(initial = emptyList())
    val taskCategory = tasksViewModel.taskCategory.collectAsState(initial = "")

    var showCategoryDialog by remember { mutableStateOf(false) }

    val buttonIcon = if (taskCategory.value.isEmpty()) {
        R.drawable.baseline_folder_24
    } else {
        R.drawable.baseline_folder_filled_24
    }

    IconButton(
        modifier = modifier,
        onClick = {
            showCategoryDialog = true

            scope.launch {
                containerViewModel.onGetCategories()
            }
        }
    ) {
        Image(
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
            painter = painterResource(id = buttonIcon),
            contentDescription = "Set task's category"
        )
    }

    if (showCategoryDialog) {
        var selectedCategory by remember { mutableStateOf(taskCategory.value) }

        CategorySelectionDialog(
            containerViewModel = containerViewModel,
            onDismissRequest = { showCategoryDialog = false },
            onConfirm = {
                scope.launch {
                    tasksViewModel.onCategoryChange(selectedCategory)
                    showCategoryDialog = false
                }
            },
            categories = categories.value,
            selectedCategory = selectedCategory,
            onCategorySelected = { category ->
                scope.launch {
                    selectedCategory = category
                }
            }
        )
    }
}

@Composable
fun CategorySelectionDialog(
    containerViewModel: ContainerViewModel,
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
) {
    PickerDialog(
        title = { Text(text = "Assign category for this task") },
        onDismissRequest = onDismissRequest,
        buttons = {
            TextButton(onClick = onDismissRequest) {
                Text(text = "Cancel")
            }

            TextButton(onClick = onConfirm) {
                Text(text = "Confirm")
            }
        }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            CategoryInputField(containerViewModel = containerViewModel)
            HorizontalDivider(modifier = Modifier.padding(top = 12.dp, bottom = 4.dp))
            CategoryList(
                categories = categories,
                selectedCategory = selectedCategory,
                onCategorySelected = onCategorySelected
            )
        }
    }
}

@Composable
private fun CategoryInputField(
    containerViewModel: ContainerViewModel,
) {
    val scope = rememberCoroutineScope()
    val categories = containerViewModel.categories.collectAsState(initial = emptyList())

    var newCategory by remember { mutableStateOf("") }

    OutlinedTextField(
        modifier = Modifier.height(64.dp),
        value = newCategory,
        onValueChange = {
            // limit length
            if (it.length <= 20) {
                newCategory = it
            }
        },
        placeholder = { Text(text = "Create new tag") },
        singleLine = true,
        trailingIcon = {
            FilledIconButton(
                modifier = Modifier.padding(horizontal = 6.dp),
                onClick = {
                    scope.launch {
                        containerViewModel.onCategoryAdd(newCategory)

                        val appendedTag = listOf(newCategory) + categories.value
                        // append manually to avoid calling onGetTags again
                        containerViewModel.onLocalCategoryChange(appendedTag)
                        newCategory = ""
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Create new category"
                )
            }
        }
    )
}

@Composable
private fun CategoryList(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
) {
    when {
        categories.contains("loading") -> LoadingState()
        categories.isEmpty() -> EmptyState("No categories created yet.")
        else -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 320.dp)
            ) {
                Column(
                    modifier = Modifier
                        .verticalScroll(ScrollState(0))
                        .selectableGroup()
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    categories.forEach { category ->
                        CategoryRow(
                            category = category,
                            isSelected = selectedCategory == category,
                            onCategorySelected = onCategorySelected
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryRow(
    category: String,
    isSelected: Boolean,
    onCategorySelected: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .selectable(
                selected = isSelected,
                onClick = { onCategorySelected(category) }
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = null
        )
        Text(
            text = category,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}
