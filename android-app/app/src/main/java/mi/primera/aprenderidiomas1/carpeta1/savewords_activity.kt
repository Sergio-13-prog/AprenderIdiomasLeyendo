package mi.primera.aprenderidiomas1.carpeta1

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mi.primera.aprenderidiomas1.R
import mi.primera.aprenderidiomas1.carpeta1.SuggestionActivity.Companion.savedWords
import network.ApiClient
import network.ProcessRequest
import network.ProcessResponse
import org.json.JSONArray
import retrofit2.Response

class SavedWordsActivity : AppCompatActivity() {

    // Declarar las variables globales
    private lateinit var translationText: TextView
    private lateinit var wordDetails: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_savewords)
        supportActionBar?.hide()





        //...
        val listsavedwords = findViewById<ListView>(R.id.listViewSavedWords)

        val source_lang = intent.getStringExtra("sourceLanguage") ?: "en"
        val target_lang = intent.getStringExtra("targetLanguage") ?: "es"
        // Log para verificar si se recibieron los idiomas correctos
        Log.d("SuggestionActivity", "Recibido Source Language: $source_lang, Target Language: $target_lang")



        listsavedwords.setOnItemClickListener { _, view, position, _ ->
            val touchedWord = savedWords[position] // Obtiene la palabra directamente de la lista
            if (touchedWord != "No hay palabras guardadas") {
                showPopup(view, touchedWord, 0, 0, source_lang, target_lang)
            }
        }



        val listView = findViewById<ListView>(R.id.listViewSavedWords)

        // Obtener la lista de palabras guardadas
        val savedWords = SuggestionActivity.savedWords

        // Verificar si la lista está vacía
        if (savedWords.isEmpty()) {
            savedWords.add("No hay palabras guardadas")
        }

        // Mostrar en un ListView
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, savedWords)
        listView.adapter = adapter

        listView.setOnItemClickListener { _, view, position, _ ->
            val touchedWord = savedWords[position]
            if (touchedWord != "No hay palabras guardadas") {
                showPopup(view, touchedWord, 0, 0, source_lang, target_lang)
            }
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
