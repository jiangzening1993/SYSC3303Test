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
public class ErrorPacket extends Packet {

	/*
		  2 bytes  2 bytes        string    1 byte
          ----------------------------------------
   ERROR | 05    |  ErrorCode |   ErrMsg   |   0  |
          ----------------------------------------

	 */

	/**
	 * Creates a new Error Packet with the specified error code and message.
	 *
	 * @param code    The error code (0 - 7).
	 * @param message The message to attach.
	 */
	public ErrorPacket(ERROR_CODES code, String message) {
		LinkedList<Byte> list = new LinkedList<>();
		list.add((byte) 0);
		list.add((byte) OP_CODE.ERROR.getCode());
		list.add((byte) 0);
		list.add((byte) code.getCode());
		for (Byte b : message.getBytes()) list.add(b);
		list.add((byte) 0);
		byte[] bytes = new byte[list.size()];
		for (int i = 0; i < list.size(); i++) {
			bytes[i] = list.get(i);
		}
		try {
			this.packet = new DatagramPacket(bytes, 0, bytes.length, InetAddress.getLocalHost(), DEFAULT_PORT);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Creates a new Error Packet.
	 *
	 * @param code    The error code.
	 * @param message The error message.
	 * @param replyTo The packet to get the address and port from.
	 */
	public ErrorPacket(ERROR_CODES code, String message, Packet replyTo) {
		this(code, message);
		this.setAddress(replyTo.getAddress());
		this.setPort(replyTo.getPort());
	}


	/**
	 * Conversion constructor for converting an ACK into an Error Packet.
	 *
	 * @param ack The ACK packet to use.
	 */
	public ErrorPacket(ACKPacket ack) {
		this.packet = ack.packet;
	}

	/**
	 * Conversion constructor for converting a Packet into an Error Packet.
	 *
	 * @param packet The generic Packet.
	 */
	public ErrorPacket(Packet packet) {
		this.packet = packet.packet;
	}

	/**
	 * Gets the error message on this packet.
	 *
	 * @return The error message (String).
	 */
	public String getMessage() {
		String str = "";
		try {
			for (int i = 4; getData()[i] != (byte) 0; i++) {
				str += (char) getData()[i];
			}
		} catch (IndexOutOfBoundsException e) {
			return "<No Error Message Supplied>";
		}
		return str;
	}

	public enum ERROR_CODES {
		NOT_DEFINED,
		FILE_NOT_FOUND,
		ACCESS_VIOLATION,
		DISK_FULL,
		ILLEGAL_OPERATION,
		UNKOWN_TID,
		FILE_EXISTS,
		NO_SUCH_USER;

		/**
		 * The same as calling ordinal. This method returns the associated error code.
		 *
		 * @return An int (0 - 7).
		 */
		public int getCode() {
			return this.ordinal();
		}
	}
}
