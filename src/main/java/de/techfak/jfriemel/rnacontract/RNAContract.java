package de.techfak.jfriemel.rnacontract;

import com.beust.jcommander.JCommander;

import java.io.File;
import java.text.DecimalFormat;
import java.util.*;

public class RNAContract {

    private static final HuffmanMaps HUFFMAN_MAPS = new HuffmanMaps();

    private static final Map<Character, char[]> VARIABLE_MAP = new HashMap<>();

    private static long runtime;

    private static boolean debug;
    private static boolean alternative;

    private static int numberOfNodes = 0;
    private static int numberOfUnaryNodes = 0;
    private static int numberOfBinaryNodes = 0;

    public static void main(String[] args) {

        Locale.setDefault(new Locale("en", "GB"));

        CommandLineArgs cmdLineArgs = new CommandLineArgs();
        JCommander.newBuilder().addObject(cmdLineArgs).build().parse(args);

        boolean statistics = cmdLineArgs.statistics;
        debug = cmdLineArgs.debug;
        alternative = cmdLineArgs.alternative;

        String input = cmdLineArgs.input;
        String output = cmdLineArgs.output;
        if (cmdLineArgs.compress) {
            if (output == null) {
                if (alternative) {
                    output = Utils.swapFileEndings(input, 3, "rnac2");
                } else {
                    output = Utils.swapFileEndings(input, 3, "rnac");
                }
            }
            compressFile(input, output);
            System.out.println("Compression successful. Compressed file at " + output);
        } else if (cmdLineArgs.decompress) {
            if (output == null) {
                if (alternative) {
                    output = Utils.swapFileEndings(input, 5, "txt");
                } else {
                    output = Utils.swapFileEndings(input, 4, "txt");
                }
            }
            decompressFile(input, output);
            System.out.println("Decompression successful. Decompressed file at " + output);
        } else if (cmdLineArgs.xml) {
            if (output == null) {
                output = Utils.swapFileEndings(input, 3, "xml");
            }
            createXML(input, output);
            System.out.println("XML generation successful. XML file at " + output);
        } else {
            System.out.println("Please use -c for compression and -d for decompression.");
            System.exit(0);
        }

        if (statistics) {
            printStatistics(input, output);
        }

    }

    /**
     * Compresses a .txt RNA file. Saves the compressed file to the given output path.
     *
     * @param input  Path of the .txt file.
     * @param output Path of the .rnac file to be written.
     */
    public static void compressFile(final String input, final String output) {
        runtime = System.currentTimeMillis();
        final String[] rna = Utils.readFile(input);
        final List<Boolean> bits;
        if (alternative) {
            bits = compressAlternative(rna[0], rna[1]);
        } else {
            bits = compress(rna[0], rna[1]);
        }
        if (debug) {
            Utils.printBits(bits);
        }
        Utils.writeBits(output, bits);
        runtime = System.currentTimeMillis() - runtime;
    }

    /**
     * Decompresses a .rnac file. Saves the decompressed RNA data to the given output path.
     * @param input  Path of the .rnac file.
     * @param output Path of the .txt file to be written.
     */
    public static void decompressFile(final String input, final String output) {
        runtime = System.currentTimeMillis();
        final List<Boolean> bits = Utils.readBits(input);
        final String[] rna;
        if (alternative) {
            rna = decompressAlternative(bits);
        } else {
            rna = decompress(bits);
        }
        final String text = rna[0] + '\n' + rna[1];
        if (debug) {
            System.out.println(text);
        }
        Utils.writeFile(output, text);
        runtime = System.currentTimeMillis() - runtime;
    }

    /**
     * Creates an XML file representing the contracted tree of a .txt RNA file.
     *
     * @param input  Path of the .txt file.
     * @param output Path of the .xml file to be written.
     */
    private static void createXML(final String input, final String output) {
        runtime = System.currentTimeMillis();
        final String[] rna = Utils.readFile(input);
        final Node<String> tree = buildContractedTree(rna[0], rna[1]);
        final String xml = Utils.generateXML(tree);
        if (debug) {
            System.out.println(xml);
        }
        Utils.writeFile(output, xml);
        runtime = System.currentTimeMillis() - runtime;
    }

