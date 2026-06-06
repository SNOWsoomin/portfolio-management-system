$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
$env:PATH="$env:JAVA_HOME\bin;$env:PATH"
Set-Location 'C:\Users\조현민\Desktop\portfolio-management-system\backend'
.\gradlew.bat bootRun *> 'manual-backend.log'
