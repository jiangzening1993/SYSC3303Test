package utilities.commands;

/**
 * Carleton University
 * Department of Systems and Computer Engineering
 * SYSC 3303 - Real-Time and Concurrent Systems
 * <p>
 * Collaborative Project
 * <p>
 * Package: utilities.commands
 * Created: 2016-05-30
 * License: MIT
 * <p>
 * Authors:
 * Christopher McMorran    (100968013)
 * Yue Zhang               (100980408)
 * Raiyan Quaium           (100962217)
 */
public class Help extends Command {

	@Override
	public void exec() {
		System.out.println("<------------------------------ Help Page ----------------------------------->\n" +
				"\n" +
				"    List of Commands:\n" +
				"\n" +
				"        connect         Connect to a server (remote/local tftp service).\n" +
				"        read <file>      Downloads the specified file from the server.\n" +
				"        write <file>      Uploads the specified file to the server.\n" +
				"        ls              Lists all of the files in the current directory.\n" +
				"        mode            Set file transfer mode.\n" +
				"        verbose         Toggle verbose mode.\n" +
				"        debug           Toggle debug mode.\n" +
				"        port            Specifies the remote port to connect to.\n" +
				"        status          Show current status.\n" +
				"        timeout         Set total retransmission timeout.\n" +
				"        help            Display this page.\n" +
				"        quit            Exits the application.\n" +
				"\n" +
				"<-------------------------- End of Help Page --------------------------------->");
	}
}
