package de.techfak.jfriemel.rnacontract;

import com.beust.jcommander.Parameter;

public class CommandLineArgs {

    @Parameter(names = {"--compress", "-c"}, description = "Compress the input file")
    boolean compress;
    @Parameter(names = {"--decompress", "-d"}, description = "Decompress the input file")
    boolean decompress;
    @Parameter(names = {"--input", "-i"}, description = "Input file path", arity = 1, required = true)
    String input;
    @Parameter(names = {"--output", "-o"}, description = "Output file path", arity = 1)
    String output;
    @Parameter(names = {"--statistics", "-s"}, description = "Print (de-)compression statistics")
    boolean statistics;
    @Parameter(names = {"--debug", "-db"}, description = "Print debugging information")
    boolean debug;
    @Parameter(names = {"--xml", "-x"}, description = "Export the contracted tree in XML format")
    boolean xml;

}
