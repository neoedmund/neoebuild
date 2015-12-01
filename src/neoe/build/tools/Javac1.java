package neoe.build.tools;

import java.io.File;

import neoe.util.Log;

public class Javac1 {
	Project1 prj;
	// boolean fork;
	String target;
	String source;
	String encoding;
	boolean debug;
	String srcdir;
	String destdir;
	String executable;

	public void setExecutable(String executable) {
		this.executable = executable;
	}

	Path1 classpath;

	public void setFork(boolean fork) {
		// this.fork = fork;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public void setDestdir(String destdir) {
		this.destdir = destdir;
	}

	public void setClasspath(Path1 classpath) {
		this.classpath = classpath;
	}

	public int execute() throws Exception {
		Exec exec = new Exec(prj);
		exec.setCmd(executable);

		if (!debug) {
			exec.addArg("-g:none");
		}
		if (destdir != null) {
			new File(destdir).mkdirs();
			exec.addArg("-d", destdir);
		}

		if (encoding != null) {
			exec.addArg("-encoding", encoding);
		}

		if (source != null) {
			exec.addArg("-source", source);
		}
		if (srcdir != null) {
			exec.addArg("-sourcepath", srcdir);
		}
		if (target != null) {
			exec.addArg("-target", target);
		}
		if (classpath != null) {
			exec.addArg("-cp", classpath.toCommandlineString());
		}

		File f = U.getTempFile("filelist");
		int cnt = U.writeFileList(f, new File(srcdir), new File(destdir));
		if (cnt==0){
			//Log.log(prj.name+":nothing to compile.");
			return cnt;
		}
		exec.addArg("@" + f.getAbsolutePath());
		exec.execute();
		return cnt;

	}

	public void setProject(Project1 prj) {
		this.prj = prj;
	}

	public void setSrcdir(String string) {
		this.srcdir = string;
	}

}
