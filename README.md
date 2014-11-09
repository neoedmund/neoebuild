neoebuild
=========

Neoe build tool for Java projects

Short story
------------------
**neoebuild** is a build tool used by neoe. It can be compared to tools like apache ant(actually neoebuild based on it),
apache maven, Makefile, ....

*neoe* use Eclipse and Netbeans for his java projects developing, 
but also on some *small* projects, it is clear and handy to use a plain text editor(like [*neoeedit*](https://github.com/neoedmund/neoeedit)) and a online javadoc,
and build use neoebuild.


Features
-----------------
* build multiple projects(one project is also OK).
* projects have dependency on each others.
* generate jars
* a json like config file


Usage
-----------------
* `build <config-file>`  -  build
* `build <config-file> clean`  -  a clean build


Config file
----------------
* A complicated sample
```
{
baseDir:"/neo/temp",
prjs:[
 [game1,game,{dep:[dbop], cp:["C:/run/h2-1.3.171.jar"]}],
 [gamebuilder1,game-builder,{dep:[mxz,game1,dbop], run:[["neoe.game.Build",main,["C:/tmp/gamerelease/lib","C:/tmp/gamerelease/build1","data1.bin"]] ]}],
 [gamecploader1,"/neo/pyj/appclassloader",{cp:["/neo/pyj/mxz/binary/xz.jar"]}],
 [dbop,"/neo/pyj/neoedbop",],
 [mxz, "/neo/pyj/mxz", {cp:["/neo/pyj/mxz/binary/xz.jar"]}],
],
destDir:"C:/tmp/gamerelease/lib",
encoding:"utf-8",
debug:"true",
}                                                             
```
* explain

as you can see, you can use

  - `dep:[]` for dependency on other projects.
  - `cp:[]` for jar in classpath
  - `main:class-name` for main-class in jar
  - `run:[[class, method, [params]]]` for customized program to run while building
  - the build result jars will be copied into `destDir`
  - `prjs` format is `[name, dir, options]` 
  - **TODO: more details**
  

Build neoebuild itself
--------------------
config file is `buildself.py`
