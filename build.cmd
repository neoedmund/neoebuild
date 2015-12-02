SET mypath=%~dp0
java -cp %mypath%\neoebuild.jar neoe.build.BuildMain %*
