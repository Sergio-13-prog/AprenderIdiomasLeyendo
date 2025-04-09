package mi.primera.aprenderidiomas1.carpeta1

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import mi.primera.aprenderidiomas1.R


class RecommendedBooksActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_recommended_books)

        // TextView para el primer libro
        val book1 = findViewById<TextView>(R.id.book1)
        book1.setOnClickListener {
            val url = "https://maxkoedood.com/" // URL del libro 1
            openWebPage(url)
        }

        // TextView para el segundo libro
        val book2 = findViewById<TextView>(R.id.book2)
        book2.setOnClickListener {
            val url = "https://libros.eco/producto/brave-new-world-bridge/?gad_source=1&gclid=CjwKCAiA34S7BhAtEiwACZzv4V7Y9flBSqvR0dE6CyTXliAuVjAsEFW3H0tyWoeapGRlk5qMfzfN8BoC7NQQAvD_BwE" // URL del libro 2
            openWebPage(url)
        }
    }

    // Método para abrir una página web en el navegador
    private fun openWebPage(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }
}
