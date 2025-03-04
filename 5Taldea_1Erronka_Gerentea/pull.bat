@echo off
cd /d ../../

:: Crear la carpeta si no existe
if not exist "EguraldiaXml" (
    mkdir EguraldiaXml
)

:: Moverse a la carpeta creada
cd EguraldiaXml

:: Clonar el repositorio dentro de la carpeta
git clone https://github.com/benatge2/EguraldiaXml.git .

echo Clonación realizada con éxito.
pause
