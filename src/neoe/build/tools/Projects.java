package neoe.build.tools;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import neoe.build.BuildMain.Prj;

public class Projects {
	public Map<String, Prj> m;
	public String baseDir = "";
	public boolean multithread = true;
	public String javaHome;
	public boolean verbose;
	public int totalJavac;
	public int totalCopy;
	public int totalJava;
	public int totalJar;
	public long totalCopyBS;
	public long totalCopyBSJar;

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
				prj.srcDir = (List) m.get("srcDir");
			}
		}
	}
}
