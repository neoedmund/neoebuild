package neoe.build;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import neoe.build.tools.Copy1;
import neoe.build.tools.FileSet1;
import neoe.build.tools.Jar1;
import neoe.build.tools.Java1;
import neoe.build.tools.Javac1;
import neoe.build.tools.Path1;
import neoe.build.tools.Project1;
import neoe.build.tools.Projects;
import neoe.util.FindJDK;
import neoe.util.Log;
import neoe.util.PyData;

public class BuildMain {

	public static class BuildAll {

		private Set<String> built, toBuild;
		private Projects prjs;
		private int turnNo;
		private Project1 project;
		private Map param;
		String destDir;

		public BuildAll(Map param) {
			this.param = param;
		}

		public BuildAll build(Projects prjs, String destDir) throws Exception {
			this.destDir = destDir;
			this.prjs = prjs;
			built = new HashSet<String>();
			toBuild = new HashSet<String>();
			for (Prj p : prjs.m.values()) {
				toBuild.add(p.name);
			}
			log("total " + toBuild);
			checkDeps();
			while (toBuild.size() > 0) {
				Set<String> turn = new HashSet<String>();
				for (String n : toBuild) {
					Prj prj = prjs.m.get(n);
					if (prj.depends == null) {
						turn.add(n);
					} else {
						if (isDepBuilt(prj.depends)) {
							turn.add(n);
						}
					}
				}
				if (turn.size() == 0) {
					throw new RuntimeException("to build " + toBuild + " but they depend on each other");
				}
				buildTurn(turn);
				built.addAll(turn);
				toBuild.removeAll(turn);
			}
			return this;
		}

