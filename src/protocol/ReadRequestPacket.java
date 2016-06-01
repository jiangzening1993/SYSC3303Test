package protocol;

import java.net.InetAddress;

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
public class ReadRequestPacket extends RequestPacket {
	/**
	 * Creates a Read Request Packet.
	 * By default, the destination address is localhost:69.
	 *
	 * @param fileName
	 */
	public ReadRequestPacket(String fileName) {
		super(OP_CODE.READ_REQUEST, fileName);
	}

	/**
	 * Creates a new Read Request Packet.
	 *
	 * @param fileName The file to get.
	 * @param dest     the server to get from.
	 * @param port     The port to get from.
	 */
	public ReadRequestPacket(String fileName, InetAddress dest, int port) {
		super(OP_CODE.READ_REQUEST, fileName);
		setAddress(dest);
		setPort(port);
	}
}
