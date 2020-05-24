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
        for (int i = 0; i < 24; i++) {
            empty.add(false);
        }
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

    private static void decompressCompressed(String sequence, String structure) {
        final String[] decompressedCompressed = RNAContract.decompress(RNAContract.compress(sequence, structure));
        assertEquals(sequence.toLowerCase(), decompressedCompressed[0].toLowerCase());
        assertEquals(structure, decompressedCompressed[1]);
    }

}