		private void buildTurn(Set<String> turn) throws Exception {
			turnNo++;
			log("Turn " + turnNo + " start " + turn.size() + " projects " + turn);
			if (!prjs.multithread) {
				for (String n : turn) {
					Prj prj = prjs.m.get(n);
					buildPrj(prj);
				}
			} else {
				final Map<Long, Boolean> success = new HashMap<Long, Boolean>();
				Thread[] ts = new Thread[turn.size()];
				int i = 0;
				for (String n : turn) {
					final Prj prj = prjs.m.get(n);
					Thread t = new Thread() {
						public void run() {
							try {
								buildPrj(prj);
								success.put(getId(), true);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					};
					t.start();
					ts[i++] = t;
				}
				for (Thread t : ts) {
					t.join();
					Boolean succ = success.get(t.getId());
					if (succ == null)
						succ = false;
					if (!succ) {
						log("build fail:" + t.getId());
						throw new RuntimeException("build failed");
					}
				}
			}
			log("Turn " + turnNo + " finish");
		}

		public void buildPrj(Prj prj) throws Exception {

			String prjName = prj.name;
			log(prjName + ":build start");
			File path = addPath(prjs.baseDir, prj.dir);

			// log("Path="+path.getCanonicalPath());
			project = new Project1(prjs);
			project.setName(prjName);
			Javac1 javac = new Javac1();
			javac.setProject(project);
			javac.setExecutable(prjs.javaHome + (FindJDK.isWindows ? "/bin/javac.exe" : "/bin/javac"));
			// javac.setFork(true);
			javac.setTarget(getParam("target", "1.7"));
			javac.setSource(getParam("source", "1.7"));
			javac.setEncoding(getParam("encoding", "utf-8"));
			javac.setDebug(new Boolean(getParam("debug", "false")));
			File srcDir = new File(path.getCanonicalPath(), "/src");
			if (!srcDir.exists()) { // check /src
				throw new RuntimeException("src dir not found:" + srcDir.getCanonicalPath());
			}
			javac.setSrcdir(path.getCanonicalPath() + "/src");
			File buildDir = new File(path.getCanonicalPath() + "/build");
			buildDir.mkdirs();
			javac.setDestdir(buildDir.getCanonicalPath());
			Path1 cp = new Path1(project);
			if (prj.cp != null) {
				for (Object o : prj.cp) {
					File f1 = addPath(prjs.baseDir, o.toString());
					if (f1.isDirectory()) {
						// v1.5 : cp can be dir ,eg lib dir
						File[] fs = f1.listFiles();
						for (File f : fs) {
							if (f.getName().endsWith(".jar")) {
								// log("[D]add
								// "+f.getCanonicalPath());
								cp.add(f.getCanonicalPath());
							}
						}
					} else {
						cp.add(addPath(prjs.baseDir, o.toString()).getCanonicalPath());
					}
				}
			}
			if (prj.depends != null) {
				for (Object o : prj.depends) {
					Prj p1 = prjs.m.get(o.toString());
					String po = addPath(prjs.baseDir, p1.dir).getCanonicalPath() + "/dist/" + p1.name + ".jar";
					cp.add(po);

				}
			}
			// log(cp);
			javac.setClasspath(cp);
			// javac.setCompiler("javac1.7");
			// javac.setFork(true);
			int cnt = javac.execute();
			if (cnt == 0) {
				log(prjName + ":no more to compile");
			} else {
				log(prjName + ":compile files (" + cnt + ")");
			}
			// copy resources

			Copy1 copy = new Copy1();
			copy.setProject(project);
			copy.setTodir(buildDir);
			FileSet1 fs = new FileSet1();
			fs.setDir(new File(path.getCanonicalPath() + "/src"));
			fs.setExcludesEndsWith(".java");
			fs.ignoreEclipsePrjFile = true;
			copy.addFileset(fs);
			int cnt2 = copy.execute();
			log(String.format("%s:copy %d resources", prjName, cnt2));

			Jar1 jar = new Jar1();
			jar.setProject(project);
			File jarFile = new File(path.getCanonicalPath() + "/dist/" + prjName + ".jar");
			jarFile.getParentFile().mkdirs();
			jar.setDestFile(jarFile);
			jar.setBasedir(buildDir);
			if (prj.mainClass != null) {
				jar.addConfiguredManifest("Main-Class", prj.mainClass);
			}
			jar.execute();
			// log(prjName+":build finish");
			copyTo(prj, destDir);

			if (prj.run != null) {
				Java1 run = new Java1();
				cp.add(jarFile.getCanonicalPath());
				run.setClasspath(cp);
				run.setProject(project);
				for (Object o : prj.run) {
					List row = (List) o;
					run.setClassname((String) row.get(0));
					for (Object o1 : (List) row.get(2)) {
						run.addArg(o1.toString());
					}
					run.execute();
				}
			}
		}

		private String getParam(String key, String value) {
			Object o = param.get(key);
			if (param == null || o == null)
				return value;
			return o.toString();
		}

		private boolean isDepBuilt(List depends) {
			for (Object pre : depends) {
				if (!built.contains(pre.toString()))
					return false;
			}
			return true;
		}

		private void checkDeps() {
			for (Prj p : prjs.m.values()) {
				if (p.depends != null) {
					for (Object n : p.depends) {
						if (!toBuild.contains(n.toString())) {
							throw new RuntimeException("[" + p.name + "] need [" + n + "] which is not exists");
						}
					}
				}
			}
		}

		public void clean(Projects prjs) throws IOException {
			for (Prj prj : prjs.m.values()) {
				String path = addPath(prjs.baseDir, prj.dir).getCanonicalPath();
				deleteDirectory(new File(path + "/dist"), 0);
				deleteDirectory(new File(path + "/build"), 0);
			}
		}

		public void copyTo(Prj prj, String dest) throws IOException {
			File destDir = addPath(prjs.baseDir, dest);
			destDir.mkdirs();
			// for (Prj prj : prjs.m.values()) {
			String path = addPath(prjs.baseDir, prj.dir).getCanonicalPath();
			Copy1 copy = new Copy1();
			copy.setProject(project);
			copy.setFile(new File(path + "/dist/" + prj.name + ".jar"));
			copy.setTodir(destDir);
			int cnt = copy.execute();
			// }
			if (cnt > 0)
				log(prj.name + ":jar copied to " + dest);
			if (prj.cp != null)
				for (Object o : prj.cp) {
					// also copy cp jars
					File f = addPath(prjs.baseDir, o.toString());
					if (f.isDirectory()) { // v1.5
						for (File f1 : f.listFiles()) {
							if (f1.getName().endsWith(".jar")) {
								copy.setFile(f1);
								copy.execute();
							}
						}
					} else {
						copy.setFile(f);
						copy.execute();
					}
				}
		}
	}

	public static final String VER = "v151202";

	static public boolean deleteDirectory(File path, int lv) throws IOException {
		if (lv == 0)
			log("delete " + path.getCanonicalPath());
		if (path.exists()) {
			File[] files = path.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					deleteDirectory(files[i], lv + 1);
				} else {
					files[i].delete();
				}
			}
		}
		if (lv == 0)
			log("delete " + path.getCanonicalPath() + " " + path.delete());
		return path.delete();
	}

