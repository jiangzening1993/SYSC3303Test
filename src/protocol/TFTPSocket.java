package protocol;

import core.Client;
import core.Server;

import java.io.IOException;
import java.net.*;
import java.nio.channels.DatagramChannel;
import java.nio.channels.IllegalBlockingModeException;
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
public class TFTPSocket extends DatagramSocket {
	private static boolean debug = false;

	/**
	 * Constructs a datagram socket and binds it to any available port
	 * on the local host machine.  The socket will be bound to the
	 * {@link InetAddress#isAnyLocalAddress wildcard} address,
	 * an IP address chosen by the kernel.
	 * <p>
	 * <p>If there is a security manager,
	 * its {@code checkListen} method is first called
	 * with 0 as its argument to ensure the operation is allowed.
	 * This could result in a SecurityException.
	 *
	 * @throws SocketException   if the socket could not be opened,
	 *                           or the socket could not bind to the specified local port.
	 * @throws SecurityException if a security manager exists and its
	 *                           {@code checkListen} method doesn't allow the operation.
	 * @see SecurityManager#checkListen
	 */
	public TFTPSocket() throws SocketException {
	}

	/**
	 * Creates an unbound datagram socket with the specified
	 * DatagramSocketImpl.
	 *
	 * @param impl an instance of a <B>DatagramSocketImpl</B>
	 *             the subclass wishes to use on the DatagramSocket.
	 * @since 1.4
	 */
	protected TFTPSocket(DatagramSocketImpl impl) {
		super(impl);
	}

	/**
	 * Creates a datagram socket, bound to the specified local
	 * socket address.
	 * <p>
	 * If, if the address is {@code null}, creates an unbound socket.
	 * <p>
	 * <p>If there is a security manager,
	 * its {@code checkListen} method is first called
	 * with the port from the socket address
	 * as its argument to ensure the operation is allowed.
	 * This could result in a SecurityException.
	 *
	 * @param bindaddr local socket address to bind, or {@code null}
	 *                 for an unbound socket.
	 * @throws SocketException   if the socket could not be opened,
	 *                           or the socket could not bind to the specified local port.
	 * @throws SecurityException if a security manager exists and its
	 *                           {@code checkListen} method doesn't allow the operation.
	 * @see SecurityManager#checkListen
	 * @since 1.4
	 */
	public TFTPSocket(SocketAddress bindaddr) throws SocketException {
		super(bindaddr);
	}


	/**
	 * Constructs a datagram socket and binds it to the specified port
	 * on the local host machine.  The socket will be bound to the
	 * {@link InetAddress#isAnyLocalAddress wildcard} address,
	 * an IP address chosen by the kernel.
	 * <p>
	 * <p>If there is a security manager,
	 * its {@code checkListen} method is first called
	 * with the {@code port} argument
	 * as its argument to ensure the operation is allowed.
	 * This could result in a SecurityException.
	 *
	 * @param port port to use.
	 * @throws SocketException   if the socket could not be opened,
	 *                           or the socket could not bind to the specified local port.
	 * @throws SecurityException if a security manager exists and its
	 *                           {@code checkListen} method doesn't allow the operation.
	 * @see SecurityManager#checkListen
	 */
	public TFTPSocket(int port) throws SocketException {
		super(port); 
		System.out.println("Socket is hosted on port: "+port);
	}

	/**
	 * Creates a datagram socket, bound to the specified local
	 * address.  The local port must be between 0 and 65535 inclusive.
	 * If the IP address is 0.0.0.0, the socket will be bound to the
	 * {@link InetAddress#isAnyLocalAddress wildcard} address,
	 * an IP address chosen by the kernel.
	 * <p>
	 * <p>If there is a security manager,
	 * its {@code checkListen} method is first called
	 * with the {@code port} argument
	 * as its argument to ensure the operation is allowed.
	 * This could result in a SecurityException.
	 *
	 * @param port  local port to use
	 * @param laddr local address to bind
	 * @throws SocketException   if the socket could not be opened,
	 *                           or the socket could not bind to the specified local port.
	 * @throws SecurityException if a security manager exists and its
	 *                           {@code checkListen} method doesn't allow the operation.
	 * @see SecurityManager#checkListen
	 * @since JDK1.1
	 */
	public TFTPSocket(int port, InetAddress laddr) throws SocketException {
		super(port, laddr);
	}

