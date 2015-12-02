package neoe.build.tools;

import java.util.ArrayList;
import java.util.List;

import neoe.util.FindJDK;

public class Java1 {

	private List<String> args;

	public Java1() {
		args = new ArrayList<String>();
	}

	private Path1 cp;
	private Project1 prj;
	private String clsName;

	public void setClasspath(Path1 cp) {
		this.cp = cp;

	}

	public void setProject(Project1 project) {
		this.prj = project;
	}

	public void setClassname(String cls) {
		this.clsName = cls;

	}

	public void execute() throws Exception {
		Exec e = new Exec(prj);
		e.setCmd(prj.prjs.javaHome + (FindJDK.isWindows ? "/bin/java.exe" : "/bin/java"));
		if (cp!=null){
			e.addArg("-cp", cp.toCommandlineString());
		}
		if (clsName!=null){
			e.addArg(clsName);
		}
		for (String s: args){
			e.addArg(s);
		}
		e.execute();
		prj.prjs.totalJava++;
	}

	public void addArg(String s) {
		args.add(s);
	}

}
