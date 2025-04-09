package mi.primera.aprenderidiomas1.carpeta1

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import mi.primera.aprenderidiomas1.R

class SuggestionAdapter(
    private val suggestions: List<Suggestion>,
    private val onItemClick: (Suggestion) -> Unit
) : RecyclerView.Adapter<SuggestionAdapter.ViewHolder>() {

    // Clase ViewHolder
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.tvLevelTitle)
        val description: TextView = itemView.findViewById(R.id.tvLevelDescription)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_suggestion, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val suggestion = suggestions[position]
        holder.title.text = suggestion.title
        holder.description.text = suggestion.description
        holder.itemView.setOnClickListener { onItemClick(suggestion) }
    }

    override fun getItemCount() = suggestions.size
}

// Clase de datos para Suggestion
data class Suggestion(
    val title: String,
    val description: String
)
