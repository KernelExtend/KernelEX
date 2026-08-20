package Kernel.Extend

import android.app.Application
import Kernel.Extend.data.AppSettings

class KernelEXApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppSettings.getInstance(this)
    }
}
