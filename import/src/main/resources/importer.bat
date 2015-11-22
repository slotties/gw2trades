 @echo off
 setLocal EnableDelayedExpansion
 set CLASSPATH="
 for /R ./lib %%a in (*.jar) do (
   set CLASSPATH=!CLASSPATH!;%%a
 )
 set CLASSPATH=!CLASSPATH!"

 java -cp %CLASSPATH% gw2trades.importer.Main