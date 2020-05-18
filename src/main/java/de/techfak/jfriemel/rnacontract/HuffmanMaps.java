package de.techfak.jfriemel.rnacontract;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class HuffmanMaps {

    public Map<String, List<Boolean>> unaryC;
    public Map<String, List<Boolean>> binaryC;

    public Map<List<Boolean>, Character> unaryD_a;
    public Map<List<Boolean>, Character> unaryD_c;
    public Map<List<Boolean>, Character> unaryD_g;
    public Map<List<Boolean>, Character> unaryD_u;
    public Map<List<Boolean>, Character> unaryD_sc;

    public Map<List<Boolean>, Character> binaryD_p;
    public Map<List<Boolean>, Character> binaryD_q;
    public Map<List<Boolean>, Character> binaryD_r;
    public Map<List<Boolean>, Character> binaryD_s;
    public Map<List<Boolean>, Character> binaryD_x;
    public Map<List<Boolean>, Character> binaryD_y;
    public Map<List<Boolean>, Character> binaryD_sc;

    public HuffmanMaps() {
        initializeUnaryC();
        initializeBinaryC();
        initializeUnaryD();
        initializeBinaryD();
    }

    public Map<String, List<Boolean>> getUnaryC() {
        return unaryC;
    }

    public Map<String, List<Boolean>> getBinaryC() {
        return binaryC;
    }

    public Map<List<Boolean>, Character> getUnaryD(final char previous) {
        switch (previous) {
            case 'a':
                return unaryD_a;
            case 'c':
                return unaryD_c;
            case 'g':
                return unaryD_g;
            case 'u':
                return unaryD_u;
        }
        return unaryD_sc;
    }

    public Map<List<Boolean>, Character> getBinaryD(final char previous) {
        switch (previous) {
            case 'p':
                return binaryD_p;
            case 'q':
                return binaryD_q;
            case 'r':
                return binaryD_r;
            case 's':
                return binaryD_s;
            case 'x':
                return binaryD_x;
            case 'y':
                return binaryD_y;
        }
        return binaryD_sc;
    }

    private void initializeUnaryC() {
        unaryC = new HashMap<>();

        unaryC.put(";a", Arrays.asList(true, false));
        unaryC.put(";c", Arrays.asList(true, true, false));
        unaryC.put(";g", Collections.singletonList(false));
        unaryC.put(";u", Arrays.asList(true, true, true));

        unaryC.put("a;", Collections.singletonList(false));
        unaryC.put("aa", Arrays.asList(true, false));
        unaryC.put("ac", Arrays.asList(true, true, true, false));
        unaryC.put("ag", Arrays.asList(true, true, true, true));
        unaryC.put("au", Arrays.asList(true, true, false));

        unaryC.put("c;", Arrays.asList(false, false));
        unaryC.put("ca", Arrays.asList(false, true));
        unaryC.put("cc", Arrays.asList(true, true, false));
        unaryC.put("cg", Arrays.asList(true, false));
        unaryC.put("cu", Arrays.asList(true, true, true));

        unaryC.put("g;", Arrays.asList(false, false));
        unaryC.put("ga", Arrays.asList(false, true));
        unaryC.put("gc", Arrays.asList(true, true, false));
        unaryC.put("gg", Arrays.asList(true, true, true));
        unaryC.put("gu", Arrays.asList(true, false));

        unaryC.put("u;", Arrays.asList(false, false));
        unaryC.put("ua", Arrays.asList(false, true));
        unaryC.put("uc", Arrays.asList(true, true, false));
        unaryC.put("ug", Arrays.asList(true, true, true));
        unaryC.put("uu", Arrays.asList(true, false));
    }

    private void initializeBinaryC() {
        binaryC = new HashMap<>();

        binaryC.put(";p", Arrays.asList(true, false, false));
        binaryC.put(";q", Arrays.asList(true, true, false));
        binaryC.put(";r", Arrays.asList(false, false));
        binaryC.put(";s", Arrays.asList(false, true));
        binaryC.put(";x", Arrays.asList(true, true, true));
        binaryC.put(";y", Arrays.asList(true, false, true));

        binaryC.put("p;", Arrays.asList(true, false, false));
        binaryC.put("pp", Arrays.asList(true, false, true));
        binaryC.put("pq", Arrays.asList(true, true, false));
        binaryC.put("pr", Arrays.asList(false, false));
        binaryC.put("ps", Arrays.asList(false, true));
        binaryC.put("px", Arrays.asList(true, true, true, true));
        binaryC.put("py", Arrays.asList(true, true, true, false));

        binaryC.put("q;", Arrays.asList(false, false));
        binaryC.put("qp", Arrays.asList(true, true, true, false));
        binaryC.put("qq", Arrays.asList(true, true, false));
        binaryC.put("qr", Arrays.asList(false, true));
        binaryC.put("qs", Arrays.asList(true, false));
        binaryC.put("qx", Arrays.asList(true, true, true, true, true));
        binaryC.put("qy", Arrays.asList(true, true, true, true, false));

        binaryC.put("r;", Arrays.asList(false, false));
        binaryC.put("rp", Arrays.asList(true, false, false));
        binaryC.put("rq", Arrays.asList(true, false, true));
        binaryC.put("rr", Arrays.asList(false, true));
        binaryC.put("rs", Arrays.asList(true, true, false));
        binaryC.put("rx", Arrays.asList(true, true, true, true));
        binaryC.put("ry", Arrays.asList(true, true, true, false));

        binaryC.put("s;", Arrays.asList(false, false));
        binaryC.put("sp", Arrays.asList(true, true, false, false));
        binaryC.put("sq", Arrays.asList(true, true, false, true));
        binaryC.put("sr", Arrays.asList(false, true));
        binaryC.put("ss", Arrays.asList(true, false));
        binaryC.put("sx", Arrays.asList(true, true, true, true));
        binaryC.put("sy", Arrays.asList(true, true, true, false));

        binaryC.put("x;", Arrays.asList(false, false));
        binaryC.put("xp", Arrays.asList(true, true, true, false));
        binaryC.put("xq", Arrays.asList(true, false, false));
        binaryC.put("xr", Arrays.asList(false, true));
        binaryC.put("xs", Arrays.asList(true, false, true));
        binaryC.put("xx", Arrays.asList(true, true, true, true));
        binaryC.put("xy", Arrays.asList(true, true, false));

        binaryC.put("y;", Arrays.asList(true, false, false));
        binaryC.put("yp", Arrays.asList(true, false, true));
        binaryC.put("yq", Arrays.asList(true, true, false));
        binaryC.put("yr", Arrays.asList(false, false));
        binaryC.put("ys", Arrays.asList(false, true));
        binaryC.put("yx", Arrays.asList(true, true, true, true));
        binaryC.put("yy", Arrays.asList(true, true, true, false));
    }

    private void initializeUnaryD() {
        unaryD_a = new HashMap<>();
        unaryD_a = unaryC.entrySet().stream().filter(map -> map.getKey().startsWith("a"))
                .collect(Collectors.toMap(Entry::getValue, e -> e.getKey().charAt(1)));

        unaryD_c = new HashMap<>();
        unaryD_c = unaryC.entrySet().stream().filter(map -> map.getKey().startsWith("c"))
                .collect(Collectors.toMap(Entry::getValue, e -> e.getKey().charAt(1)));

        unaryD_g = new HashMap<>();
        unaryD_g = unaryC.entrySet().stream().filter(map -> map.getKey().startsWith("g"))
                .collect(Collectors.toMap(Entry::getValue, e -> e.getKey().charAt(1)));

        unaryD_u = new HashMap<>();
        unaryD_u = unaryC.entrySet().stream().filter(map -> map.getKey().startsWith("u"))
                .collect(Collectors.toMap(Entry::getValue, e -> e.getKey().charAt(1)));

        unaryD_sc = new HashMap<>();
        unaryD_sc = unaryC.entrySet().stream().filter(map -> map.getKey().startsWith(";"))
                .collect(Collectors.toMap(Entry::getValue, e -> e.getKey().charAt(1)));
    }

    private void initializeBinaryD() {
        binaryD_p = new HashMap<>();
        binaryD_p = binaryC.entrySet().stream().filter(map -> map.getKey().startsWith("p"))
                .collect(Collectors.toMap(Entry::getValue, e -> e.getKey().charAt(1)));

        binaryD_q = new HashMap<>();
        binaryD_q = binaryC.entrySet().stream().filter(map -> map.getKey().startsWith("q"))
                .collect(Collectors.toMap(Entry::getValue, e -> e.getKey().charAt(1)));

        binaryD_r = new HashMap<>();
        binaryD_r = binaryC.entrySet().stream().filter(map -> map.getKey().startsWith("r"))
                .collect(Collectors.toMap(Entry::getValue, e -> e.getKey().charAt(1)));

        binaryD_s = new HashMap<>();
        binaryD_s = binaryC.entrySet().stream().filter(map -> map.getKey().startsWith("s"))
                .collect(Collectors.toMap(Entry::getValue, e -> e.getKey().charAt(1)));

        binaryD_x = new HashMap<>();
        binaryD_x = binaryC.entrySet().stream().filter(map -> map.getKey().startsWith("x"))
                .collect(Collectors.toMap(Entry::getValue, e -> e.getKey().charAt(1)));

        binaryD_y = new HashMap<>();
        binaryD_y = binaryC.entrySet().stream().filter(map -> map.getKey().startsWith("y"))
                .collect(Collectors.toMap(Entry::getValue, e -> e.getKey().charAt(1)));

        binaryD_sc = new HashMap<>();
        binaryD_sc = binaryC.entrySet().stream().filter(map -> map.getKey().startsWith(";"))
                .collect(Collectors.toMap(Entry::getValue, e -> e.getKey().charAt(1)));
    }
}