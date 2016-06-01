package utilities;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;
import java.util.Vector;

/**
 * Carleton University
 * Department of Systems and Computer Engineering
 * SYSC 3303 - Real-Time and Concurrent Systems
 * <p>
 * Collaborative Project
 * Iteration: 3
 * Package: Utilities
 * Created: 2016-05-11
 * License: MIT
 * <p>
 * Authors:
 * Christopher McMorran    (100968013)
 * Yue Zhang               (100980408)
 * Omar Al-Dib             (100906705)
 * Osama Rachid            (100899866)
 * Jenish Zalavadiya       (100910343)
 * Raiyan Quaium           (100962217)
 */
public class IO {

	protected static final Scanner cin = new Scanner(System.in);

	public static String input(String prompt) {
		System.out.print(prompt);
		return cin.nextLine();
	}

	/**
	 * Executes the specified command using the shell.
	 *
	 * @param input The command to execute.
	 */
	public static void shell(String input) {
		if (input.isEmpty()) return;
		try {
			Process process = Runtime.getRuntime().exec(input);
			Scanner scanner = new Scanner(process.getInputStream());
			while (scanner.hasNextLine()) System.out.println(scanner.nextLine());
			scanner.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Given a specified File, this static method creates a byte[].
	 *
	 * @param file The file to create a byte[] out of.
	 * @return A byte[].
	 */
	public static byte[] fileToByteArray(File file) {
		try {
			FileInputStream fileInputStream = new FileInputStream(file);
			byte[] array = new byte[Math.toIntExact(file.length())];
			fileInputStream.read(array);
			fileInputStream.close();
			return array;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Given a File, this static method creates a Vector of byte arrays, all of which
	 * will be 512 bytes, until a byte[] is created that is less than 512 bytes.
	 *
	 * @param file The file to split.
	 * @return A Vector of byte[].
	 */
	public static Vector<byte[]> fileToVectorOfByteArrays(File file) {
		if (file == null) return null;
		byte[] fbytes = IO.fileToByteArray(file);
		if (fbytes == null) return null;

		Vector<byte[]> segments = new Vector<>();
		for (int i = 0; i != fbytes.length; i += 512) {
			if (i + 512 >= fbytes.length) {
				int x = fbytes.length - i;
				segments.add(Arrays.copyOfRange(fbytes, i, i + x));
				break;
			}
			segments.add(Arrays.copyOfRange(fbytes, i, i + 512));
		}

		return segments;
	}

	/**
	 * Appends the specified byte array to the file corresponding to the specified file name.
	 *
	 * @param fileName The name of the file to write to.
	 * @param data     The byte array to append.
	 */
	public static void appendBytesToFile(String fileName, byte[] data) {
		FileOutputStream output;

		try {
			output = new FileOutputStream(fileName, true);
			output.write(data);
			output.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			System.out.println("A byte array has been successfully appended to the file " + fileName);
		}

	}

	/**
	 * Removes trailing null bytes from a specified byte array.
	 *
	 * @param data The byte[] to use.
	 * @return A byte[].
	 */
	public static byte[] stripTrailingNullBytes(byte[] data) {
		int i = data.length - 1;
		while (i >= 0 && data[i] == (byte) 0) --i;
		return Arrays.copyOf(data, i + 1);
	}


	/**
	 * Appends the specified byte array to the file corresponding to the specified file name.
	 * Strips all data from the first null byte.
	 *
	 * @param fileName The file name to write to.
	 * @param data     The data to use.
	 */
	public static void appendBytesToFileExcludeNull(String fileName, byte[] data) {
		int length = 0;
		while (data[length] != (byte) 0) length++;
		byte[] newData = new byte[length];
		System.arraycopy(data, 0, newData, 0, length);

		appendBytesToFile(fileName, newData);
	}

	/**
	 * Appends the specified byte array to the file corresponding to the specified file name.
	 * Strips all data from the first null byte.
	 *
	 * @param fileName The file name to write to.
	 * @param data     The data to use.
	 * @param offset   The index to start including from.
	 */
	public static void appendBytesToFileExcludeNull(String fileName, byte[] data, int offset) {
		int length = 0;
		while (data[length] != (byte) 0) length++;
		byte[] newData = new byte[length];
		System.arraycopy(data, offset, newData, offset, length - offset);

		appendBytesToFile(fileName, newData);
	}

	/**
	 * Determines if the specified String is an integer.
	 *
	 * @param sequence The string to evaluate.
	 * @return Boolean.
	 */
	public static boolean isInteger(String sequence) {
		try {
			Integer.parseInt(sequence);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Lists all of the files in the current directory.
	 */
	public static void listFiles() {
		java.io.File[] files = new java.io.File(".").listFiles();
		if (files != null) {
			for (java.io.File f : files) System.out.println(f.getName());
			System.out.println(">>>>>>>>>>>>>END<<<<<<<<<<<<<");
		} else System.out.println("No file is found");
	}

	/**
	 * Prompts the user for an integer until one is specified.
	 *
	 * @param s The prompt.
	 * @return An integer.
	 */
	public static int forceInt(String s) {
		while (true) {
			String val = IO.input(s);
			if (IO.isInteger(val)) return Integer.valueOf(val);
			System.out.println("Invalid integer. Please try again.");
			return forceInt(s);
		}
	}
}