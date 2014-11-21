SET mypath=%~dp0
java -cp %mypath%\ant.jar;%mypath%\neoebuild.jar;"%JAVA_HOME%\lib\tools.jar" neoe.build.BuildMain %1 %2
