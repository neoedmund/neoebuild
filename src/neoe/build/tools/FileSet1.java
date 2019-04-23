package neoe.build.tools;

import java.io.File;
import java.util.Iterator;

import neoe.build.util.FileIterator;

public class FileSet1 implements Iterable<File> {

	File dir;
	private String excludeEnds;
	public boolean ignoreEclipsePrjFile;

	public void setDir(File file) {
		this.dir = file;

	}

	public void setExcludesEndsWith(String substr) {
		this.excludeEnds = substr;

	}

	@Override
	public Iterator<File> iterator() {
		return new Iterator<File>() {
			Iterator<File> i;

			{
				i = new FileIterator(dir.getAbsolutePath()).iterator();
				checkNext();
			}

			File buf;

			@Override
			public boolean hasNext() {
				if (buf == null) {
					return false;
				}
				return true;
			}

			private void checkNext() {
				if (!i.hasNext()) {
					buf = null;
					return;
				}
				File f = i.next();
				boolean ok = true;
				while (true) {

					if (ok && f.isDirectory()) {
						ok = false;
					}
					if (ok && excludeEnds != null && f.isFile() && f.getName().endsWith(excludeEnds)) {
						ok = false;
					}
					if (ok && ignoreEclipsePrjFile
							&& (f.getName().equals(".classpath") || f.getName().equals(".project"))) {
						ok = false;
					}
					if (ok == false) {
						if (!i.hasNext()) {
							buf = null;
							return;
						}
						f = i.next();
						ok = true;
						continue;
					}
					break;
				}
				if (ok) {
					buf = f;
				} else {
					buf = null;
				}
			}

			@Override
			public File next() {
				File ret = buf;
				if (buf != null)
					checkNext();
				return ret;
			}
		};
	}

}
