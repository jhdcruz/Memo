package io.github.jhdcruz.memo.ui.tasks

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.speech.RecognizerIntent
import androidx.compose.ui.text.input.TextFieldValue
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jhdcruz.memo.data.model.Task
import io.github.jhdcruz.memo.data.task.AttachmentsRepository
import io.github.jhdcruz.memo.data.task.TasksRepository
import io.github.jhdcruz.memo.domain.createTimestamp
import io.github.jhdcruz.memo.domain.response.FirestoreResponseUseCase
import io.github.jhdcruz.memo.domain.toTimestamp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.LocalDateTime
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class TasksViewModelImpl @Inject constructor(
    private val tasksRepository: TasksRepository,
    private val attachmentsRepository: AttachmentsRepository,
) : TasksViewModel() {
    private val _query = MutableStateFlow("")
    override val query: Flow<String> = _query

    private val _taskList = MutableStateFlow<List<Task>>(emptyList())
    override val taskList: Flow<List<Task>> = _taskList

    // Populating tasks
    private val _taskTitle = MutableStateFlow("")
    override val taskTitle: Flow<String> = _taskTitle

    private val _taskDescription = MutableStateFlow(TextFieldValue(""))
    override val taskDescription: Flow<TextFieldValue> = _taskDescription

    private val _taskCategory = MutableStateFlow("")
    override val taskCategory: Flow<String> = _taskCategory

    private val _taskTags = MutableStateFlow<List<String>>(emptyList())
    override val taskTags: Flow<List<String>> = _taskTags

    private val _taskAttachments = MutableStateFlow<List<Map<String, String>>?>(emptyList())
    override val taskAttachments: Flow<List<Map<String, String>>?> = _taskAttachments

    private val _taskSelectedDate = MutableStateFlow<Long?>(null)
    override val taskSelectedDate: Flow<Long?> = _taskSelectedDate

    private val _taskSelectedHour = MutableStateFlow<Int?>(null)
    override val taskSelectedHour: Flow<Int?> = _taskSelectedHour

    private val _taskSelectedMinute = MutableStateFlow<Int?>(null)
    override val taskSelectedMinute: Flow<Int?> = _taskSelectedMinute


    private val _taskPriority = MutableStateFlow(0)
    override val taskPriority: Flow<Int> = _taskPriority

    private val _taskUpdated = MutableStateFlow(LocalDateTime.now().toTimestamp())
    override val taskUpdated: Flow<Timestamp> = _taskUpdated

    private val _taskLocalAttachments = MutableStateFlow<List<Pair<String, Uri>>>(emptyList())
    override val taskLocalAttachments: Flow<List<Pair<String, Uri>>> = _taskLocalAttachments

    override fun onVoiceSearch(): Intent {
        return Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).also {
            it.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH
            )
            it.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            it.putExtra(RecognizerIntent.EXTRA_PROMPT, "What tasks to search for?")
        }
    }

    override suspend fun onSearch() {
        val taskList = tasksRepository.onSearch(query.toString())
        onTaskListChange(taskList)
    }

    override suspend fun onTaskAdd(task: Task): FirestoreResponseUseCase {
        return tasksRepository.onTaskAdd(task)
    }

    override suspend fun onTaskUpdate(uid: String, task: Task) {
        tasksRepository.onTaskUpdate(uid, task)
    }

    override suspend fun onTaskDelete(uid: String) {
        tasksRepository.onTaskDelete(uid)
    }

    override suspend fun onTaskCompleted(uid: String) {
        tasksRepository.onTaskCompleted(uid)
    }

    override suspend fun onCategoryAdd(category: String) {
        tasksRepository.onCategoryAdd(category)
    }

    override suspend fun onCategoryUpdate(category: String, newCategory: String) {
        tasksRepository.onCategoryUpdate(category, newCategory)
    }

    override suspend fun onCategoriesDelete(categories: List<String>) {
        tasksRepository.onCategoriesDelete(categories)
    }

    override suspend fun onTagAdd(tag: String) {
        tasksRepository.onTagAdd(tag)
    }

    override suspend fun onTagUpdate(tag: String, newTag: String) {
        tasksRepository.onTagUpdate(tag, newTag)
    }

    override suspend fun onTagsDelete(tags: List<String>) {
        tasksRepository.onTagsDelete(tags)
    }

    override suspend fun onAttachmentsUpload(
        id: String,
        attachments: List<Pair<String, Uri>>,
    ): FirestoreResponseUseCase {
        return attachmentsRepository.onAttachmentsUpload(id, attachments)
    }

    override suspend fun onAttachmentDelete(id: String, path: String): FirestoreResponseUseCase {
        return attachmentsRepository.onAttachmentDelete(id, path)
    }

    override suspend fun onAttachmentDownload(path: String): FirestoreResponseUseCase {
        return attachmentsRepository.onAttachmentDownload(path)
    }

    override suspend fun onGetCategories(): List<String> {
        return tasksRepository.onGetCategories()
    }

    override suspend fun onGetTags(): List<String> {
        return tasksRepository.onGetTags()
    }

    override fun onQueryChange(query: String) {
        _query.value = query
    }

    override fun onTaskListChange(taskList: List<Task>) {
        _taskList.value = taskList
    }

    override fun onTagsChange(tags: List<String>) {
        _taskTags.value = tags
    }

    override fun onCategoryChange(category: String) {
        _taskCategory.value = category
    }

    override fun onTaskTitleChange(title: String) {
        _taskTitle.value = title
    }

    override fun onTaskDescriptionChange(description: TextFieldValue) {
        _taskDescription.value = description
    }

    override fun onTaskCategoryChange(category: String) {
        _taskCategory.value = category
    }

    override fun onTaskTagsChange(tags: List<String>) {
        _taskTags.value = tags
    }

    override fun onTaskAttachmentsChange(attachments: List<Map<String, String>>?) {
        _taskAttachments.value = attachments
    }

    override fun onTaskLocalAttachmentsChange(attachments: List<Pair<String, Uri>>) {
        _taskLocalAttachments.value = attachments
    }

    override suspend fun onTaskAttachmentPreview(
        context: Context,
        attachment: Map<String, String>,
    ) {
        val intent = Intent(Intent.ACTION_VIEW)
        val fileProvider = context.contentResolver

        intent.setDataAndType(
            Uri.parse(attachment.values.last()),
            fileProvider.getType(Uri.parse(attachment.values.last()))
        ).apply {
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }

        context.startActivity(intent)
    }

    override suspend fun onTaskAttachmentPreview(context: Context, attachment: Pair<String, Uri>) {
        val intent = Intent(Intent.ACTION_VIEW)
        val fileProvider = context.contentResolver

        intent.setDataAndType(
            attachment.second,
            fileProvider.getType(attachment.second)
        ).apply {
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }

        context.startActivity(intent)
    }

    override fun removeTaskAttachment(
        attachment: Map<String, String>,
        originalAttachments: List<Map<String, String>>,
    ) {
        val updatedAttachments = originalAttachments.toMutableList()
        updatedAttachments.remove(attachment)

        _taskAttachments.value = updatedAttachments
    }

    override fun onTaskSelectedDateChange(date: Long) {
        _taskSelectedDate.value = date
    }

    override fun onTaskSelectedHourChange(hour: Int) {
        _taskSelectedHour.value = hour
    }

    override fun onTaskSelectedMinuteChange(minute: Int) {
        _taskSelectedMinute.value = minute
    }

    override fun onTaskPriorityChange(priority: Int) {
        _taskPriority.value = priority
    }

    override fun onTaskUpdatedChange(updated: Timestamp) {
        _taskUpdated.value = updated
    }

    override fun getTaskDueDate(millis: Long, hour: Int, minute: Int): Timestamp {
        return createTimestamp(millis, hour, minute)
    }
}
