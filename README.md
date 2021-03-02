# vonas/tournament

Diese Modifikation unterstützt den Spieler dabei
einfache Turniere innerhalb des Spiels mit Befehlen zu verwalten.

Dadurch muss nicht das Anwendungsfenster gewechselt werden
und man kann bei Eingabe des Namens eines Spielers
auf die Auto-Vervollständigung des Spiels zurückgreifen.
Ankündigungen darüber, wer als Nächstes dran ist,
werden automatisch im Chat an alle Spieler gesendet,
sodass diese nicht manuell eingegeben werden müssen.
Außerdem können Paarungen automatisch generiert werden,
was den Verwaltungsaufwand auf Seiten des Turnierleiters
immens senkt.

## Definitionen

Ein Turnier wird im K.O.-Format gespielt
und besteht aus mehreren Runden.
Jede Runde besteht aus mehreren Paarungen.
Eine Paarung ist ein Zusammentreffen zweier Spieler,
die gegeneinander in einem Spiel oder Kampf antreten.
Der Gewinner einer Paarung kommt in die nächste Runde.
Verlierer fliegen raus und kommen nicht weiter.

Spieler können zu Beginn oder während einer Runde
zum Spiel hinzugefügt werden.
Spieler können ebenfalls zu jedem Zeitpunkt entfernt werden.
Paarungen zwischen Spielern werden zufällig bestimmt.

Der Ablauf des Finales eines Turniers
wird durch die Anzahl der übrigen Spieler bestimmt.
Bei vier Spielern wird im K.O.-Verfahren fortgesetzt.
Bei drei Spielern geht es im Round Robin weiter.

## Konzepte

Daten werden in einer SQLite-Datenbank gespeichert.
Neben des strukturierten Formats profitiert man
von einer persistenten Speicherung der Daten
und von Ausfallsicherheit, falls das Spiel abstürzen sollte.

Damit ein Turnier nach einem Neustart des Spiels
fortgesetzt werden kann, speichert das Programm
keine Zustandsdaten im Hauptspeicher.
Jeder Befehl nimmt sich die Daten aus der Datenbank,
welcher er benötigt, macht seine Arbeit
und speichert den neuen Zustand direkt im Anschluss ab.

Die Befehle sind so gestaltet,
dass sie sich in ihrer Form einer REPL ähneln
(Read-Eval-Print-Loop).

## Befehle

### Verwaltung

#### Ein Turnier starten

`/t:new`  
`/t:start`  
Startet ein neues Turnier.
Dies ist nur möglich,
sofern nicht bereits ein Turnier vorhanden ist.

#### Ein Turnier beenden

`/t:delete`  
`/t:stop`  
Beendet das aktuelle Turnier
und löscht alle vorhandenen Daten.
Zur Bestätigung muss `/t:yes` eingegeben werden.

#### Daten exportieren

`/t:export`  
Exportiert Turnierdaten im leserlichen Textformat,
als auch die Datenbank im Rohformat.
Die Text-Datei ist so aufgebaut,
dass mit den Dort verfügbaren Daten
das Turnier manuell forgesetzt werden kann.

#### Kritische Aktionen

`/t:yes`  
Bestätigt eine kritische Aktion.

`/t:cancel`
`/t:no`
Bricht eine kritische Aktion ab.
Diese werden ebenfalls abgebrochen,
wenn ein anderer Befehl vorher eingegeben wurde.

### Turnier-Durchführung

`/t:a`  
`/t:add <Player> [<Player> [...]]`  
Fügt einen oder mehrere Spieler der aktiven Runde hinzu

## Lizenz

(c) 2021 Jonas van den Berg  
currently `UNLICENSED`
