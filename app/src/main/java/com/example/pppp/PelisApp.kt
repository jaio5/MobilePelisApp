package com.example.pppp

import android.app.Application
import androidx.room.Room
import com.example.pppp.data.local.AppDatabase

class PelisApp : Application() {
    companion object {
        lateinit var database: AppDatabase
            private set
    }

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "pelisapp_db"
        ).build()
    }
}
