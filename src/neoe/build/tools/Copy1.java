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

	private static final long CHECK_SIZE = 20000000;
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
	public boolean continueWhenError;
	private long t1, totalCopyBS, lastSizeDiv, lastBs, t0;
	private boolean debugSpeed;

	public int execute() throws IOException {
		t0 = t1 = System.currentTimeMillis();
		totalCopyBS = 0;
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
		try {
			if (U.isNewer(src, target)) {
				doCopy(src, target);
				if (debugSpeed)
					doDebugSpeed();
			}
		} catch (IOException ex) {
			if (continueWhenError) {
				System.err.println(ex);
			} else {
				throw ex;
			}
		}
	}

	private void doDebugSpeed() {
		long sizeDiv = totalCopyBS / CHECK_SIZE;
		if (sizeDiv > lastSizeDiv) {
			long t2 = System.currentTimeMillis();
			long t = t2 - t1;
			if (t > 0) {
				long bs = totalCopyBS - lastBs;
				lastBs = totalCopyBS;
				t1 = t2;
				lastSizeDiv = sizeDiv;
				Log.log(String.format("[D]speed %,dKB/s, cnt=%,d", bs / t, cnt));
			}
		}
	}

	private void doCopy(File src, File target) throws IOException {
		target.getParentFile().mkdirs();
		if (prj.prjs != null && prj.prjs.verbose)
			Log.log("[I]" + prj.name + ":copy " + src.getCanonicalPath() + " -> " + target.getCanonicalPath());
		FileInputStream in = null;
		FileOutputStream out = null;
		try {
			in = new FileInputStream(src);
			out = new FileOutputStream(target);
			long size = copy(in, out);
			prj.prjs.totalCopyBS += size;
			totalCopyBS += size;
			in.close();
			out.close();
			target.setLastModified(src.lastModified());
			// Files.copy(src.toPath(), target.toPath(),
			// StandardCopyOption.COPY_ATTRIBUTES,
			// StandardCopyOption.REPLACE_EXISTING);
			cnt++;
		} finally {
			if (in != null)
				in.close();
			if (out != null)
				out.close();
		}

	}

	public static long copy(InputStream in, OutputStream outstream) throws IOException {
		BufferedOutputStream out = new BufferedOutputStream(outstream);
		byte[] buf = new byte[1024 * 10];
		long total = 0;
		int len;
		while ((len = in.read(buf)) > 0) {
			out.write(buf, 0, len);
			total += len;
		}
		in.close();
		out.close();
		return total;
	}

	public void setFile(File file) {
		fs.clear();
		this.file = file;

	}

	public static void main(String[] args) throws IOException {

		// test , copy a dir
		String from = args[0];
		String to = args[1];
		Copy1 copy = new Copy1();
		Projects prjs = new Projects();
		Project1 prj = new Project1(prjs);
		prj.name = "testcopy";
		copy.setProject(prj);
		copy.setTodir(new File(to));
		copy.continueWhenError = true;
		copy.debugSpeed = true;
		FileSet1 fs = new FileSet1();
		fs.setDir(new File(from));
		copy.addFileset(fs);
		int cnt2 = copy.execute();
		long t2 = System.currentTimeMillis();
		long t = t2 - copy.t0;
		Log.log(String.format("%s:copy %d resources, %,d bytes in %,dms (%,dKB/s)", prj.name, cnt2, copy.totalCopyBS, t,
				t == 0 ? 0 : copy.totalCopyBS / t));
	}

}
