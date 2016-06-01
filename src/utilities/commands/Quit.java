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
public class Quit extends Command {
	@Override
	public void exec() {
		if (TFTPSocket.isDebug()) {
			System.out.println("User called exit.");
		}
		System.exit(0);
	}
}
