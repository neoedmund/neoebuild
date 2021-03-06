package neoe.build.tools;

import java.util.ArrayList;
import java.util.List;

import neoe.build.util.FindJDK;

public class Path1 {

	// private Project1 prj;
	public List<String> sub;
	public String basePath;

	public Path1(Project1 project) {
		// this.prj = project;
		sub = new ArrayList<String>();
	}

	public Path1(Project1 project, String path) {
		this(project);
		add(path);
	}

	public void add(String path) {
		sub.add(path);
	}

	public String toCommandlineString() {
		char sep = FindJDK.isWindows ? ';' : ':';
		StringBuilder sb = new StringBuilder();
		for (String p1 : sub) {
			if (sb.length() > 0) {
				sb.append(sep);
			}
			if (FindJDK.isWindows)
				sb.append('"');
			if (basePath != null) {
				sb.append(basePath).append('/');
			}
			sb.append(p1);
			if (FindJDK.isWindows)
				sb.append('"');
		}
		return sb.toString();
	}

	public void addArg(Exec exec, String arg) {
		for (String p1 : sub) {
			if (basePath != null) {
				exec.addArg(arg, Javac1.enclosePath(basePath + "/" + p1));
			} else {
				exec.addArg(arg, Javac1.enclosePath(p1));
			}
		}
	}

}