    /**
     * Prints some very basic statistics to the console.
     *
     * @param input  Input path of the (de-)compressed file.
     * @param output Output path of the (de-)compressed file.
     */
    public static void printStatistics(final String input, final String output) {
        long inputSize = new File(input).length();
        System.out.println("\nInput file size:  " + Utils.humanReadableByteCount(inputSize));
        long outputSize = new File(output).length();
        System.out.println("Output file size: " + Utils.humanReadableByteCount(outputSize));
        double ratio = ((double) inputSize) / ((double) outputSize);
        if (ratio > 1) {
            ratio = 1 / ratio;
        }
        DecimalFormat percentFormat = new DecimalFormat("0.00%");
        System.out.println("Compression rate: " + percentFormat.format(ratio));
        System.out.println("Processing time:  " + (double) runtime/1000 + "s\n");

        System.out.println("Total number of nodes:  " + numberOfNodes);
        System.out.println("Number of unary nodes:  " + numberOfUnaryNodes);
        System.out.println("Number of binary nodes: " + numberOfBinaryNodes);
    }

    /**
     * Compresses an RNA sequence with the corresponding secondary structure.
     *
     * @param sequence  RNA sequence.
     * @param structure RNA secondary structure.
     * @return List of bits.
     */
    public static List<Boolean> compress(final String sequence, final String structure) {
        final Node<String> tree = buildContractedTree(sequence.toLowerCase(), structure);
        final List<Boolean> compression = new ArrayList<>();

        compression.addAll(compressUnlabeledTree(tree));
        compression.addAll(compressLabels(tree));

        return compression;
    }

    /**
     * Decompresses a bit sequence into an RNA sequence including its secondary structure.
     *
     * @param compressed Compressed bit sequence.
     * @return Decompressed RNA. 0: Sequence. 1: Structure.
     */
    public static String[] decompress(final List<Boolean> compressed) {
        final Node<String> tree = decompressUnlabeledTree(compressed);

        decompressLabels(compressed, tree);

        return treeToRNA(tree);
    }

    /**
     * Compresses the structure of a given tree into a bit sequence (in this case: list of booleans).
     *
     * @param <T>  Type of the tree data. Irrelevant in this case as only the structure is compressed.
     * @param tree Root node of the tree.
     * @return Bit sequence.
     */
    public static<T> List<Boolean> compressUnlabeledTree(final Node<T> tree) {
        final String imbalancedBrackets = tree.getImbalancedBrackets();
        List<Boolean> result = new ArrayList<>();
        for (int i = 1; i < imbalancedBrackets.length(); i++) {
            result.add(imbalancedBrackets.charAt(i) == '(');
        }
        return result;
    }

    /**
     * Compresses the labels of a contracted RNA tree into a bit sequence.
     *
     * @param tree Root node of the contracted RNA tree.
     * @return Bit sequence.
     */
    public static List<Boolean> compressLabels(final Node<String> tree) {
        final StringBuilder labelBuilder = new StringBuilder();
        labelBuilder.append(';');
        for (final String nodeLabel : tree.getPreorder()) {
            labelBuilder.append(nodeLabel);
            labelBuilder.append(';');
        }
        final String labels = labelBuilder.toString();
        List<Boolean> result = new ArrayList<>();
        String currentPair;
        Map<String, List<Boolean>> huffmanUnary = HUFFMAN_MAPS.getUnaryC();
        Map<String, List<Boolean>> huffmanBinary = HUFFMAN_MAPS.getBinaryC();
        for (int i = 0; i < labels.length() - 1; i++) {
            currentPair = labels.substring(i, i + 2);
            if (huffmanUnary.containsKey(currentPair)) {
                result.addAll(huffmanUnary.get(currentPair));
            } else if (huffmanBinary.containsKey(currentPair)) {
                result.addAll(huffmanBinary.get(currentPair));
            }
        }
        return result;
    }

    /**
     * Decompresses part of a bit sequence into an unlabeled tree.
     *
     * @param compressedTree Bit sequence of the compressed tree.
     * @return Root node of the decompressed tree.
     */
    public static Node<String> decompressUnlabeledTree(final List<Boolean> compressedTree) {
        Node<String> current = new Node<>("");
        Node<String> root = current;
        int i = 0;
        while (current != null && i < compressedTree.size()) {
            if (compressedTree.get(i)) {
                Node<String> child = new Node<>("");
                current.addChild(child);
                current = child;
            } else {
                current = current.parent;
            }
            while (current != null && current.children.size() == 2) {
                current = current.parent;
            }
            i++;
        }
        compressedTree.subList(0, i).clear();
        return root;
    }

