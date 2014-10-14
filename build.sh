mydir=`dirname $0`
java -cp $mydir/ant.jar:$mydir/neoebuild.jar:"$JAVA_HOME/lib/tools.jar" neoe.build.BuildMain $1 $2
