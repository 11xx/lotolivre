package org.x11xx.loterias

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import org.x11xx.loterias.ui.LotoLivreApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as LotoLivreApplication).container
        setContent {
            LotoLivreApp(container)
        }
    }
}
