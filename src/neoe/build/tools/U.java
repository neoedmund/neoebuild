package neoe.build.tools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import neoe.util.FileIterator;
import neoe.util.Log;

public class U {
	public static final long ignoreMs = 20;

	public static boolean isNewer(File src, File target) {
		if (!target.exists())
			return true;
		long t2 = target.lastModified();
		long t1 = src.lastModified();
		return t1 > t2 + ignoreMs;
	}

	public static void err(String s) {
		System.err.println(s);
		Log.log("[E]" + s);
	}

	public static File getTempFile(String fn) throws IOException {
		return File.createTempFile(fn, null);
		// return new File(fn+".tmp");
	}

	public static int writeFileList(File outf, File srcdir, File destdir) throws Exception {
		int cnt = 0;
		String base = srcdir.getAbsolutePath().replace('\\', '/');
		if (!base.endsWith("/")) {
			base = base + "/";
		}
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outf), "UTF-8"));
		for (File f : new FileIterator(srcdir.getAbsolutePath())) {
			String fn = f.getName();
			if (f.isFile() && fn.endsWith(".java")) {
				String fn1 = f.getAbsolutePath().replace('\\', '/');
				if (!fn1.startsWith(base)) {
					Log.log("[W]Cannot list java file, please check:" + fn1);
					continue;
				}
				String fn2 = fn1.substring(base.length());
				File cls = new File(destdir, fn2.substring(0, fn2.length() - 5) + ".class");
				//System.out.println("check "+cls.getAbsolutePath());
				if (!isNewer(f, cls)) {
					//Log.log("[D]skip compiled " + fn1);
					continue;
				}
				cnt++;
				out.write(fn1);
				out.write("\n");
			}
		}
		out.close();
		return cnt;

	}

	public static void writeManifest(File mf, Map<String, String> manifest) {
		// TODO Auto-generated method stub

	}

}
