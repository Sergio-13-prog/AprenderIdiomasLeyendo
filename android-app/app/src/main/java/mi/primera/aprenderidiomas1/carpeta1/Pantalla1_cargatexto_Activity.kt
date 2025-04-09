package mi.primera.aprenderidiomas1.carpeta1

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import mi.primera.aprenderidiomas1.R
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class Pantalla1_cargatexto_Activity : AppCompatActivity() {

    private val client = OkHttpClient() // Cliente HTTP para descargas
    private lateinit var etInputText: EditText
    private var sourceLanguage = "en"  // Idioma de origen (por defecto es inglés)
    private var targetLanguage = "es"  // Idioma de destino (por defecto es español)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_pantalla1_cargatexto)

//        val btnSendText = findViewById<Button>(R.id.btnSendText)
        etInputText = findViewById(R.id.etInputText)
        val btnUploadText = findViewById<Button>(R.id.btnUploadText)
        val btnSendText = findViewById<Button>(R.id.btnSendText) // Asegúrate de que coincida con tu XML
        val btnSuggestText = findViewById<Button>(R.id.btnSuggestText)
        val btnRecommendedBooks = findViewById<Button>(R.id.btnRecommendedBooks)
        val btnSetSourceLanguage = findViewById<Button>(R.id.btnSetSourceLanguage)
        btnSetSourceLanguage.setOnClickListener {
            showLanguagePicker(true) // Mostrar el selector de idiomas para el idioma de origen
        }
        val btnSetTargetLanguage = findViewById<Button>(R.id.btnSetTargetLanguage)
        btnSetTargetLanguage.setOnClickListener {
            showLanguagePicker(false) // Mostrar el selector de idiomas para el idioma de destino
        }
        //val btnSendText
        // Botón "Subir desde archivo"
        btnUploadText.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "text/plain"
            startActivityForResult(intent, 200)
        }
//        // Suponiendo que ya tienes las variables sourceLanguage y targetLanguage
//        val tvSelectedLanguages = findViewById<TextView>(R.id.tvSelectedLanguages)
//
//    // Muestra los idiomas seleccionados
//        tvSelectedLanguages.text = "Idiomas seleccionados: $sourceLanguage -> $targetLanguage"

        // Botón "Sugerir texto" con niveles que descargan desde Internet
        btnSuggestText.setOnClickListener {
            val levels = arrayOf("Nivel 1", "Nivel 2", "Nivel 3")
            val builder = AlertDialog.Builder(this, R.style.AlertDialogTheme)
            builder.setTitle("Selecciona un nivel")
            builder.setItems(levels) { _, which ->
                val url = when (which) {
                    0 -> "https://es.wikipedia.org/wiki/Ajedrez" // Nivel 1
                    1 -> "https://es.wikipedia.org/api/rest_v1/page/summary/Ciencia"   // Nivel 2
                    2 -> "https://es.wikipedia.org/api/rest_v1/page/summary/Tecnolog%C3%ADa" // Nivel 3
                    else -> "https://es.wikipedia.org/api/rest_v1/page/summary/Ajedrez"
                }
                downloadTextFromInternet(url, levels[which])
            }
            builder.show()
        }
        btnSendText.setOnClickListener {
            val enteredText = etInputText.text.toString().trim() // Obtener el texto ingresado

            if (enteredText.isNotEmpty()) {
                // Verificar los idiomas seleccionados
                Log.d("Translation", "Source Language: $sourceLanguage, Target Language: $targetLanguage")

                val intent = Intent(this, SuggestionActivity::class.java).apply {
                    putExtra("text", enteredText)
                    putExtra("source", "Texto ingresado manualmente")
                    putExtra("sourceLanguage", sourceLanguage)
                    putExtra("targetLanguage", targetLanguage)
                }
                startActivity(intent)
            } else {
                Toast.makeText(this, "Por favor, ingresa un texto", Toast.LENGTH_SHORT).show()
            }
        }
        // Botón "Enviar texto a otra pantalla"

//        btnSendText.setOnClickListener {
//            val enteredText = etInputText.text.toString()
//            if (enteredText.isNotEmpty()) {
//                val intent = Intent(this, SuggestionActivity::class.java)
//                intent.putExtra("text", enteredText)
//                intent.putExtra("source", "Texto ingresado manualmente")
//                startActivity(intent)
//            } else {
//                Toast.makeText(this, "Por favor, ingresa un texto", Toast.LENGTH_SHORT).show()
//            }
//        }

        // Navegar a pantalla de libros recomendados
        btnRecommendedBooks.setOnClickListener {
            val intent = Intent(this, RecommendedBooksActivity::class.java)
            startActivity(intent)
        }
    }
    private fun showLanguagePicker(isSource: Boolean) {
        val languages = arrayOf("en", "es", "fr", "de") // Idiomas disponibles
        val currentLanguage = if (isSource) sourceLanguage else targetLanguage

        val currentLanguageIndex = languages.indexOf(currentLanguage)

        AlertDialog.Builder(this)
            .setTitle("Seleccionar idioma")
            .setSingleChoiceItems(languages, currentLanguageIndex) { dialog, which ->
                if (isSource) {
                    sourceLanguage = languages[which]
                } else {
                    targetLanguage = languages[which]
                }
                dialog.dismiss()
            }
            .show()
    }
    // Método para descargar texto desde Internet y enviarlo a SuggestionActivity
    private fun downloadTextFromInternet(url: String, level: String) {
        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@Pantalla1_cargatexto_Activity, "Error al descargar el texto: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }


override fun onResponse(call: Call, response: Response) {
    response.body?.string()?.let { responseBody ->
        try {
            // Analizar la respuesta JSON
            val jsonObject = JSONObject(responseBody)
            val extractedText = jsonObject.optString("extract", "Texto no disponible")

            runOnUiThread {
                // Log para verificar el texto descargado
                android.util.Log.d("DownloadText", "Texto descargado: $extractedText")

                val intent = Intent(this@Pantalla1_cargatexto_Activity, SuggestionActivity::class.java)
                intent.putExtra("text", extractedText)
                intent.putExtra("source", "Texto sugerido - $level")
                intent.putExtra("targetLanguage", "en") // Cambia según la selección
                intent.putExtra("sourceLanguage", "es")
                startActivity(intent)
            }
        } catch (e: Exception) {
            runOnUiThread {
                Toast.makeText(this@Pantalla1_cargatexto_Activity, "Error al procesar la respuesta: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    } ?: runOnUiThread {
        Toast.makeText(this@Pantalla1_cargatexto_Activity, "Respuesta vacía del servidor", Toast.LENGTH_SHORT).show()
    }
}
        })
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 200 && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                try {
                    val inputStream = contentResolver.openInputStream(uri)
                    val content = inputStream?.bufferedReader().use { it?.readText() }
                    inputStream?.close()

                    val intent = Intent(this, SuggestionActivity::class.java).apply {
                        putExtra("text", content)
                        putExtra("source", "Archivo seleccionado")
                        intent.putExtra("targetLanguage", "en") // Cambia según la selección
                        intent.putExtra("sourceLanguage", "es")
                    }
                    startActivity(intent)

                } catch (e: Exception) {
                    Toast.makeText(this, "Error al leer el archivo", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
