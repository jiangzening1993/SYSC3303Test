package core;

import utilities.PacketUtility;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Scanner;

import static java.lang.System.exit;

/* This class contains functionality related to managing and manipulating packages */

public class ErrorSimulator {
	private static int DEFAULT_PACKET_LENGTH = 516;
	private static int DEFAULT_SERVER_PORT = 69;
	byte[] datagram = new byte[DEFAULT_PACKET_LENGTH];
	byte[] ackData = new byte[4];
	private int serverPort;
	private DatagramSocket clientSocket, sendRecvSocket;
	private DatagramPacket receivePacket, packet, ackPacket;
	private boolean errorModeON;
	private boolean serverErrorON;
	private boolean clientErrorON; 
	private int clientPort;
	private int errorID = 0; // 1 = lose a packet // 2 = delay a packet // 3 = duplicate a packet 
	private int packetID; // 1 = RRQ  // 2 = WRQ // 3 = DATA packet // 4 = ACK packet // 5 = ERROR
	private int blockNum = -1; // denotes which packet to generate error on
	private int delayTime = 0; 


	private ErrorSimulator() {
		try {
			clientSocket = new DatagramSocket(23);
			sendRecvSocket = new DatagramSocket(6000);
			packet = new DatagramPacket(datagram, datagram.length);
			ackPacket = new DatagramPacket(ackData, ackData.length);
			startSim();
		} catch (SocketException se) {
			se.printStackTrace();
			exit(1);
		}
	}

	public static void main(String[] args) {
		new ErrorSimulator();
	}

	/**
	 * This method prints out the user menu
	 */
	private void startMenu() {
		Scanner scan = new Scanner(System.in);

		boolean bool = true;
		errorModeON = false;
		serverErrorON = false;
		clientErrorON = false;

		int option;
		int count = 0;

		while (bool) {
			switch (count) {
				case 0:
					System.out.println("\tError Core.Simulator started.");
					System.out.println("\tChoose an option.");
					System.out.println("\t\t1: Simulate no errors (normal operation).");
					System.out.println("\t\t2: Simulate errors.");
					System.out.print("\t\tEnter valid option: ");
					option = scan.nextInt();
					if (option == 1) {
						bool = false;
					} else if (option == 2) {
						errorModeON = true;
						count++;
					} else {
						System.out.println("\tPlease enter a valid option");
					}
					break;
				case 1:
					System.out.println("\tChoose an option.");
					System.out.println("\t\t1: Simulate error when packets are sent to server.");
					System.out.println("\t\t2: Simulate error when packets are sent to client.");
					System.out.print("\t\tEnter valid option: ");
					option = scan.nextInt();
					if (option == 1) {
						serverErrorON = true;
						count++;
					} else if (option == 2) {
						clientErrorON = true;
						count++;
					} else {
						System.out.println("\tPlease enter a valid option");
					}
					break;
				case 2:
					System.out.println("\tGenerate error on:");
					System.out.println("\t\t1: RRQ packet.");
					System.out.println("\t\t2: WRQ packet.");
					System.out.println("\t\t3: DATA packet.");
					System.out.println("\t\t4: ACK packet.");
					System.out.print("\t\tEnter valid option: ");
					option = scan.nextInt();
					if (option == 1 || option == 2 || option == 3 || option == 4) {
						packetID = option;
						count++;
					} else {
						System.out.println("\tPlease enter a valid option");
					}
					break;
				case 3:
					System.out.println("\tChoose what type of error to simulate.");
					System.out.println("\t\t1: Lose a packet.");
					System.out.println("\t\t2: Delay a packet.");
					System.out.println("\t\t3: Duplicate a packet.");
					System.out.println("\t\t4: Invalid protocol option.");
					System.out.println("\t\t5: Invalid transfer ID.");
					System.out.print("\t\tEnter valid option: ");
					option = scan.nextInt();
					if (option == 1 || option == 2 || option == 3 || option == 4 || option == 5) {
						errorID = option;
						if (errorID == 2) {
							while (true) {
								System.out.print("\tEnter the delay time in milliseconds: ");
								delayTime = scan.nextInt();
								if (delayTime >= 0) break;
							}
						}
						if (packetID == 3 || packetID == 4) {
							while (true) {
								System.out.print("\tEnter the packet number on which to create error: ");
								blockNum = scan.nextInt();
								if (clientErrorON && packetID == 4 && blockNum >= 0) break;
								else if (blockNum > 0) break;
							}
						}
					} else {
						System.out.println("\tPlease enter a valid option");
					}
					bool = false;
					break;
			}
		}
	}

