@prompt $$$S
cmd /c gradlew.bat build
copy .\app\build\outputs\apk\app-release.apk .\Home.apk
@pause
