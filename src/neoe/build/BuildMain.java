package neoe.build;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.taskdefs.Jar;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.taskdefs.Javac;
import org.apache.tools.ant.taskdefs.Manifest;
import org.apache.tools.ant.taskdefs.Manifest.Attribute;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;

import neoe.util.PyData;

public class BuildMain {

	public static class BuildAll {

		private Set<String> built, toBuild;
		private Projects prjs;
		private int turnNo;
		private Project project;
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
			System.out.println("total " + toBuild);
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
						log("build fail:"+t.getId());
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

			// System.out.println("Path="+path.getAbsolutePath());
			project = new Project();
			project.setName(prjName);
			Javac javac = new Javac();
			javac.setProject(project);
			javac.setTarget(getParam("target", "1.7"));
			javac.setSource(getParam("source", "1.7"));
			javac.setEncoding(getParam("encoding", "utf-8"));
			javac.setDebug(new Boolean(getParam("debug", "false")));
			File srcDir = new File(path.getAbsolutePath(), "/src");
			if (!srcDir.exists()) { // check /src
				throw new RuntimeException("src dir not found:" + srcDir.getAbsolutePath());
			}
			javac.setSrcdir(new Path(project, path.getAbsolutePath() + "/src"));
			File buildDir = new File(path.getAbsolutePath() + "/build");
			buildDir.mkdirs();
			javac.setDestdir(buildDir);
			Path cp = new Path(project);
			if (prj.cp != null) {
				for (Object o : prj.cp) {
					File f1 = addPath(prjs.baseDir, o.toString());
					if (f1.isDirectory()) {
						// v1.5 : cp can be dir ,eg lib dir
						File[] fs = f1.listFiles();
						for (File f : fs) {
							if (f.getName().endsWith(".jar")) {
								// System.out.println("[D]add
								// "+f.getAbsolutePath());
								cp.add(new Path(project, f.getAbsolutePath()));
							}
						}
					} else {
						cp.add(new Path(project, addPath(prjs.baseDir, o.toString()).getAbsolutePath()));
					}
				}
			}
			if (prj.depends != null) {
				for (Object o : prj.depends) {
					Prj p1 = prjs.m.get(o.toString());
					String po = addPath(prjs.baseDir, p1.dir).getAbsolutePath() + "/dist/" + p1.name + ".jar";
					cp.add(new Path(project, po));

				}
			}
			// System.out.println(cp);
			javac.setClasspath(cp);
			// javac.setCompiler("javac1.7");
			// javac.setFork(true);
			javac.execute();
			if (javac.getFileList().length == 0) {
				log(prjName + ":no more to compile");
			} else {
				log(prjName + ":compile files (" + javac.getFileList().length + ")");
			}
			// copy resources
			log(prjName + ":copy resources");
			Copy copy = new Copy();
			copy.setProject(project);
			copy.setTodir(buildDir);
			FileSet fs = new FileSet();
			fs.setDir(new File(path.getAbsolutePath() + "/src"));
			fs.setExcludes("**/*.java");
			copy.addFileset(fs);
			copy.execute();

			Jar jar = new Jar();
			jar.setProject(project);
			File jarFile = new File(path.getAbsolutePath() + "/dist/" + prjName + ".jar");
			jarFile.getParentFile().mkdirs();
			jar.setDestFile(jarFile);
			jar.setBasedir(buildDir);
			if (prj.mainClass != null) {
				Manifest newManifest = new Manifest();
				newManifest.addConfiguredAttribute(new Attribute("Main-Class", prj.mainClass));
				jar.addConfiguredManifest(newManifest);
			}
			jar.execute();
			// log(prjName+":build finish");
			copyTo(prj, destDir);

			if (prj.run != null) {
				Java run = new Java();
				cp.add(new Path(project, jarFile.getAbsolutePath()));
				run.setError(new File("error.log"));
				run.setClasspath(cp);
				run.setProject(project);
				for (Object o : prj.run) {
					List row = (List) o;
					run.setClassname((String) row.get(0));
					for (Object o1 : (List) row.get(2)) {
						run.createArg().setValue(o1.toString());
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

		public void clean(Projects prjs) {
			for (Prj prj : prjs.m.values()) {
				String path = addPath(prjs.baseDir, prj.dir).getAbsolutePath();
				deleteDirectory(new File(path + "/dist"), 0);
				deleteDirectory(new File(path + "/build"), 0);
			}
		}

		public void copyTo(Prj prj, String dest) {
			File destDir = addPath(prjs.baseDir, dest);
			destDir.mkdirs();
			// for (Prj prj : prjs.m.values()) {
			String path = addPath(prjs.baseDir, prj.dir).getAbsolutePath();
			Copy copy = new Copy();
			copy.setProject(project);
			copy.setFile(new File(path + "/dist/" + prj.name + ".jar"));
			copy.setTodir(destDir);
			copy.execute();
			// }
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

	static public boolean deleteDirectory(File path, int lv) {
		if (lv == 0)
			System.out.println("delete " + path.getAbsolutePath());
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
			System.out.println("delete " + path.getAbsolutePath() + " " + path.delete());
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

	public static class Projects {

		private Map<String, Prj> m;
		public String baseDir = "";
		boolean multithread = true;

		public Projects() {
			m = new HashMap<String, Prj>();
		}

		public void addPrjs(List prjs) {
			for (int i = 0; i < prjs.size(); i++) {
				if (!(prjs.get(i) instanceof List)) {
					continue;
				}
				List list = (List) prjs.get(i);
				Prj prj = new Prj();
				m.put((String) list.get(0), prj);
				prj.name = (String) list.get(0);
				prj.dir = (String) list.get(1);
				if (list.size() >= 3) {
					Map m = (Map) list.get(2);
					prj.depends = (List) m.get("dep");
					prj.cp = (List) m.get("cp");
					prj.mainClass = (String) m.get("main");
					prj.run = (List) m.get("run");
				}
			}
		}

	}

	static Map makeDefaultEmptyConfig() throws Exception {
		File dir = new File(".");
		log("Current Dir:" + dir.getAbsolutePath());
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
		System.out.println("neoebuild v151104");
		Map param = null;
		if (args.length == 0) {
			param = makeDefaultEmptyConfig();
			if (param == null)
				return;
		} else {
			param = new HashMap();
			param.putAll((Map) PyData.parseAll(readString(new FileInputStream(args[0]), "utf8")));
		}
		System.out.println(param);
		String pb1 = (String) param.get("baseDir");
		String destDir = (String) param.get("destDir");
		if (destDir == null)
			destDir = ".";
		Object prjs = param.get("prjs");
		Projects prjs1 = new Projects();
		prjs1.addPrjs((List) prjs);
		prjs1.baseDir = pb1;
		if (args.length > 1 && args[1].equals("clean"))
			new BuildAll(param).clean(prjs1);
		new BuildAll(param).build(prjs1, destDir);
		log("program end.");
	}

	public static void log(String s) {
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		System.out.println("[" + sdf.format(new Date()) + "]" + s);
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
