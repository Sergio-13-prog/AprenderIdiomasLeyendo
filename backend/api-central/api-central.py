import requests
from flask import Flask, request, jsonify
import json  # Asegura que json esté importado

app = Flask(__name__)

# URLs de los otros contenedores
LIBRETRANSLATE_URL = "http://traduccion:5001/translate"
SPACY_ANALYSIS_URL = "http://analisis:5002/analyze"

# Usa la IP local del PC en vez de localhost
PC_IP = "192.168.174.218"  # Cambia esto por la IP real de tu PC

#LIBRETRANSLATE_URL = f"http://{PC_IP}:5001/translate"
#SPACY_ANALYSIS_URL = f"http://{PC_IP}:5002/analyze"



@app.route("/process", methods=["POST"])
def process_text():
    try:
        data = request.get_json()
        if not data or "text" not in data:
            return jsonify({"error": "Falta el texto a analizar"}), 400

        text = data["text"]
        source_lang = data.get("source_lang", "es")
        target_lang = data.get("target_lang", "en")

        # Imprimimos los datos recibidos para depuración
        print(f"📥 Datos recibidos: {json.dumps(data, ensure_ascii=False)}")

        # 1️⃣ Traducir el texto con LibreTranslate
        translation_response = requests.post(LIBRETRANSLATE_URL, json={
            "text": text,
            "source_lang": source_lang,
            "target_lang": target_lang
        })

        if translation_response.status_code != 200:
            print(f"❌ Error en la traducción: {translation_response.text}")
            return jsonify({"error": f"Error en la traducción: {translation_response.text}"}), 500

        translated_text = translation_response.json().get("translated_text")
        if not translated_text:
            return jsonify({"error": "No se recibió texto traducido"}), 500

        # 2️⃣ Analizar el texto original con spaCy
        spacy_payload = {
            "text": text,
            "language": source_lang  # Enviamos el idioma como parte de la solicitud
        }

        # Imprimimos los datos enviados a spaCy para depuración
        print(f"📤 Datos enviados a spaCy: {json.dumps(spacy_payload, ensure_ascii=False)}")

        spacy_response = requests.post(SPACY_ANALYSIS_URL, json=spacy_payload)

        if spacy_response.status_code != 200:
            print(f"❌ Error en el análisis de spaCy: {spacy_response.text}")
            return jsonify({"error": f"Error en el análisis de spaCy: {spacy_response.text}"}), 500

        analysis_result = spacy_response.json()

        # 🔹 Imprimimos la respuesta recibida de spaCy para depuración
        print(f"📄 Respuesta de spaCy: {json.dumps(analysis_result, ensure_ascii=False)}")

        # 🔹 Convertimos el resultado de análisis en string para facilidad en Kotlin
        analysis_text = analysis_result.get("analysis", "No se pudo analizar")

        return jsonify({
            "original_text": text,
            "translated_text": translated_text,
            "analysis": str(analysis_text)  # 🔹 Convertimos a string explícitamente
        })

    except Exception as e:
        return jsonify({"error": f"Ha ocurrido un error: {str(e)}"}), 500


if __name__ == "__main__":
    app.run(debug=True, host="0.0.0.0", port=8081)
