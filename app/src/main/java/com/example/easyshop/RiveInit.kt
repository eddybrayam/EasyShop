package com.example.easyshop

import android.app.Application
import app.rive.runtime.kotlin.core.Rive

class RiveInit : Application() {
    override fun onCreate() {
        super.onCreate()
        Rive.init(this)
    }
}
