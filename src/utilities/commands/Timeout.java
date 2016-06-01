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
public class Timeout extends Command {
	@Override
	public void exec() {
		System.out.println("By default, the timeout is set to 5000 ms (5 seconds).");
		String val = IO.input("Enter new timeout in milliseconds > ");
		if (IO.isInteger(val)) {
			Client.setTimeout(Integer.valueOf(val));
			System.out.println("The timeout has been set to " + Client.getTimeout() + " milliseconds.");
		} else {
			System.out.println("Invalid timeout.");
			System.out.println("The timeout will remain to be " + Client.getTimeout() + " milliseconds.");
		}
	}
}
