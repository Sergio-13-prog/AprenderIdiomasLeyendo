package mi.primera.aprenderidiomas1.carpeta1

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*
import mi.primera.aprenderidiomas1.R
import network.ApiClient
import network.ProcessRequest
import network.ProcessResponse
import org.json.JSONArray
import retrofit2.Response
import mi.primera.aprenderidiomas1.carpeta1.SavedWordsActivity

class SuggestionActivity : AppCompatActivity() {

    private var lastClickTime: Long = 0
    private var clickCount = 0
    private var lastTouchedWord: String? = null
    companion object {
        val savedWords = mutableListOf<String>()  // Lista compartida para almacenar palabras
    }

    // Declarar las variables globales
    private lateinit var translationText: TextView
    private lateinit var wordDetails: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_suggestion)
        supportActionBar?.hide()

        val tvSuggestedText = findViewById<TextView>(R.id.tvSuggestedText)

        val text = intent.getStringExtra("text") ?: "No se recibió texto"
        val source_lang = intent.getStringExtra("sourceLanguage") ?: "en"
        val target_lang = intent.getStringExtra("targetLanguage") ?: "es"
        // Log para verificar si se recibieron los idiomas correctos
        Log.d("SuggestionActivity", "Recibido Source Language: $source_lang, Target Language: $target_lang")

        tvSuggestedText.text = text
        // Encuentra el TextView y muestra los idiomas seleccionados
        val tvLanguagesInSuggestion = findViewById<TextView>(R.id.tvLanguagesInSuggestion)
        tvLanguagesInSuggestion.text = "Idiomas seleccionados: $source_lang -> $target_lang"
        // Configurar el listener para traducir solo al tocar una palabra
        tvSuggestedText.setOnTouchListener { view, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val touchedWord = getTouchedWord(tvSuggestedText, event)
                if (touchedWord != null) {
                    showPopup(view, touchedWord, event.rawX.toInt(), event.rawY.toInt(), source_lang, target_lang)
                }
            }
            true
        }
        // Botón para acceder a la lista de palabras guardadas
        findViewById<Button>(R.id.btnSavedWords).setOnClickListener {
            startActivity(Intent(this, SavedWordsActivity::class.java))
        }

    }

    private fun showPopup(view: View, word: String, x: Int, y: Int, sourceLanguage: String, targetLanguage: String) {
        val inflater = LayoutInflater.from(this)
        val popupView = inflater.inflate(R.layout.popup_translation, null)

        val popupWindow = PopupWindow(
            popupView,
            600,  // Ancho del popup (ajústalo según tus necesidades)
            LinearLayout.LayoutParams.WRAP_CONTENT,
            true
        )

        popupWindow.setBackgroundDrawable(ColorDrawable(Color.BLUE))

        // Inicializar las vistas del popup
        translationText = popupView.findViewById(R.id.translationText)
        wordDetails = popupView.findViewById(R.id.wordDetails)
        val saveButton = popupView.findViewById<Button>(R.id.btnSaveButton) // Nuevo botón

        translationText.text = "Cargando..."
        wordDetails.text = ""

        popupWindow.showAtLocation(view, 0, x, y + 100)

        // Llamar a la traducción solo al tocar
        CoroutineScope(Dispatchers.IO).launch {
            val apiResult = processText(word, sourceLanguage, targetLanguage)
            withContext(Dispatchers.Main) {
                if (apiResult.translated_text.contains("Error")) {
                    translationText.setTextColor(Color.RED)
                    translationText.text = "⚠️ ${apiResult.translated_text}"

                } else {
                    translationText.setTextColor(Color.BLACK)

                    // Mostrar la palabra original y la traducción
                    translationText.text = "${apiResult.original_text} = ${apiResult.translated_text}"

                    // Formatear el análisis
                    val formattedAnalysis = formatAnalysis(apiResult.analysis)
                    wordDetails.text = "Análisis:\n$formattedAnalysis"
                }
            }
        }
        // Configurar el botón de guardar
        saveButton.setOnClickListener {
            savedWords.add(word) // Guarda la palabra original en source_lang
            Toast.makeText(this, "Palabra guardada", Toast.LENGTH_SHORT).show()
            popupWindow.dismiss()
        }
    }

    private fun formatAnalysis(analysis: String): String {
        val result = StringBuilder()

        try {
            val jsonArray = JSONArray(analysis)
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val lexema = jsonObject.optString("lexeme", "") // Usamos "lexeme"
                val lema = jsonObject.optString("lemma", "")
                val posInSpanish = jsonObject.optString("pos_spanish", "")
                val category = jsonObject.optString("category", "")
                val verbForm = jsonObject.optString("verb_form", "")
                val tense = jsonObject.optString("tense", "")
                val mood = jsonObject.optString("mood", "")
                val number = jsonObject.optString("number", "")

                val stem = jsonObject.optString("stem", "") // Obtener la raíz de la palabra
                if (stem.isNotEmpty()) result.append("Raíz: $stem\n") // Mostrar la raíz

                result.append("🔹 **Palabra:** $lexema\n")
                result.append("   ➡ **Lema:** $lema\n")
                result.append("   ➡ **Tipo:** $posInSpanish\n")
                1

                if (category == "Verbo") {
                    result.append("   ➡ **Forma verbal:** $verbForm\n")
                    result.append("   ➡ **Tiempo:** $tense\n")
                    result.append("   ➡ **Modo:** $mood\n")
                    result.append("   ➡ **Número:** $number\n")
                }

                result.append("\n") // Espacio entre palabras
            }
        } catch (e: Exception) {
            result.append("Error al analizar los datos")
        }

        return result.toString().trim()
    }



    private suspend fun processText(text: String, source_lang: String, target_lang: String): ProcessResponse {
        return withContext(Dispatchers.IO) {
            try {
                val response: Response<ProcessResponse> = ApiClient.apiService.processText(
                    ProcessRequest(text, source_lang, target_lang)
                )
                if (response.isSuccessful) {
                    response.body() ?: ProcessResponse("Error: respuesta vacía del servidor", "No se pudo traducir", "Error")
                } else {
                    ProcessResponse("Error en la API: ${response.code()} - ${response.message()}", "No se pudo traducir", "Error")
                }
            } catch (e: Exception) {
                ProcessResponse("Error de red: ${e.localizedMessage}", "No se pudo traducir", "Error")
            }
        }
    }

    private fun getTouchedWord(textView: TextView, event: MotionEvent): String? {
        val layout = textView.layout ?: return null
        val x = (event.x - textView.totalPaddingLeft + textView.scrollX).toInt()
        val y = (event.y - textView.totalPaddingTop + textView.scrollY).toInt()

        val line = layout.getLineForVertical(y)
        val offset = layout.getOffsetForHorizontal(line, x.toFloat())

        val text = textView.text.toString()
        val words = text.split(" ")

        return words.find { text.indexOf(it) <= offset && offset <= text.indexOf(it) + it.length }
    }
}