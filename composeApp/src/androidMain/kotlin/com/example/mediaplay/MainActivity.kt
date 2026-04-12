package com.example.mediaplay

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.startapp.sdk.adsbase.StartAppAd
import com.startapp.sdk.adsbase.StartAppSDK
import com.example.mediaplay.App
import com.example.mediaplay.database.initDatabaseContext
import java.lang.ref.WeakReference

class MainActivity : ComponentActivity() {
    
    companion object {
        var activityRef: WeakReference<ComponentActivity>? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        activityRef = WeakReference(this)
        initDatabaseContext(applicationContext)

        // Inicializa Start.io com o App ID real do usuário
        StartAppSDK.init(this, "203866853", true)
        
        // Modo de teste desativado para distribuição
        StartAppSDK.setTestAdsEnabled(false) 
        
        setContent {
            App()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (activityRef?.get() == this) {
            activityRef = null
        }
    }
}
