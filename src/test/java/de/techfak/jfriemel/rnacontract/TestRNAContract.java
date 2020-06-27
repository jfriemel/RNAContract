package de.techfak.jfriemel.rnacontract;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestRNAContract {

    public static String[] unpairedBases = {"a", "c", "g", "u"};
    public static String[] pairs = {"au", "ua", "cg", "gc", "gu", "ug"};
    public static char[] pairsXML = {'p', 'q', 'r', 's', 'y', 'x'};

    @Test
    public void testEmpty() {
        decompressCompressed("", "");
        List<Boolean> empty = new ArrayList<>();
        assertArrayEquals(new String[]{"", ""}, RNAContract.decompress(empty));
    }

    @Test
    public void testSingleBase() {
        for (final String base : unpairedBases) {
            decompressCompressed(base, ".");
        }
    }

    @Test
    public void testSinglePair() {
        for (final String pair : pairs) {
            decompressCompressed(pair, "()");
        }
    }

    @Test
    public void testShortExample1() {
        decompressCompressed("CGCGCGCGCGCAGGACCCCGGGGCUAUUAGCUCAGUUUGGUUAGAGCGCACCCCUGAUAAGGGUGAGGUCGCUGAUUCGAAUUCAGCAUAGCCCA", "()()()()()(.)(.)..()(((((((..((((..........)))).(((((.......))))).....(((((.......)))))))))))).");
    }

    @Test
    public void testShortExample2() {
        decompressCompressed("accgugagccauguuaggga", "..((((...)))..(.).).");
    }

    private static void decompressCompressed(final String sequence, final String structure) {
        final String[] decComp = RNAContract.decompress(RNAContract.compress(sequence, structure));
        assertEquals(sequence.toLowerCase(), decComp[0].toLowerCase());
        assertEquals(structure, decComp[1]);
    }

    @Test
    public void testEmptyAlt() {
        decompressCompressedAlt("", "");
        List<Boolean> empty = new ArrayList<>();
        assertArrayEquals(new String[]{"", ""}, RNAContract.decompressAlternative(empty));
    }

    @Test
    public void testSingleBaseAlt() {
        for (final String base : unpairedBases) {
            decompressCompressedAlt(base, ".");
        }
    }

    @Test
    public void testSinglePairAlt() {
        for (final String pair : pairs) {
            decompressCompressedAlt(pair, "()");
        }
    }

    @Test
    public void testShortExample1Alt() {
        decompressCompressedAlt("CGCGCGCGCGCAGGACCCCGGGGCUAUUAGCUCAGUUUGGUUAGAGCGCACCCCUGAUAAGGGUGAGGUCGCUGAUUCGAAUUCAGCAUAGCCCA", "()()()()()(.)(.)..()(((((((..((((..........)))).(((((.......))))).....(((((.......)))))))))))).");
    }

    @Test
    public void testShortExample2Alt() {
        decompressCompressedAlt("accgugagccauguuaggga", "..((((...)))..(.).).");
    }

    @Test
    public void testEmptyXML() {
        assertEquals("<f/>", RNAContract.createXML("", ""));
    }

    @Test
    public void testSingleBaseXML() {
        for (final String base : unpairedBases) {
            assertEquals("<" + base + "><e/></" + base + ">", RNAContract.createXML(base, "."));
        }
    }

    @Test
    public void testSinglePairXML() {
        for (int i = 0; i < pairs.length; i++) {
            assertEquals("<" + pairsXML[i] + "><e/><e/></" + pairsXML[i] + ">", RNAContract.createXML(pairs[i], "()"));
        }
    }

    @Test
    public void testShortExample2XML() {
        assertEquals("<ac><r><yqs><agc><e/></agc><gu><x><a><e/></a><g><e/></g></x></gu></yqs><a><e/></a></r></ac>", RNAContract.createXML("ACCGUGAGCCAUGUUAGGGA", "..((((...)))..(.).)."));
    }

    private static void decompressCompressedAlt(final String sequence, final String structure) {
        final String[] decComp = RNAContract.decompressAlternative(RNAContract.compressAlternative(sequence, structure));
        assertEquals(sequence.toLowerCase(), decComp[0].toLowerCase());
        assertEquals(structure, decComp[1]);
    }

}
