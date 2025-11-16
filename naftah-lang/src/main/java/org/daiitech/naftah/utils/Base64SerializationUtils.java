package org.daiitech.naftah.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.Base64;

import static org.daiitech.naftah.errors.ExceptionUtils.newNaftahBugInvalidUsageError;

/**
 * Utility class for serializing and deserializing Java objects to and from Base64 strings.
 * <p>
 * Supports both in-memory and file-based operations.
 * All methods are static and the class cannot be instantiated.
 * </p>
 *
 * <p>
 * Usage examples:
 * </p>
 * <pre>{@code
 * String base64 = Base64SerializationUtils.serialize(myObject);
 * MyClass obj = (MyClass) Base64SerializationUtils.deserialize(base64);
 * }</pre>
 *
 * @author Chakib Daii
 */
public final class Base64SerializationUtils {

	/**
	 * Private constructor to prevent instantiation.
	 */
	private Base64SerializationUtils() {
		throw newNaftahBugInvalidUsageError();
	}

	/**
	 * Serializes a {@link Serializable} object to a Base64-encoded string.
	 *
	 * @param o the serializable object to serialize
	 * @return a Base64-encoded string representation of the object
	 * @throws IOException if an I/O error occurs during serialization
	 */
	public static String serialize(Serializable o) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(o);
		oos.close();
		return Base64.getEncoder().encodeToString(baos.toByteArray());
	}

	/**
	 * Serializes a {@link Serializable} object to a Base64-encoded string and writes it to a file.
	 *
	 * @param o    the serializable object to serialize
	 * @param path the file path where the Base64 string should be saved
	 * @return the string path to the file
	 * @throws IOException if an I/O error occurs during serialization or file writing
	 */
	public static String serialize(Serializable o, Path path) throws IOException {
		return serialize(o, path.toFile());
	}

	/**
	 * Serializes a {@link Serializable} object to a Base64-encoded string and writes it to a file.
	 *
	 * @param o    the serializable object to serialize
	 * @param file the file where the Base64 string should be saved
	 * @return the string path to the file
	 * @throws IOException if an I/O error occurs during serialization or file writing
	 */
	public static String serialize(Serializable o, File file) throws IOException {
		FileOutputStream fos = new FileOutputStream(file);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		oos.writeObject(serialize(o));
		oos.close();
		fos.close();
		return file.getPath();
	}

	/**
	 * Deserializes an object from a Base64-encoded string.
	 *
	 * @param s the Base64 string to deserialize
	 * @return the deserialized object
	 * @throws IOException            if an I/O error occurs during deserialization
	 * @throws ClassNotFoundException if the class of the serialized object cannot be found
	 */
	public static Object deserialize(String s) throws IOException, ClassNotFoundException {
		byte[] data = Base64.getDecoder().decode(s);
		ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
		Object o = ois.readObject();
		ois.close();
		return o;
	}

	/**
	 * Deserializes an object from a file containing a Base64-encoded string.
	 *
	 * @param path the path to the file
	 * @return the deserialized object
	 * @throws IOException            if an I/O error occurs during file reading or deserialization
	 * @throws ClassNotFoundException if the class of the serialized object cannot be found
	 */
	public static Object deserialize(Path path) throws IOException, ClassNotFoundException {
		return deserialize(path.toFile());
	}

	/**
	 * Deserializes an object from a file containing a Base64-encoded string.
	 *
	 * @param file the file containing the Base64 string
	 * @return the deserialized object
	 * @throws IOException            if an I/O error occurs during file reading or deserialization
	 * @throws ClassNotFoundException if the class of the serialized object cannot be found
	 */
	public static Object deserialize(File file) throws IOException, ClassNotFoundException {
		FileInputStream fis = new FileInputStream(file);
		ObjectInputStream oin = new ObjectInputStream(fis);
		String base64Object = (String) oin.readObject();
		oin.close();
		fis.close();
		return deserialize(base64Object);
	}
}
