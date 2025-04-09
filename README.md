# AprenderIdiomasLeyendo

Este es un proyecto creado para aprender idiomas a través de la lectura de textos en el idioma que se desea aprender. La aplicación está desarrollada en Kotlin para Android y utiliza contenedores Docker para gestionar los servicios backend, como la traducción y análisis de textos. 

La aplicación permite traducir textos y realizar actividades interactivas para mejorar el aprendizaje de idiomas.

## Funcionalidades

- **Interfaz de usuario en Android**: Una aplicación móvil sencilla y fácil de usar que permite a los usuarios seleccionar textos para traducir y aprender idiomas.
- **Servicios Backend**: Utiliza contenedores Docker para gestionar diferentes servicios de análisis de texto y traducción.
- **Conexión con Google Cloud**: Los servicios de backend interactúan con APIs externas (como la API de Google Cloud) para traducir textos y realizar análisis semánticos.

## Tecnologías Utilizadas

- **Kotlin**: Para desarrollar la aplicación Android.
- **Docker**: Para crear contenedores de servicios backend que gestionan el análisis de texto y traducción.
- **Google Cloud API**: Para realizar las traducciones, aunque la clave API no está incluida en este repositorio por motivos de seguridad.
- **Firebase**: Para almacenar datos y gestionar la comunicación entre la app y los servicios backend.

## Estructura del Proyecto

El proyecto está dividido en dos partes principales:

1. **Aplicación Android (Kotlin)**: Se encuentra en la carpeta `AndroidApp/`.
2. **Backend (Python y Docker)**: Se encuentra en la carpeta `Backend/`. En ella, encontrarás los contenedores Docker y los scripts de Python para realizar las tareas de traducción y análisis.

