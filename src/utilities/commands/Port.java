package utilities.commands;

import core.Client;
import utilities.IO;

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
public class Port extends Command {
	public Port() {}

	@Override
	public void exec() {
		System.out.println("The default port is 69.");
		String newPort = IO.input("Enter the new port to use > ");
		if (IO.isInteger(newPort)) {
			Client.setPort(Integer.valueOf(newPort));
			System.out.println("Port has been set to " + Client.getPort() + ".");
		} else {
			System.out.println("Failed to change the destination port.");
			System.out.println("Port is currently " + Client.getPort() + ".");

		}
	}
}
