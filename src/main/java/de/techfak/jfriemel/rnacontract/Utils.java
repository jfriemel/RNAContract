package de.techfak.jfriemel.rnacontract;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Utils {

    private Utils() {

    }

    /**
     * Prints a list of booleans in humanly readable form. true -> 1, false -> 0.
     *
     * @param bits List of booleans, where each boolean corresponds to one bit.
     */
    public static void printBits(final List<Boolean> bits) {
        StringBuilder outputBuilder = new StringBuilder();
        for (final Boolean bit : bits) {
            if (bit) {
                outputBuilder.append('1');
            } else {
                outputBuilder.append('0');
            }
        }
        System.out.println(outputBuilder.toString());
        System.out.print("Number of bits: ");
        System.out.println(bits.size());
    }

    /**
     * Generates a list of paths of all the files in a directory that have a given file ending.
     *
     * @param directory  The directory.
     * @param fileEnding The file ending that should be considered (including .).
     * @return List of paths of all the files in a directory that have the file ending.
     */
    public static List<String> getDirectoryList(final String directory, final String fileEnding) {
        List<String> paths = new ArrayList<>();
        Path start = Paths.get(directory);
        try (Stream<Path> stream = Files.walk(start, 2)) {
            paths = stream.map(String::valueOf).filter(s -> s.endsWith(fileEnding)).collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        return paths;
    }

    /**
     * Reads the first two lines of a text file.
     *
     * @param path Path to the input text file.
     * @return String array with the first two lines of the input file.
     */
    public static String[] readFile(final String path) {
        BufferedReader reader;
        String[] result = new String[2];
        try {
            reader = new BufferedReader(new FileReader(path));
            result[0] = reader.readLine();
            result[1] = reader.readLine();
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        return result;
    }

    /**
     * Writes a String to a text file.
     *
     * @param path    Path to the output text file.
     * @param content String to be written to the text file.
     */
    public static void writeFile(final String path, final String content) {
        BufferedWriter writer;
        try {
            writer = new BufferedWriter(new FileWriter(path));
            writer.write(content);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    /**
     * Reads bits from a file into a list of booleans.
     *
     * @param path Path of the file.
     * @return List of booleans.
     */
    public static List<Boolean> readBits(final String path) {
        List<Boolean> bits = new ArrayList<>();
        try {
            InputStream inputStream = new FileInputStream(path);
            byte[] bytes = inputStream.readAllBytes();
            inputStream.close();
            for (byte aByte : bytes) {
                for (int bitIndex = 0; bitIndex < 8; bitIndex++) {
                    bits.add((aByte & (byte) (128 / (1 << bitIndex))) != 0);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        return bits;
    }

    /**
     * Writes a list of booleans as bits to a file.
     *
     * @param path Path of the file.
     * @param bits List of booleans.
     */
    public static void writeBits(final String path, final List<Boolean> bits) {
        while (bits.size() % 8 != 0) {
            bits.add(false);
        }
        byte[] bytes = new byte[bits.size() / 8];
        for (int byteIndex = 0; byteIndex < bytes.length; byteIndex++) {
            for (int bitIndex = 0; bitIndex < 8; bitIndex++) {
                if (bits.get(8 * byteIndex + bitIndex)) {
                    bytes[byteIndex] |= (128 >> bitIndex);
                }
            }
        }
        try {
            OutputStream outputStream = new FileOutputStream(path);
            outputStream.write(bytes);
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    /**
     * Takes a byte number and makes it humanly readable.
     *
     * @author aioobe
     * Source: https://stackoverflow.com/questions/3758606/how-to-convert-byte-size-into-human-readable-format-in-java
     *
     * @param bytes Number of bytes.
     * @return Number of bytes in humanly readable form.
     */
    public static String humanReadableByteCount(long bytes) {
        long absB = bytes == Long.MIN_VALUE ? Long.MAX_VALUE : Math.abs(bytes);
        if (absB < 1024) {
            return bytes + " B";
        }
        long value = absB;
        CharacterIterator ci = new StringCharacterIterator("KMGTPE");
        for (int i = 40; i >= 0 && absB > 0xfffccccccccccccL >> i; i -= 10) {
            value >>= 10;
            ci.next();
        }
        value *= Long.signum(bytes);
        return String.format("%.1f %ciB", value / 1024.0, ci.current());
    }

    /**
     * Converts a tree to an XML String.
     *
     * @param tree Root of the tree.
     * @param <T>  Type of the node labels.
     * @return XML representation of the tree.
     */
    public static<T> String generateXML(Node<T> tree) {
        final StringBuilder xmlBuilder = new StringBuilder();
        recursiveXMLGenerator(tree, xmlBuilder);
        return xmlBuilder.toString();
    }

    /**
     * Recursively builds an XML String from a tree.
     *
     * @param tree       Root of the tree.
     * @param xmlBuilder StringBuilder for the XML String.
     * @param <T>        Type of the node labels.
     */
    private static<T> void recursiveXMLGenerator(final Node<T> tree, final StringBuilder xmlBuilder) {
        T label = tree.key;
        xmlBuilder.append('<');
        if (tree.children.size() > 0) {
            xmlBuilder.append(label);
            xmlBuilder.append('>');
            for (final Node<T> child : tree.children) {
                recursiveXMLGenerator(child, xmlBuilder);
            }
            xmlBuilder.append("</");
            xmlBuilder.append(label);
            xmlBuilder.append('>');
        } else {
            xmlBuilder.append(label);
            xmlBuilder.append("/>");
        }
    }

    /**
     * Exchanges the last 'length' characters of 'path' with 'newEnding'. Used to swap file endings.
     *
     * @param path      File path.
     * @param length    Length of the suffix to be removed.
     * @param newEnding New suffix to be added.
     * @return Path with swapped file endings.
     */
    public static String swapFileEndings(final String path, final int length, final String newEnding) {
        return path.substring(0, path.length() - length) + newEnding;
    }
}