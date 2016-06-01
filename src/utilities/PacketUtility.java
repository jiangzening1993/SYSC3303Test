package utilities;


import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Vector;

/**
 * Carleton University
 * Department of Systems and Computer Engineering
 * SYSC 3303 - Real-Time and Concurrent Systems
 * <p>
 * Collaborative Project
 * Package: Utilities
 * Created: 2016-05-12
 * License: MIT
 * <p>
 * Authors:
 * Christopher McMorran    (100968013)
 * Yue Zhang               (100980408)
 */
public class PacketUtility {
	private static String mode = "netascii";
	private static int retry = 3;

	/**
	 * Creates an empty ACK Packet, which is filled by the provided DatagramSocket.
	 *
	 * @param socket The socket to receive with.
	 * @return A DatagramPacket that resembles an ACK.
	 */
	public static DatagramPacket createAndReceiveAck(DatagramSocket socket) {
		try {
			DatagramPacket ACK = createEmptyAck();
			socket.receive(ACK);
			return ACK;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Creates a packet that is of the DataPacket format.
	 *
	 * @param payload The byte[] excluding OP Code and Block Number.
	 * @param block   The block number for the Data Packet.
	 * @param dest    The address of the host to send this to.
	 * @param port    The port of the host to send this to.
	 * @return A DatagramPacket.
	 */
	public static DatagramPacket createDataPacket(byte[] payload, int block, InetAddress dest, int port) {
		byte[] buffer = new byte[payload.length + 4];
		buffer[0] = (byte) 0;
		buffer[1] = (byte) 3;
		buffer[2] = (byte) 0;
		buffer[3] = (byte) block;
		System.arraycopy(payload, 0, buffer, 4, buffer.length - 4);
		return new DatagramPacket(buffer, buffer.length, dest, port);
	}

	/**
	 * Creates a DatagramPacket with 516 bytes of space.
	 *
	 * @return DatagramPacket.
	 */
	public static DatagramPacket createEmptyDataPacket() {
		return new DatagramPacket(new byte[516], 516);
	}

	/**
	 * Creates a new DatagramPacket with a 4 byte payload.
	 * The OP Code is set to 04.
	 *
	 * @return A DatagramPacket.
	 */
	public static DatagramPacket createEmptyAck() {
		DatagramPacket packet = new DatagramPacket(new byte[4], 4);
		byte[] data = packet.getData();
		data[1] = (byte) 4;
		packet.setData(data);
		return packet;
	}

	/**
	 * Creates a generic File Not Found error packet.
	 *
	 * @param address The target host.
	 * @param port    The target port.
	 * @return DatagramPacket.
	 */
	public static DatagramPacket createErrorPacketFileNotFound(InetAddress address, int port) {
		return new DatagramPacket(new byte[]{0, 5, 0, 1, 0, 0}, 6);
	}

	/**
	 * Creates a new error packet with error code 4.
	 *
	 * @param info The packet to obtain forwarding info from.
	 * @return The Error Packet.
	 */
	public synchronized static DatagramPacket createErrorPacket4(DatagramPacket info) {
		return new DatagramPacket(new byte[]{0, 5, 0, 4}, 0, 4, info.getAddress(), info.getPort());
	}

	/**
	 * Creates a new error packet with error code 5.
	 *
	 * @param info The packet to obtain forwarding info from.
	 * @return The Error Packet.
	 */
	public synchronized static DatagramPacket createErrorPacket5(DatagramPacket info) {
		return new DatagramPacket(new byte[]{0, 5, 0, 5}, 0, 4, info.getAddress(), info.getPort());
	}

	/**
	 * Creates a new packet that can be sent to initiate a file transfer from a client to a server.
	 *
	 * @param fileName The file to ask to receive.
	 * @param dest     The server address to send the packet to.
	 * @param port     The port on the server to send the packet to.
	 * @return The new DatagramPacket.
	 */
	public static DatagramPacket createReadRequest(String fileName, String dest, int port) {
		try {
			return createReadRequest(fileName, InetAddress.getByName(dest), port);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Creates a new packet that can be sent to initiate a file transfer from a client to a server.
	 *
	 * @param fileName The file to ask to receive.
	 * @param address  The server address to send the packet to.
	 * @param port     The port on the server to send the packet to.
	 * @return The new DatagramPacket.
	 */
	public static DatagramPacket createReadRequest(String fileName, InetAddress address, int port) {
		return createRequest(fileName, address, port, 1);
	}

	/**
	 * Private method to be called by the createWriteRequest and creatReadRequest methods.
	 * This si for code simplicity.
	 *
	 * @param fileName The file name to be requested.
	 * @param address  The address to be sent to.
	 * @param port     The port to be sent to.
	 * @param sigBit   The significant bit (1 for read, 2 for write).
	 * @return The Read or Write Request Packet.
	 */
	private static DatagramPacket createRequest(String fileName, InetAddress address, int port, int sigBit) {
		File file = new File(fileName);

		Vector<Byte> vector = new Vector<>();
		vector.add((byte) 0);
		vector.add((byte) sigBit);
		for (Byte b : fileName.getBytes()) vector.add(b);
		vector.add((byte) 0);
		for (Byte b : mode.getBytes()) vector.add(b);
		vector.add((byte) 0);

		byte[] bytes = new byte[vector.size()];
		for (int i = 0; i < bytes.length; i++) {
			bytes[i] = vector.get(i);
		}

		return new DatagramPacket(bytes, bytes.length, address, port);
	}

	/**
	 * Creates a packet containing the ACK Sequence "0400", which should be used by a TFTP server to allow a write request.
	 *
	 * @param replyToPacket The packet to retrieve the return address and port from.
	 * @return A DatagramSocket.
	 */
	public static DatagramPacket createServerWriteRequestPermitted(DatagramPacket replyToPacket) {
		return new DatagramPacket(new byte[]{0, 4, 0, 0}, 0, 4, replyToPacket.getAddress(), replyToPacket.getPort());
	}

	/**
	 * Creates a packet fromatted for a Write Request.
	 *
	 * @param fileName The file to ask to write.
	 * @param dest     The server to send the file to.
	 * @param port     The port to send the file to.
	 * @return This request packet.
	 */
	public static DatagramPacket createWriteRequest(String fileName, String dest, int port) {
		try {
			return createWriteRequest(fileName, InetAddress.getByName(dest), port);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Creates a new packet that can be sent to initiate a file transfer from a client to a server.
	 *
	 * @param fileName The file to ask to send.
	 * @param address  The server address to send the file to.
	 * @param port     The port on the server to send the packet to.
	 * @return The new DatagramPacket.
	 */
	public static DatagramPacket createWriteRequest(String fileName, InetAddress address, int port) {
		return createRequest(fileName, address, port, 2);
	}

	/**
	 * Creates a Vector<DatagramPacket> from a Byte[].
	 *
	 * @param arrays The byte[] to use.
	 * @param dest   The server to set the packet addresses to.
	 * @param port   The server port to set the packet ports to.
	 * @return A Vector<DatagramPacket>.
	 */
	public static Vector<DatagramPacket> createVectorOfPacketsFromByteArrays(Vector<Byte[]> arrays, InetAddress dest, int port) {
		Vector<DatagramPacket> packets = new Vector<>(arrays.size());

		for (int i = 0; i < arrays.size(); i++) {
			byte[] b = new byte[arrays.get(i).length];
			for (int j = 0; j < b.length; j++) {
				b[j] = arrays.get(i)[j];
			}
			packets.add(createDataPacket(b, i, dest, port));
		}

		return packets;
	}

	/**
	 * Delays a given packet.
	 *
	 * @param socket
	 * @param packet
	 * @param delayTime
	 */
	public static void delayPacket(DatagramSocket socket, DatagramPacket packet, int delayTime) {
		try {
			socket.setSoTimeout(delayTime);
			socket.send(packet);
			print(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Duplicates a given packet (for error testing)
	 *
	 * @param socket to send duplicated packet
	 * @param packet to duplicate
	 */
	public static void duplicatePacket(DatagramSocket socket, DatagramPacket packet) {
		System.out.println(">Simulating error: packet duplication.");
		try {
			socket.send(packet);
			socket.send(packet);
			print(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Gets the block number of the specified packet.
	 *
	 * @param packet The packet to use.
	 * @return The block number (byte).
	 */
	public static int getBlockNumber(DatagramPacket packet) {
		byte[] blockID = {packet.getData()[2], packet.getData()[3]};
		ByteBuffer wrapped = ByteBuffer.wrap(blockID);
		Short num = wrapped.getShort();
		return ((int) num);
	}

	/**
	 * Assuming that the specified Packet is an Error Packet, this function will return
	 * the error code associated with the given packet.
	 *
	 * @param packet The packet to use.
	 * @return The error code (int from 0 - 7).
	 */
	public static int getErrorPacketErrorCode(DatagramPacket packet) {
		return packet.getData()[3];
	}

	/**
	 * Given an Error Packet, this function will get the error message.
	 *
	 * @param packet The error packet.
	 * @return The error message.
	 */
	public static String getMessageFromErrorPacket(DatagramPacket packet) {
		LinkedList<Byte> list = new LinkedList<>();
		for (int i = 4; packet.getData()[i] != (byte) 0; i++) {
			list.add(packet.getData()[i]);
		}
		String str = "";
		for (byte b : list) str += (char) b;
		return str;
	}

	/**
	 * Gets the mode.
	 *
	 * @return NetASCII or Octet.
	 */
	public static String getMode() {
		return mode;
	}

	public static int getPacketID(DatagramPacket packet) {
		return packet.getData()[1];
	}

	/**
	 * Disregards the OP Code and Block Number of the specified packet. Returns only the payload.
	 *
	 * @param packet The packet to use.
	 * @return A byte[].
	 */
	public static byte[] getPayloadFromDataPacket(DatagramPacket packet) {
		byte[] bytes = new byte[packet.getData().length - 4];
		byte[] ref = packet.getData();
		for (int i = 4; ref[i] != (byte) 0 && i < packet.getData().length - 4; ++i) {
			bytes[i - 4] = ref[i];
		}
		return bytes;
	}

	/**
	 * Gets the OP Code of the specified packet.
	 *
	 * @param packet The packet to use.
	 * @return Byte (OP Code).
	 */
	public static byte getOPCode(DatagramPacket packet) {
		return packet.getData()[1];
	}

	/**
	 * Given a RRQ or WRQ, this method will determine the name of the requested file.
	 *
	 * @param packet The WRQ or RRQ to use.
	 * @return The name being asked for.
	 */
	public static String getRequestedFileName(DatagramPacket packet) {
		String name = "";
		for (int i = 2; packet.getData()[i] != (byte) 0; i++) {
			name += (char) packet.getData()[i];
		}
		return name;
	}

	/**
	 * Determines if the specified packet is an error packet.
	 *
	 * @param packet The packet to verify.
	 * @return Boolean.
	 */
	public static boolean isErrorPacket(DatagramPacket packet) {
		if (packet.getData()[1] == (byte) 5 || packet.getData()[1] == (byte) 0) return true;
		return false;
	}

	/**
	 * Determines if the specified packet is the last data packet in the sequence.
	 * This works by reading until the last byte in the payload, and returning true if
	 * the loop encounters a 0 byte.
	 *
	 * @param packet The Data Packet to use.
	 * @return Boolean.
	 */
	public static boolean isLastDataPacket(DatagramPacket packet) {
		for (int i = 4; i < packet.getData().length; i++) {
			if (i != 515 && packet.getData()[i] == (byte) 0) return true;
		}
		return false;
	}

	/**
	 * Determines if the specified packet is a Read Request.
	 *
	 * @param packet The packet to use.
	 * @return Boolean.
	 */
	public static boolean isReadRequest(DatagramPacket packet) {
		byte[] data = packet.getData();
		return data[1] == (byte) 1;
	}

	/**
	 * Determines if the specified packet is a Write Request.
	 *
	 * @param packet The packet to use.
	 * @return Boolean.
	 */
	public static boolean isWriteRequest(DatagramPacket packet) {
		byte[] data = packet.getData();
		return data[1] == (byte) 2;
	}

	/**
	 * Prints debugging information about a specific packet.
	 *
	 * @param packet
	 */
	public static void print(DatagramPacket packet) {
		System.out.println(">>>>>>>>Packet Information<<<<<<<<");
		System.out.println("Destination: " + packet.getAddress());
		System.out.println("Port: " + packet.getPort());
		System.out.println("Payload: " + Arrays.toString(packet.getData()));
		System.out.println("Length: " + packet.getLength());
		System.out.println("Offset: " + packet.getOffset());
		System.out.println(">>>>>>>>End<<<<<<<<\n");
	}

	/**
	 * Receives a Data Packet using the specified socket.
	 *
	 * @param socket The socket to use to receive.
	 * @return A DatagramPacket.
	 */
	public static DatagramPacket receiveDataPacket(DatagramSocket socket) {
		try {
			DatagramPacket packet = createEmptyDataPacket();
			socket.receive(packet);
			return packet;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Creates an ACK request, that is sent to the destination that the specified packet came from.
	 *
	 * @param socket  The socket to send with.
	 * @param replyTo The packet to get the destination information from.
	 * @param block   The block number to use.
	 * @return The ACK packet that was sent.
	 */
	public static DatagramPacket replyToPacketWithACK(DatagramSocket socket, DatagramPacket replyTo, int block) {
		DatagramPacket ack = createEmptyAck();
		ack.setAddress(replyTo.getAddress());
		ack.setPort(replyTo.getPort());
		byte[] data = ack.getData();
		data[3] = (byte) block;
		ack.setData(data);
		try {
			for (int i = 0; i < retry; i++) {
				socket.send(ack);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ack;
	}

	/**
	 * Sends the specified Packet over the specified Socket.
	 *
	 * @param packet The Packet to send.
	 * @param socket The Socket to send with.
	 */
	public static void send(DatagramPacket packet, DatagramSocket socket) {
		try {
			socket.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Given a packet and socket, this function will send the given packet up to the total retry count, and return the
	 * packet that the socket receives.
	 *
	 * @param packet The packet to send.
	 * @param socket The socket to send with.
	 * @return The response packet.
	 */
	public synchronized static DatagramPacket sendAndReceivePacket(DatagramPacket packet, DatagramSocket socket) {
		for (int i = 0; i < retry; i++) {
			try {
				socket.send(packet);
			} catch (IOException e) {
				e.printStackTrace();
			}
			DatagramPacket ack = createEmptyAck();
			try {
				socket.receive(ack);
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (ack.getLength() > 1) return ack;
		}
		return null;
	}

	/**
	 * Toggles the mode between NetASCII and Octet.
	 *
	 * @return The current mode.
	 */
	public static String toggleMode() {
		if (mode.equalsIgnoreCase("netascii")) mode = "octet";
		else mode = "netascii";
		return mode;
	}

}
