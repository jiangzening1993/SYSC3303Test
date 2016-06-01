package utilities.commands;

import protocol.ACKPacket;
import protocol.DataPacket;
import protocol.ReadRequestPacket;
import protocol.TFTPSocket;
import utilities.File;
import utilities.IO;

import java.net.InetAddress;
import java.net.SocketException;

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
public class Get extends Command {
	private int port;
	private InetAddress dest;

	public Get(int port, InetAddress destination) {
		this.port = port;
		this.dest = destination;
	}

	@SuppressWarnings("resource")
	@Override
	public void exec() {
		TFTPSocket socket;
		String fileName = IO.input("Enter file name > ");
		File file = new File(fileName);

		// Validate the file.
		if (file.exists()) {
			String decision = IO.input("The specified file already exists. Would you like to overwrite?\nEnter yes/no > ");
			if (decision.trim().equalsIgnoreCase("no")) return;
		}
		if (file.isDirectory()) {
			System.out.println("Cannot transfer directories.");
			return;
		} else if (!new File(".").canWrite()) {
			System.out.println("You do not have the correct permissions.");
			return;
		}


		// Open the socket.

		try {
			socket = new TFTPSocket();
		} catch (SocketException e) {
			e.printStackTrace();
			return;
		}

		// Form the RRQ

		ReadRequestPacket rrq = new ReadRequestPacket(fileName, dest, port);
		int block = 1;

		if (socket.send(rrq) != null) {
			while (true) {
				DataPacket response = new DataPacket(socket.receive());
				// Check the response for errors.
				if (Put.handleError(new ACKPacket(response))) {
					if (file.exists()) shouldDelete(file);
					System.out.println("File transfer failed.");
					return;
				}
				// Only append the data if it has the correct block number, otherwise discard it.
				if (response.getBlockNumber() == block) {
					file.append(response.getPayload());

					// Send back an ACK with the matching block number.
					socket.send(new ACKPacket(block, response));

					// Determine if the last of the data has been received.
					if (response.getPayload().length < 512) break;

					// Increase the block number.
					block++;
				}
			}
		}

		socket.close();
		if (file.exists() && file.length() > 0) {
			System.out.println("File transfer complete.");
		} else {
			System.out.println("File transfer failed.");
			file.delete();
		}
	}

	private synchronized void shouldDelete(File file) {
		String answer = IO.input("Would you like to erase the partial contents of " + file.getName() + " from the disk?\nEnter yes/no > ");
		if (answer.trim().equalsIgnoreCase("yes")) {
			file.delete();
			if (!file.exists()) {
				System.out.println("The file has been deleted.");
			} else {
				System.out.println("An undefined error occurred when attempting to delete the file.");
			}
		} else {
			System.out.println("The partial file will remain on disk located at : " + file.getAbsolutePath());
		}
	}
}
