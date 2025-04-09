from flask import Flask, request, jsonify 
import stanza
import nltk
from nltk.stem.snowball import SnowballStemmer  

# Inicializar Flask
app = Flask(__name__)

# Descargar modelos de Stanza
stanza.download("es")
stanza.download("fr")
stanza.download("de")
stanza.download("en")

# Inicializar analizadores de Stanza
nlp = {
    "es": stanza.Pipeline(lang="es", processors="tokenize,mwt,pos,lemma"),
    "fr": stanza.Pipeline(lang="fr", processors="tokenize,mwt,pos,lemma"),
    "de": stanza.Pipeline(lang="de", processors="tokenize,mwt,pos,lemma"),
    "en": stanza.Pipeline(lang="en", processors="tokenize,pos,lemma"),
}

# Inicializar los stemmers para cada idioma  
stemmers = {  
    "es": SnowballStemmer("spanish"),  
    "fr": SnowballStemmer("french"),  
    "de": SnowballStemmer("german"),  
    "en": SnowballStemmer("english"),  
}  

nltk.download("averaged_perceptron_tagger")
nltk.download("punkt")

# Mapeo de etiquetas UPOS a español
upos_to_spanish = {
    "NOUN": "Sustantivo",
    "VERB": "Verbo",
    "ADJ": "Adjetivo",
    "ADV": "Adverbio",
    "PRON": "Pronombre",
    "DET": "Determinante",
    "CONJ": "Conjunción",
    "PUNCT": "Puntuación",
    "SPACE": "Espacio",
    "PROPN": "Nombre propio",
    "ADP": "Preposición",
    "AUX": "Verbo auxiliar",
    "CCONJ": "Conjunción coordinante",
    "SCONJ": "Conjunción subordinante",
    "INTJ": "Interjección",
    "NUM": "Número",
    "SYM": "Símbolo",
    "X": "Otro"
}

def analyze_word(word, lemma, pos, feats,language):
    """ Analiza una palabra y detecta su morfología (verbo, adjetivo, adverbio, etc.). """
    
    stem = stemmers[language].stem(word) if language in stemmers else "N/A"  

    info = {
            "lexeme": word, 
            "lemma": lemma, 
            "pos": pos, 
            "stem": stem,  # Agregar la raíz  
            "pos_spanish": upos_to_spanish.get(pos, pos),  # Traducir etiqueta POS
            "morphology": feats if feats else "N/A"}
    

    

    # 🔹 Análisis de verbos
    if pos == "VERB" or pos == "AUX":
        info["category"] = "Verbo"
        if feats:
            if "VerbForm=Inf" in feats:
                info["verb_form"] = "Infinitivo"
            elif "VerbForm=Ger" in feats:
                info["verb_form"] = "Gerundio"
            elif "VerbForm=Part" in feats:
                info["verb_form"] = "Participio"
            else:
                info["verb_form"] = "Conjugado"

            info["tense"] = "Presente" if "Tense=Pres" in feats else "Pasado" if "Tense=Past" in feats else "Futuro" if "Tense=Fut" in feats else "N/A"
            info["mood"] = "Indicativo" if "Mood=Ind" in feats else "Subjuntivo" if "Mood=Sub" in feats else "Imperativo" if "Mood=Imp" in feats else "N/A"
            info["person"] = feats.split("Person=")[1][0] if "Person=" in feats else "N/A"
            info["number"] = "Singular" if "Number=Sing" in feats else "Plural" if "Number=Plur" in feats else "N/A"

    # 🔹 Análisis de adjetivos
    elif pos == "ADJ":
        info["category"] = "Adjetivo"
        if "Degree=Cmp" in feats:
            info["adjective_type"] = "Comparativo"
        elif "Degree=Sup" in feats:
            info["adjective_type"] = "Superlativo"
        if "Number" in feats:
            info["sustantivado"] = True  # Posible adjetivo sustantivado

    # 🔹 Análisis de adverbios con "-mente"
    elif pos == "ADV":
        info["category"] = "Adverbio"
        if word.endswith("mente") and len(word) > 6:  # Evitar errores con palabras cortas
            possible_adj = word[:-5]  # Elimina "-mente"
            info["derived_from"] = "Adjetivo"
            info["base_adjective"] = possible_adj  # Posible adjetivo de origen

    return info

@app.route("/analyze", methods=["POST"])
def analyze_text():
    try:
        data = request.get_json()
        if not data or "text" not in data or "language" not in data:
            return jsonify({"error": "Faltan datos (texto y/o idioma)"}), 400

        text = data["text"]
        language = data["language"]

        if language not in nlp:
            return jsonify({"error": "Idioma no soportado"}), 400
        
        doc = nlp[language](text)
        analysis = []

        for sent in doc.sentences:
            for word in sent.words:
                word_data = analyze_word(word.text, word.lemma, word.upos, word.feats,language)
                analysis.append(word_data)

        return jsonify({"original_text": text, "analysis": analysis})

    except Exception as e:
        return jsonify({"error": f"Ha ocurrido un error: {str(e)}"}), 500

if __name__ == "__main__":
    app.run(debug=True, host="0.0.0.0", port=5002)

