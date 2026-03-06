package com.tonial.apicarros

import android.app.Application
import com.tonial.apicarros.database.DatabaseBuilder

class Application : Application() {

    override fun onCreate() {
        super.onCreate()
        init()
    }

    private fun init() {
        DatabaseBuilder.getInstance(this)
    }

}