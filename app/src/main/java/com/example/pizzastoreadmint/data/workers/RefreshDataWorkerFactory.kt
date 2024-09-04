package com.example.pizzastoreadmint.data.workers

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.example.pizzastoreadmint.data.localdb.PizzaDao
import com.example.pizzastoreadmint.data.remotedb.FirebaseService
import javax.inject.Inject

class RefreshDataWorkerFactory @Inject constructor(
    private val firebaseService: FirebaseService,
    private val pizzaDao: PizzaDao
): WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker {
        return LoadOrdersWorker(
            appContext,
            workerParameters,
            firebaseService,
            pizzaDao
        )
    }
}