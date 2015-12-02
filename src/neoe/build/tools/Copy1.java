package neoe.build.tools;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import neoe.util.Log;

public class Copy1 {

	private Project1 prj;
	private File todir;

	List<FileSet1> fs;
	private File file;

	public Copy1() {
		fs = new ArrayList<FileSet1>();
	}

	public void setProject(Project1 project) {
		this.prj = project;

	}

	public void setTodir(File dir) {
		todir = dir;
	}

	public void addFileset(FileSet1 fs1) {
		fs.add(fs1);

	}

	int cnt;

	public int execute() throws IOException {
		cnt = 0;
		if (file != null) {
			copyFile(file);
		}
		for (FileSet1 fs1 : fs) {
			copyFileSet(fs1);
		}
		// if (cnt > 0) Log.log(String.format("[I]%s:Copied %,d files.",
		// prj.name, cnt));
		prj.prjs.totalCopy += cnt;
		return cnt;
	}

	private void copyFileSet(FileSet1 fs1) throws IOException {
		for (File f : fs1) {
			copyRel(fs1.dir, f);
		}

	}

	private void copyRel(File base, File src) throws IOException {

		String s1 = src.getParentFile().getCanonicalPath();
		String s2 = base.getCanonicalPath();
		if (!s1.startsWith(s2)) {
			U.err(prj.name + ":cannot copyRel, please check file:" + src.getCanonicalPath());
			return;
		}
		String rel = s1.substring(s2.length()).replace('\\', '/');
		if (rel.startsWith("/"))
			rel = rel.substring(1);
		if (!rel.isEmpty() && !rel.endsWith("/"))
			rel = rel + "/";
		File target = new File(todir, rel + src.getName());
		copyOneFile(src, target);
	}

	private void copyFile(File src) throws IOException {
		if (!src.exists()) {
			U.err(prj.name + ":warning:file not exists for copy:" + src.getCanonicalPath());
			return;
		}
		if (src.isFile()) {
			File target = new File(todir, src.getName());
			copyOneFile(src, target);
		} else if (src.isDirectory()) {
			FileSet1 fs1 = new FileSet1();
			fs1.setDir(src);
			copyFileSet(fs1);
		} else {
			U.err(prj.name + ":warning:file not copied, please check:" + src.getCanonicalPath());
			return;
		}

	}

	private void copyOneFile(File src, File target) throws IOException {

		if (U.isNewer(src, target)) {
			doCopy(src, target);
		}
	}

	private void doCopy(File src, File target) throws IOException {
		target.getParentFile().mkdirs();
		if (prj.prjs.verbose)
			Log.log("[I]" + prj.name + ":copy " + src.getCanonicalPath() + " -> " + target.getCanonicalPath());
		FileOutputStream out = new FileOutputStream(target);
		FileInputStream in = new FileInputStream(src);
		copy(in, out);
		in.close();
		out.close();
		target.setLastModified(src.lastModified());
		// Files.copy(src.toPath(), target.toPath(),
		// StandardCopyOption.COPY_ATTRIBUTES,
		// StandardCopyOption.REPLACE_EXISTING);
		cnt++;

	}

	public static void copy(InputStream in, OutputStream outstream) throws IOException {
		BufferedOutputStream out = new BufferedOutputStream(outstream);
		byte[] buf = new byte[1024 * 10];
		int len;
		while ((len = in.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
		in.close();
		out.close();
	}

	public void setFile(File file) {
		fs.clear();
		this.file = file;

	}

}
