package com.exemplo.oficina

import android.app.Application
import com.exemplo.oficina.di.AppContainer
import com.exemplo.oficina.di.DefaultAppContainer

class OficinaApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}
