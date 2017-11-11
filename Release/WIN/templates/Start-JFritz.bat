::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
:: 
::   Start-JFritz.bat
::
::   Version 1.3.1, 11.07.2009, ru
::
::   Copyright by JFritz-Team
::
::   -- Funktion --
::   Startet JFritz mittels javaw
::
::   -- Parameter --
::   keine
::
::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
@echo off & setlocal enableextensions disabledelayedexpansion


:: --- Überprüfen, wo JFritz installiert wurde und Pfad setzen ---
for %%N in ( . %ProgramFiles%\JFritz c:\programme\JFritz d:\programme\JFritz e:\programme\JFritz ) do (
  IF EXIST %%N\jfritz.jar (
      set jFritzVerzeichnis=%%N
      goto JavaSuchen
  )
)
cls
echo.
echo JFritz konnte nicht in Standard-Verzeichnissen gefunden werden!
echo.
echo.
pause
goto Ende


:: --- javaw.exe suchen ---
:JavaSuchen
set SuchenNach=javaw.exe
set JavawWithFullPath=

:: Suchen im Pfad
for %%f in ("%SuchenNach%") do set JavawWithFullPath="%%~$PATH:f"
if exist "%SuchenNach%" set JavawWithFullPath="%SuchenNach%"

:: Wenn Javaw nicht gefunden wurde
if [%JavawWithFullPath%]==[""] (
    echo.
    echo "%SuchenNach%" konnte weder im aktuellen Verzeichnis, noch im Pfad gefunden werden!
    echo.
    echo.
    pause
    goto Ende
)
:: echo Found %JavawWithFullPath%


:: Auf betreffende Platte und Verzeichnis wechseln
cd /d %jFritzVerzeichnis%

echo JFritz starting...
:: start javaw -jar jfritz.jar 
:: start javaw -Xmx300m -jar jfritz.jar   ; Speicher-Grenze 100MB aufgehoben
start "JFritz starten" %JavawWithFullPath% -Xmx500m -jar jfritz.jar 


:: --- Ende ---
:Ende
endlocal & goto :EOF
