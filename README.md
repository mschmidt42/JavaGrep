# grep 

... implemented in Java

## Compile:

```
mvn package
```

## Run:

```
java -jar target/grep-0.1.jar
```

## Usage

```
Usage: grep [OPTION]... PATTERN [FILE]...
Search for PATTERN in each FILE or standard input.
PATTERN is, by default, a basic regular expression (BRE).
Example: grep -i 'hello world' menu.h main.c
Regexp selection and interpretation:
        -E, --extended-regexp     PATTERN is an extended regular expression (ERE)
        -F, --fixed-strings       PATTERN is a set of newline-separated fixed strings
       -G, --basic-regexp        PATTERN is a basic regular expression (BRE)
       -P, --perl-regexp         PATTERN is a Perl regular expression
       -e, --regexp=PATTERN      use PATTERN for matching
       -f, --file=FILE           obtain PATTERN from FILE
       -i, --ignore-case         ignore case distinctions
       -w, --word-regexp         force PATTERN to match only whole words
       -x, --line-regexp         force PATTERN to match only whole lines
       -z, --null-data           a data line ends in 0 byte, not newline
Miscellaneous:
       -s, --no-messages         suppress error messages
       -v, --invert-match        select non-matching lines
       -V, --version             print version information and exit
           --help                display this help and exit
           --mmap                ignored for backwards compatibility
Output control:
       -m, --max-count=NUM       stop after NUM matches
       -b, --byte-offset         print the byte offset with output lines
       -n, --line-number         print line number with output lines
           --line-buffered       flush output on every line
       -H, --with-filename       print the filename for each match
       -h, --no-filename         suppress the prefixing filename on output
           --label=LABEL         print LABEL as filename for standard input
       -o, --only-matching       show only the part of a line matching PATTERN
       -q, --quiet, --silent     suppress all normal output
           --binary-files=TYPE   assume that binary files are TYPE;
                                 TYPE is `binary', `text', or `without-match'
       -a, --text                equivalent to --binary-files=text
       -I                        equivalent to --binary-files=without-match
       -d, --directories=ACTION  how to handle directories;
                                 ACTION is `read', `recurse', or `skip'
       -D, --devices=ACTION      how to handle devices, FIFOs and sockets;
                                 ACTION is `read' or `skip'
       -R, -r, --recursive       equivalent to --directories=recurse
           --include=FILE_PATTERN  search only files that match FILE_PATTERN
           --exclude=FILE_PATTERN  skip files and directories matching FILE_PATTERN
           --exclude-from=FILE   skip files matching any file pattern from FILE
           --exclude-dir=PATTERN  directories that match PATTERN will be skipped.
       -L, --files-without-match  print only names of FILEs containing no match
       -l, --files-with-matches  print only names of FILEs containing matches
       -c, --count               print only a count of matching lines per FILE
       -T, --initial-tab         make tabs line up (if needed)
       -Z, --null                print 0 byte after FILE name
Context control:
       -B, --before-context=NUM  print NUM lines of leading context
       -A, --after-context=NUM   print NUM lines of trailing context
       -C, --context=NUM         print NUM lines of output context
       -NUM                      same as --context=NUM
           --color[=WHEN],
           --colour[=WHEN]       use markers to highlight the matching strings;
                                 WHEN is `always', `never', or `auto'
       -U, --binary              do not strip CR characters at EOL (MSDOS)
       -u, --unix-byte-offsets   report offsets as if CRs were not there (MSDOS)
```
