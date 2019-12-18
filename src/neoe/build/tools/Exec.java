package neoe.build.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import neoe.build.util.Log;

public class Exec {
	private Project1 prj;

	public Exec(Project1 prj) {
		this.prj = prj;
	}

	List<String> sb;
	public boolean verbose;

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

	public int execute() throws Exception {
		if (prj.prjs.verbose || verbose)
			Log.log("[I]" + prj.name + "@" + prj + ":Exec:" + String.join(" ", sb));
		Process p = new ProcessBuilder().command(sb).start();
		StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream(), "stderr");
		StreamGobbler outputGobbler = new StreamGobbler(p.getInputStream(), "stdout");
		outputGobbler.start();
		errorGobbler.start();
		return p.waitFor();
	}

	private class StreamGobbler extends Thread {
		InputStream is;
		String type;
		private PrintWriter out;

		private StreamGobbler(InputStream is, String type) {
			this.is = is;
			this.type = type;
			this.out = Log.getWriter();
		}

		@Override
		public void run() {
			try {
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);
				String line = null;
				while ((line = br.readLine()) != null)
					out.println(type + "> " + line);
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}

}
