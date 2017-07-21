package neoe.build.tools;

import java.util.ArrayList;
import java.util.List;

import neoe.util.FindJDK;

public class Path1 {

//	private Project1 prj;
	private List<String> sub;

	public Path1(Project1 project) {
//		this.prj = project;
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
			if (FindJDK.isWindows) sb.append('"');
			sb.append(p1);
			if (FindJDK.isWindows) sb.append('"');
		}
		return sb.toString();
	}

}