	/**
	 * This method prints out error message if errorModeON is true
	 */

	private void printErrMsg() {
		if (errorModeON) {
			String msg = "\n\t\tError will be generated on ";
			String des;
			if (serverErrorON) {
				des = "server.";
				msg += des;
			} else {
				des = "client.";
				msg += des;
			}
			System.out.println(msg);
			msg = "\t\tError: ";
			if (packetID == 1) msg += "RRQ packet ";
			else if (packetID == 2) msg += "WRQ packet ";
			else if (packetID == 3) msg += "DATA packet #" + blockNum + " ";
			else if (packetID == 4) msg += "ACK packet #" + blockNum + " ";

			if (errorID == 1) msg += "will be lost.";
			else if (errorID == 2) msg += "will be delayed by " + delayTime + " milliseconds.";
			else if (errorID == 3) msg += "will be duplicated.";
			else if (errorID == 4) msg = "Error packet with error code 4 will be froward to " + des;
			else if (errorID == 5) msg = "Error packet with error code 5 will be froward to " + des;

			System.out.println(msg + "\n");
		} else {
			System.out.println("No error is going to be simulated");
		}
	}

	private void handlePackets() {
		boolean isFinished = false;
		while (!isFinished) {
			byte[] data = new byte[DEFAULT_PACKET_LENGTH];
			receivePacket = new DatagramPacket(data, data.length);

			System.out.println("\tWaiting for packet from client...");

			try {
				clientSocket.receive(receivePacket);
				System.out.println("\tReceived packet from client: ");
				PacketUtility.print(receivePacket);
				clientPort = receivePacket.getPort();
				//data = receivePacket.getData();

				/**
				 * when user set error code to 4 or 5
				 */
				isFinished = processError(receivePacket);

				/**
				 * when error code is not 4 or 5
				 */
				if (PacketUtility.isReadRequest(receivePacket)) {
					System.out.println("\tRRQ received from client.");
					isFinished = processRRQ();
				} else if (PacketUtility.isWriteRequest(receivePacket)) {
					System.out.println("\tWRQ received from client.");
					isFinished = processWRQ();
				} else if (PacketUtility.isErrorPacket(receivePacket)) {
					System.out.println("\tError packet received from client");
					sendRecvSocket.send(receivePacket);
					isFinished = true;
				}
			} catch (IOException e) {
				e.printStackTrace();
				exit(1);
			}

		}


	}

	private void startSim() {
		while (true) {
			startMenu();
			printErrMsg();
			handlePackets();
		}
	}


	private void errorGenerator(DatagramPacket packet, DatagramSocket socket) {
		System.out.println("\tSimulating error started...\n");
		String des, packetName;
		if (serverErrorON) {
			des = "server.";
		} else if (clientErrorON) {
			des = "client.";
		} else des = "nowhere.";
		if (packetID == 1 || packetID == 2) {
			if (packetID == 1) packetName = "RRQ packet ";
			else packetName = "WRQ packet ";
			switch (errorID) {
				case 1:
					System.out.println("\tError simulated, " + packetName + "was lost.");
					System.out.println("\tExpecting to receive " + packetName + "again.");
					try {
						clientSocket.receive(receivePacket);
						System.out.println("\t" + packetName + "received from client.");
						PacketUtility.print(receivePacket);
						receivePacket.setPort(DEFAULT_SERVER_PORT);
						sendRecvSocket.send(receivePacket);
						System.out.println("\t" + packetName + "forwarded to server.");
					} catch (IOException e) {
						e.printStackTrace();
						System.exit(1);
					}
					break;
				case 2:
					System.out.println("\tDelaying " + packetName + "by" + delayTime + " milliseconds.");
					PacketUtility.delayPacket(socket, packet, delayTime);
					System.out.println("\t" + packetName + "forwarded to " + des);
					break;
				case 3:
					PacketUtility.duplicatePacket(socket, packet);
					System.out.println("\tError simulated, two " + packetName + "were sent to " + des);
					break;
			}
		} else if (packetID == 3 || packetID == 4) {
			int currentBlockNum = PacketUtility.getBlockNumber(packet);
			if (packetID == 3) packetName = "DATA packet";
			else packetName = "ACK packet";
			if (currentBlockNum == blockNum) {
				switch (errorID) {
					case 1:
						System.out.println("\tError simulated, " + packetName + ",#" + blockNum + " was lost");
						System.out.println("\tExpecting to receive " + packetName + ",#" + blockNum + " again.");
						try {
							if (packet.getPort() == clientPort) {
								sendRecvSocket.receive(packet);
								System.out.println("\t" + packetName + ",#" + blockNum + " received from server.");
								PacketUtility.print(packet);
								packet.setPort(clientPort);
								socket.send(packet);
								System.out.println("\t" + packetName + ",#" + blockNum + " forwarded to client.");
							} else {
								clientSocket.receive(packet);
								System.out.println("\t" + packetName + ",#" + blockNum + " received from client.");
								PacketUtility.print(packet);
								packet.setPort(DEFAULT_SERVER_PORT);
								socket.send(packet);
								System.out.println("\t" + packetName + ",#" + blockNum + " forwarded to server.");
							}
						} catch (IOException e) {
							e.printStackTrace();
							System.exit(1);
						}
						break;
					case 2:
						System.out.println("\tDelaying " + packetName + ",#" + blockNum + " by " + delayTime + " milliseconds.");
						PacketUtility.delayPacket(socket, packet, delayTime);
						System.out.println("\t" + packetName + ",#" + blockNum + " forwarded to " + des);
						break;
					case 3:
						PacketUtility.duplicatePacket(socket, packet);
						System.out.println("\tError simulated, " + packetName + ",#" + blockNum + " was sent twice to " + des);
						break;
				}
			}
		} else {
			System.out.println("\tUnknown packetID has been caught, unable to process, terminating the program...");
			exit(1);
		}
		System.out.println("\n" + "\tError Generator ended.\n");
	}

