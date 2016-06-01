package core;

import protocol.*;
import utilities.File;
import utilities.IO;
import utilities.PacketUtility;
import utilities.commands.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.file.Paths;
import java.util.Vector;

/**
 * Carleton University Department of Systems and Computer Engineering SYSC 3303
 * - Real-Time and Concurrent Systems
 * <p>
 * Collaborative Project
 * <p>
 * Package: core Created: 2016-05-30 License: MIT
 * <p>
 * Authors: Christopher McMorran (100968013) Yue Zhang (100980408)
 */
public class Client {
	public static boolean inProgress = false;
	private static InetAddress address;
	private static int port = 69;
	private static int timeout = 0;
	private static boolean verbose = true, debug = true;

	public static void main(String[] args) throws IOException {
		TFTPSocket.setDebug(true);
		address = InetAddress.getLocalHost();

		System.out.println("Enter 'help' for a list of commands.");

		while (true) {
			String command = IO.input("tftp > ");
			if (command.equalsIgnoreCase("write")) {
				sendFile(IO.input("Enter File Name > "), address, port);
			} else if (command.equalsIgnoreCase("read")) {
				getFile(IO.input("Enter File Name > "), address, port);
			} else if (command.equalsIgnoreCase("help")) {
				new Help().exec();
			} else if (command.equalsIgnoreCase("debug")) {
				debug = !debug;
				TFTPSocket.setDebug(debug);
				System.out.printf("Debug mode is now %s.\n", debug ? "ON" : "OFF");
			} else if (command.equalsIgnoreCase("verbose")) {
				verbose = !verbose;
				System.out.printf("Verbose mode is now %s.\n", verbose ? "ON" : "OFF");
			} else if (command.equalsIgnoreCase("port")) {
				new Port().exec();
			} else if (command.equalsIgnoreCase("ls")) {
				new ListFiles().exec();
			} else if (command.equalsIgnoreCase("quit")) {
				new Quit().exec();
			} else if (command.equalsIgnoreCase("timeout")) {
				new Timeout().exec();
			} else if (command.equalsIgnoreCase("mode")) {
				System.out.println("Mode swapping is disabled. Both NetASCII and Octet will be handled.");
			} else if (command.equalsIgnoreCase("pwd")) {
				System.out.println(new File(".").getAbsolutePath());
			} else {
				new Shell(command);
			}
		}

	}

	/**
	 * Displays verbose logging information.
	 *
	 * @param message
	 *            The message to log.
	 */
	private static void log(Object message) {
		if (verbose)
			System.out.println(message);
	}

