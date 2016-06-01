package core;

import protocol.*;
import protocol.ErrorPacket.ERROR_CODES;
import utilities.File;
import utilities.IO;
import utilities.PacketUtility;
import utilities.commands.Shell;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.Vector;

/**
 * Carleton University Department of Systems and Computer Engineering SYSC 3303
 * - Real-Time and Concurrent Systems
 * <p>
 * Collaborative Project Created: 2016-05-12 License: MIT
 * <p>
 * Authors: Christopher McMorran (100968013) Yue Zhang (100980408) Jenish Zalavadiya (100910343)
 */
public class Server {
	/**
	 * The port to be hosted on.
	 */
	private static final int port = 69;
	/**
	 * This variable is to be set true when a transfer starts, and false when a
	 * transfer ends. It is used to determine when the server can be shut down
	 * without causing issues.
	 */
	public static boolean transferring = false;

	private static int timeout = 5000;
	/**
	 * The socket to receive RRQ/WRQs on.
	 */
	private static TFTPSocket socket;
	/**
	 * The Deque of running threads.
	 */
	private static Deque<Thread> threads;

	/**
	 * Determines if the server is in debug mode.
	 */
	private static boolean debug = true;

	/**
	 * Determines if the server is in verbose/logging mode.
	 */
	private static boolean verbose = true;

	/**
	 * Keeps track of the time the server was started.
	 */
	private static long serverStartTime = System.currentTimeMillis();

	/**
	 * Keeps track of the number of packets that have been received and sent.
	 */
	private static int packetsIn = 0, packetsOut = 0;

	/**
	 * Keeps track of the number of completed and failed file transfers.
	 */
	private static int completedTransfers = 0, failedTransfers = 0;

	/**
	 * Core.Server Constructor. Calls run(), initiates the main socket on port
	 * 69.
	 *
	 * @throws Exception
	 */
	private Server() throws Exception {
		threads = new ArrayDeque<>();
		socket = new TFTPSocket(69);
		System.out.println("Server started. Listen on port:"+socket.getLocalPort());
		initMenu();
		start();
	}

	/**
	 * Main method.
	 *
	 * @param args
	 *            CLI Arguments.
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		new Server();
	}

	/**
	 * Starts the user interface to the server.
	 */
	private void initMenu() {
		new Thread(new Menu()).start();
	}

	/**
	 * Handles incoming requests by starting worker threads and forwarding the
	 * incoming requests.
	 *
	 * @throws Exception
	 */
	private void start() throws Exception {
		while (!socket.isClosed()) { // Do this forever. Or until Shutdown is
										// entered.
			System.out.println("\n\nWaiting for request....");
			// Get a read or write request
			Packet request = socket.receive();

			System.out.println("A request has been received.");
			request.print();
			// Hand it off to a worker thread to deal with.
			Thread thread = new Thread(new Worker(request));
			threads.add(thread);
			thread.start();

		}

	}

	/**
	 * A Worker class that can be run as a Thread. This class handles requests
	 * that are sent by the main server thread.
	 */
	private class Worker implements Runnable {
		/**
		 * The packet that was passed over from the main server thread.
		 */
		Packet packet;
		/**
		 * The socket which will be used to transfer packets in this worker
		 * thread.
		 */
		TFTPSocket socket;

		Worker(Packet packet) {
			this.packet = packet;
			try {
				this.socket = new TFTPSocket();
			} catch (SocketException e) {
				e.printStackTrace();
			}
			run();
		}

		/**
		 * Does the actual thread work in the thread.
		 */
		@Override
		public void run() {
			// Determine if RRQ, WRQ, or Error
			try {
				++packetsIn;
				String name = new RequestPacket(packet).getFileName();
				if (packet.getOPCode() == Packet.OP_CODE.READ_REQUEST.getCode()) {
					sendFile(name);
					System.out.println(
							"Worker with TID: " + Thread.currentThread().getId() + " has completed it's task.");
				} else if (packet.getOPCode() == Packet.OP_CODE.WRITE_REQUEST.getCode()) {
					acceptFile(name);
					System.out.println(
							"Worker with TID: " + Thread.currentThread().getId() + " has completed it's task.");
				}
			} catch (Exception e) {
				System.out.println("Error ?");
				e.printStackTrace();
			}

		}

