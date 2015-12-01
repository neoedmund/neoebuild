package neoe.build.tools;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
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
		if (cnt > 0)
			Log.log(String.format("[I]%s:Copied (%,d) files.", prj.name, cnt));
		return cnt;
	}

	private void copyFileSet(FileSet1 fs1) throws IOException {
		for (File f : fs1) {
			copyRel(fs1.dir, f);
		}

	}

	private void copyRel(File base, File src) throws IOException {

		String s1 = src.getParentFile().getAbsolutePath();
		String s2 = base.getAbsolutePath();
		if (!s1.startsWith(s2)) {
			U.err(prj.name + ":cannot copyRel, please check file:" + src.getAbsolutePath());
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
			U.err(prj.name + ":warning:file not exists for copy:" + src.getAbsolutePath());
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
			U.err(prj.name + ":warning:file not copied, please check:" + src.getAbsolutePath());
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
		System.out.println("copy " + src.getAbsolutePath() + " -> " + target.getAbsolutePath());
		Files.copy(src.toPath(), target.toPath(), StandardCopyOption.COPY_ATTRIBUTES,
				StandardCopyOption.REPLACE_EXISTING);
		cnt++;
	}

	public void setFile(File file) {
		fs.clear();
		this.file = file;

	}

}