    /**
     * Decompresses a bit sequence into RNA symbols and inserts them into the contracted tree.
     *
     * @param compressed Compressed bit sequence.
     * @param tree       Root node of the contracted tree.
     */
    public static void decompressLabels(final List<Boolean> compressed, final Node<String> tree) {
        KeyAndIndex kai;
        int start = 0;
        for (final Node<String> node : tree.getPreorderNodes()) {
            numberOfNodes++;
            if (node.children.size() == 0) {
                node.key = "e";
            } else if (node.children.size() == 1) {
                numberOfUnaryNodes++;
                kai = decompressNode(compressed, true, start);
                node.key = kai.key;
                start = kai.index;
            } else if (node.children.size() == 2) {
                numberOfBinaryNodes++;
                kai = decompressNode(compressed, false, start);
                node.key = kai.key;
                start = kai.index;
            }
        }
        compressed.clear();
    }

    /**
     * Decompresses part of a bit sequence into RNA symbols from a single node.
     *
     * @param compressed Compressed bit sequence.
     * @param unary      True, if the sequence contains unary symbols; False, otherwise.
     * @param start      Start index of the compressed bit sequence.
     * @return RNA symbols of one node and new start index of the bit sequence (encapsulated in KeyAndIndex object).
     */
    private static KeyAndIndex decompressNode(final List<Boolean> compressed, final boolean unary, int start) {
        final StringBuilder seqBuilder = new StringBuilder();
        Map<List<Boolean>, Character> huffman;
        char previous = ';';
        Character current;
        int subSize;
        List<Boolean> prefix;
        while (true) {
            if (unary) {
                huffman = HUFFMAN_MAPS.getUnaryD(previous);
                subSize = 0;
            } else {
                huffman = HUFFMAN_MAPS.getBinaryD(previous);
                subSize = 1;
            }
            do {
                subSize++;
                prefix = compressed.subList(start, start + subSize);
                current = huffman.get(prefix);
            } while (current == null);
            start += subSize;
            if (current == ';') {
                break;
            }
            seqBuilder.append(current);
            previous = current;
        }
        return new KeyAndIndex(seqBuilder.toString(), start);
    }

    /**
     * An alternative compression method that does not explicitly compress the tree but implicitly by adding marker bits
     * to the preorder sequence.
     * This method is not properly documented in the thesis as it achieves basically the same compression rate (only a
     * slightly fewer bits) but it not as easily extensible.
     *
     * @param sequence  RNA sequence.
     * @param structure RNA secondary structure.
     * @return Bit sequence, compressed with alternative method.
     */
    public static List<Boolean> compressAlternative(final String sequence, final String structure) {
        final Node<String> tree = buildContractedTree(sequence.toLowerCase(), structure);
        List<Boolean> bits = new ArrayList<>();
        Map<String, List<Boolean>> huffmanUnary = HUFFMAN_MAPS.getUnaryC();
        Map<String, List<Boolean>> huffmanBinary = HUFFMAN_MAPS.getBinaryC();
        for (final Node<String> node : tree.getPreorderNodes()) {
            final String label = ';' + node.key + ';';
            if (node.children.size() == 1) {
                bits.add(false);
                for (int i = 0; i < label.length() - 1; i++) {
                    bits.addAll(huffmanUnary.get(label.substring(i, i + 2)));
                }
                bits.add(!node.children.get(0).key.equals("e"));
            } else if (node.children.size() == 2) {
                bits.add(true);
                for (int i = 0; i < label.length() - 1; i++) {
                    bits.addAll(huffmanBinary.get(label.substring(i, i + 2)));
                }
                bits.add(!node.children.get(0).key.equals("e"));
                bits.add(!node.children.get(1).key.equals("e"));
            }
        }
        return bits;
    }

