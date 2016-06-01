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
public class DataPacket extends Packet {
	/**
	 * Creates a new Data Packet with the specified block number and data.
	 *
	 * @param blockNumber The block number.
	 * @param data        The data to encode.
	 */
	public DataPacket(int blockNumber, byte[] data) {
		LinkedList<Byte> builder = new LinkedList<>();
		builder.add((byte) 0);
		builder.add((byte) OP_CODE.DATA.getCode());
		builder.add((byte) 0);
		builder.add((byte) blockNumber);
		for (Byte b : data) builder.add(b);
		byte[] bytes = new byte[builder.size()];
		for (int i = 0; i < builder.size(); i++) {
			bytes[i] = builder.get(i);
		}
		try {
			this.packet = new DatagramPacket(bytes, 0, bytes.length, InetAddress.getLocalHost(), DEFAULT_PORT);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Creates a new data packet.
	 *
	 * @param blockNumber The block number.
	 * @param data        The data to encode.
	 * @param addr        The destination address.
	 * @param port        The destination port.
	 */
	public DataPacket(int blockNumber, byte[] data, InetAddress addr, int port) {
		this(blockNumber, data);
		setAddress(addr);
		setPort(port);
	}

	/**
	 * Creates a new Data Packet from a generic Packet.
	 *
	 * @param packet The packet to use.
	 */
	public DataPacket(Packet packet) {
		this.packet = packet.packet;
	}

	/**
	 * Ignores the OP Code and Block Number. Returns only the data from the packet.
	 *
	 * @return A byte[].
	 */
	public byte[] getPayload() {
		byte[] _return = new byte[getLength() - 4];
		System.arraycopy(getData(), 4, _return, 0, _return.length);
		return _return;
	}

}
