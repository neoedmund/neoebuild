package neoe.build;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * BuildBox is a GUI, drop a build script into it and it will build.
 *
 */
public class BuildBox {

	public static void main(String[] args) {
		new BuildBox().run();

	}

	private JCheckBox jcb;

	public void run() {
		JFrame frame = new JFrame("NeoeBuildBox");
		JLabel lb = new JLabel("<html>Drop a neoebuild script here and get built.</html>");
		lb.setPreferredSize(new Dimension(200, 120));
		lb.setDropTarget(new DropTarget() {
			public synchronized void drop(DropTargetDropEvent evt) {
				try {
					evt.acceptDrop(DnDConstants.ACTION_COPY);
					List<File> droppedFiles = (List<File>) evt.getTransferable()
							.getTransferData(DataFlavor.javaFileListFlavor);
					if (!droppedFiles.isEmpty()) {
						File f = droppedFiles.get(0);
						tryBuild(f);
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});

		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		JPanel jp = new JPanel(new BorderLayout());
		jcb = new JCheckBox("Clear before build");
		jp.add(new JLabel("NeoeBuild " + BuildMain.VER), BorderLayout.NORTH);
		jp.add(lb, BorderLayout.CENTER);
		lb.setBackground(Color.WHITE);
		lb.setOpaque(true);
		jp.add(jcb, BorderLayout.SOUTH);
		frame.getContentPane().add(jp);
		frame.setAlwaysOnTop(true);
		frame.pack();
		frame.setVisible(true);

	}

	protected void tryBuild(File f) {
		try {
			if (jcb.isSelected()) {
				BuildMain.main(new String[] { f.getAbsolutePath(), "clean" });
			} else {
				BuildMain.main(new String[] { f.getAbsolutePath() });
			}
			JOptionPane.showMessageDialog(null, "Build end, see console for details");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
