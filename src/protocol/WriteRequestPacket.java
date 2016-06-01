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
public class WriteRequestPacket extends RequestPacket {
	/**
	 * Creates a Write Request Packet.
	 *
	 * @param fileName The file name to request.
	 */
	public WriteRequestPacket(String fileName) {
		super(OP_CODE.WRITE_REQUEST, fileName);
	}

	/**
	 * Creates a write request packet.
	 *
	 * @param fileName The file to ask for.
	 * @param dest     The destination address.
	 * @param port     The destination port.
	 */
	public WriteRequestPacket(String fileName, InetAddress dest, int port) {
		super(OP_CODE.WRITE_REQUEST, fileName);
		setAddress(dest);
		setPort(port);
	}
}
