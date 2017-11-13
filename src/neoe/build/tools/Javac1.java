package neoe.build.tools;

import java.io.File;
import java.util.List;

import neoe.util.Log;

public class Javac1 {
	Project1 prj;
	// boolean fork;
	String target;
	String source;
	String encoding;
	boolean debug;
	Path1 srcdir;
	String destdir;
	String executable;

	public void setExecutable(String executable) {
		this.executable = executable;
	}

	Path1 classpath;
	private List opt;

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
		// exec.verbose = true;
		exec.setCmd(executable);
		if (opt != null) {
			for (Object o : opt) {
				exec.addArg(o.toString());
			}
		}
		if (!debug) {
			exec.addArg("-g:none");
		}

		if (destdir != null) {
			new File(destdir).mkdirs();
			exec.addArg("-d", enclosePath(destdir));
		}

		if (encoding != null) {
			exec.addArg("-encoding", encoding);
		}

		if (source != null) {
			exec.addArg("-source", source);
		}
		if (srcdir != null) {
			// exec.addArg("-sourcepath", enclosePath(srcdir));
			String s = srcdir.toCommandlineString();
			if (!s.isEmpty())
				exec.addArg("-sourcepath", enclosePath(s));
		}
		if (target != null) {
			exec.addArg("-target", target);
		}

		if (classpath != null) {
			String cp = classpath.toCommandlineString();
			if (!cp.isEmpty())
				exec.addArg("-cp", enclosePath(cp));
		}

		File f = U.getTempFile("filelist");
		int cnt = U.writeFileList(f, srcdir, new File(destdir), prj);
		prj.prjs.totalJavac += cnt;
		prj.prjs.totalSkipJavac += prj.skipJavac;
		if (cnt <= 0) {
			f.delete();
			return 0;
		}
		exec.addArg(enclosePath("@" + f.getCanonicalPath()));
		int code = exec.execute();
		f.delete();
		if (code != 0) {
			throw new RuntimeException("javac error");
		}
		return cnt;
	}

	public static String enclosePath(String s) {
		int p1 = s.indexOf(' ');
		if (p1 < 0)
			return s;
		return "\"" + s + "\"";
	}

	public void setProject(Project1 prj) {
		this.prj = prj;
	}

	public void setSrcdir(Path1 string) {
		this.srcdir = string;
	}

	public void setOpt(List opt) {
		if (opt != null) {
			this.opt = opt;
		}

	}

}
