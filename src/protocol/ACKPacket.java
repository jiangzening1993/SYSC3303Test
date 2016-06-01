package protocol;

import java.net.DatagramPacket;
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
public class ACKPacket extends Packet {
	/**
	 * Creates a new ACK Packet with OP Code 04 and Block Number 00.
	 */
	public ACKPacket() {
		this.packet = new DatagramPacket(new byte[]{0, (byte) OP_CODE.ACK.getCode(), 0, 0}, 4);
	}

	/**
	 * Creates a new ACK Packet with the specified block number.
	 *
	 * @param blockNumber The block number to use.
	 */
	public ACKPacket(int blockNumber) {
		this.packet = new DatagramPacket(new byte[]{0, (byte) OP_CODE.ACK.getCode(), 0, (byte) blockNumber}, 4);
	}

	/**
	 * Not to be used liberally. This constructor creates an ACK Packet from any kind of packet.
	 *
	 * @param packet
	 */
	public ACKPacket(Packet packet) {
		this.packet = packet.packet;
	}

	/**
	 * Creates a new ACK Packet, using the specified block number, and obtains the address and port from the returnToSender packet.
	 *
	 * @param blockNumber    The block number to use.
	 * @param returnToSender The packet to reply to.
	 */
	public ACKPacket(int blockNumber, Packet returnToSender) {
		this.packet = new DatagramPacket(new byte[]{0, (byte) OP_CODE.ACK.getCode(), 0, (byte) blockNumber}, 4);
		this.packet.setAddress(returnToSender.getAddress());
		this.packet.setPort(returnToSender.getPort());
	}

	/**
	 * Creates a new ACK Packet.
	 *
	 * @param block The block number.
	 * @param dest  The destination address.
	 * @param port  The port number to be sent to.
	 */
	public ACKPacket(int block, InetAddress dest, int port) {
		this.packet = new DatagramPacket(new byte[]{0, (byte) OP_CODE.ACK.getCode(), 0, (byte) block}, 4);
		this.packet.setAddress(dest);
		this.packet.setPort(port);
	}
}
