@file:OptIn(ExperimentalMaterial3Api::class)

package io.github.jhdcruz.memo.ui.tasks.details

import androidx.compose.foundation.Image
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import io.github.jhdcruz.memo.R
import io.github.jhdcruz.memo.ui.shared.TimePickerDialog
import io.github.jhdcruz.memo.ui.tasks.TasksViewModel

@Composable
fun DueDatePicker(
    tasksViewModel: TasksViewModel,
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    IconButton(
        onClick = {
            showDatePicker = true
        }) {
        Image(
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
            painter = painterResource(id = R.drawable.baseline_calendar_24),
            contentDescription = "Set task's due date"
        )
    }

    // Modal Dialogs
    if (showDatePicker) {
        TaskDatePickerDialog(
            tasksViewModel = tasksViewModel,
            onDismissRequest = {
                showDatePicker = false
            },
            onConfirmRequest = {
                showDatePicker = false
                showTimePicker = true
            }
        )
    }

    if (showTimePicker) {
        TaskTimePickerDialog(
            tasksViewModel = tasksViewModel,
            onDismissRequest = {
                showTimePicker = false
            }
        )
    }
}

/**
 * Modal dialog that shows a date picker
 * to be assigned at the tasks view model instance passed.
 */
@Composable
fun TaskDatePickerDialog(
    modifier: Modifier = Modifier,
    tasksViewModel: TasksViewModel,
    onDismissRequest: () -> Unit,
    onConfirmRequest: () -> Unit,
) {
    val datePickerState = rememberDatePickerState()

    DatePickerDialog(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = "Cancel")
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val date = datePickerState.selectedDateMillis

                if (date != null) {
                    tasksViewModel.onTaskSelectedDateChange(date)
                }

                onConfirmRequest()
            }) {
                Text(text = "Confirm")
            }
        }
    ) {
        DatePicker(
            state = datePickerState,
        )
    }
}

/**
 * Modal dialog that shows a time picker
 * to be assigned at the tasks view model instance passed.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun TaskTimePickerDialog(
    modifier: Modifier = Modifier,
    tasksViewModel: TasksViewModel,
    onDismissRequest: () -> Unit,
) {
    TimePickerDialog(
        modifier = modifier,
        onCancel = {
            onDismissRequest()
        },
        onConfirm = { timePickerState ->
            val hour = timePickerState.hour
            val minute = timePickerState.minute

            tasksViewModel.onTaskSelectedHourChange(hour)
            tasksViewModel.onTaskSelectedMinuteChange(minute)

            onDismissRequest()
        }
    )
}
