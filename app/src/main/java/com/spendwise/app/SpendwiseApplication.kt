package com.spendwise.app

import android.app.Application
import com.spendwise.app.data.local.SpendwiseDatabase
import com.spendwise.app.data.repository.SpendwiseRepositoryImpl
import com.spendwise.app.domain.repository.SpendwiseRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class SpendwiseApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val database by lazy {
        SpendwiseDatabase.getDatabase(this, applicationScope)
    }

    val repository: SpendwiseRepository by lazy {
        SpendwiseRepositoryImpl(database.spendwiseDao())
    }

    override fun onCreate() {
        super.onCreate()
    }
}
