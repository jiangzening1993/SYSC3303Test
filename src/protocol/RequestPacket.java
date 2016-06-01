package protocol;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;

/**
 * Carleton University
 * Department of Systems and Computer Engineering
 * SYSC 3303 - Real-Time and Concurrent Systems
 * <p>
 * Collaborative Project
 * <p>
 * Package: protocol
 * Created: 2016-05-29
 * License: MIT
 * <p>
 * Authors:
 * Christopher McMorran    (100968013)
 * Yue Zhang               (100980408)
 * Raiyan Quaium           (100962217)
 */
public class RequestPacket extends Packet {

	/**
	 * Creates a new RequestPacket using the specified OP Code.
	 * This OP Code should only be either 1 or 2.
	 *
	 * @param OP_CODE  The OP Code for the Request Packet.
	 * @param fileName The file name to be requested.
	 */
	protected RequestPacket(OP_CODE OP_CODE, String fileName) {
		LinkedList<Byte> packetBuilder = new LinkedList<>();
		packetBuilder.add((byte) 0);
		packetBuilder.add((byte) OP_CODE.getCode());
		for (Byte b : fileName.getBytes()) packetBuilder.add(b);
		packetBuilder.add((byte) 0);
		for (Byte b : "netascii".getBytes()) packetBuilder.add(b);
		packetBuilder.add((byte) 0);

		byte[] bytes = new byte[packetBuilder.size()];
		for (int i = 0; i < packetBuilder.size(); ++i) bytes[i] = packetBuilder.get(i);
		try {
			this.packet = new DatagramPacket(bytes, 0, bytes.length, InetAddress.getLocalHost(), DEFAULT_PORT);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Creates a new Requestpacket from a Packet.
	 *
	 * @param packet
	 */
	public RequestPacket(Packet packet) {
		this.packet = packet.packet;
	}

	/**
	 * Gets the requested file name from the packet.
	 *
	 * @return String
	 */
	public String getFileName() {
		String str = "";
		for (int i = 2; this.packet.getData()[i] != (byte) 0; i++) {
			str += (char) this.packet.getData()[i];
		}
		return str;
	}

	/**
	 * Prints the data associated with the specified packet.
	 *
	 * @return This packet.
	 */
	public void print() {
		System.out.printf("Address: %s\nPort: %d\nOP Code: %d\nPayload: %s\n", this.packet.getAddress().getHostAddress(), this.packet.getPort(), this.packet.getData()[1], new String(this.packet.getData()));
	}
}
