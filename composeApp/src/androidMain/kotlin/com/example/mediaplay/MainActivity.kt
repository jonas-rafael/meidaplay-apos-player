package com.example.mediaplay

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.mediaplay.App
import com.example.mediaplay.database.initDatabaseContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Inicializa o contexto do banco de dados Room
        initDatabaseContext(applicationContext)
        
        setContent {
            App()
        }
    }
}
