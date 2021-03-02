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

Platzierungen gehen immer an die ersten 3 Gewinner.
Basierend darauf wird auch das Finale ausgetragen.

## Konzepte

### Dynamisches Turnier

Ein Turnier im Sinne dieser Modifikation
ist ein dynamisches Turnier.
Das bedeutet, dass Spieler auch
während der Durchführung des Turniers
hinzugefügt werden können.
Paarungen werden nicht im Vorhinein generiert.

### Finale niemals in der ersten Runde

Aufgrund der Dynamik eines Turniers
kann es vorkommen, dass in der ersten Runde
nicht genügend Teilnehmer vorhanden sind.
Bei drei Teilnehmern wäre zum Beispiel zu entscheiden,
ob diese bereits im Round Robin um die Trophäe spielen sollen,
oder im Nachhinein noch Spieler hinzugefügt werden können.
Diese Modifikation nimmt an,
dass die erste Runde niemals das Finale ist.

### Parallel aktive Paarungen

Paarungen können parallel durchgeführt werden.
Wenn ein Sieger festgelegt wird,
kann damit auf die zugehörige Paarung geschlossen werden.

> **TODO**  
> Die aktuelle Implementierung kann nur eine Paarung handhaben.

### Speicherung von Zustandsdaten

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

#### Teilnehmer verwalten

`/t:a`  
`/t:add <Player> [<Player> [...]]`  
Fügt einen oder mehrere Spieler der aktiven Runde hinzu.
Falls der Spieler bereits in der Runde ist passiert nichts.
Falls dieser jedoch verloren hat und sonst nicht mehr teilnehmen würde,
erhält der Spieler eine zweite Chance.

`/t:rm`  
`/t:remove <Player>`  
Entfernt einen Spieler aus der aktiven Runde.

#### Informationen erhalten

`/t:l`  
`/t:ls`  
`/t:list`  
Listet alle Spieler der aktuellen Runde auf.
Informiert ebenfalls darüber wer bereits gespielt,
wer gewonnen und wer verloren hat.

`/t:h`  
`/t:history`  
Listet alle Paarungen vergangener Runden auf.
Zeigt ebenfalls aktuell laufende, ausstehende Paarungen.

`/t:s`  
`/t:status`
Gibt aktuell wichtige Statusinformationen aus,
wie z.B. die aktuell laufende(n) Paarung(en).

#### Paarungen generieren

`/t:p`  
`/t:play`  
Erzeugt eine neue Paarung zwischen zwei zufälligen Spielern.
Wählt unter Spielern aus,
welche während der aktuellen Runde noch in keiner Paarung vorhanden waren.
Falls keine Spieler mehr vorhanden sind,
so wird eine Fehlermeldung ausgegeben.
Falls ein Spieler keinen Gegner haben würde,
so wird darauf hingewiesen,
dass ein Spieler hinzugefügt
oder der letzte Spieler entfernt werden muss.
Bei weniger als vier Spielern
werden entsprechend die Finalrunden eingeleitet.
Sofern bereits eine Paarung im Gange ist,
werden aktuelle Informationen über die Paarung ausgegeben.

`/t:w`  
`/t:win <Player>`  
Setzt den Gewinner der aktuellen Paarung.
Hierfür muss vorher `/t:play` aufgerufen worden sein,
d.h. dass aktuell eine Paarung laufen muss.

#### Runden fortschreiten

`/r:next`  
Setzt mit der nächsten Runde fort.
Dies ist nur möglich,
sofern alle Spieler der Runde bereits in einer Paarung waren.
Wirft einen Fehler falls noch eine Paarung aktiv ist (`/t:play`).

`/r:prev`  
`/r:previous`  
Geht zurück in die letzte Runde,
falls ein Spieler nachträglich teilnehmen möchte.
Dies ist nur möglich,
wenn noch keine Paarung der aktuellen Runde begonnen hat.

## Installation

### Abhängigkeiten

Diese Modifikation benötigt Fabric API Version 0.31.0 oder höher.

## Lizenz

MIT License  
(c) 2021 Jonas van den Berg
