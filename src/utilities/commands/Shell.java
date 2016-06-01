package utilities.commands;

import java.io.IOException;
import java.util.Scanner;

/**
 * Carleton University
 * Department of Systems and Computer Engineering
 * SYSC 3303 - Real-Time and Concurrent Systems
 * <p>
 * Collaborative Project
 * <p>
 * Package: utilities.commands
 * Created: 2016-05-29
 * License: MIT
 * <p>
 * Authors:
 * Christopher McMorran    (100968013)
 * Yue Zhang               (100980408)
 * Raiyan Quaium           (100962217)
 */
public class Shell {
	/**
	 * Creates a new shell instance, executing the string specified in the native shell.
	 *
	 * @param value The command to execute.
	 */
	public Shell(String value) {
		if (value.isEmpty()) return;
		try {
			Process process = Runtime.getRuntime().exec(System.getProperty("os.name").contains("Win") ? "cmd /c " + value : value);
			Scanner scanner = new Scanner(process.getInputStream());
			while (scanner.hasNextLine()) System.out.println(scanner.nextLine());
			scanner.close();
		} catch (IOException e) {
			System.out.println("Command not understood.");
		}
	}
}
