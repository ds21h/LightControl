# LightControl

This is an Android app to control the Lights project. For an overview please see repository Lights.

The project structure is that of AndroidStudio.

This app started its life completely in the Dutch language. Starting from version 2.9 it is in English.

History:

Versie 1.0 - 01-11-2015<br />
    -   Eerste stabiele versie<br />

Versie 2.0 - 08-10-2017<br />
    -   Ondersteuning ESP8266 Schakelaars<br />
    -   Lokale DB met schakelaarinfo zodat ESP8266 schakelaars ook zonder server geschakeld kunnen worden<br />
    -   Server definitie in lokale DB ipv XML file<br />

Versie 2.01 - 10-07-2018<br />
    -   ACCESS_COARSE_LOCATION toegevoegd. Is noodzakelijk voor Android 8.1 (Oreo) om SSID op te vragen.<br />

Version 2.9 - 03-12-2018<br />
    -   Translated into English (only program itself, database and communication still in dutch).<br />
    -   Communication with esp switch changed to English<br />
    -   URI Switch/Button abandoned. Now Switch/Setting is used<br />
    -   Classes Setting and Switch no longer in seperate package.<br />
    -   Requires ESP8266-Switch software version 2.1 or later.<br />

Version 3.0 - 16-12-2018<br />
    -   Switch to English server, so all URIs are in English.<br />
    -   Grouped all used URIs in one static class URIs. This makes it easier to find where which URI is used.<br />
    -   Upgraded to Android 6+ permissions model<br />
    -   Upgraded Settings communication to JSON<br />
    -   Translated db to English --> Not compatible, in first run for a server please re-enter server details.<br />
    -   Completely multi lingual. Now English and Dutch. Additional languages are easily added.<br />
    -   Requires LightAPI version 1.0 or later on server<br />

Version 3.1 - 13-01-2019<br />
    -   REST calls model simplified<br />
    -   Added active status enquiries (every 10 seconds)<br />
    -   Time-outs for status enquiries reduced to 2 seconds<br />
    -   AsyncTasks changed to static with WeakReference to main class<br />
    -   Changed to package-private where possible<br />
    -   All ListAdapters changed to recycle model with holder class<br />
