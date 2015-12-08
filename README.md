neoebuild
=========

Neoe build tool for Java projects

Short story
------------------
**neoebuild** is a build tool used by neoe for java source compile. 
It can be compared with tools like apache ant, maven, Makefile, ...



Features
-----------------
* build multiple projects.
* projects have dependency on each others.
* generate jars
* a json like config file


Usage
-----------------
* `build <config-file>`  -  build
* `build <config-file> clean`  -  a clean build
* run `buildBox` and drop `build script file` into it 

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
debug:true,
source:7,
taget:7,
}                                                             
```
* explain

as you can see, you can use

  - `dep:[]` for dependency on other projects.
  - `cp:[]` for jar in classpath
  - `main:class-name` for main-class of jar
  - `run:[[class, method, [params]]]` for customized program to run while building
  - the build result jars will be copied into `destDir`
  - `prjs` format is `[name, dir, options]` 
  - **TODO: more details**
  

Build neoebuild itself
--------------------
`neoebuild mybuild`
