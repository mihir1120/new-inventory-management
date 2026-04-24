@rem
@rem Gradle wrapper script for Windows.
@rem
@echo off
setlocal

set "DIRNAME=%~dp0"
if "%DIRNAME%" == "" set "DIRNAME=."
set "APP_BASE_NAME=%~n0"
set "APP_HOME=%DIRNAME%"

@rem Resolve "." and ".." in APP_HOME
for %%i in ("%APP_HOME%") do set "APP_HOME=%%~fi"

set "DEFAULT_JVM_OPTS="
set "JAVA_OPTS="
set "GRADLE_OPTS="

if defined JAVA_HOME (
  set "JAVA_EXE=%JAVA_HOME%\\bin\\java.exe"
) else (
  set "JAVA_EXE=java.exe"
)

set "CLASSPATH=%APP_HOME%\\gradle\\wrapper\\gradle-wrapper.jar"

if not exist "%CLASSPATH%" (
  echo.
  echo ERROR: Gradle wrapper JAR not found: "%CLASSPATH%"
  echo.
  exit /b 1
)

@rem Execute Gradle Wrapper
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %GRADLE_OPTS% -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*
endlocal