    /**
     * An alternative decompression method that is not properly documented. See JavaDoc for compressAlternative().
     *
     * @param compressed Bit sequence, compressed with alternative method.
     * @return Decompressed RNA. 0: Sequence. 1: Structure.
     */
    public static String[] decompressAlternative(final List<Boolean> compressed) {
        final Node<String> root = new Node<>("");
        int index = 0;
        Deque<Node<String>> nodesToDecode = new LinkedList<>();
        Node<String> current = root;
        while (index < compressed.size()) {
            numberOfNodes++;
            boolean unary = !compressed.get(index++);
            KeyAndIndex kai = decompressNode(compressed, unary, index);
            current.key = kai.key;
            index = kai.index;
            if (unary) {
                numberOfUnaryNodes++;
                Node<String> child = new Node<>("");
                current.addChild(child);
                if (compressed.get(index++)) {
                    current = child;
                } else {
                    numberOfNodes++;
                    child.key = "e";
                    if (nodesToDecode.isEmpty()) {
                        break;
                    } else {
                        current = nodesToDecode.pop();
                    }
                }
            } else {
                numberOfBinaryNodes++;
                Node<String> left = new Node<>("");
                Node<String> right = new Node<>("");
                current.addChild(left);
                current.addChild(right);
                boolean leftEmpty = true;
                if (compressed.get(index++)) {
                    current = left;
                    leftEmpty = false;
                } else {
                    numberOfNodes++;
                    left.key = "e";
                }
                if (compressed.get(index++)) {
                    if (leftEmpty) {
                        current = right;
                    } else {
                        nodesToDecode.push(right);
                    }
                } else {
                    numberOfNodes++;
                    right.key = "e";
                    if (leftEmpty) {
                        if (!nodesToDecode.isEmpty()) {
                            current = nodesToDecode.pop();
                        } else {
                            break;
                        }
                    }
                }
            }
        }
        return treeToRNA(root);
    }

    /**
     * Takes an RNA sequence and structure and builds the corresponding unary-binary tree.
     * Nodes with 'a', 'c', 'g' or 'u' are always unary. If there is no child node, an empty node is appended.
     * In the same mindset, nodes with 'p', 'q', 'r', 's', 'x' or 'y' are always binary.
     *
     * @param sequence  RNA sequence.
     * @param structure RNA secondary structure.
     * @return Root of the strict unary-binary RNA tree.
     */
    public static Node<Character> buildStrictlyRankedTree(final String sequence, final String structure) {
        Node<Character> current = new Node<>('f');
        Node<Character> unclear;
        char base;
        char key = ' ';
        int singleDepth = 0;
        Deque<Integer> singleDepthStack = new LinkedList<>();
        Deque<Node<Character>> unclearPairStack = new LinkedList<>();
        for (int i = 0; i < sequence.length(); i++) {
            base = sequence.charAt(i);
            switch (structure.charAt(i)) {
                case '.':
                    current.key = base;
                    Node<Character> next = new Node<>('e');
                    current.addChild(next);
                    current = next;
                    singleDepth++;
                    break;
                case '(':
                    singleDepthStack.push(++singleDepth);
                    singleDepth = 0;
                    switch (base) {
                        case 'a':
                            key = 'p';
                            break;
                        case 'u':
                            key = 'q';
                            break;
                        case 'c':
                            key = 'r';
                            break;
                        case 'g':
                            key = 's';
                    }
                    current.key = key;
                    Node<Character> left = new Node<>('e');
                    current.addChild(left);
                    unclearPairStack.push(current);
                    current = left;
                    break;
                case ')':
                    unclear = unclearPairStack.pop();
                    switch (base) {
                        /* Since G and U have two possible pairings, the key for the corresponding
                         * node might need to be altered. */
                        case 'u':
                            if (unclear.key == 's') {
                                unclear.key = 'y';
                            }
                            break;
                        case 'g':
                            if (unclear.key == 'q') {
                                unclear.key = 'x';
                            }
                    }
                    while (singleDepth > 0) {
                        current = current.parent;
                        singleDepth--;
                    }
                    if (!singleDepthStack.isEmpty()) {
                        singleDepth = singleDepthStack.pop();
                    }
                    current = current.parent;
                    Node<Character> right = new Node<>('e');
                    current.addChild(right);
                    current = right;
            }
        }
        while (current.parent != null) {
            current = current.parent;
        }
        return current;
    }

    /**
     * Builds a strict unary-binary tree but contracts sequences of unary and binary nodes into single super nodes to
     * compress the tree.
     *
     * @param sequence  RNA sequence.
     * @param structure RNA secondary structure.
     * @return Root of the contracted unary-binary tree.
     */
    public static Node<String> buildContractedTree(final String sequence, final String structure) {
        return buildContractedTree(buildStrictlyRankedTree(sequence, structure));
    }

    /**
     * Does the same as buildContractedTree(String, String) but uses a pre-existing strict unary-binary tree to avoid
     * having to generate it again.
     *
     * @param root Root of the strict unary-binary tree.
     * @return Root of the contracted unary-binary tree.
     */
    public static Node<String> buildContractedTree(Node<Character> root) {
        Node<String> contractedRoot = new Node<>("f");
        copyToContractedTree(contractedRoot, root);
        if (!contractedRoot.children.isEmpty()) {
            contractedRoot = contractedRoot.children.get(0);
        }
        return contractedRoot;
    }

