package neoe.build.tools;

import java.util.ArrayList;
import java.util.List;

import neoe.util.Log;

public class Exec {
	private Project1 prj;

	public Exec(Project1 prj) {
		this.prj = prj;
	}

	List<String> sb;

	public void setCmd(String executable) {
		sb = new ArrayList<>();
		sb.add(executable);
	}

	public void addArg(String s) {
		sb.add(s);
	}
	public void addArg(String s1, String s2) {
		sb.add(s1);
		sb.add(s2);
	}


	public void execute() throws Exception {
		Log.log("[I]" + prj.name + ":Exec:" + sb);
		Process p = new ProcessBuilder().inheritIO().command(sb).start();
		p.waitFor();
	}

}
