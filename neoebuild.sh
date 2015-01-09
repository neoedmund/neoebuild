export NB=/home/neoe/oss/neoebuild/
java -cp $NB/ant.jar:$NB/neoebuild.jar:"$JAVA_HOME/lib/tools.jar" neoe.build.BuildMain $*
