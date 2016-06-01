package utilities.commands;

import protocol.TFTPSocket;

/**
 * Carleton University
 * Department of Systems and Computer Engineering
 * SYSC 3303 - Real-Time and Concurrent Systems
 * <p>
 * Collaborative Project
 * <p>
 * Package: utilities.commands
 * Created: 2016-05-29
 * License: MIT
 * <p>
 * Authors:
 * Christopher McMorran    (100968013)
 * Yue Zhang               (100980408)
 * Raiyan Quaium           (100962217)
 */
public class Debug extends Command {
	@Override
	public void exec() {
		if (TFTPSocket.isDebug()) {
			TFTPSocket.setDebug(false);
			System.out.println("Debugging is now off.");
		} else {
			TFTPSocket.setDebug(true);
			System.out.println("Debugging is now on.");
		}
	}
}
