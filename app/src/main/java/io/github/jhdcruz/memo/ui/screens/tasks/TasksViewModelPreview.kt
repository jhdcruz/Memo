package io.github.jhdcruz.memo.ui.screens.tasks

import android.content.Context
import android.net.Uri
import androidx.compose.ui.text.input.TextFieldValue
import com.google.firebase.Timestamp
import io.github.jhdcruz.memo.data.model.Task
import io.github.jhdcruz.memo.data.model.TaskAttachment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class TasksViewModelPreview : TasksViewModel() {
    override val taskId: Flow<String> = flowOf("Generic ID")
    override val taskTitle = flowOf("Generic Title")
    override val taskDescription = flowOf(TextFieldValue("Generic Description"))
    override val taskCategory = flowOf("Generic Category")
    override val taskTags = flowOf(listOf("Generic Tag"))
    override val taskAttachments: Flow<Map<String, TaskAttachment>?> = flowOf(null)
    override val taskDueDate: Flow<Timestamp?> = flowOf(null)
    override val taskSelectedDate = flowOf(0L)
    override val taskSelectedHour = flowOf(0)
    override val taskSelectedMinute = flowOf(0)
    override val taskPriority = flowOf(0)
    override val taskUpdated = flowOf(Timestamp.now())
    override val taskLocalAttachments = flowOf(listOf<Pair<String, Uri>>())

    override fun getTaskDueDate(millis: Long, hour: Int, minute: Int): Timestamp {
        return Timestamp.now()
    }

    override fun onTagsChange(tags: List<String>) {}

    override fun onCategoryChange(category: String) {}
    override fun onTaskIdChange(id: String) {}

    override fun onTaskTitleChange(title: String) {}

    override fun onTaskDescriptionChange(description: TextFieldValue) {}

    override fun onTaskCategoryChange(category: String) {}

    override fun onTaskTagsChange(tags: List<String>) {}
    override fun onTaskAttachmentsChange(attachments: Map<String, TaskAttachment>?) {
    }


    override fun onTaskLocalAttachmentsChange(attachments: List<Pair<String, Uri>>) {}

    override fun onTaskAttachmentPreview(
        context: Context,
        attachment: Map<String, String>,
    ) {
    }

    override fun onTaskAttachmentPreview(context: Context, attachment: Pair<String, Uri>) {}
    override fun removeTaskAttachment(
        taskId: String,
        filename: String,
        originalAttachments: Map<String, TaskAttachment>,
    ) {
    }

    override fun onTaskDueDateChange(date: Timestamp) {}

    override fun onTaskSelectedDateChange(date: Long) {}

    override fun onTaskSelectedHourChange(hour: Int) {}

    override fun onTaskSelectedMinuteChange(minute: Int) {}

    override fun onTaskPriorityChange(priority: Int) {}

    override fun onTaskUpdatedChange(updated: Timestamp) {}

    override fun onClearInput() {}
    override fun onTaskPreview(task: Task) {}
}
