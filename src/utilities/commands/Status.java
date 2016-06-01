package utilities.commands;

import core.Client;

import java.net.InetAddress;
import java.net.UnknownHostException;

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
public class Status extends Command {

	public Status() {
		
	}

	@Override
	public void exec() {
		try {
			System.out.println("Destination Address: " + InetAddress.getLocalHost());
			System.out.println("Destination Port: " + Client.getPort());
			System.out.println("Mode: NetASCII");
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
