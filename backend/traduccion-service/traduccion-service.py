import os
import argostranslate.package
import argostranslate.translate
from flask import Flask, request, jsonify

app = Flask(__name__)

# Directorio persistente donde se guardarán los modelos
models_directory = "/app/models"

# Crear el directorio si no existe
if not os.path.exists(models_directory):
    os.makedirs(models_directory)

# Lista de idiomas que se van a instalar
languages = [
    ("en", "es"), ("es", "en"),
    ("en", "fr"), ("fr", "en"),
    ("en", "it"), ("it", "en"),
    ("en", "de"), ("de", "en"),
    ("en", "nl"), ("nl", "en"),
    ("en", "pt"), ("pt", "en")
]

# Descargar e instalar modelos
def install_models():
    for from_lang, to_lang in languages:
        print(f"Instalando {from_lang} -> {to_lang}")
        argostranslate.package.update_package_index()
        available_packages = argostranslate.package.get_available_packages()
        package_to_install = next(
            (p for p in available_packages if p.from_code == from_lang and p.to_code == to_lang),
            None
        )
        if package_to_install:
            downloaded_package_path = package_to_install.download()
            argostranslate.package.install_from_path(downloaded_package_path)
            print(f"✅ Modelo {from_lang} -> {to_lang} instalado.")
        else:
            print(f"❌ No se encontró el modelo {from_lang} -> {to_lang}")

install_models()

# Endpoint de traducción
@app.route("/translate", methods=["POST"])
def translate():
    data = request.get_json()
    text = data.get("text", "")
    source_lang = data.get("source_lang", "en")
    target_lang = data.get("target_lang", "es")
    
    if not text:
        return jsonify({"error": "No se proporcionó texto para traducir"}), 400

    try:
        translated_text = argostranslate.translate.translate(text, source_lang, target_lang)
        return jsonify({"translated_text": translated_text})
    except Exception as e:
        return jsonify({"error": f"Error en la traducción: {str(e)}"}), 500

# Iniciar Flask en el puerto correcto
if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5001, debug=True)
