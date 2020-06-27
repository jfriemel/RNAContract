# RNAContract
RNAContract is contraction-based compression algorithm for RNA secondary structures. This program was written as part of an undergraduate thesis in computer science at Bielefeld University.

## Prerequisites
In order to compile the code, you need Java version 9 or higher. RNAContract also uses the Java framework [JCommander](https://github.com/cbeust/jcommander) for command-line argument parsing. For Maven users, the corresponding dependency is already added to the _pom.xml_.

## Flags
| Flag              | Short       | Required | Description |
|:------------------|:------------|:--------:|:------------|
| `--input [path]`  | `-i [path]` | **X**    | Specify the input file.
| `--output [path]` | `-o [path]` |          | Specify the output file.
| `--compress`      | `-c`        |          | Compress the input file.
| `--decompress`    | `-d`        |          | Decompress the input file.
| `--xml`           | `-x`        |          | Create a contracted tree and output it in XML format.
| `--statistics`    | `-s`        |          | Print (de-)compression statistics: File sizes, compression rate, processing time, number of nodes in the contracted tree.
| `--debug`         | `-db`       |          | Print the result before saving it to the output file.
| `--alt`           | `-a`        |          | Use an alternative (de-)compression method (not documented) that implicitly encodes the tree within the sequence (file ending `.rnac2`).

## Input / Output
For compression, the program requires a `.txt` file, which contains the RNA sequence and its secondary structure separated by a line break. For decompression, it requires a `.rnac` file and saves the decompressed RNA data to a text file containing the sequence and secondary structure at the specified output path. If no output is specified, the result is saved at the same location as the input file with the appropriate file extension (`.rnac` for `-c`, `.txt` for `-d`, `.xml` for `-x`).

## Example
Say you want to compress the file tRNA.txt, save it as tRNA_c.rnac, and you want to know how small the compressed file ends up being. Then you can run the following command:
```console
user@example:~$ java -jar RNAContract.jar -c -i tRNA.txt -o tRNA_c.rnac -s
Compression successful. Compressed file at tRNA_c.rnac
Input file size:  146.7 KiB
Output file size: 24.0 KiB
Compression rate: 16.39%
Processing time:  0.167s

Total number of nodes:  15688
Number of unary nodes:  7247
Number of binary nodes: 4220
```