    /**
     * Helper method that recursively performs contractions for the contracted tree.
     *
     * @param contractedRoot Root of the contracted tree.
     * @param root           Root of the strict unary-binary tree.
     */
    private static void copyToContractedTree(final Node<String> contractedRoot, Node<Character> root) {
        Node<String> contractedChild;
        numberOfNodes++;
        if (root.children.size() == 0) {
            contractedRoot.addChild(new Node<>(root.key.toString()));
        } else if (root.children.size() == 1) {
            numberOfUnaryNodes++;
            StringBuilder superNodeKeys = new StringBuilder();
            while (root.children.size() == 1) {
                superNodeKeys.append(root.key.toString());
                root = root.children.get(0);
            }
            contractedChild = new Node<>(superNodeKeys.toString());
            contractedRoot.addChild(contractedChild);
            copyToContractedTree(contractedChild, root);
        } else {
            numberOfBinaryNodes++;
            StringBuilder contractedNodeKeys = new StringBuilder();
            Node<Character> rightSide = root.children.get(1);
            do {
                contractedNodeKeys.append(root.key);
                root = root.children.get(0);
            } while (root.children.size() > 1 && root.children.get(1).key == 'e');
            contractedChild = new Node<>(contractedNodeKeys.toString());
            contractedRoot.addChild(contractedChild);
            copyToContractedTree(contractedChild, root);
            copyToContractedTree(contractedChild, rightSide);
        }
    }

    /**
     * Converts a labeled contracted tree to an RNA sequence and secondary structure.
     *
     * @param root Root of the contracted tree.
     * @return RNA. 0: Sequence. 1: Structure.
     */
    public static String[] treeToRNA(final Node<String> root) {
        StringBuilder seqBuilder = new StringBuilder();
        StringBuilder strucBuilder = new StringBuilder();
        VARIABLE_MAP.put('p', new char[]{'A', 'U'});
        VARIABLE_MAP.put('q', new char[]{'U', 'A'});
        VARIABLE_MAP.put('r', new char[]{'C', 'G'});
        VARIABLE_MAP.put('s', new char[]{'G', 'C'});
        VARIABLE_MAP.put('x', new char[]{'U', 'G'});
        VARIABLE_MAP.put('y', new char[]{'G', 'U'});
        recursiveTreeToRNA(root, seqBuilder, strucBuilder);

        final String sequence = seqBuilder.toString();
        final String structure = strucBuilder.toString();
        return new String[]{sequence, structure};
    }

    /**
     * Helper method that recursively traverses the tree to create the corresponding RNA sequence and secondary
     * structure.
     *
     * @param root         Root of the contracted tree.
     * @param seqBuilder   StringBuilder that builds the RNA sequence.
     * @param strucBuilder StringBuilder that builds the RNA secondary structure.
     */
    private static void recursiveTreeToRNA(final Node<String> root, final StringBuilder seqBuilder,
                                           final StringBuilder strucBuilder) {
        if (root.children.size() == 1) {
            seqBuilder.append(root.key.toUpperCase());
            for (int i = 0; i < root.key.length(); i++) {
                strucBuilder.append('.');
            }
            recursiveTreeToRNA(root.children.get(0), seqBuilder, strucBuilder);
        } else if (root.children.size() == 2) {
            Deque<Character> reverse = new LinkedList<>();
            for (int i = 0; i < root.key.length(); i++) {
                char[] current = VARIABLE_MAP.get(root.key.charAt(i));
                seqBuilder.append(current[0]);
                strucBuilder.append('(');
                reverse.push(current[1]);
            }
            recursiveTreeToRNA(root.children.get(0), seqBuilder, strucBuilder);
            while (!reverse.isEmpty()) {
                seqBuilder.append(reverse.pop());
                strucBuilder.append(')');
            }
            recursiveTreeToRNA(root.children.get(1), seqBuilder, strucBuilder);
        }
    }
}

/**
 * decompressNode() needs to return two values: The decompressed key and the new start index of the compressed bit list.
 * Therefore it returns a KeyAndIndex object which contains both values.
 */
class KeyAndIndex {
    public String key;
    public int index;
    public KeyAndIndex(final String key, final int index) {
        this.key = key;
        this.index = index;
    }
}
