package io.github.jhdcruz.memo.data.task

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.SetOptions
import io.github.jhdcruz.memo.data.model.Task
import io.github.jhdcruz.memo.domain.createTimestamp
import io.github.jhdcruz.memo.domain.response.FirestoreResponseUseCase
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class TasksRepositoryImpl
    @Inject
    constructor(
        private val firestore: FirebaseFirestore,
        private val auth: FirebaseAuth,
    ) : TasksRepository {
        override suspend fun onGetTasks(): List<Task> {
            val userUid = auth.currentUser?.uid ?: throw IllegalStateException("User not signed in")

            return try {
                // get tasks from Firestore nested collection located in 'users/uid/tasks'
                firestore.collection("users").document(userUid).collection("tasks")
                    .whereEqualTo("isCompleted", false)
                    .get()
                    .await()
                    .toObjects(Task::class.java)
                    .sortedByDescending { it.priority }
            } catch (e: FirebaseFirestoreException) {
                Log.e("TasksRepository", "Error querying tasks", e)
                emptyList()
            }
        }

        override suspend fun onGetTask(id: String): Task {
            val userUid = auth.currentUser?.uid ?: throw IllegalStateException("User not signed in")

            return try {
                // get task from Firestore nested collection located in 'users/uid/tasks'
                firestore.collection("users").document(userUid).collection("tasks")
                    .document(id)
                    .get()
                    .await()
                    .toObject(Task::class.java)
                    ?: throw IllegalStateException("Task not found")
            } catch (e: FirebaseFirestoreException) {
                Log.e("TasksRepository", "Error querying task $id", e)
                throw e
            }
        }

        override suspend fun onSearch(query: String): List<Task> {
            val userUid = auth.currentUser?.uid ?: throw IllegalStateException("User not signed in")

            return try {
                // search tasks in Firestore nested collection located in 'users/uid/tasks'
                val result =
                    firestore.collection("users").document(userUid).collection("tasks")
                        .whereEqualTo("title", query)
                        .get()
                        .await()
                        .toObjects(Task::class.java)

                Log.i("TasksRepository", "Tasks search yields ${result.size} results")
                return result
            } catch (e: FirebaseFirestoreException) {
                Log.e("TasksRepository", "Error searching tasks", e)
                emptyList()
            }
        }

        override suspend fun onTaskAdd(task: Task): FirestoreResponseUseCase {
            val userUid = auth.currentUser?.uid ?: throw IllegalStateException("User not signed in")

            return try {
                // save task to Firestore nested collection located in 'users/uid/tasks' and get id back
                val taskId =
                    firestore.collection("users").document(userUid).collection("tasks")
                        .add(task)
                        .await()
                        .id

                // update task with id
                firestore.collection("users").document(userUid).collection("tasks")
                    .document(taskId)
                    .update("id", taskId)
                    .await()

                FirestoreResponseUseCase.Success(taskId)
            } catch (e: Exception) {
                Log.e("TasksRepository", "Error adding new task", e)
                FirestoreResponseUseCase.Error(e)
            }
        }

        override suspend fun onTaskUpdate(
            id: String,
            task: Task,
        ): FirestoreResponseUseCase {
            val userUid = auth.currentUser?.uid ?: throw IllegalStateException("User not signed in")

            return try {
                // update task in Firestore nested collection located in 'users/uid/tasks'
                firestore.collection("users").document(userUid).collection("tasks")
                    .document(id)
                    .set(task, SetOptions.merge())
                    .await()

                FirestoreResponseUseCase.Success("Task updated!")
            } catch (e: Exception) {
                Log.e("TasksRepository", "Error updating task", e)
                FirestoreResponseUseCase.Error(e)
            }
        }

        override suspend fun onTaskDelete(id: String): FirestoreResponseUseCase {
            val userUid = auth.currentUser?.uid ?: throw IllegalStateException("User not signed in")

            return try {
                // delete task in Firestore nested collection located in 'users/uid/tasks'
                firestore.collection("users").document(userUid).collection("tasks")
                    .document(id)
                    .delete()
                    .await()

                FirestoreResponseUseCase.Success("Task deleted!")
            } catch (e: Exception) {
                Log.e("TasksRepository", "Error deleting task $id", e)
                FirestoreResponseUseCase.Error(e)
            }
        }

        override suspend fun onTaskCompleted(id: String): FirestoreResponseUseCase {
            val userUid = auth.currentUser?.uid ?: throw IllegalStateException("User not signed in")

            return try {
                // update task in Firestore nested collection located in 'users/uid/tasks'
                firestore.collection("users").document(userUid).collection("tasks")
                    .document(id)
                    .update("isCompleted", true)
                    .await()

                FirestoreResponseUseCase.Success("Task completed!")
            } catch (e: Exception) {
                FirestoreResponseUseCase.Error(e)
            }
        }

        override suspend fun onCategoryAdd(category: String): FirestoreResponseUseCase {
            val userUid = auth.currentUser?.uid ?: throw IllegalStateException("User not signed in")

            return try {
                val originalCategories: List<String> = onGetCategories()
                val updatedCategories = originalCategories.plus(category)

                // append category to 'users/uid/tasksMetadata/labels' in categories field
                firestore.collection("users").document(userUid).collection("tasksMetadata")
                    .document("labels")
                    .update("categories", updatedCategories)
                    .await()

                FirestoreResponseUseCase.Success("New category added!")
            } catch (e: Exception) {
                Log.e("TasksRepository", "Error adding new category", e)
                FirestoreResponseUseCase.Error(e)
            }
        }

        override suspend fun onCategoryUpdate(
            category: String,
            newCategory: String,
        ): FirestoreResponseUseCase {
            val userUid = auth.currentUser?.uid ?: throw IllegalStateException("User not signed in")

            return try {
                val originalCategories: List<String> = onGetCategories()

                // replace matching categories with newCategory
                val updatedCategories =
                    originalCategories.map { if (it == category) newCategory else it }

                // update category in 'users/uid/tasksMetadata/labels' in categories field
                firestore.collection("users").document(userUid).collection("tasksMetadata")
                    .document("labels")
                    .update("categories", updatedCategories)
                    .await()

                FirestoreResponseUseCase.Success("Category updated!")
            } catch (e: Exception) {
                Log.e("TasksRepository", "Error updating category", e)
                FirestoreResponseUseCase.Error(e)
            }
        }

        override suspend fun onCategoriesDelete(
            categories: List<String>,
        ): FirestoreResponseUseCase {
            val uid = auth.currentUser?.uid ?: throw IllegalStateException("User not signed in")

            return try {
                val originalCategories: List<String> = onGetCategories()
                val updatedCategories = originalCategories.minus(categories.toSet())

                // delete categories from Firestore nested collection located in 'users/uid/tasksMetadata/labels'
                firestore.collection("users").document(uid).collection("tasksMetadata")
                    .document("labels")
                    .update("categories", updatedCategories)
                    .await()

                FirestoreResponseUseCase.Success("Categories deleted!")
            } catch (e: Exception) {
                Log.e("TasksRepository", "Error deleting categories", e)
                FirestoreResponseUseCase.Error(e)
            }
        }

        override suspend fun onTagAdd(tag: String): FirestoreResponseUseCase {
            val userUid = auth.currentUser?.uid ?: throw IllegalStateException("User not signed in")

            return try {
                val originalTags: List<String> = onGetTags()

                if (originalTags.isEmpty()) {
                    firestore.collection("users").document(userUid).collection("tasksMetadata")
                        .document("labels")
                        .set(mapOf("tags" to listOf(tag)), SetOptions.merge())
                        .await()
                } else {
                    val updatedTags = originalTags.plus(tag)
                    firestore.collection("users").document(userUid).collection("tasksMetadata")
                        .document("labels")
                        .update("tags", updatedTags)
                        .await()
                }

                FirestoreResponseUseCase.Success("New tag added!")
            } catch (e: Exception) {
                Log.e("TasksRepository", "Error adding new tag", e)
                FirestoreResponseUseCase.Error(e)
            }
        }

        override suspend fun onTagUpdate(
            tag: String,
            newTag: String,
        ): FirestoreResponseUseCase {
            val userUid = auth.currentUser?.uid ?: throw IllegalStateException("User not signed in")

            return try {
                val originalTags: List<String> = onGetTags()

                // replace matching tags with newTag
                val updatedTags = originalTags.map { if (it == tag) newTag else it }

                // update tag in 'users/uid/tasksMetadata/labels' in tags field
                firestore.collection("users").document(userUid).collection("tasksMetadata")
                    .document("labels")
                    .update("tags", updatedTags)
                    .await()

                FirestoreResponseUseCase.Success("Tag updated!")
            } catch (e: Exception) {
                Log.e("TasksRepository", "Error updating tag", e)
                FirestoreResponseUseCase.Error(e)
            }
        }

        override suspend fun onTagsDelete(tags: List<String>): FirestoreResponseUseCase {
            TODO("Not yet implemented")
        }

        @Suppress("UNCHECKED_CAST")
        override suspend fun onGetCategories(): List<String> {
            val uid = auth.currentUser?.uid ?: throw IllegalStateException("User not signed in")

            return try {
                // get categories from users/uid/tasksMetadata/labels stored in an array in categories field
                firestore.collection("users").document(uid).collection("tasksMetadata")
                    .document("labels")
                    .get()
                    .await()
                    .data
                    ?.get("categories") as? List<String>
                    ?: emptyList()
            } catch (e: FirebaseFirestoreException) {
                Log.e("TasksRepository", "Error querying categories", e)
                emptyList()
            }
        }

        @Suppress("UNCHECKED_CAST")
        override suspend fun onGetTags(): List<String> {
            val uid = auth.currentUser?.uid ?: throw IllegalStateException("User not signed in")

            return try {
                // get tags from users/uid/tasksMetadata/labels stored in an array in tags field
                firestore.collection("users").document(uid).collection("tasksMetadata")
                    .document("labels")
                    .get()
                    .await()
                    .data
                    ?.get("tags") as? List<String>
                    ?: emptyList()
            } catch (e: FirebaseFirestoreException) {
                Log.e("TasksRepository", "Error querying tags", e)
                emptyList()
            }
        }

        override suspend fun onFilterCategory(category: String): List<Task> {
            val userUid = auth.currentUser?.uid ?: throw IllegalStateException("User not signed in")

            return try {
                // filter tasks by category in Firestore nested collection located in 'users/uid/tasks'
                firestore.collection("users").document(userUid).collection("tasks")
                    .whereEqualTo("category", category)
                    .get()
                    .await()
                    .toObjects(Task::class.java)
                    .sortedByDescending { it.priority }
            } catch (e: FirebaseFirestoreException) {
                Log.e("TasksRepository", "Error filtering tasks by category", e)
                emptyList()
            }
        }

        override suspend fun onFilterTag(tag: String): List<Task> {
            val userUid = auth.currentUser?.uid ?: throw IllegalStateException("User not signed in")

            return try {
                // filter tasks by tag in Firestore nested collection located in 'users/uid/tasks'
                firestore.collection("users").document(userUid).collection("tasks")
                    .whereArrayContains("tags", tag)
                    .get()
                    .await()
                    .toObjects(Task::class.java)
                    .sortedByDescending { it.priority }
            } catch (e: FirebaseFirestoreException) {
                Log.e("TasksRepository", "Error filtering tasks by tag", e)
                emptyList()
            }
        }

        override suspend fun onFiterInbox(): List<Task> {
            // get all tasks without category
            val uid = auth.currentUser?.uid ?: throw IllegalStateException("User not signed in")

            return try {
                firestore.collection("users").document(uid).collection("tasks")
                    .whereEqualTo("category", null)
                    .get()
                    .await()
                    .toObjects(Task::class.java)
                    .sortedByDescending { it.priority }
            } catch (e: FirebaseFirestoreException) {
                Log.e("TasksRepository", "Error filtering tasks by inbox", e)
                emptyList()
            }
        }

        override suspend fun onFilterDueWeek(): List<Task> {
            // get tasks with dueDate within 7 days from now
            val uid = auth.currentUser?.uid ?: throw IllegalStateException("User not signed in")

            return try {
                val now = System.currentTimeMillis()
                val week = now + (7 * 24 * 60 * 60 * 1000)

                val nowTimestamp = createTimestamp(now)
                val weekTimestamp = createTimestamp(week)

                firestore.collection("users").document(uid).collection("tasks")
                    .whereEqualTo("isCompleted", false)
                    .whereGreaterThanOrEqualTo("dueDate", nowTimestamp)
                    .whereLessThanOrEqualTo("dueDate", weekTimestamp)
                    .get()
                    .await()
                    .toObjects(Task::class.java)
                    .sortedByDescending { it.priority }
            } catch (e: FirebaseFirestoreException) {
                Log.e("TasksRepository", "Error filtering tasks by due week", e)
                emptyList()
            }
        }
    }