		/**
		 * When a valid get packet is received, this function will package a
		 * file on the server into multiple packets, and transmit them to a TFTP
		 * client.
		 *
		 * @param name
		 *            The file requested.
		 * @throws IOException
		 */
		private void sendFile(String name) throws IOException {
			transferring = true;
			socket.setSoTimeout(timeout);
			File file = new File(name);
			if (!file.exists()) {
				System.err.println("The specified file does not exist.");
				socket.send(new ErrorPacket(ErrorPacket.ERROR_CODES.FILE_NOT_FOUND,
						"The requested file '" + name + "' does not exist.", this.packet));
				System.out.println("Available Files:");
				++packetsOut;
				failedTransfers++;
				transferring = false;
			} else {
				System.out.println("Sending file...");
				Vector<byte[]> bytes = file.toVectorOfByteArrays();

				int block = 1;

				while (true) {

					if (socket.isClosed()) {
						transferring = false;
						++failedTransfers;
						System.out.println("File transfer failed.");
					}

					DataPacket data = new DataPacket(block, bytes.get(block - 1), packet.getAddress(),
							packet.getPort());
					ACKPacket ACK = socket.send(data).receiveResponse();
					while (ACK.getAddress() == null) {
						ACK = socket.receiveResponse();
					}
					if (ACK.getBlockNumber() == block) {
						dbg("Received the correct block. Incrementing block number.");
						++block;
						dbg("Block is now " + block + ".");
						dbg("Checking for last packet...");
						if ((data.getLength() - 4) < 512) {
							dbg("That was the last packet.");
							break;
						}
					} else {
						dbg("That wasn't expected. Time to resend the same data packet.");
					}

				}

				System.out.println("File transfer complete.");
				transferring = false;
			}

		}

		/**
		 * When a WRQ is encountered, this method will send the appropriate
		 * requests to download the file.
		 *
		 * @param requestedFileName
		 *            The file name to be downloaded, or accepted from the
		 *            client.
		 * @throws Exception
		 */
		private void acceptFile(String requestedFileName) throws Exception {
			transferring = true;
			requestedFileName = Paths.get(requestedFileName).getFileName().toString();
			System.out.println(requestedFileName);
			File file = new File(requestedFileName);
			System.out.println("Requested name: " + requestedFileName);
			// Windows patch. Create the file if it does not exist.

			if (!file.exists())
				file.createNewFile();

			if (!file.canWrite()) {
				System.out.println("Writing to this directory is not permitted.\nIssuing error.");
				++failedTransfers;
				socket.send(new ErrorPacket(ErrorPacket.ERROR_CODES.ACCESS_VIOLATION,
						"You cannot write to that location.", packet));
				transferring = false;
				return;
			}

			// Now the first ACK must be sent back. This should be 0400,
			// signaling a "go ahead" response.
			System.out.println("Allowing the upload.");
			DatagramPacket sendPacket = new DatagramPacket(new byte[] { 0, 4, 0, 0 }, 0, 4, packet.getAddress(), packet.getPort());
			this.socket
					.send(sendPacket);
			++packetsOut;
			System.out.println("1111111111" + Arrays.toString(sendPacket.getData()));
			// dbg("Sending permission packet.");
			// The permission reply started with block number 0, therefore all
			// other ACKs should increment from 1...n.
			int blockNumber = 1;
			// Get the current time for statistics.
			long startTime = System.currentTimeMillis();

			// Let's receive the first data packet.
			while (true) {
				/**
				 * Receive data packets from the client.
				 */
				Packet data = this.socket.receive();
				++packetsIn;

				/**
				 * Check if the received packet is an error packet.
				 */
				if (data.getOPCode() == 4) {
					if (parseError(data))
						return;
				} else {
					// the packet is not an error
					/**
					 * Only accept the data (add it to the list) if it's block
					 * number matches our block number.
					 */

					if (data.getBlockNumber() == blockNumber) {
						if (file.getFreeSpace() < file.getFreeSpace() + data.getData().length -4) {
							// Check for unavailable space.
							socket.send(new ErrorPacket(ERROR_CODES.DISK_FULL, "No disk space is available.", packet));
							transferring = false;
							++failedTransfers;
							return;
						}
						file.append(new DataPacket(data).getPayload());

						/**
						 * Send back the ACK with the matching block number.
						 */

						socket.send(new ACKPacket(blockNumber, data));
						++packetsOut;

						/**
						 * Increment the block number.
						 */
						blockNumber++;

						/**
						 * Determine if the last of the data has been received.
						 * If so, break from this loop, and notify the client.
						 *
						 * 4 bytes are the op code and block number.
						 */

						if (data.getLength() - 4 <= 511)
							break;

					}

					/**
					 * We do not add the data, instead we discard it, and do not
					 * send the ack.
					 */
				}

			} // End of while loop.

			++completedTransfers;
			transferring = false;
			socket.close();
			System.out.println("File Transfer Complete.");
			System.out.println("Received " + file.length() + " bytes in " + (System.currentTimeMillis() - startTime)
					+ " milliseconds.");

		}

