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
	private static final int CHECK_CNT_SIZE = 20000;
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

	int cnt, lastCnt;
	public boolean continueWhenError;
	private long t1, totalCopyBS, lastSizeDiv, lastBs, t0, lastCntDiv;
	private boolean debugSpeed;
	public boolean isCopyJar;
	int cntjar;

	public int execute() throws IOException {
		t0 = t1 = System.currentTimeMillis();
		totalCopyBS = 0;
		cnt = 0;
		cntjar = 0;
		if (file != null) {
			copyFile(file);
		}
		for (FileSet1 fs1 : fs) {
			copyFileSet(fs1);
		}
		prj.prjs.totalCopy += cnt;
		prj.prjs.totalSkipResource += prj.skipResource;
		return cnt + cntjar;
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
			} else {
				prj.skipResource++;
			}
		} catch (IOException ex) {
			if (continueWhenError) {
				Log.log("[W]" + ex);
			} else {
				throw ex;
			}
		}
	}

	private void doDebugSpeed() {
		long sizeDiv = totalCopyBS / CHECK_SIZE;
		int cntDiv = cnt / CHECK_CNT_SIZE;
		if (sizeDiv > lastSizeDiv || cntDiv > lastCntDiv) {
			long t2 = System.currentTimeMillis();
			long t = t2 - t1;
			if (t > 0) {
				long bs = totalCopyBS - lastBs;
				int cnt1 = cnt - lastCnt;
				lastBs = totalCopyBS;
				t1 = t2;
				lastCnt = cnt;
				lastSizeDiv = sizeDiv;
				lastCntDiv = cntDiv;
				Log.log(String.format("[D]speed %,dKB/s, %,dKiloFile/sec, cnt=%,d", bs / t, cnt1 / t, cnt));
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

			in.close();
			out.close();
			target.setLastModified(src.lastModified());
			// Files.copy(src.toPath(), target.toPath(),
			// StandardCopyOption.COPY_ATTRIBUTES,
			// StandardCopyOption.REPLACE_EXISTING);
			if (!isCopyJar) {
				prj.prjs.totalCopyBS += size;
				totalCopyBS += size;
				cnt++;
			} else {
				prj.prjs.totalCopyBSJar += size;
				prj.prjs.totalCopyJar++;
				cntjar++;
			}

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

}