	private static synchronized void getFile(String fileName, InetAddress address, int port) throws IOException {
		String path = "";
		try {
			System.out.println(
					"Please enter the path to download this file to. By default, it will be placed in the current directory.\n"
							+ "The current directory is : " + new File(".").getCanonicalPath());
			path = IO.input("path > ");
			path = path.trim();
			if (System.getProperty("os.name").contains("Win") && !path.endsWith("\\") && !fileName.startsWith("\\")) {
				path += "\\";
			} else if (!System.getProperty("os.name").contains("Win") && !path.endsWith("/")
					&& !fileName.startsWith("/")) {
				path += "/";
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		if (path.equals(".") || path.equals("")) {
			path = System.getProperty("user.dir");
		}
		String localName = path;
		if (!localName.endsWith(System.lineSeparator()))
			localName += (System.getProperty("os.name").contains("Win") ? "\\" : "/");
		if (!path.isEmpty())
			localName += Paths.get(fileName).getFileName().toString();
		else
			localName = Paths.get(fileName).getFileName().toString();
		log("The local file to write will be: " + localName);
		inProgress = true;
		long start = System.currentTimeMillis();
		while (inProgress) {
			TFTPSocket socket = new TFTPSocket();
			socket.setSoTimeout(timeout);
			File file = new File(localName);

			// Windows patch. If the file does not exist, create it.
			if (!file.exists()) {
				try {
					file.createNewFile();
				} catch (IOException e) {
					if (!file.exists()) {
						System.out.println("You do not have permission to create a new file.");
						System.out.println("File transfer failed.");
						inProgress = false;
						return;
					}
				}
			}

			if (file != null && !file.getParentFile().canWrite()) {
				System.out.println("You do not have permission to write to that directory.");
				inProgress = false;
				return;
			}

			if (file.exists() && file.length() != 0) {
				System.out.println("The specified file already exists. Please delete it and try again.");
				inProgress = false;
				return;
			}
			if (file.exists() && !file.canWrite()) {
				System.out.println("Read only access.");
				inProgress = false;
				return;
			}

			ReadRequestPacket rrq = new ReadRequestPacket(fileName, address, port);
			int block = 1;
			socket.send(rrq);

			while (System.currentTimeMillis() - start < socket.getSoTimeout()) {
				DataPacket data = socket.receiveData();
				if (data.getOPCode() == 5) {
					System.out.println("Error Received.");
					logError(data);
					System.out.println("File transfer failed.");
					inProgress = false;
					return;
				}
				if (data.getBlockNumber() == block) {

					file.append(data.getPayload());
					socket.send(new ACKPacket(block, data.getAddress(), data.getPort()));
					++block;
				} else {
					log("Incorrect block number.");
				}

				if (data.getPayload().length < 512)
					break;
			}
			if (System.currentTimeMillis() - start < socket.getSoTimeout()) {
				System.out.println("File transfer complete.");
				System.out.println("Transferred " + file.length() + " bytes in " + (System.currentTimeMillis() - start)
						+ " milliseconds.");
				inProgress = false;
				return;
			}

		}
		System.out.println("File transfer failed.");
		inProgress = false;
	}

	/**
	 * Sends a file to the specified destination:port.
	 *
	 * @param fileName
	 *            The name of the file to send.
	 * @param destination
	 *            The destination address.
	 * @param port
	 *            The destination port.
	 * @throws SocketException
	 *             If the socket cannot be created.
	 */
	private static synchronized void sendFile(String fileName, InetAddress destination, int port)
			throws SocketException {
		inProgress = true;
		String path = "";
		try {
			System.out.println(
					"Please enter the path to upload this file to. By default, it will be placed in the current server's running directory.");
			path = IO.input("path > ");
			path = path.trim();
			if (System.getProperty("os.name").contains("Win") && !path.endsWith("\\")) {
				path += "\\";
			} else if (!System.getProperty("os.name").contains("Win") && !path.endsWith("/")) {
				path += "/";
			}
		} catch (Exception e) {
			System.out.println("Undefined error.");
		}
		long start = System.currentTimeMillis();
		while (inProgress) {
			TFTPSocket socket = new TFTPSocket();
			File file = new File(fileName);
			if (!file.exists()) {
				System.out.println("The specified file does not exist.");
				inProgress = false;
				return;
			}
			if (file.isDirectory()) {
				System.out.println("File is a directory.");
				inProgress = false;
				return;
			}
			if (!file.canRead()) {
				System.out.println("File is read only.");
				inProgress = false;
				return;
			}
			if (file.length() <= 0) {
				System.out.println("The specified file is empty. You cannot send an empty file.");
				inProgress = false;
				return;
			}

			Vector<byte[]> bytes = file.toVectorOfByteArrays();
			int block = 0;
			int loc = 0;
			Packet ack = null;
			socket.setSoTimeout(timeout);
			log("Socket timeout: " + timeout);
			log("Block Number: " + block);
			while (true) {
				if (block == 0) {
					socket.send(new WriteRequestPacket((fileName).trim(), destination, port));
					ack = socket.receive();
					if (ack.getData()[1] == 5 || ack.getData()[1] == 0) {
						logError(ack);
						log("File transfer failed.");
						inProgress = false;
						return;
					} else {
						++block;
					}

				} else {
					DataPacket data = new DataPacket(block, bytes.get(loc), ack.getAddress(), ack.getPort());
					socket.send(data);
					ack = socket.receiveResponse();
					if (ack.getData()[1] == 5) {
						log("Error received.");
						logError(ack);
						inProgress = false;
						return;
					} else if (ack.getBlockNumber() == block) {
						++block;
						++loc;
					}
					// Check if all have been sent.
					log("Checking for last ACK...");
					if(block == bytes.size() && block == ack.getBlockNumber()) break;
				}
			}
			/*byte[] msg;
			boolean fileEnd = false;
			for (int i = 1; !fileEnd; i++) {
				msg = new byte[516];
				msg[0] = 0;
				msg[1] = 3;
				msg[2] = (byte) ((i / 256) & 0xFF);
				msg[3] = (byte) ((i % 256) & 0xFF);
				
				FileInputStream inStream;
				int len;
				if ((len = inStream.read(msg, 4, 512)) != 512)
					fileEnd = true;

				if (len == -1)
					len++;

				DatagramPacket dataPacket = new DatagramPacket(msg, len + 4,
						InetAddress.getLocalHost(), ack.getPort());

				//socket.setSoTimeout(TIMEOUT);
				//timeouts = 0;

				//do {
					System.out.println("Sending to...");
					//printPacket(dataPacket);
					socket.send(dataPacket);
					DatagramPacket ackPacket = null;
					try {
						int result;
						do {
							msg = new byte[516];
							ackPacket = new DatagramPacket(msg,
									msg.length);
							socket.receive(ackPacket);
							System.out.println("Received from...");
							//printPacket(ackPacket);

							//result = handleErrors(ackPacket, i, ACK,
							//		receivePort, receiveHost);
							//if (result == ERROR4)
							//	return;
						} while (!PacketUtility.isErrorPacket(ackPacket));
						break;
					} catch (SocketTimeoutException e) {
						//System.out.println("Client timed out " + TIMEOUT + "ms.");
						//timeouts++;
					}
				//} //while (timeouts < 5);

				//if (timeouts >= 5) {
				//	System.out.println("Client reached 5 timeout, transfer aborted");
				//	return;
				//}
			}
			inStream.close();*/
			
			log("File transfer complete.");
			log("Transferred " + file.length() + " bytes in " + (System.currentTimeMillis() - start)
					+ " milliseconds.");
			inProgress = false;
		}

	}

	/**
	 * Prints relevent error messages depending on the error packet.
	 *
	 * @param packet
	 *            The error packet.
	 */
	private static void logError(Packet packet) {
		ErrorPacket error = new ErrorPacket(packet);
		switch (error.getError()) {
		case 0:
			System.out.println("An undefined error occurred.");
			break;
		case 1:
			System.out.println("File not found.");
			break;
		case 2:
			System.out.println("Access violation.");
			break;
		case 3:
			System.out.println("Disk full or allocation exceeded.");
			break;
		case 4:
			System.out.println("Illegal TFTP operation.");
			break;
		case 5:
			System.out.println("Unknown transfer ID.");
			break;
		case 6:
			System.out.println("File already exists.");
			break;
		case 7:
			System.out.println("No such user.");
			break;
		}

		if (!error.getMessage().isEmpty())
			log("Additional Information: " + error.getMessage());
	}

	/**
	 * Get the destination port.
	 *
	 * @return Int.
	 */
	public static int getPort() {
		return port;
	}

	/**
	 * Sets the port.
	 *
	 * @param port
	 */
	public static void setPort(Integer port) {
		Client.port = port;
	}

	/**
	 * Gets the timout.
	 *
	 * @return
	 */
	public static int getTimeout() {
		return timeout;
	}

	/**
	 * Sets the timeout.
	 *
	 * @param timeout
	 */
	public static void setTimeout(Integer timeout) {
		Client.timeout = timeout;
	}
}
