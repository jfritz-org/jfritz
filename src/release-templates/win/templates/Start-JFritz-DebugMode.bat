::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
:: 
::   Start-JFritz-DebugMode.bat
::
::   Version 1.3.1, 16.07.2009, ru
::
::   Copyright by JFritz-Team
::
::   -- Funktion --
::   Startet JFritz mittels javaw mit Debug-Ausgabe in ein Logfile, welches mit Datum und Uhrzeit
::   im JFritz-Verzeichnis in der Mappe "debug-logs" angelegt wird
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


:: --- Debug-Verzeichnis anlegen ---
:: In diese (Unter-)Mappe werden die Logs abgelegt
set debugmappe=debug-logs

:: Auf betreffende Platte und Verzeichnis wechseln
cd /d %jFritzVerzeichnis%

:: Schauen, ob DebugMappe schon vorhanden ist, wenn nicht, anlegen
IF not EXIST %debugmappe% (
  echo Debugmappe anlegen
  mkdir %debugmappe%
) 


:: --- LogFile setzen --- 
:: Datum und Uhrzeit ermitteln
set yyyymmddhhmmss=%date:~6,4%%date:~3,2%%date:~0,2%-%time:~0,2%%time:~3,2%%time:~6,2%
:: Blank durch Null ersetzen, also bei 1-stelliger Stundenzahl
set yyyymmddhhmmss=%yyyymmddhhmmss: =0%
:: Logfilenamen mit User-Namen und Datum+Uhrzeit
set LogFile=%debugmappe%\jfritz-debuglog-%USERNAME%-%yyyymmddhhmmss%.txt


:: --- JFritz im Debug-mode starten ---
echo JFritz starting with debug-logging...
:: Old version: start javaw -jar jfritz.jar -l%LogFile%
:: Old version: start javaw -Xmx1600m -jar jfritz.jar -l%LogFile%   ; Speicher-Grenze 100MB aufgehoben
:: Neu seit V0.7.2.29+30  -v<SEVERITY>
:: SEVERITY can be ERROR, WARNING, INFO, DEBUG - 
:: z.B. -vERROR oder --verboseWARNING
start "JFritz starten" %JavawWithFullPath% -Xmx500m -jar jfritz.jar -vDEBUG -l%LogFile%

:: --- Ende ---
:Ende
endlocal & goto :EOF