	public static String join(String delima, List list) {
		if (list == null || list.size() == 0)
			return "";
		StringBuffer sb = new StringBuffer();
		sb.append(list.get(0));
		for (int i = 1; i < list.size(); i++) {
			sb.append(delima).append(list.get(i));
		}
		return sb.toString();
	}

	public static File addPath(String baseDir, String dir) {
		File path;
		if (dir.startsWith("/") || dir.indexOf(":") > 0) {
			path = new File(dir);
		} else {
			path = new File(baseDir, dir);
		}
		return path;
	}

	public static class Prj {

		public String mainClass;
		public String name;
		public String dir;
		public List depends;
		public List cp;
		public List run;

	}

	static Map makeDefaultEmptyConfig(String[] args) throws Exception {
		File dir = args.length > 0 ? new File(args[0]).getAbsoluteFile().getParentFile() : new File(".");
		log("Current Dir:" + dir.getCanonicalPath());
		File srcDir = new File(dir, "src");
		if (srcDir.exists() && srcDir.isDirectory()) {

		} else {
			log("'src' dir not found, exiting...");
			return null;
		}
		String prjName = dir.getCanonicalFile().getName();
		log("user default project name:" + prjName);
		Map m = new HashMap();
		m.put("baseDir", ".");
		m.put("destDir", ".");
		m.put("debug", "true");
		m.put("prjs", (List) PyData.parseAll(String.format("[ [ %s , . ],  ]", prjName)));
		return m;
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		System.out.println("neoebuild " + VER);
		System.out.println("args:"+Arrays.toString(args));
		Map param = makeDefaultEmptyConfig(args);
		if (args.length == 0) {
			if (param == null)
				return;
		} else {
			param.putAll((Map) PyData.parseAll(readString(new FileInputStream(args[0]), "utf8")));
		}
		System.out.println(param.toString());
		String pb1 = (String) param.get("baseDir");
		String destDir = (String) param.get("destDir");
		String javaHome = (String) param.get("javaHome");
		if (destDir == null)
			destDir = ".";
		if (javaHome == null) {
			String javaPath = new FindJDK().find(0, true);
			if (!javaPath.isEmpty()) {
				log("found latest JDK:" + javaPath);
				javaHome = javaPath;
			} else {
				log("didnot found JDK");
			}
		}
		Object prjs = param.get("prjs");
		Projects prjs1 = new Projects();
		prjs1.verbose = true;
		prjs1.addPrjs((List) prjs);
		prjs1.baseDir = args.length == 0 ? "." : addPath(new File(args[0]).getParent(), pb1).getCanonicalPath();
		prjs1.javaHome = javaHome;
		if (args.length > 1 && args[1].equals("clean"))
			new BuildAll(param).clean(prjs1);
		new BuildAll(param).build(prjs1, destDir);
		log("program end.");
	}

	public static void log(String s) {
		Log.log(s);
	}

	public static String readString(InputStream ins, String enc) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(ins, enc));
		char[] buf = new char[1000];
		int len;
		StringBuffer sb = new StringBuffer();
		while ((len = in.read(buf)) > 0) {
			sb.append(buf, 0, len);
		}
		in.close();
		return sb.toString();
	}
}
