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
baseDir:"/path/to/foo/bar",
prjs:[
 [prjname1,path1,{dep:[other_prjnames], cp:["/path/xxx.jar"], main:"neoe.game1.Main", run:[[class,methodToInvokeWhenBuild,[params]]]}],
 [prjname2,path2,{dep:[other_prjnames], cp:["/path/xxx.jar"], main:"neoe.game1.Main"}],
],
destDir:"/path/to/all-jars",
encoding:"utf-8",
debug:"true",
source:7,
taget:7,
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
