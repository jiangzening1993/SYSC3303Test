package utilities.commands;

import protocol.*;
import utilities.File;
import utilities.IO;

import java.net.InetAddress;
import java.net.SocketException;
import java.util.Vector;

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
public class Put extends Command {
	private int port;
	private InetAddress dest;

	public Put(int port, InetAddress dest) {
		this.port = port;
		this.dest = dest;
	}

	/**
	 * This method is called when an error packet is received. It deals with errors in different ways, depending on the type of error.
	 *
	 * @param permission The ACK Packet to get the error info from.
	 * @return True if an error occurs.
	 */

	protected static boolean handleError(ACKPacket permission) {
		if (permission.getOPCode() != 4) return false;
		ErrorPacket error = new ErrorPacket(permission);
		switch ((int) error.getError()) {
			case 0:
				System.out.println("Undefined error.");
				return true;
			case 1:
				System.out.println("File not found.");
				return true;
			case 2:
				System.out.println("Access violation.");
				return true;
			case 3:
				System.out.println("Disk full.");
				return true;
			case 4:
				System.out.println("Illegal operation.");
				return true;
			case 5:
				System.out.println("Unknown Transfer ID.");
				return true;
			case 6:
				System.out.println("File already exists.");
				return true;
			case 7:
				System.out.println("No such user.");
				return true;
			default:
				return false; // No idea what happened.
		}
	}

	@Override
	public synchronized void exec() {
		String fileName = IO.input("Enter the file name > ");
		System.out.println("Leave blank to specify the default directory.");
		String path = IO.input("Enter the path to write to on the host server > ");
		File file = new File(fileName);

		// Add the path to the file properly.
		if (!path.isEmpty()) {
			if (System.getProperty("os.name").contains("Win")) {
				if (!path.endsWith("\\")) path += "\\";
			} else {
				if (!path.endsWith("/")) path += "/";
			}
			fileName = path + fileName;
			fileName = fileName.trim();
		}

		System.out.println("Sending file...");

		// Check for file properties.
		if (!file.exists()) {
			System.out.println("The specified file does not exist.");
			return;
		} else if (!file.canRead()) {
			System.out.println("Cannot read the specified file.");
			return;
		} else if (file.isDirectory()) {
			System.out.println("Cannot send a directory.");
			return;
		}

		// Attempt to open a socket.
		TFTPSocket socket = null;
		try {
			socket = new TFTPSocket();
		} catch (SocketException e) {
			e.printStackTrace();
		}

		// Assume that the file is valid.
		WriteRequestPacket wrq = new WriteRequestPacket(fileName, dest, port);
		socket.send(wrq);

		ACKPacket permit = socket.receiveResponse();

		// We must ensure that communication occurs with a port other than 69.
		port = permit.getPort();

		if (permit.getData()[1] == (byte) 4) {
			Vector<byte[]> bytes = file.toVectorOfByteArrays();
			int block = 1;
			while (true) {
				socket.send(new DataPacket(block, bytes.get(block - 1), dest, port));
				ACKPacket reply = socket.receiveResponse();
				if (reply.getBlockNumber() == block) block++;

				if (block >= bytes.size()) break;

			}
		}

		System.out.println("File transfer complete.");

	}
}
