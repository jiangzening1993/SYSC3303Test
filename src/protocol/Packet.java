package protocol;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

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
public class Packet {
	protected static final int DEFAULT_PORT = 69;
	protected DatagramPacket packet;

	/*
			opcode  operation
            1     Read request (RRQ)
            2     Write request (WRQ)
            3     Data (DATA)
            4     Acknowledgment (ACK)
            5     Error (ERROR)
	 */

	public Packet(DatagramPacket packet) {
		this.packet = packet;
	}

	/**
	 * Creates an empty packet with 516 bytes of data, all set to 0. The default address is localhost:69
	 */
	public Packet() {
		try {
			this.packet = new DatagramPacket(new byte[516], 0, 516, InetAddress.getLocalHost(), DEFAULT_PORT);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Returns the IP address of the machine to which this datagram is being
	 * sent or from which the datagram was received.
	 *
	 * @return the IP address of the machine to which this datagram is being
	 * sent or from which the datagram was received.
	 * @see InetAddress
	 * @see #setAddress(InetAddress)
	 */
	public InetAddress getAddress() {
		return packet.getAddress();
	}

	/**
	 * Sets the IP address of the machine to which this datagram
	 * is being sent.
	 *
	 * @param iaddr the {@code InetAddress}
	 * @see #getAddress()
	 * @since JDK1.1
	 */
	public void setAddress(InetAddress iaddr) {
		packet.setAddress(iaddr);
	}

	/**
	 * Returns the port number on the remote host to which this datagram is
	 * being sent or from which the datagram was received.
	 *
	 * @return the port number on the remote host to which this datagram is
	 * being sent or from which the datagram was received.
	 * @see #setPort(int)
	 */
	public int getPort() {
		return packet.getPort();
	}

	/**
	 * Sets the port number on the remote host to which this datagram
	 * is being sent.
	 *
	 * @param iport the port number
	 * @see #getPort()
	 * @since JDK1.1
	 */
	public void setPort(int iport) {
		packet.setPort(iport);
	}

	/**
	 * Set the data buffer for this packet. This sets the
	 * data, length and offset of the packet.
	 *
	 * @param buf    the buffer to set for this packet
	 * @param offset the offset into the data
	 * @param length the length of the data
	 *               and/or the length of the buffer used to receive data
	 * @throws NullPointerException if the argument is null
	 * @see #getData
	 * @see #getOffset
	 * @see #getLength
	 * @since 1.2
	 */
	public void setData(byte[] buf, int offset, int length) {
		packet.setData(buf, offset, length);
	}

	/**
	 * Returns the length of the data to be sent or the length of the
	 * data received.
	 *
	 * @return the length of the data to be sent or the length of the
	 * data received.
	 * @see #setLength(int)
	 */
	public int getLength() {
		return packet.getLength();
	}

	/**
	 * Set the length for this packet. The length of the packet is
	 * the number of bytes from the packet's data buffer that will be
	 * sent, or the number of bytes of the packet's data buffer that
	 * will be used for receiving data. The length must be lesser or
	 * equal to the offset plus the length of the packet's buffer.
	 *
	 * @param length the length to set for this packet.
	 * @throws IllegalArgumentException if the length is negative
	 *                                  of if the length is greater than the packet's data buffer
	 *                                  length.
	 * @see #getLength
	 * @see #setData
	 * @since JDK1.1
	 */
	public void setLength(int length) {
		packet.setLength(length);
	}

	/**
	 * Returns the offset of the data to be sent or the offset of the
	 * data received.
	 *
	 * @return the offset of the data to be sent or the offset of the
	 * data received.
	 * @since 1.2
	 */
	public int getOffset() {
		return packet.getOffset();
	}

	/**
	 * Gets the SocketAddress (usually IP address + port number) of the remote
	 * host that this packet is being sent to or is coming from.
	 *
	 * @return the {@code SocketAddress}
	 * @see #setSocketAddress
	 * @since 1.4
	 */
	public SocketAddress getSocketAddress() {
		return packet.getSocketAddress();
	}

	/**
	 * Sets the SocketAddress (usually IP address + port number) of the remote
	 * host to which this datagram is being sent.
	 *
	 * @param address the {@code SocketAddress}
	 * @throws IllegalArgumentException if address is null or is a
	 *                                  SocketAddress subclass not supported by this socket
	 * @see #getSocketAddress
	 * @since 1.4
	 */
	public void setSocketAddress(SocketAddress address) {
		packet.setSocketAddress(address);
	}

	/**
	 * Returns the data buffer. The data received or the data to be sent
	 * starts from the {@code offset} in the buffer,
	 * and runs for {@code length} long.
	 *
	 * @return the buffer used to receive or  send data
	 * @see #setData(byte[], int, int)
	 */
	public byte[] getData() {
		return packet.getData();
	}

	/**
	 * Set the data buffer for this packet. With the offset of
	 * this DatagramPacket set to 0, and the length set to
	 * the length of {@code buf}.
	 *
	 * @param buf the buffer to set for this packet.
	 * @throws NullPointerException if the argument is null.
	 * @see #getLength
	 * @see #getData
	 * @since JDK1.1
	 */
	public void setData(byte[] buf) {
		packet.setData(buf);
	}

	/**
	 * Gets the OP Code associated with the specified packet.
	 *
	 * @return The OP Code. (byte)
	 */
	public byte getOPCode() {
		return this.packet.getData()[1];
	}

	/**
	 * Sets the OP Code on the specified packet.
	 *
	 * @param code The OP Code.
	 * @return This packet.
	 */
	public Packet setOPCode(int code) {
		byte[] data = this.getData();
		data[1] = (byte) code;
		this.packet.setData(data);
		return this;
	}

	/**
	 * Gets the byte representing the error code.
	 *
	 * @return The error code. (byte).
	 */
	public byte getError() {
		return this.getData()[3];
	}

	/**
	 * Gets the block number on the associated, appropriate packet.
	 *
	 * @return The block number.
	 */
	public int getBlockNumber() {
		return (int) this.getData()[3];
	}

	/**
	 * Sets the payload on this Packet. This ignored the first 4 bytes.
	 *
	 * @return This packet.
	 */
	public Packet setPayload(byte[] payload) {
		byte[] nData = new byte[payload.length + 4];
		nData[0] = this.getData()[0];
		nData[1] = this.getData()[1];
		nData[2] = this.getData()[2];
		nData[3] = this.getData()[3];

		try {
			System.arraycopy(payload, 4, nData, 4, nData.length - 3);
		} catch (ArrayIndexOutOfBoundsException e) {
		}
		this.setData(nData);

		return this;
	}

	public enum OP_CODE {
		READ_REQUEST, WRITE_REQUEST, DATA, ACK, ERROR;

		public int getCode() {
			return this.ordinal() + 1;
		}
	}
	
	public void print(){
		System.out.println(">>>>>>>>Packet Information<<<<<<<<");
		System.out.println("Destination: " + packet.getAddress());
		System.out.println("Port: " + packet.getPort());
		System.out.println("Payload: " + Arrays.toString(packet.getData()));
		System.out.println("Length: " + packet.getLength());
		System.out.println("Offset: " + packet.getOffset());
		System.out.println(">>>>>>>>End<<<<<<<<\n");
	}
}
