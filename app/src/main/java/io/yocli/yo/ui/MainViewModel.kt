package io.yocli.yo.ui

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel

private val KEY_TOKEN = "token"

class MainViewModel(app: Application) : AndroidViewModel(app) {

    private var prefs: SharedPreferences? = app.applicationContext.getSharedPreferences("yocli", Context.MODE_PRIVATE)

    fun runIfPaired(block: () -> Unit) {
        val prefs = prefs!!
        val token = prefs.getString(KEY_TOKEN, null)
        if (token != null) {
            block()
        }
    }

    fun persistPairing(deviceToken : String){
        prefs?.edit {
            putString(KEY_TOKEN, deviceToken)
        }
    }

    fun rePairDevice() {
        prefs?.edit { remove(KEY_TOKEN) }
    }

    override fun onCleared() {
        super.onCleared()
        prefs = null
    }

}