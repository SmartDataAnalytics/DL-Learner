﻿/**
 * translation for: xajax v.x.x
 * @version: 1.0.0
 * @author: mic <info@joomx.com>
 * @copyright xajax project
 * @license GNU/GPL
 * @package xajax x.x.x
 * @since v.x.x.x
 * save as UTF-8
 */

if ('undefined' != typeof xajax.debug) {
	/*
		Array: text
	*/
	xajax.debug.text = [];
	xajax.debug.text[100] = 'WARNUNG: ';
	xajax.debug.text[101] = 'FEHLER: ';
	xajax.debug.text[102] = 'XAJAX FEHLERSUCHE NACHRICHT:\n';
	xajax.debug.text[103] = '...\n[UMGFANGREICHE ANTWORT]\n...';
	xajax.debug.text[104] = 'SENDE ANFRAGE';
	xajax.debug.text[105] = 'GESENDET [';
	xajax.debug.text[106] = ' bytes]';
	xajax.debug.text[107] = 'RUFE AUF: ';
	xajax.debug.text[108] = 'URI: ';
	xajax.debug.text[109] = 'BEGINNE ANFRAGE';
	xajax.debug.text[110] = 'PARAMETER IN BEARBEITUNG [';
	xajax.debug.text[111] = ']';
	xajax.debug.text[112] = 'KEINE PARAMETER ZU VERARBEITEN';
	xajax.debug.text[113] = 'BEREITE REQUEST VOR';
	xajax.debug.text[114] = 'BEGINNE XAJAX CALL (veraltet: verwendet stattdessen xajax.request)';
	xajax.debug.text[115] = 'BEGINNE XAJAX ANFRAGE';
	xajax.debug.text[116] = 'Die vom Server erhaltenen Daten konnten nicht verarbeitet werden.\n';
	xajax.debug.text[117] = '.\nPrüfe auf Fehlermeldungen des Servers.';
	xajax.debug.text[118] = 'ERHALTEN [status: ';
	xajax.debug.text[119] = ', Größe: ';
	xajax.debug.text[120] = ' bytes, Zeit: ';
	xajax.debug.text[121] = 'ms]:\n';
	xajax.debug.text[122] = 'Der Server hat folgenden HTTP-Status zurück gesendet: ';
	xajax.debug.text[123] = '\nERHALTEN:\n';
	xajax.debug.text[124] = 'Der Server lieferte einen Redirect nach:<br />';
	xajax.debug.text[125] = 'ERLEDIGT [';
	xajax.debug.text[126] = 'ms]';
	xajax.debug.text[127] = 'INITIALISIERE REQUEST OBJEKT';

	/*
		Array: exceptions
	*/
	xajax.debug.exceptions = [];
	xajax.debug.exceptions[10001] = 'Ungültige XML-Antwort: die Antwort enthält ein ungültiges Tag: {data}.';
	xajax.debug.exceptions[10002] = 'GetRequestObject: XMLHttpRequest ist nicht verfügbar, XajaX ist nicht verfügbar.';
	xajax.debug.exceptions[10003] = 'Warteschleife-Überlauf: kann Objekt nicht an Warteschleife übergeben da diese voll ist.';
	xajax.debug.exceptions[10004] = 'Ungültige XML-Antwort: die Antwort enthält einen unerwarteten Tag oder Text: {data}.';
	xajax.debug.exceptions[10005] = 'Ungültige Request-URI: Ungültige oder Fehlende URI; Autoerkennung fehlgeschlagen; bitte nur eine einzige URI angeben.';
	xajax.debug.exceptions[10006] = 'Ungültiges Antwort-Befehl: Unvollständiges Objekt zurück erhalten.';
	xajax.debug.exceptions[10007] = 'Ungültiges Antwort-Befehl: Befehl [{data}] ist nicht bekannt.';
	xajax.debug.exceptions[10008] = 'Es konnte kein Element mit der ID [{data}] konnte im Dokument gefunden werden.';
	xajax.debug.exceptions[10009] = 'Ungültige Anfrage: Fehlender Funktionsparameter - name.';
	xajax.debug.exceptions[10010] = 'Ungültige Anfrage: Fehlender Funktionsparameter - object.';
}

if ('undefined' != typeof xajax.config) {
	if ('undefined' != typeof xajax.config.status) {
		/*
			Object: update
		*/
		xajax.config.status.update = function() {
			return {
				onRequest: function() {
					window.status = "Sende Anfrage...";
				},
				onWaiting: function() {
					window.status = "Warten auf Antwort...";
				},
				onProcessing: function() {
					window.status = "Verarbeitung...";
				},
				onComplete: function() {
					window.status = "Abgeschlossen.";
				}
			}
		}
	}
}
