package neoe.build.tools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Map;

import neoe.build.BuildMain;
import neoe.build.util.FileIterator;
import neoe.build.util.Log;

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

	public static int writeFileList(File outf, Path1 srcdirs, File destdir, Project1 prj) throws Exception {
		int cnt = 0;
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outf), "UTF-8"));

		for (String srcdir : srcdirs.sub) {
			String base = new File(srcdirs.basePath, srcdir).getCanonicalPath().replace('\\', '/');
			if (!base.endsWith("/")) {
				base = base + "/";
			}
			// System.out.println("base=" + base);
			for (File f : new FileIterator(new File(base).getAbsolutePath())) {
				String fn = f.getName();
				if (fn.equals("package-info.java"))
					continue;
				if (f.isFile() && fn.endsWith(".java")) {
					String fn1 = f.getCanonicalPath().replace('\\', '/');
					if (!fn1.startsWith(base)) {
						Log.log("[W]Cannot list java file, please check:" + fn1);
						continue;
					}
					String fn2 = fn1.substring(base.length());
					File cls = new File(destdir, fn2.substring(0, fn2.length() - 5) + ".class");
					// System.out.println("check " + cls.getCanonicalPath());
					if (!isNewer(f, cls)) {
						// Log.log("[D]skip compiled " + fn1);
						prj.skipJavac++;
						continue;
					}
					cnt++;
					out.write(fn1);
					out.write("\n");
				}
			}
		}
		out.close();
		return cnt;

	}

	public static void writeManifest(File mf, Map<String, String> manifest) throws IOException {
		PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(mf), "utf8"));
		out.println("Manifest-Version: 1.0");
		out.println("Build-By: neoebuild " + BuildMain.VER);
		for (String k : manifest.keySet()) {
			out.println(String.format("%s: %s", k, manifest.get(k)));
		}
		out.close();
	}

	public static String[] getCntAndSkip(int cnt, int skip) {
		String s1 = cnt > 0 ? String.format("%,d files", cnt) : "";
		String s2 = skip > 0 ? "skip " + String.format("%,d files", skip) : "";
		if (s1.length() > 0 && s2.length() > 0)
			s2 = ", " + s2;
		return new String[] { s1, s2 };
	}

}
