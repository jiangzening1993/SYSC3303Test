package utilities.commands;

/**
 * Carleton University
 * Department of Systems and Computer Engineering
 * SYSC 3303 - Real-Time and Concurrent Systems
 * <p>
 * Collaborative Project
 * <p>
 * Package: utilities.commands
 * Created: 2016-05-30
 * License: MIT
 * <p>
 * Authors:
 * Christopher McMorran    (100968013)
 * Yue Zhang               (100980408)
 * Raiyan Quaium           (100962217)
 */
public class ListFiles extends Command {
	@Override
	public void exec() {
		try {
			java.io.File files[] = new java.io.File(".").listFiles();
			for (java.io.File f : files) if (!f.isDirectory()) System.out.println(f.getName());
		} catch (Exception e) {
			System.out.println("You do not have access to read from this directory.");
		}
	}
}
