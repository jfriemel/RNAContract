package de.techfak.jfriemel.rnacontract;

import com.beust.jcommander.JCommander;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.LinkedList;
import java.util.List;

public class RNAContract {

    /* p : A-U   q : U-A
     * r : C-G   s : G-C
     * x : U-G   y : G-U
     */
    private static final List<Character> BINARIES = Arrays.asList('p', 'q', 'r', 's', 'x', 'y');
    private static final List<Character> UNARIES = Arrays.asList('a', 'c', 'g', 'u');

    private static final HuffmanMaps HUFFMAN_MAPS = new HuffmanMaps();

    private static final Map<Character, char[]> VARIABLE_MAP = new HashMap<>();

    private static final int DELIMITER_BITS = 24;

    private static long runtime;

    private static boolean debug;

    public static void main(String[] args) {

        CommandLineArgs cmdLineArgs = new CommandLineArgs();
        JCommander.newBuilder().addObject(cmdLineArgs).build().parse(args);

        boolean statistics = cmdLineArgs.statistics;
        debug = cmdLineArgs.debug;

        String input = cmdLineArgs.input;
        String output = cmdLineArgs.output;
        if (cmdLineArgs.compress) {
            if (output == null) {
                output = Utils.swapFileEndings(input, 3, "rnac");
            }
            compressFile(input, output);
            System.out.println("Compression successful. Compressed file at " + output);
        } else if (cmdLineArgs.decompress) {
            if (output == null) {
                output = Utils.swapFileEndings(input, 4, "txt");
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
        final List<Boolean> bits = compress(rna[0], rna[1]);
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
        final String[] rna = decompress(bits);
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
        System.out.println("Input file size:  " + Utils.humanReadableByteCount(new File(input).length()));
        System.out.println("Output file size: " + Utils.humanReadableByteCount(new File(output).length()));
        System.out.println("(De-)Compression time: " + (double) runtime/1000 + "s");
    }

    /**
     * Compresses an RNA sequence with the corresponding secondary structure.
     *
     * @param sequence  RNA sequence.
     * @param structure RNA secondary structure.
     * @return List of bits.
     */
    public static List<Boolean> compress(final String sequence, final String structure) {
        Node<String> tree = buildContractedTree(sequence.toLowerCase(), structure);
        String[] preorder = preorderContractedUnaryBinary(tree);

        int numberOfBinaries;
        if (preorder[1].contains(";")) {
            numberOfBinaries = preorder[1].split(";").length;
        } else {
            numberOfBinaries = 0;
        }

        List<Boolean> compression = new ArrayList<>();

        compression.addAll(Utils.intToBinary(numberOfBinaries, DELIMITER_BITS));
        compression.addAll(compressUnlabeledTree(tree));
        compression.addAll(compressBinaryPreorder(preorder[1]));
        compression.addAll(compressUnaryPreorder(preorder[0]));

        return compression;
    }

    /**
     * Decompresses a bit sequence into an RNA sequence including its secondary structure.
     *
     * @param compressed Compressed bit sequence.
     * @return Decompressed RNA. 0: Sequence. 1: Structure.
     */
    public static String[] decompress(List<Boolean> compressed) {
        final List<Boolean> sublist = compressed.subList(0, DELIMITER_BITS);
        final int numberOfBinaries = Utils.binaryToInt(sublist);
        sublist.clear();
        final Node<String> tree = decompressUnlabeledTree(compressed);
        final String binary = decompressBinaryPreorder(compressed, numberOfBinaries);
        final String unary = decompressUnaryPreorder(compressed);
        insertLabels(tree, unary, binary);
        return treeToRNA(tree);
    }

    /**
     * Compressed the structure of a given tree into a bit sequence (in this case: list of booleans).
     *
     * @param <T>  Type of the tree data. Irrelevant in this case as only the structure is compressed.
     * @param tree Root node of the tree.
     * @return Bit sequence.
     */
    public static<T> List<Boolean> compressUnlabeledTree(final Node<T> tree) {
        final String balancedParentheses = tree.getImbalancedBrackets();
        List<Boolean> result = new ArrayList<>();
        for (int i = 1; i < balancedParentheses.length(); i++) {
            result.add(balancedParentheses.charAt(i) == '(');
        }
        return result;
    }

    /**
     * Compresses the preorder sequence of the unary nodes of a tree using the conditional Huffman code.
     *
     * @param unary Preorder sequence of the unary node labels.
     * @return Bit sequence.
     */
    public static List<Boolean> compressUnaryPreorder(final String unary) {
        return compressSomePreorder(unary, HUFFMAN_MAPS.getUnaryC());
    }

    /**
     * Compresses the preorder sequence of the binary nodes of a tree using the conditional Huffman code.
     *
     * @param binary Preorder sequence of the binary node labels.
     * @return Bit sequence.
     */
    public static List<Boolean> compressBinaryPreorder(final String binary) {
        return compressSomePreorder(binary, HUFFMAN_MAPS.getBinaryC());
    }

    /**
     * Compresses the preorder sequence of either unary or binary nodes of a tree using the corresponding conditional
     * Huffman code.
     *
     * @param symbols Preorder sequence of the node labels.
     * @param huffman Corresponding conditional Huffman code.
     * @return Bit sequence.
     */
    private static List<Boolean> compressSomePreorder(final String symbols, final Map<String, List<Boolean>> huffman) {
        List<Boolean> result = new ArrayList<>();
        if (symbols.length() == 0) {
            return result;
        }
        result.addAll(huffman.get(";" + symbols.charAt(0)));
        String currentPair;
        for (int i = 0; i < symbols.length() - 1; i++) {
            currentPair = symbols.substring(i, i + 2);
            result.addAll(huffman.get(currentPair));
        }
        return result;
    }

    /**
     * Decompresses a bit sequence into an unlabeled tree.
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
     * Decompresses a bit sequence into a preorder sequence of labels of unary nodes using a conditional Huffman code.
     *
     * @param compressedUnary Bit sequence of the compressed preorder sequence.
     * @return Preorder sequence.
     */
    public static String decompressUnaryPreorder(List<Boolean> compressedUnary) {
        StringBuilder unaryBuilder = new StringBuilder();
        Map<List<Boolean>, Character> huffman;
        char previous = ';';
        Character current;
        int subSize;
        List<Boolean> prefix;
        while (compressedUnary.size() > 0) {
            huffman = HUFFMAN_MAPS.getUnaryD(previous);
            subSize = 0;
            do {
                subSize++;
                prefix = compressedUnary.subList(0, subSize);
                current = huffman.get(prefix);
            } while (current == null && subSize < compressedUnary.size());
            if (current == null) {
                break;
            }
            unaryBuilder.append(current);
            previous = current;
            prefix.clear();
        }
        return unaryBuilder.toString();
    }

    /**
     * Decompresses a bit sequence into a preorder sequence of labels of binary nodes using a conditional Huffman code.
     *
     * @param compressedBinary Bit sequence of the compressed preorder sequence.
     * @return Preorder sequence.
     */
    public static String decompressBinaryPreorder(List<Boolean> compressedBinary) {
        return decompressBinaryPreorder(compressedBinary, Integer.MAX_VALUE);
    }

    /**
     * Decompresses a bit sequence into a preorder sequence of labels of binary nodes using a conditional Huffman code.
     * Stops after a given number of nodes.
     *
     * @param compressedBinary Bit sequence of the compressed preorder sequence.
     * @param maxNodes         Maximum number of nodes to be decompressed.
     * @return Preorder sequence.
     */
    public static String decompressBinaryPreorder(List<Boolean> compressedBinary, int maxNodes) {
        StringBuilder binaryBuilder = new StringBuilder();
        Map<List<Boolean>, Character> huffman;
        char previous = ';';
        Character current;
        int subSize;
        List<Boolean> prefix;
        while (compressedBinary.size() > 1 && maxNodes > 0) {
            huffman = HUFFMAN_MAPS.getBinaryD(previous);
            subSize = 1;
            do {
                subSize++;
                prefix = compressedBinary.subList(0, subSize);
                current = huffman.get(prefix);
            } while (current == null && subSize < compressedBinary.size());
            if (current == null) {
                break;
            }
            binaryBuilder.append(current);
            previous = current;
            prefix.clear();
            if (current == ';') {
                maxNodes--;
            }
        }
        return binaryBuilder.toString();
    }

    /**
     * Inserts unary and binary labels into a contracted tree.
     *
     * @param tree   Contracted tree.
     * @param unary  Unary labels.
     * @param binary Binary labels.
     */
    public static void insertLabels(final Node<String> tree, final String unary, final String binary) {
        final String[] unaryNodes = unary.split(";");
        final String[] binaryNodes = binary.split(";");

        int unaryIndex = 0;
        int binaryIndex = 0;
        for (final Node<String> node : tree.getPreorderNodes()) {
            if (node.children.size() == 0) {
                node.key = "e";
            } else if (node.children.size() == 1) {
                node.key = unaryNodes[unaryIndex];
                unaryIndex++;
            } else {
                node.key = binaryNodes[binaryIndex];
                binaryIndex++;
            }
        }
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
        if (root.children.size() == 0) {
            contractedRoot.addChild(new Node<>(root.key.toString()));
        } else if (root.children.size() == 1) {
            StringBuilder superNodeKeys = new StringBuilder();
            while (root.children.size() == 1) {
                superNodeKeys.append(root.key.toString());
                root = root.children.get(0);
            }
            contractedChild = new Node<>(superNodeKeys.toString());
            contractedRoot.addChild(contractedChild);
            copyToContractedTree(contractedChild, root);
        } else {
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
        return new String[]{seqBuilder.toString(), strucBuilder.toString()};
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

    /**
     * Generates two preorder sequences for a contracted unary-binary tree.
     * First sequence: Unary symbols. Second sequence: Binary symbols.
     * Symbols of different super nodes are separated by ';'.
     *
     * @param root Root of the contracted tree.
     * @return String array with the two sequences.
     */
    public static String[] preorderContractedUnaryBinary(final Node<String> root) {
        List<String> preorder = root.getPreorder();
        StringBuilder unaryBuilder = new StringBuilder();
        StringBuilder binaryBuilder = new StringBuilder();
        char firstSymbol;
        for (final String symbols : preorder) {
            firstSymbol = symbols.charAt(0);
            if (UNARIES.contains(firstSymbol)) {
                unaryBuilder.append(symbols);
                unaryBuilder.append(';');
            } else if (BINARIES.contains(firstSymbol)) {
                binaryBuilder.append(symbols);
                binaryBuilder.append(';');
            }
        }
        return new String[]{unaryBuilder.toString(), binaryBuilder.toString()};
    }

}
