SET mypath=%~dp0
java -cp %mypath%\ant.jar;%mypath%\neoebuild.jar;"C:\Program Files\Java\jdk1.8.0_60\lib\tools.jar" neoe.build.BuildMain %*
