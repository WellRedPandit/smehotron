@set SCRIPT_DIR=%~dp0
@java -Xms512M -Xmx1536M -Xss1M -XX:+CMSClassUnloadingEnabled -jar "%SCRIPT_DIR%sbt-launch-0.13.15.jar" %*