	/**
	 * Determines if the socket is in debug mode.
	 *
	 * @return
	 */
	public static boolean isDebug() {
		return debug;
	}

	/**
	 * Sets debug mode on the socket.
	 *
	 * @param debug
	 */
	public static void setDebug(boolean debug) {
		TFTPSocket.debug = debug;
	}

	/**
	 * Sends the specified packet to it's destination.
	 *
	 * @param packet The packet to be sent.
	 * @return This socket. Null if the packet failed to send.
	 */
	public TFTPSocket send(Packet packet) {
		if (packet.packet.getAddress() == null) {
			System.out.println("Packet error. Null Address.");
		}
		try {
			this.send(packet.packet);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Resending packet...");
			try {
				this.send(packet.packet);
			} catch (IOException e1) {
				e1.printStackTrace();
				System.out.println("Packet resend failed.");
				return null;
			}
		}
		return this;
	}

	/**
	 * Receives a packet on this socket.
	 *
	 * @return The received packet.
	 */
	public Packet receive() {
		Packet packet = new Packet();
		//packet.setPort(-1);
		try {
			this.receive(packet.packet);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Attempting to receive again...");
			try {
				this.receive(packet.packet);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		return packet;
	}

	/**
	 * Receives a Data Packet on this socket.
	 *
	 * @return The received data segment. (Packet).
	 */
	public DataPacket receiveData() {
		return new DataPacket(receive());
	}

	/**
	 * Receives an ACK packet on this socket.
	 *
	 * @return An ACKPacket.
	 * @throws IOException
	 */
	public ACKPacket receiveResponse() {
		ACKPacket packet = new ACKPacket();
		try {
			this.receive(packet.packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return packet;
	}

	/**
	 * Binds this DatagramSocket to a specific address and port.
	 * <p>
	 * If the address is {@code null}, then the system will pick up
	 * an ephemeral port and a valid local address to bind the socket.
	 * <p>
	 *
	 * @param addr The address and port to bind to.
	 * @throws SocketException          if any error happens during the bind, or if the
	 *                                  socket is already bound.
	 * @throws SecurityException        if a security manager exists and its
	 *                                  {@code checkListen} method doesn't allow the operation.
	 * @throws IllegalArgumentException if addr is a SocketAddress subclass
	 *                                  not supported by this socket.
	 * @since 1.4
	 */
	@Override
	public synchronized void bind(SocketAddress addr) throws SocketException {
		super.bind(addr);
	}

	/**
	 * Connects the socket to a remote address for this socket. When a
	 * socket is connected to a remote address, packets may only be
	 * sent to or received from that address. By default a datagram
	 * socket is not connected.
	 * <p>
	 * <p>If the remote destination to which the socket is connected does not
	 * exist, or is otherwise unreachable, and if an ICMP destination unreachable
	 * packet has been received for that address, then a subsequent call to
	 * send or receive may throw a PortUnreachableException. Note, there is no
	 * guarantee that the exception will be thrown.
	 * <p>
	 * <p> If a security manager has been installed then it is invoked to check
	 * access to the remote address. Specifically, if the given {@code address}
	 * is a {@link InetAddress#isMulticastAddress multicast address},
	 * the security manager's {@link
	 * SecurityManager#checkMulticast(InetAddress)
	 * checkMulticast} method is invoked with the given {@code address}.
	 * Otherwise, the security manager's {@link
	 * SecurityManager#checkConnect(String, int) checkConnect}
	 * and {@link SecurityManager#checkAccept checkAccept} methods
	 * are invoked, with the given {@code address} and {@code port}, to
	 * verify that datagrams are permitted to be sent and received
	 * respectively.
	 * <p>
	 * <p> When a socket is connected, {@link #receive receive} and
	 * {@link #send send} <b>will not perform any security checks</b>
	 * on incoming and outgoing packets, other than matching the packet's
	 * and the socket's address and port. On a send operation, if the
	 * packet's address is set and the packet's address and the socket's
	 * address do not match, an {@code IllegalArgumentException} will be
	 * thrown. A socket connected to a multicast address may only be used
	 * to send packets.
	 *
	 * @param address the remote address for the socket
	 * @param port    the remote port for the socket.
	 * @throws IllegalArgumentException if the address is null, or the port is out of range.
	 * @throws SecurityException        if a security manager has been installed and it does
	 *                                  not permit access to the given remote address
	 * @see #disconnect
	 */
	@Override
	public void connect(InetAddress address, int port) {
		super.connect(address, port);
	}

	/**
	 * Connects this socket to a remote socket address (IP address + port number).
	 * <p>
	 * <p> If given an {@link InetSocketAddress InetSocketAddress}, this method
	 * behaves as if invoking {@link #connect(InetAddress, int) connect(InetAddress,int)}
	 * with the the given socket addresses IP address and port number.
	 *
	 * @param addr The remote address.
	 * @throws SocketException          if the connect fails
	 * @throws IllegalArgumentException if {@code addr} is {@code null}, or {@code addr} is a SocketAddress
	 *                                  subclass not supported by this socket
	 * @throws SecurityException        if a security manager has been installed and it does
	 *                                  not permit access to the given remote address
	 * @since 1.4
	 */
	@Override
	public void connect(SocketAddress addr) throws SocketException {
		super.connect(addr);
	}

	/**
	 * Disconnects the socket. If the socket is closed or not connected,
	 * then this method has no effect.
	 *
	 * @see #connect
	 */
	@Override
	public void disconnect() {
		super.disconnect();
	}

	/**
	 * Returns the binding state of the socket.
	 * <p>
	 * If the socket was bound prior to being {@link #close closed},
	 * then this method will continue to return {@code true}
	 * after the socket is closed.
	 *
	 * @return true if the socket successfully bound to an address
	 * @since 1.4
	 */
	@Override
	public boolean isBound() {
		return super.isBound();
	}

	/**
	 * Returns the connection state of the socket.
	 * <p>
	 * If the socket was connected prior to being {@link #close closed},
	 * then this method will continue to return {@code true}
	 * after the socket is closed.
	 *
	 * @return true if the socket successfully connected to a server
	 * @since 1.4
	 */
	@Override
	public boolean isConnected() {
		return super.isConnected();
	}

	/**
	 * Returns the address to which this socket is connected. Returns
	 * {@code null} if the socket is not connected.
	 * <p>
	 * If the socket was connected prior to being {@link #close closed},
	 * then this method will continue to return the connected address
	 * after the socket is closed.
	 *
	 * @return the address to which this socket is connected.
	 */
	@Override
	public InetAddress getInetAddress() {
		return super.getInetAddress();
	}

	/**
	 * Returns the port number to which this socket is connected.
	 * Returns {@code -1} if the socket is not connected.
	 * <p>
	 * If the socket was connected prior to being {@link #close closed},
	 * then this method will continue to return the connected port number
	 * after the socket is closed.
	 *
	 * @return the port number to which this socket is connected.
	 */
	@Override
	public int getPort() {
		return super.getPort();
	}

	/**
	 * Returns the address of the endpoint this socket is connected to, or
	 * {@code null} if it is unconnected.
	 * <p>
	 * If the socket was connected prior to being {@link #close closed},
	 * then this method will continue to return the connected address
	 * after the socket is closed.
	 *
	 * @return a {@code SocketAddress} representing the remote
	 * endpoint of this socket, or {@code null} if it is
	 * not connected yet.
	 * @see #getInetAddress()
	 * @see #getPort()
	 * @see #connect(SocketAddress)
	 * @since 1.4
	 */
	@Override
	public SocketAddress getRemoteSocketAddress() {
		return super.getRemoteSocketAddress();
	}

	/**
	 * Returns the address of the endpoint this socket is bound to.
	 *
	 * @return a {@code SocketAddress} representing the local endpoint of this
	 * socket, or {@code null} if it is closed or not bound yet.
	 * @see #getLocalAddress()
	 * @see #getLocalPort()
	 * @see #bind(SocketAddress)
	 * @since 1.4
	 */
	@Override
	public SocketAddress getLocalSocketAddress() {
		return super.getLocalSocketAddress();
	}

	/**
	 * Sends a datagram packet from this socket. The
	 * {@code DatagramPacket} includes information indicating the
	 * data to be sent, its length, the IP address of the remote host,
	 * and the port number on the remote host.
	 * <p>
	 * <p>If there is a security manager, and the socket is not currently
	 * connected to a remote address, this method first performs some
	 * security checks. First, if {@code p.getAddress().isMulticastAddress()}
	 * is true, this method calls the
	 * security manager's {@code checkMulticast} method
	 * with {@code p.getAddress()} as its argument.
	 * If the evaluation of that expression is false,
	 * this method instead calls the security manager's
	 * {@code checkConnect} method with arguments
	 * {@code p.getAddress().getHostAddress()} and
	 * {@code p.getPort()}. Each call to a security manager method
	 * could result in a SecurityException if the operation is not allowed.
	 *
	 * @param p the {@code DatagramPacket} to be sent.
	 * @throws IOException                  if an I/O error occurs.
	 * @throws SecurityException            if a security manager exists and its
	 *                                      {@code checkMulticast} or {@code checkConnect}
	 *                                      method doesn't allow the send.
	 * @throws PortUnreachableException     may be thrown if the socket is connected
	 *                                      to a currently unreachable destination. Note, there is no
	 *                                      guarantee that the exception will be thrown.
	 * @throws IllegalBlockingModeException if this socket has an associated channel,
	 *                                      and the channel is in non-blocking mode.
	 * @throws IllegalArgumentException     if the socket is connected,
	 *                                      and connected address and packet address differ.
	 * @revised 1.4
	 * @spec JSR-51
	 * @see DatagramPacket
	 * @see SecurityManager#checkMulticast(InetAddress)
	 * @see SecurityManager#checkConnect
	 */
	@Override
	public void send(DatagramPacket p) throws IOException {
		if (debug) {
			System.out.println("<----------Packet Dump---------->");
			System.out.println("Destination: " + p.getAddress());
			System.out.println("Dest. Port: " + p.getPort());
			System.out.println("Data: " + Arrays.toString(p.getData()));
			System.out.println("Data: " + new String(p.getData()));
			System.out.println("Length: " + p.getLength());
			System.out.println("Offset: " + p.getOffset());
			System.out.println("Socket Address: " + p.getSocketAddress());
			System.out.println("<------------------------------->");
		}
		super.send(p);
	}

	/**
	 * Receives a datagram packet from this socket. When this method
	 * returns, the {@code DatagramPacket}'s buffer is filled with
	 * the data received. The datagram packet also contains the sender's
	 * IP address, and the port number on the sender's machine.
	 * <p>
	 * This method blocks until a datagram is received. The
	 * {@code length} field of the datagram packet object contains
	 * the length of the received message. If the message is longer than
	 * the packet's length, the message is truncated.
	 * <p>
	 * If there is a security manager, a packet cannot be received if the
	 * security manager's {@code checkAccept} method
	 * does not allow it.
	 *
	 * @param p the {@code DatagramPacket} into which to place
	 *          the incoming data.
	 * @throws IOException                  if an I/O error occurs.
	 * @throws SocketTimeoutException       if setSoTimeout was previously called
	 *                                      and the timeout has expired.
	 * @throws PortUnreachableException     may be thrown if the socket is connected
	 *                                      to a currently unreachable destination. Note, there is no guarantee that the
	 *                                      exception will be thrown.
	 * @throws IllegalBlockingModeException if this socket has an associated channel,
	 *                                      and the channel is in non-blocking mode.
	 * @revised 1.4
	 * @spec JSR-51
	 * @see DatagramPacket
	 * @see DatagramSocket
	 */
	@Override
	public synchronized void receive(DatagramPacket p) throws IOException {
		try {
			super.receive(p);
			Server.transferring = false;
			Client.inProgress = false;
		} catch (IOException e) {
		}
		if (debug) {
			System.out.println("<-----------Packet Receive Dump------------->");
			System.out.println("Destination Address: " + p.getAddress());
			System.out.println("Dest. Port: " + p.getPort());
			System.out.println("Data: " + Arrays.toString(p.getData()));
			System.out.println("Data: " + new String(p.getData()));
			System.out.println("Offset: " + p.getOffset());
			// System.out.println("Socket: " + p.getSocketAddress());
			System.out.println("<------------------------------------------->");
		}
	}

	/**
	 * Gets the local address to which the socket is bound.
	 * <p>
	 * <p>If there is a security manager, its
	 * {@code checkConnect} method is first called
	 * with the host address and {@code -1}
	 * as its arguments to see if the operation is allowed.
	 *
	 * @return the local address to which the socket is bound,
	 * {@code null} if the socket is closed, or
	 * an {@code InetAddress} representing
	 * {@link InetAddress#isAnyLocalAddress wildcard}
	 * address if either the socket is not bound, or
	 * the security manager {@code checkConnect}
	 * method does not allow the operation
	 * @see SecurityManager#checkConnect
	 * @since 1.1
	 */
	@Override
	public InetAddress getLocalAddress() {
		return super.getLocalAddress();
	}

	/**
	 * Returns the port number on the local host to which this socket
	 * is bound.
	 *
	 * @return the port number on the local host to which this socket is bound,
	 * {@code -1} if the socket is closed, or
	 * {@code 0} if it is not bound yet.
	 */
	@Override
	public int getLocalPort() {
		return super.getLocalPort();
	}

	/**
	 * Retrieve setting for SO_TIMEOUT.  0 returns implies that the
	 * option is disabled (i.e., timeout of infinity).
	 *
	 * @return the setting for SO_TIMEOUT
	 * @throws SocketException if there is an error in the underlying protocol, such as an UDP error.
	 * @see #setSoTimeout(int)
	 * @since JDK1.1
	 */
	@Override
	public synchronized int getSoTimeout() throws SocketException {
		return super.getSoTimeout();
	}

	/**
	 * Enable/disable SO_TIMEOUT with the specified timeout, in
	 * milliseconds. With this option set to a non-zero timeout,
	 * a call to receive() for this DatagramSocket
	 * will block for only this amount of time.  If the timeout expires,
	 * a <B>java.net.SocketTimeoutException</B> is raised, though the
	 * DatagramSocket is still valid.  The option <B>must</B> be enabled
	 * prior to entering the blocking operation to have effect.  The
	 * timeout must be {@code > 0}.
	 * A timeout of zero is interpreted as an infinite timeout.
	 *
	 * @param timeout the specified timeout in milliseconds.
	 * @throws SocketException if there is an error in the underlying protocol, such as an UDP error.
	 * @see #getSoTimeout()
	 * @since JDK1.1
	 */
	@Override
	public synchronized void setSoTimeout(int timeout) {
		try {
			super.setSoTimeout(timeout);
		} catch (SocketException e) {
			System.out.println("Maximum file transfer time exceeded.");
			Client.inProgress = false;
		}
	}

	/**
	 * Get value of the SO_SNDBUF option for this {@code DatagramSocket}, that is the
	 * buffer size used by the platform for output on this {@code DatagramSocket}.
	 *
	 * @return the value of the SO_SNDBUF option for this {@code DatagramSocket}
	 * @throws SocketException if there is an error in
	 *                         the underlying protocol, such as an UDP error.
	 * @see #setSendBufferSize
	 */
	@Override
	public synchronized int getSendBufferSize() throws SocketException {
		return super.getSendBufferSize();
	}

	/**
	 * Sets the SO_SNDBUF option to the specified value for this
	 * {@code DatagramSocket}. The SO_SNDBUF option is used by the
	 * network implementation as a hint to size the underlying
	 * network I/O buffers. The SO_SNDBUF setting may also be used
	 * by the network implementation to determine the maximum size
	 * of the packet that can be sent on this socket.
	 * <p>
	 * As SO_SNDBUF is a hint, applications that want to verify
	 * what size the buffer is should call {@link #getSendBufferSize()}.
	 * <p>
	 * Increasing the buffer size may allow multiple outgoing packets
	 * to be queued by the network implementation when the send rate
	 * is high.
	 * <p>
	 * Note: If {@link #send(DatagramPacket)} is used to send a
	 * {@code DatagramPacket} that is larger than the setting
	 * of SO_SNDBUF then it is implementation specific if the
	 * packet is sent or discarded.
	 *
	 * @param size the size to which to set the send buffer
	 *             size. This value must be greater than 0.
	 * @throws SocketException          if there is an error
	 *                                  in the underlying protocol, such as an UDP error.
	 * @throws IllegalArgumentException if the value is 0 or is
	 *                                  negative.
	 * @see #getSendBufferSize()
	 */
	@Override
	public synchronized void setSendBufferSize(int size) throws SocketException {
		super.setSendBufferSize(size);
	}

	/**
	 * Get value of the SO_RCVBUF option for this {@code DatagramSocket}, that is the
	 * buffer size used by the platform for input on this {@code DatagramSocket}.
	 *
	 * @return the value of the SO_RCVBUF option for this {@code DatagramSocket}
	 * @throws SocketException if there is an error in the underlying protocol, such as an UDP error.
	 * @see #setReceiveBufferSize(int)
	 */
	@Override
	public synchronized int getReceiveBufferSize() throws SocketException {
		return super.getReceiveBufferSize();
	}

	/**
	 * Sets the SO_RCVBUF option to the specified value for this
	 * {@code DatagramSocket}. The SO_RCVBUF option is used by the
	 * the network implementation as a hint to size the underlying
	 * network I/O buffers. The SO_RCVBUF setting may also be used
	 * by the network implementation to determine the maximum size
	 * of the packet that can be received on this socket.
	 * <p>
	 * Because SO_RCVBUF is a hint, applications that want to
	 * verify what size the buffers were set to should call
	 * {@link #getReceiveBufferSize()}.
	 * <p>
	 * Increasing SO_RCVBUF may allow the network implementation
	 * to buffer multiple packets when packets arrive faster than
	 * are being received using {@link #receive(DatagramPacket)}.
	 * <p>
	 * Note: It is implementation specific if a packet larger
	 * than SO_RCVBUF can be received.
	 *
	 * @param size the size to which to set the receive buffer
	 *             size. This value must be greater than 0.
	 * @throws SocketException          if there is an error in
	 *                                  the underlying protocol, such as an UDP error.
	 * @throws IllegalArgumentException if the value is 0 or is
	 *                                  negative.
	 * @see #getReceiveBufferSize()
	 */
	@Override
	public synchronized void setReceiveBufferSize(int size) throws SocketException {
		super.setReceiveBufferSize(size);
	}

	/**
	 * Tests if SO_REUSEADDR is enabled.
	 *
	 * @return a {@code boolean} indicating whether or not SO_REUSEADDR is enabled.
	 * @throws SocketException if there is an error
	 *                         in the underlying protocol, such as an UDP error.
	 * @see #setReuseAddress(boolean)
	 * @since 1.4
	 */
	@Override
	public synchronized boolean getReuseAddress() throws SocketException {
		return super.getReuseAddress();
	}

	/**
	 * Enable/disable the SO_REUSEADDR socket option.
	 * <p>
	 * For UDP sockets it may be necessary to bind more than one
	 * socket to the same socket address. This is typically for the
	 * purpose of receiving multicast packets
	 * (See {@link MulticastSocket}). The
	 * {@code SO_REUSEADDR} socket option allows multiple
	 * sockets to be bound to the same socket address if the
	 * {@code SO_REUSEADDR} socket option is enabled prior
	 * to binding the socket using {@link #bind(SocketAddress)}.
	 * <p>
	 * Note: This functionality is not supported by all existing platforms,
	 * so it is implementation specific whether this option will be ignored
	 * or not. However, if it is not supported then
	 * {@link #getReuseAddress()} will always return {@code false}.
	 * <p>
	 * When a {@code DatagramSocket} is created the initial setting
	 * of {@code SO_REUSEADDR} is disabled.
	 * <p>
	 * The behaviour when {@code SO_REUSEADDR} is enabled or
	 * disabled after a socket is bound (See {@link #isBound()})
	 * is not defined.
	 *
	 * @param on whether to enable or disable the
	 * @throws SocketException if an error occurs enabling or
	 *                         disabling the {@code SO_RESUEADDR} socket option,
	 *                         or the socket is closed.
	 * @see #getReuseAddress()
	 * @see #bind(SocketAddress)
	 * @see #isBound()
	 * @see #isClosed()
	 * @since 1.4
	 */
	@Override
	public synchronized void setReuseAddress(boolean on) throws SocketException {
		super.setReuseAddress(on);
	}

	/**
	 * Tests if SO_BROADCAST is enabled.
	 *
	 * @return a {@code boolean} indicating whether or not SO_BROADCAST is enabled.
	 * @throws SocketException if there is an error
	 *                         in the underlying protocol, such as an UDP error.
	 * @see #setBroadcast(boolean)
	 * @since 1.4
	 */
	@Override
	public synchronized boolean getBroadcast() throws SocketException {
		return super.getBroadcast();
	}

	/**
	 * Enable/disable SO_BROADCAST.
	 * <p>
	 * <p> Some operating systems may require that the Java virtual machine be
	 * started with implementation specific privileges to enable this option or
	 * send broadcast datagrams.
	 *
	 * @param on whether or not to have broadcast turned on.
	 * @throws SocketException if there is an error in the underlying protocol, such as an UDP
	 *                         error.
	 * @see #getBroadcast()
	 * @since 1.4
	 */
	@Override
	public synchronized void setBroadcast(boolean on) throws SocketException {
		super.setBroadcast(on);
	}

	/**
	 * Gets traffic class or type-of-service in the IP datagram
	 * header for packets sent from this DatagramSocket.
	 * <p>
	 * As the underlying network implementation may ignore the
	 * traffic class or type-of-service set using {@link #setTrafficClass(int)}
	 * this method may return a different value than was previously
	 * set using the {@link #setTrafficClass(int)} method on this
	 * DatagramSocket.
	 *
	 * @return the traffic class or type-of-service already set
	 * @throws SocketException if there is an error obtaining the
	 *                         traffic class or type-of-service value.
	 * @see #setTrafficClass(int)
	 * @since 1.4
	 */
	@Override
	public synchronized int getTrafficClass() throws SocketException {
		return super.getTrafficClass();
	}

	/**
	 * Sets traffic class or type-of-service octet in the IP
	 * datagram header for datagrams sent from this DatagramSocket.
	 * As the underlying network implementation may ignore this
	 * value applications should consider it a hint.
	 * <p>
	 * <P> The tc <B>must</B> be in the range {@code 0 <= tc <=
	 * 255} or an IllegalArgumentException will be thrown.
	 * <p>Notes:
	 * <p>For Internet protocol v4 the value consists of an
	 * {@code integer}, the least significant 8 bits of which
	 * represent the value of the TOS octet in IP packets sent by
	 * the socket.
	 * RFC 1349 defines the TOS values as follows:
	 * <p>
	 * <UL>
	 * <LI><CODE>IPTOS_LOWCOST (0x02)</CODE></LI>
	 * <LI><CODE>IPTOS_RELIABILITY (0x04)</CODE></LI>
	 * <LI><CODE>IPTOS_THROUGHPUT (0x08)</CODE></LI>
	 * <LI><CODE>IPTOS_LOWDELAY (0x10)</CODE></LI>
	 * </UL>
	 * The last low order bit is always ignored as this
	 * corresponds to the MBZ (must be zero) bit.
	 * <p>
	 * Setting bits in the precedence field may result in a
	 * SocketException indicating that the operation is not
	 * permitted.
	 * <p>
	 * for Internet protocol v6 {@code tc} is the value that
	 * would be placed into the sin6_flowinfo field of the IP header.
	 *
	 * @param tc an {@code int} value for the bitset.
	 * @throws SocketException if there is an error setting the
	 *                         traffic class or type-of-service
	 * @see #getTrafficClass
	 * @since 1.4
	 */
	@Override
	public synchronized void setTrafficClass(int tc) throws SocketException {
		super.setTrafficClass(tc);
	}

	/**
	 * Closes this datagram socket.
	 * <p>
	 * Any thread currently blocked in {@link #receive} upon this socket
	 * will throw a {@link SocketException}.
	 * <p>
	 * <p> If this socket has an associated channel then the channel is closed
	 * as well.
	 *
	 * @revised 1.4
	 * @spec JSR-51
	 */
	@Override
	public void close() {
		super.close();
	}

	/**
	 * Returns whether the socket is closed or not.
	 *
	 * @return true if the socket has been closed
	 * @since 1.4
	 */
	@Override
	public boolean isClosed() {
		return super.isClosed();
	}

	/**
	 * Returns the unique {@link DatagramChannel} object
	 * associated with this datagram socket, if any.
	 * <p>
	 * <p> A datagram socket will have a channel if, and only if, the channel
	 * itself was created via the {@link DatagramChannel#open
	 * DatagramChannel.open} method.
	 *
	 * @return the datagram channel associated with this datagram socket,
	 * or {@code null} if this socket was not created for a channel
	 * @spec JSR-51
	 * @since 1.4
	 */
	@Override
	public DatagramChannel getChannel() {
		return super.getChannel();
	}
}