	// Once the errorSim receives a RRQ, it will call this function
	// which will then continue processing RRQ.
	private boolean processRRQ() throws IOException {
		int count = 0;
		try {
			/**
			 * Generate errors on RRQ packet when user set packetID to 1
			 */
			receivePacket.setPort(DEFAULT_SERVER_PORT);
			if (errorModeON && packetID == 1) { //only generates errors when user set packetID to 1
				System.out.println("Prepare to generate errors on RRQ packet...");
				errorGenerator(receivePacket, sendRecvSocket);
				count++;
			} else {
				sendRecvSocket.send(receivePacket);
				System.out.println("\tRRQ forwarded to server.");
				PacketUtility.print(receivePacket);
			}

			sendRecvSocket.receive(packet);
			int packetBlockID = PacketUtility.getBlockNumber(packet);
			System.out.println("\tDATA packet, BlockID: " + packetBlockID + ", received from server.");
			PacketUtility.print(packet);
			serverPort = packet.getPort();

			while (true) {
				/**
				 * Generate errors on DATA packet to CLIENT
				 */
				packet.setPort(clientPort);
				packetBlockID = PacketUtility.getBlockNumber(packet);
				if (errorModeON && packetID == 3 && clientErrorON && packetBlockID == blockNum) {
					System.out.println("Prepare to generate errors on Data packet #" + blockNum + "...");
					errorGenerator(packet, sendRecvSocket);
					count++;
				} else {
					clientSocket.send(packet);
					System.out.println("\tDATA packet #" + packetBlockID + ", forwarded to client.");
					PacketUtility.print(packet);
				}

				clientSocket.receive(ackPacket);
				packetBlockID = PacketUtility.getBlockNumber(ackPacket);
				System.out.println("\tACK packet #" + packetBlockID + ", received from client.");
				PacketUtility.print(ackPacket);

				/**
				 * Generate errors on ACK packet to SERVER
				 */
				ackPacket.setPort(serverPort);
				if (errorModeON && serverErrorON && packetBlockID == blockNum) {
					System.out.println("Prepare to generate errors on ACK packet #" + blockNum + "...");
					errorGenerator(ackPacket, sendRecvSocket);
					count++;
				} else {
					sendRecvSocket.send(ackPacket);
					System.out.println("\tACK packet #" + packetBlockID + ", forwarded to server.");
					PacketUtility.print(ackPacket);
				}

				sendRecvSocket.receive(packet);
				System.out.println("\tDATA packet #" + packet.getData()[3] + ", received from server.");
				PacketUtility.print(packet);

				/**
				 * Check if the packet is the last one
				 */
				if (PacketUtility.isLastDataPacket(packet)) {
					System.out.println(">>>>>>>>>>The last packet has been processed.<<<<<<<<<<<<<<<");
					ifError(count);
					return true;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			if (e.toString().matches("SocketTimeoutException")) {
				System.out.println("Core.Client's read request is completely processed.");
				return true;
			}
			exit(1);
		}
		System.out.println("This should never happen!!!!!");
		return false;
	}


	// Once the errorSim receives a WRQ, it will call this function
	// which will then continue processing WRQ.
	private boolean processWRQ() {
		int count = 0;
		try {
			/**
			 * Generate error on WRQ packet when user set PacketID to 2
			 */
			receivePacket.setPort(DEFAULT_SERVER_PORT);
			if (errorModeON && packetID == 2) {
				System.out.println("Prepare to generate errors on WRQ packet...");
				errorGenerator(receivePacket, sendRecvSocket);
				count++;
			} else {
				sendRecvSocket.send(receivePacket);
				System.out.println("\tWRQ forwarder to server.");
				PacketUtility.print(receivePacket);
			}

			sendRecvSocket.receive(ackPacket);
			System.out.println("Received ACK packet:");
			PacketUtility.print(ackPacket);
			System.out.println("\tACK packet for WRQ request received from server");
			serverPort = ackPacket.getPort();

			/**
			 * Generate errors on ACK packet to CLIENT
			 */
			ackPacket.setPort(clientPort);
			if (errorModeON && PacketUtility.getPacketID(ackPacket) == packetID && ackPacket.getData()[3] == blockNum) {
				System.out.println("\tGenerating error on ACK packet" + blockNum + "...");
				errorGenerator(ackPacket, clientSocket);
				count++;
			} else {
				System.out.println("\tACK packet for WRQ request forwarded to client");
				PacketUtility.print(ackPacket);
			}

			clientSocket.receive(packet);
			System.out.println("\tDATA packet #" + packet.getData()[3] + ", received from client.");
			PacketUtility.print(packet);

			while (true) {
				/**
				 * generate errors on DATA packets to SERVER
				 */
				packet.setPort(serverPort);
				if (errorModeON && serverErrorON && packet.getData()[3] == blockNum) {
					System.out.println("Prepare to generate error on DATA packet" + blockNum + "...");
					errorGenerator(packet, sendRecvSocket);
					count++;
				} else {
					sendRecvSocket.send(packet);
					System.out.println("\tDATA packet #" + packet.getData()[3] + ", forwarded to server.");
					PacketUtility.print(packet);
				}

				sendRecvSocket.receive(ackPacket);
				System.out.println("\tACK packet #" + ackPacket.getData()[3] + ", received from server.");
				PacketUtility.print(ackPacket);

				/**
				 * generate errors on ACK packets to CLIENT
				 */
				ackPacket.setPort(clientPort);
				if (errorModeON && clientErrorON && ackPacket.getData()[3] == blockNum) {
					System.out.println("Prepare to generate errors on ACK packet" + blockNum + "...");
					errorGenerator(ackPacket, clientSocket);
					count++;
				} else {
					clientSocket.send(ackPacket);
					System.out.println("\tACK packet #" + ackPacket.getData()[3] + ", forwarded to client.");
					PacketUtility.print(ackPacket);
				}

				//clientSocket.setSoTimeout(2000);
				clientSocket.receive(packet);
				System.out.println("\tDATA packet #" + packet.getData()[3] + ", received from client.");
				PacketUtility.print(packet);

				/**
				 * Check if the packet is the last one
				 */
				if (PacketUtility.isLastDataPacket(packet)) {
					System.out.println(">>>>>>>>>>The last packet has been processed.<<<<<<<<<<<<<<<");
					ifError(count);
					return true;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			if (e.toString().matches("SocketTimeoutException")) {
				System.out.println("Core.Client's read request is completely processed.");
				return true;
			}
			exit(1);
		}
		System.out.println("This should never happen!!!!!");
		return false;
	}

	private boolean processError(DatagramPacket addr) throws IOException {
		/**
		 * When the user set the error code to 4 or 5
		 */
		if (errorID == 4 || errorID == 5) {
			if (serverErrorON) addr.setPort(serverPort);
			else if (clientErrorON) addr.setPort(clientPort);
			if (errorID == 4) {
				sendRecvSocket.send(PacketUtility.createErrorPacket4(addr));
			} else if (errorID == 5) {
				sendRecvSocket.send(PacketUtility.createErrorPacket5(addr));
			}
			return true;
		} else return false;
	}

	//This method checks if any error has been made in errorGenerator method
	private void ifError(int i) {
		if (i == 0) {
			System.out.println("No Error can be generated, please check user input.");
		} else {
			System.out.println("Errors has been successfully generated.");
		}
	}


}
