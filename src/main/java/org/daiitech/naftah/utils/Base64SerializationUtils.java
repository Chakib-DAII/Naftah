package org.daiitech.naftah.utils;

import java.io.*;
import java.nio.file.Path;
import java.util.Base64;

/**
 * @author Chakib Daii
 **/
public final class Base64SerializationUtils {
    private Base64SerializationUtils() {}

    public static String serialize(Serializable o) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(o);
        oos.close();
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    public static String serialize(Serializable o, Path path) throws IOException {
        return serialize(o, path.toFile());
    }

    public static String serialize(Serializable o, File file) throws IOException {
        FileOutputStream fos = new FileOutputStream(file);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(serialize(o));
        oos.close();
        fos.close();
        return file.getPath();
    }

    public static Object deserialize(String s) throws IOException, ClassNotFoundException {
        byte[] data = Base64.getDecoder().decode(s);
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
        Object o = ois.readObject();
        ois.close();
        return o;
    }

    public static Object deserialize(Path path) throws IOException, ClassNotFoundException {
        return deserialize(path.toFile());
    }

    public static Object deserialize(File file) throws IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream(file);
        ObjectInputStream oin = new ObjectInputStream(fis);
        String base64Object = (String) oin.readObject();
        oin.close();
        fis.close();
        return deserialize(base64Object);
    }
}
