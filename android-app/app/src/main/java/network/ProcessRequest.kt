package network

data class ProcessRequest(
    val text: String,
    val source_lang: String = "auto",
    val target_lang: String = "es"
)