		/**
		 * Parses an error packet. This is specific for iteration 3.
		 *
		 * @param packet
		 *            The packet to verify.
		 * @return True if the calling function should also return.
		 */
		private boolean parseError(Packet packet) {
			if (packet.getOPCode() == 4)
				return false;
			int errorCode = packet.getError();

			if (errorCode == 0) {
				log("Undefined error.");
				socket.send(new ErrorPacket(ErrorPacket.ERROR_CODES.NOT_DEFINED, "An undefined error occurred. :(",
						packet));
				return true;
			} else if (errorCode == 1) {
				log("File not found.");
				socket.send(new ErrorPacket(ErrorPacket.ERROR_CODES.FILE_NOT_FOUND,
						"The specified file does not exist. Please create it.", packet));
				return true;
			} else if (errorCode == 2) {
				log("Access violation.");
				socket.send(new ErrorPacket(ErrorPacket.ERROR_CODES.ACCESS_VIOLATION,
						"Access violation occurred. Nice try though.", packet));
				return true;
			} else if (errorCode == 3) {
				log("Disk full.");
				socket.send(new ErrorPacket(ErrorPacket.ERROR_CODES.DISK_FULL, "The server's disk is full.", packet));
				return true;
			} else if (errorCode == 4) {
				log("Illegal TFTP Operation Occurred!");
				socket.send(new ErrorPacket(ErrorPacket.ERROR_CODES.ILLEGAL_OPERATION,
						"Invalid TFTP Operation occurred. No hacking.", packet));
				return true;
			} else if (errorCode == 5) {
				log("Incorrect Transfer ID.");
				socket.send(new ErrorPacket(ErrorPacket.ERROR_CODES.UNKOWN_TID, "Invalid TID.", packet));
				return true;
			} else if (errorCode == 6) {
				log("File already exists.");
				socket.send(new ErrorPacket(ErrorPacket.ERROR_CODES.FILE_EXISTS, "The specified file already exists.",
						packet));
				return true;
			} else if (errorCode == 7) {
				log("No such user...");
				socket.send(new ErrorPacket(ErrorPacket.ERROR_CODES.NO_SUCH_USER,
						"The specified user does not exist in this context.", packet));
				return true;
			}
			return false;
		}

		private void log(String s) {
			if (verbose)
				System.out.println(s);
		}

		/**
		 * Prints debug information with the associated thread ID.
		 *
		 * @param message
		 *            The message to print.
		 */
		private synchronized void dbg(Object message) {
			if (debug)
				System.out.println("<" + Thread.currentThread().getId() + ">\t" + message);
		}
	}

	/**
	 * A menu thread to interact with the server, while it does background
	 * processing.
	 */
	private class Menu implements Runnable {
		@Override
		public void run() {
			System.out.println("Enter 'help' for a list of commands.");
			while (!Server.socket.isClosed())
				try {
					parse(IO.input("TFTP Server > "));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
		}

		/**
		 * Parses input retrieved form the standard input.
		 *
		 * @param input
		 *            The string received.
		 * @throws InterruptedException
		 */
		private void parse(String input) throws InterruptedException {
			if (input.equalsIgnoreCase("shutdown")) {
				gracefulExit();
			} else if (input.equalsIgnoreCase("help")) {
				displayHelpPage();
			} else if (input.equalsIgnoreCase("debug")) {
				toggleDebug();
			} else if (input.equalsIgnoreCase("status")) {
				displayStatus();
			} else if (input.equalsIgnoreCase("pwd")) {
				System.out.println(new File(".").getAbsolutePath());
			} else if (input.equalsIgnoreCase("ls")) {
				IO.listFiles();
			} else
				shell(input);
		}

		/**
		 * Executes shell commands, displaying their output.
		 *
		 * @param input
		 *            The input command.
		 */
		private void shell(String input) {
			new Shell(input);
		}

		/**
		 * Displays server status information.
		 */
		private synchronized void displayStatus() {
			int threadCount = threads.size();
			long time = System.currentTimeMillis();
			System.out.println("Number of threads: " + (threadCount + 2)); // add
																			// 2
																			// for
																			// UI
																			// +
																			// main.
			System.out.println("Number of worker threads: " + threadCount);
			System.out.println("Uptime: " + (time - serverStartTime) / 1000 + " seconds.");
			System.out.println("Packets Received: " + packetsIn);
			System.out.println("Packets Sent: " + packetsOut);
			System.out.println("Completed Transfers: " + completedTransfers);
			System.out.println("Failed transfers: " + failedTransfers);
		}

		/**
		 * Toggles the debug mode status.
		 */
		private void toggleDebug() {
			debug = !debug;
			TFTPSocket.setDebug(debug);
			if (debug)
				System.out.println("Debugging mode is now ON.");
			else
				System.out.println("Debugging mode is now OFF.");
		}

		/**
		 * Connects to github to display a help page.
		 */
		private void displayHelpPage() {
			System.out.println("<------------------- Server Help Page ------------------->\n" + "  Commands:\n"
					+ "    help          Displays this page.\n" + "    shutdown      Shuts down the server.\n"
					+ "    status        Outputs detailed server information.\n"
					+ "    debug         Toggles debug information on and off.\n"
					+ "    verbose       Toggles verbose output on and off.\n"
					+ "<-------------------------------------------------------->");
		}

		/**
		 * Waits for each thread to finish, before shutting down.
		 *
		 * @throws InterruptedException
		 */
		private synchronized void gracefulExit() throws InterruptedException {
			System.out.println("Waiting for transfers to finish...");
			while (transferring)
				System.out.print(".");
			System.out.println("Closing the main socket...");
			socket.close();
			if (!socket.isClosed()) {
				System.err.println("Operation failed.");
			} else {
				/**
				 * This will wait for each running thread to finish.
				 */
				for (Thread t : threads)
					t.join();

				System.out.println("Server is ready to shut down.");
				System.exit(0);
			}
		}
	}
}