package com.example.pizzastoreadmin.data.workers

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkerParameters
import com.example.pizzastoreadmin.R
import com.example.pizzastoreadmin.data.localdb.PizzaDao
import com.example.pizzastoreadmin.data.remotedb.FirebaseService
import kotlinx.coroutines.delay

class LoadOrdersWorker(
    private val context: Context,
    workerParameters: WorkerParameters,
    private val firebaseService: FirebaseService,
    private val pizzaDao: PizzaDao
) : CoroutineWorker(context, workerParameters) {

    override suspend fun doWork(): Result {
        while (true) {
            val ordersFromRemoteDb = firebaseService.getListOrdersOneTime()
            val ordersFromLocalDb = pizzaDao.getOrdersNoFlow()
            val sizeListOrdersRemoteDb = ordersFromRemoteDb.size
            val sizeListOrdersLocalDb = ordersFromLocalDb?.size
            if (sizeListOrdersLocalDb != sizeListOrdersRemoteDb) {
                showNotification()
            }
            delay(60000)
        }
    }

    private fun showNotification() {
        val notificationManager = getNotificationManager()
        val notification = getNotification(notificationManager)
        notificationManager.notify(1, notification)
    }

    private fun getNotification(notificationManager: NotificationManager): Notification {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(notificationChannel)
        }
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Внимание!")
            .setContentText("Возможно новый заказ!")
            .setSmallIcon(R.drawable.ic_order)
            .setOnlyAlertOnce(true)
            .build()
    }

    private fun getNotificationManager() =
        context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

    companion object {

        const val NAME = "RefreshDataWorker"

        private const val CHANNEL_ID = "channel_id"
        private const val CHANNEL_NAME = "orders_notification"

        fun makeRequest() =
            OneTimeWorkRequest.Builder(
                LoadOrdersWorker::class.java
            ).build()

    }
}