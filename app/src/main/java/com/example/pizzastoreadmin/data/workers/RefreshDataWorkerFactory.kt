package com.example.pizzastoreadmin.data.workers

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.example.pizzastoreadmin.data.localdb.PizzaDao
import com.example.pizzastoreadmin.data.remotedb.FirebaseService
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