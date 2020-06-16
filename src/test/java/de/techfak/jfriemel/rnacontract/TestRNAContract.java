package de.techfak.jfriemel.rnacontract;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestRNAContract {

    @Test
    public void testEmpty() {
        decompressCompressed("", "");
        List<Boolean> empty = new ArrayList<>();
        assertArrayEquals(new String[]{"", ""}, RNAContract.decompress(empty));
    }

    @Test
    public void testSingleBase() {
        for (final String base : new String[]{"a", "c", "g", "u"}) {
            decompressCompressed(base, ".");
        }
    }

    @Test
    public void testSinglePair() {
        for (final String pair : new String[]{"au", "ua", "cg", "gc", "gu", "ug"}) {
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
        for (final String base : new String[]{"a", "c", "g", "u"}) {
            decompressCompressedAlt(base, ".");
        }
    }

    @Test
    public void testSinglePairAlt() {
        for (final String pair : new String[]{"au", "ua", "cg", "gc", "gu", "ug"}) {
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

    private static void decompressCompressedAlt(final String sequence, final String structure) {
        final String[] decComp = RNAContract.decompressAlternative(RNAContract.compressAlternative(sequence, structure));
        assertEquals(sequence.toLowerCase(), decComp[0].toLowerCase());
        assertEquals(structure, decComp[1]);
    }

}
