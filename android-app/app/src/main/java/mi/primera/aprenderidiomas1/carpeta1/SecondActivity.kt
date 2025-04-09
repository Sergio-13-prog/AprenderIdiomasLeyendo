package mi.primera.aprenderidiomas1.carpeta1
import mi.primera.aprenderidiomas1.R

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView

class SecondActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)

        val textView = findViewById<TextView>(R.id.tvDisplayText)
        val receivedText = intent.getStringExtra("text") ?: "Sin texto recibido"

        textView.text = receivedText
    }
}
