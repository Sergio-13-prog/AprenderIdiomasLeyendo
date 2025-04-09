package network

data class ProcessResponse(
    val translated_text: String,
    val analysis: String,
    val original_text: String,
)
