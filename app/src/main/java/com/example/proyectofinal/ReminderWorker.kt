package com.example.proyectofinal

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.ListenableWorker.Result
import kotlinx.coroutines.runBlocking

class ReminderWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val reminderId = inputData.getInt("REMINDER_ID", -1)
        val title = inputData.getString("REMINDER_TITLE") ?: "Recordatorio"
        val description = inputData.getString("REMINDER_DESCRIPTION") ?: ""

        if (reminderId != -1) {
            showNotification(reminderId, title, description)
        }

        return Result.success()
    }

    private fun showNotification(reminderId: Int, title: String, reminderDescription: String) {
        val channelId = "reminders_channel"
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Recordatorios",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                enableLights(true)
                lightColor = ContextCompat.getColor(applicationContext, R.color.primary)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val database = AppDatabase.getDatabase(applicationContext)
        val noteId = runBlocking {
            val note = database.noteDao().getNoteByTitle(title)
            note?.id ?: -1
        }

        val intent = Intent(applicationContext, AddNoteActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            if (noteId != -1) {
                putExtra("NOTE_ID", noteId)
            }
        }
        
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, reminderId, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Si la descripción está vacía, ponemos un mensaje predeterminado para evitar espacios en blanco
        val bodyText = reminderDescription.ifBlank { "Tienes una tarea pendiente: $title" }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.logoapp)
            .setContentTitle(title) // Título de la tarea
            .setContentText(bodyText) // Nota de la tarea
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText(bodyText)) // Estilo para que se vea todo si es largo
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setColor(ContextCompat.getColor(applicationContext, R.color.primary))
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(reminderId, notification)
    }
}
