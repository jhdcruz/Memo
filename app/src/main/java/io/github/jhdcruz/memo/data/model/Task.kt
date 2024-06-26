package io.github.jhdcruz.memo.data.model

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize

@Parcelize
data class Task(
    val id: String? = null,
    val priority: Int = 0,
    val dueDate: Timestamp? = null,
    val title: String = "",
    /**
     * This serves as a brief (one-liner) description of the task,
     * and is different from the body which contains
     * the detailed information about the task.
     */
    val description: String? = null,
    @field:JvmField
    val isCompleted: Boolean = false,
    val category: String? = null,
    val tags: List<String>? = null,
    /**
     * Stores URL links to files or images uploaded to storage/buckets
     */
    val attachments: Map<String, TaskAttachment>? = null,
    val created: Timestamp = Timestamp.now(),
    val updated: Timestamp? = null,
) : Parcelable

@Parcelize
data class TaskAttachment(
    val name: String = "",
    val path: String = "",
    val downloadUrl: String = "",
) : Parcelable

@Parcelize
data class TaskList(val tasks: List<Task>) : Parcelable
