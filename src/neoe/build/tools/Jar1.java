package neoe.build.tools;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import neoe.util.FindJDK;

public class Jar1 {

	private Project1 prj;
	private File dest;
	private File base;
	private Map<String, String> manifest;

	public Jar1() {
		manifest = new HashMap<String, String>();
	}

	public void setProject(Project1 project) {
		this.prj = project;

	}

	public void setDestFile(File jarFile) {
		this.dest = jarFile;

	}

	public void setBasedir(File buildDir) {
		this.base = buildDir;

	}

	public void addConfiguredManifest(String key, String value) {
		manifest.put(key, value);

	}

	public void execute() throws Exception {
		Exec exec = new Exec(prj);
		exec.setCmd(prj.prjs.javaHome + (FindJDK.isWindows ? "/bin/jar.exe" : "/bin/jar"));
		// jar cvfm classes.jar mymanifest -C foo/ .
		if (manifest.isEmpty()) {
			exec.addArg("cf");
			exec.addArg(dest.getAbsolutePath());
			exec.addArg("-C", base.getAbsolutePath());
			exec.addArg(".");
		} else {
			exec.addArg("cf");
			exec.addArg(dest.getAbsolutePath());
			File mf = U.getTempFile("manifest");
			U.writeManifest(mf, manifest);
			exec.addArg(mf.getAbsolutePath());
			exec.addArg("-C", base.getAbsolutePath());
			exec.addArg(".");
		}
		exec.execute();

	}

}
