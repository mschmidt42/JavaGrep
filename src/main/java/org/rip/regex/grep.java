package org.rip.regex;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

public class grep {

    private static Options grepOptions;

    private static CommandLine commandLine;

    private Pattern             grepPattern;

    private String              regex;

    static {
        grepOptions = createOptions();
    }

    private List< File >        filesToProcess;

    private ArrayList< String > excludes;

    public Pattern compilePattern( final String pat ) {

        int flags = 0;

        String pattern = new String( pat );

        if( ignoreCase ) {
            flags |= Pattern.CASE_INSENSITIVE;
        }

        /*
         * if( wordRegex && lineRegex ) {
         * printErrorMessage( "Conflicting options (-w|--word-regexp) && (-x|--line-regexp)" );
         * return null;
         * }
         */
        // we are overriding line-regex if both are supplied
        if( wordRegex ) {
            // poor mans way to do it
            pattern = "\\b" + pattern + "\\b";
        }
        else {
            if( lineRegex ) {

                // poor mans way to do it
                pattern = "^" + pattern + "$";
            }
        }

        return Pattern.compile( pattern, flags );

    }

    private static Options createOptions() {

        final Options opts = new Options();

        opts.addOption( "E", "extended-regexp", false, "PATTERN is an extended regular expression (ERE)" );
        opts.addOption( "F", "fixed-strings", false, "PATTERN is a set of newline-separated fixed strings" );
        opts.addOption( "G", "basic-regexp", false, "PATTERN is a basic regular expression (BRE)" );
        opts.addOption( "P", "perl-regexp", false, "PATTERN is a Perl regular expression" );
        opts.addOption( OptionBuilder.withLongOpt( "regexp" ).hasArg().withArgName( "PATTERN" )
            .withDescription( "use PATTERN for matching" ).create( 'e' ) );
        opts.addOption( OptionBuilder.withLongOpt( "file" ).hasArg().withArgName( "FILE" )
            .withDescription( "obtain PATTERN from FILE" ).create( 'f' ) );
        opts.addOption( "i", "ignore-case", false, "ignore case distinctions" );
        opts.addOption( "w", "word-regexp", false, "force PATTERN to match only whole words" );
        opts.addOption( "x", "line-regexp", false, "force PATTERN to match only whole lines" );
        opts.addOption( "z", "null-data", false, "a data line ends in 0 byte, not newline" );
        opts.addOption( "s", "no-messages", false, "suppress error messages" );
        opts.addOption( "v", "invert-match", false, "select non-matching lines" );
        opts.addOption( "V", "version", false, "print version information and exit" );
        opts.addOption( OptionBuilder.withLongOpt( "help" ).withDescription( "display this help and exit" ).create() );
        // opts.addOption( OptionBuilder.withLongOpt( "mmap" ).withDescription(
        // "ignored for backwards compatibility" ).create() );
        opts.addOption( OptionBuilder.withLongOpt( "max-count" ).withArgName( "NUM" ).hasArg()
            .withDescription( "stop after NUM matches" ).create( 'm' ) );
        opts.addOption( "b", "byte-offset", false, "print the byte offset with output lines" );
        opts.addOption( "n", "line-number", false, "print line number with output lines" );
        opts.addOption( OptionBuilder.withLongOpt( "line-buffered" ).withDescription( "flush output on every line" )
            .create() );
        opts.addOption( "H", "with-filename", false, "print the filename for each match" );
        opts.addOption( "h", "no-filename", false, "suppress the prefixing filename on output" );
        opts.addOption( OptionBuilder.withLongOpt( "label" ).hasArg().withArgName( "LABEL" )
            .withDescription( "print LABEL as filename for standard input" ).create() );
        opts.addOption( "o", "only-matching", false, "show only the part of a line matching PATTERN" );
        opts.addOption( "q", "quiet", false, "suppress all normal output" );
        opts.addOption( OptionBuilder.withLongOpt( "silent" ).withDescription( "suppress all normal output" ).create() );
        opts.addOption( OptionBuilder.withLongOpt( "binary-files" ).hasArg().withArgName( "TYPE" )
            .withDescription( "assume that binary files are TYPE" ).create() );
        // TYPE is `binary', `text', or `without-match'
        opts.addOption( "a", "text", false, "equivalent to --binary-files=text" );
        opts.addOption( "I", false, "equivalent to --binary-files=without-match" );
        opts.addOption( OptionBuilder.withLongOpt( "directories" ).hasArg().withArgName( "ACTION" )
            .withDescription( "how to handle directories" ).create( 'd' ) );
        // ACTION is `read', `recurse', or `skip'"
        opts.addOption( OptionBuilder.withLongOpt( "devices" ).hasArg().withArgName( "ACTION" )
            .withDescription( "how to handle devices, FIFOs and sockets" ).create( 'D' ) );
        // ACTION is `read' or `skip'
        opts.addOption( "R", false, "equivalent to --directories=recurse" );
        opts.addOption( "r", "recursive", false, "equivalent to --directories=recurse" );
        opts.addOption( OptionBuilder.withLongOpt( "include" ).hasArg().withArgName( "FILE_PATTERN" )
            .withDescription( "search only files that match FILE_PATTERN" ).create() );
        opts.addOption( OptionBuilder.withLongOpt( "exclude" ).hasArg().withArgName( "FILE_PATTERN" )
            .withDescription( "skip files and directories matching FILE_PATTERN" ).create() );
        opts.addOption( OptionBuilder.withLongOpt( "exclude-from" ).hasArg().withArgName( "FILE" )
            .withDescription( "skip files matching any file pattern from FILE" ).create() );
        opts.addOption( OptionBuilder.withLongOpt( "exclude-dir" ).hasArg().withArgName( "PATTERN" )
            .withDescription( "directories that match PATTERN will be skipped" ).create() );
        opts.addOption( "L", "files-without-match", false, "print only names of FILEs containing no match" );
        opts.addOption( "l", "files-with-matches", false, "print only names of FILEs containing matches" );
        opts.addOption( "c", "count", false, "print only a count of matching lines per FILE" );
        opts.addOption( "T", "initial-tab", false, "make tabs line up (if needed)" );
        opts.addOption( "Z", "null", false, "print 0 byte after FILE name" );
        opts.addOption( OptionBuilder.withLongOpt( "before-context" ).hasArg().withArgName( "NUM" )
            .withDescription( "print NUM lines of leading context" ).create( 'B' ) );
        opts.addOption( OptionBuilder.withLongOpt( "after-context" ).hasArg().withArgName( "NUM" )
            .withDescription( "print NUM lines of trailing context" ).create( 'A' ) );
        opts.addOption( OptionBuilder.withLongOpt( "contex" ).hasArg().withArgName( "NUM" )
            .withDescription( "print NUM lines of output context" ).create( 'C' ) );
        // * + "		  -NUM                      same as --context=NUM\n" +
        opts.addOption( OptionBuilder.withLongOpt( "color" ).hasOptionalArg().withArgName( "WHEN" )
            .withDescription( "use markers to highlight the matching strings" ).create() );
        opts.addOption( OptionBuilder.withLongOpt( "colour" ).hasOptionalArg().withArgName( "WHEN" )
            .withDescription( "use markers to highlight the matching strings" ).create() );
        // *
        // "		                            WHEN is `always', `never', or `auto'\n"
        opts.addOption( "U", "binary", false, "do not strip CR characters at EOL (MSDOS)" );
        opts.addOption( "u", "unix-byte-offsets", false, "report offsets as if CRs were not there (MSDOS)" );

        opts.addOption( OptionBuilder.withLongOpt( "debug" ).withDescription( "turn on debugging output" ).create() );
        opts.addOption( OptionBuilder.withLongOpt( "verbose" ).withDescription( "turn on verbose output" ).create() );
        opts.addOption( OptionBuilder.withLongOpt( "test" ).withDescription( "run in test mode" ).create() );
        opts.addOption( OptionBuilder.withLongOpt( "logging" ).withDescription( "use nice logging, Log4J/SLF4J" )
            .create() );

        return opts;
    }



    private void printMessage( final String msg ) {

        if( !quiet ) {
            System.out.println( msg );
        }
    }

    private void grepFiles() {

        // at this point the list was expanded into file names
        for( final File f : filesToProcess ) {

            grepFile( f );
        }

    }



    private void transferOptions( final CommandLine commandLine ) {

        long context = 0;

        if( commandLine.hasOption( 'C' ) || commandLine.hasOption( "context" ) ) {
            String s = commandLine.getOptionValue( 'C' );
            context = Long.parseLong( s );
        }


        if( commandLine.hasOption( 'A' ) || commandLine.hasOption( "after-context" ) ) {
            String s = commandLine.getOptionValue( 'A' );
            afterContext = Long.parseLong( s );
        }
        else {
            afterContext = context;
        }

        if( commandLine.hasOption( 'B' ) || commandLine.hasOption( "before-context" ) ) {
            String s = commandLine.getOptionValue( 'B' );
            beforeContext = Long.parseLong( s );
        }
        else {
            beforeContext = context;
        }

        if( commandLine.hasOption( 'i' ) || commandLine.hasOption( "ignore-case" ) ) {
            ignoreCase = true;
        }

        if( commandLine.hasOption( 'v' ) || commandLine.hasOption( "invert-match" ) ) {
            invertMatch = true;
        }

        maxCount = 0;
        if( commandLine.hasOption( 'm' ) || commandLine.hasOption( "max-count" ) ) {
            String s = commandLine.getOptionValue( 'm' );
            maxCount = Long.parseLong( s );
        }

        if( commandLine.hasOption( 'l' ) || commandLine.hasOption( "files-with-matches" ) ) {
            printFileNameOnly = true;
        }

        if( commandLine.hasOption( 'b' ) || commandLine.hasOption( "byte-offset" ) ) {
            printByteOffset = true;
        }

        if( commandLine.hasOption( 'q' ) || commandLine.hasOption( "--quiet" ) || commandLine.hasOption( "silent" ) ) {
            quiet = true;
        }

        if( commandLine.hasOption( 'c' ) || commandLine.hasOption( "count" ) ) {
            printCountOnly = true;
        }

        printFilesWithoutMatch = commandLine.hasOption( 'L' ) || commandLine.hasOption( "files-without-match" );

        wordRegex = commandLine.hasOption( 'w' ) || commandLine.hasOption( "word-regexp" );

        lineRegex = commandLine.hasOption( 'x' ) || commandLine.hasOption( "line-regexp" );

        if( commandLine.hasOption( "exclude-from" ) ) {
            readExcludeFrom( commandLine.getOptionValue( "exclude-from" ) );
        }

        // this could be specified multiple times
        if( commandLine.hasOption( 'e' ) || commandLine.hasOption( "regexp" ) ) {
            regex = commandLine.getOptionValue( commandLine.getOptionValue( 'e' ) );
        }

        if( commandLine.hasOption( 'f' ) || commandLine.hasOption( "file" ) ) {
            readRegexFromFile( commandLine.getOptionValue( 'f' ) );
        }

        noMessages = commandLine.hasOption( 's' ) || commandLine.hasOption( "no-messages" );

        if( commandLine.hasOption( 'H' ) || commandLine.hasOption( "with-filename" ) ) {
            printFileName = true;
        }

        if( commandLine.hasOption( 'h' ) || commandLine.hasOption( "no-filename" ) ) {
            printFileName = false;
        }

        printMatchOnly = commandLine.hasOption( 'o' ) || commandLine.hasOption( "only-matching" );

        printLineNumber = commandLine.hasOption( 'n' ) || commandLine.hasOption( "line-number" );

        recurseDirectories = false;

        if( commandLine.hasOption( 'd' ) || commandLine.hasOption( "directories" ) ) {

            String val = commandLine.getOptionValue( 'd' );
            if( StringUtils.endsWithIgnoreCase( val, "recurse" ) ) {
                recurseDirectories = true;
            }

        }

        if( commandLine.hasOption( 'R' )
            || commandLine.hasOption( 'r' )
            || commandLine.hasOption( "recursive" ) ) {
            recurseDirectories = true;
        }

        if( commandLine.hasOption( "include" ) ) {
            includeFilePattern = commandLine.getOptionValue( "include" );
        }

        if( commandLine.hasOption( "exclude" ) ) {
            excludeFilePattern = commandLine.getOptionValue( "exclude" );
        }

        if( commandLine.hasOption( "exclude-dir" ) ) {
            excludeDirPattern = commandLine.getOptionValue( "exclude-dir" );
        }

    }

    private boolean invertMatch = false;
    private boolean ignoreCase = false;
    private long    maxCount;

    private boolean printFileNameOnly = false;

    private boolean printByteOffset   = false;

    private boolean quiet             = false;

    private boolean printCountOnly    = false;

    private boolean printFilesWithoutMatch = false;

    private boolean wordRegex              = false;

    private boolean lineRegex              = false;

    private boolean noMessages             = false;

    private boolean printFileName          = true;

    private boolean printMatchOnly         = false;
    private boolean printLineNumber=false;

    private boolean recurseDirectories     = false;

    private String  includeFilePattern     = null;

    private String  excludeFilePattern     = null;

    private String  excludeDirPattern      = null;

    public static void main( final String[] args ) {

        grep theGrep = new grep();

        if( ArrayUtils.isEmpty( args ) ) {
            usage();
            return;
        }

        // parse the command line
        try {
            final CommandLineParser parser = new PosixParser();
            commandLine = parser.parse( grepOptions, args );
        }
        catch( final ParseException pe ) {
            System.out.print( pe.getMessage() );
            usage();
            return;
        }

        theGrep.transferOptions( commandLine );

        // if version or help was given then print and quit
        if( commandLine.hasOption( "help" ) ) {
            usage();
            return;
        }

        if( commandLine.hasOption( "version" ) || commandLine.hasOption( 'V' ) ) {
            version();
            return;
        }

        //
        if( commandLine.hasOption( 'e' ) || commandLine.hasOption( "regexp" ) ) {
            if( ArrayUtils.isEmpty( commandLine.getOptionValues( 'e' ) ) ) {
                usage();
                return;
            }
        }

        if( commandLine.hasOption( 'f' ) || commandLine.hasOption( "file" ) ) {
            if( ArrayUtils.isEmpty( commandLine.getOptionValues( 'f' ) ) ) {
                usage();
                return;
            }
        }

        if( commandLine.hasOption( "exclude-from" ) ) {
            String s = commandLine.getOptionValue( "exclude-from" );
            if( StringUtils.isEmpty( s ) ) {
                usage();
                return;
            }
        }

        // get the unprocessed items
        final List argList = commandLine.getArgList();

        // we must have some args besides options
        if( CollectionUtils.isEmpty( argList ) ) {
            usage();
            return;
        }

        // just to get things started
        // we are assuming: [OPTION]... PATTERN [FILE]...
        // options are pulled out by the command line processor
        int fileOffset = 0;
        if( StringUtils.isEmpty( theGrep.regex ) ) {
            theGrep.regex = (String)argList.get( 0 );
            fileOffset++;
        }

        // make sure its a good regex
        try {
            theGrep.grepPattern = theGrep.compilePattern( theGrep.regex );
        }
        catch( final PatternSyntaxException pse ) {
            theGrep.printErrorMessage( "invalid regex syntax\nregex:" + theGrep.regex + "\n" + pse.getMessage() );
            return;
        }

        if( theGrep.grepPattern == null ) {
            return;
        }

        // just to get things going
        theGrep.processFileArgs( argList.subList( fileOffset, argList.size() ) );

        // ok lets start
        theGrep.grepFiles();

    }

    private void readExcludeFrom( final String val ) {


        try( BufferedReader br = new BufferedReader( new FileReader( new File( val ) ) ) ) {
            excludes = new ArrayList< String >();

            for( String line = br.readLine(); line != null; line = br.readLine() ) {
                excludes.add( line );
            }

        }
        catch( IOException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }





    public void printErrorMessage( final String msg ) {

        if( !noMessages ) {
            System.out.println(msg);
        }
    }







    private void printMatch( final File file, final String line, final long lineNumber,
        final long count, final long byteOffset, final List< String > beforeContextLines,
        final List< String > afterContextLines ) {

        if( quiet || printFilesWithoutMatch || printCountOnly ) {
            return;
        }

        /*
         * if( ( beforeContext() > 0 ) || ( afterContext() > 0 ) ) {
         * System.out.println( "--" );
         * }
         */

        if( beforeContext > 0 ) {
            for( String bc : beforeContextLines ) {
                String s = new String( bc );
                if( filesToProcess.size() > 1 ) {
                    try {
                        s = file.getCanonicalPath() + "-" + s;
                    }
                    catch( IOException ioe ) {}
                }
                System.out.println( s );
            }
        }

        String msg = "";
        if( printFileNamesOnly() ) {
            try {
                System.out.println( String.format( "%1$s", file.getCanonicalPath() ) );
            }
            catch( IOException ioe ) {
                // TODO:
            }
            return;
        }

        if( printFileName && ( filesToProcess.size() > 1 ) ) {
            try {
                msg += file.getCanonicalPath() + ":";
            }
            catch( IOException ioe ) {}
        }

        if( printLineNumber ) {
            msg += lineNumber + ":";
        }

        if( printByteOffset ) {
            msg += byteOffset + ":";
        }

        msg = msg.length() > 1 ? msg + "    " + line : line;

        System.out.println( msg );

        if( afterContext > 0 ) {
            for( String ac : afterContextLines ) {
                String s = new String( ac );
                if( filesToProcess.size() > 1 ) {
                    try {
                        s = file.getCanonicalPath() + "-" + s;
                    }
                    catch( IOException ioe ) {}
                }
                System.out.println( s );
            }
        }

        if( ( beforeContext > 0 ) || ( afterContext > 0 ) ) {
            System.out.println( "--" );
        }
    }



    // TODO: note that file names can contain wild cards too
    // so this method should also expand directories as well
    private void processFileArgs( final List list ) {

        filesToProcess = new ArrayList< File >();

        for( final Object o : list ) {
            final String name = (String)o;

            final File f = new File( name );

            if( f.exists() ) {
                if( f.isFile() ) {
                    if( includeFile( f ) ) {
                        if( !excludeFile( f ) ) {
                            filesToProcess.add( f );
                        }
                    }
                }
                else if( f.isDirectory() ) {
                    if( recurseDirectories ) {
                        filesToProcess.addAll( recurseDir( f ) );
                    }
                }
            }
        }

    }

    private List< File > recurseDir( final File dir ) {

        List< File > files = new ArrayList< File >();

        for( File f : dir.listFiles() ) {

            if( f.isFile() ) {
                if( includeFile( f ) ) {
                    if( !excludeFile( f ) ) {
                        files.add( f );
                    }
                }
            }
            else if( f.isDirectory() ) {
                if( !excludeDir( f ) ) {
                    files.addAll( recurseDir( f ) );
                }
            }

        }

        return files;

    }





    private void readRegexFromFile( final String fname ) {
        String val = null;

        File file = new File( fname );
        if( file.exists() && file.isFile() ) {

            try( BufferedReader br = new BufferedReader( new FileReader( file ) ) ) {

                String line = br.readLine();
                if( StringUtils.isNotBlank( line ) ) {
                    val = line ;
                }

            }
            catch( IOException e ) {
                printErrorMessage( "Cannot open file:" + fname );
            }
        }

        regex = val;
    }

    private void reset() {

        commandLine = null;
        grepPattern = null;
        regex = null;
        filesToProcess = null;
        excludes = null;


    }

    public static void usage() {

        final String msg =
            "Usage: grep [OPTION]... PATTERN [FILE]...\n" + "Search for PATTERN in each FILE or standard input.\n"
                + "PATTERN is, by default, a basic regular expression (BRE).\n"
                + "Example: grep -i 'hello world' menu.h main.c\n"
                + "Regexp selection and interpretation:\n"
                + "        -E, --extended-regexp     PATTERN is an extended regular expression (ERE)\n"     // no
                + "        -F, --fixed-strings       PATTERN is a set of newline-separated fixed strings\n" // no
                + "		  -G, --basic-regexp        PATTERN is a basic regular expression (BRE)\n"          // no
                + "		  -P, --perl-regexp         PATTERN is a Perl regular expression\n"                 // no
                + "		  -e, --regexp=PATTERN      use PATTERN for matching\n"                // done
                + "		  -f, --file=FILE           obtain PATTERN from FILE\n"                // done
                + "		  -i, --ignore-case         ignore case distinctions\n"                // done
                + "		  -w, --word-regexp         force PATTERN to match only whole words\n"     // done
                + "		  -x, --line-regexp         force PATTERN to match only whole lines\n"     // done
                + "		  -z, --null-data           a data line ends in 0 byte, not newline\n"     // yes?
                + "Miscellaneous:\n"
                + "		  -s, --no-messages         suppress error messages\n"                     // done
                + "		  -v, --invert-match        select non-matching lines\n"                   // done
                + "		  -V, --version             print version information and exit\n"          // done
                + "		      --help                display this help and exit\n"                  // done
                // +
                + "		      --mmap                ignored for backwards compatibility\n" // no
                + "Output control:\n"
                + "		  -m, --max-count=NUM       stop after NUM matches\n"                      // done - max-total
                + "		  -b, --byte-offset         print the byte offset with output lines\n"     // done
                + "		  -n, --line-number         print line number with output lines\n"         // done
                + "		      --line-buffered       flush output on every line\n"                  // yes
                + "		  -H, --with-filename       print the filename for each match\n"           // done
                + "		  -h, --no-filename         suppress the prefixing filename on output\n"   // done
                + "		      --label=LABEL         print LABEL as filename for standard input\n"  // yes?
                + "		  -o, --only-matching       show only the part of a line matching PATTERN\n"   // done
                + "		  -q, --quiet, --silent     suppress all normal output\n"                      // done
                + "		      --binary-files=TYPE   assume that binary files are TYPE;\n"              // yes?
                + "		                            TYPE is `binary', `text', or `without-match'\n"
                + "		  -a, --text                equivalent to --binary-files=text\n"               // yes?
                + "		  -I                        equivalent to --binary-files=without-match\n"      // yes?
                + "		  -d, --directories=ACTION  how to handle directories;\n"                      // done
                + "		                            ACTION is `read', `recurse', or `skip'\n"
                + "		  -D, --devices=ACTION      how to handle devices, FIFOs and sockets;\n"       // no
                + "		                            ACTION is `read' or `skip'\n"
                + "		  -R, -r, --recursive       equivalent to --directories=recurse\n"             // done
                + "		      --include=FILE_PATTERN  search only files that match FILE_PATTERN\n"     // done
                + "		      --exclude=FILE_PATTERN  skip files and directories matching FILE_PATTERN\n"  // done
                + "		      --exclude-from=FILE   skip files matching any file pattern from FILE\n"      // done
                + "		      --exclude-dir=PATTERN  directories that match PATTERN will be skipped.\n"    // done
                + "		  -L, --files-without-match  print only names of FILEs containing no match\n"      // done
                + "		  -l, --files-with-matches  print only names of FILEs containing matches\n"        // done
                + "		  -c, --count               print only a count of matching lines per FILE\n"       // done
                + "		  -T, --initial-tab         make tabs line up (if needed)\n"                       // no
                + "		  -Z, --null                print 0 byte after FILE name\n"                        // no - use --sep=CHAR
                + "Context control:\n"
                + "		  -B, --before-context=NUM  print NUM lines of leading context\n"                  // done
                + "		  -A, --after-context=NUM   print NUM lines of trailing context\n"                 // done
                + "		  -C, --context=NUM         print NUM lines of output context\n"                   // done
                + "		  -NUM                      same as --context=NUM\n"                               // no
                + "		      --color[=WHEN],\n"                                                           // no
                + "		      --colour[=WHEN]       use markers to highlight the matching strings;\n"      // no
                + "		                            WHEN is `always', `never', or `auto'\n"
                + "		  -U, --binary              do not strip CR characters at EOL (MSDOS)\n"           // no?
                + "		  -u, --unix-byte-offsets   report offsets as if CRs were not there (MSDOS)\n"     // no?
                + "\n"
                + "		`egrep' means `grep -E'.  `fgrep' means `grep -F'.\n"
                + "		Direct invocation as either `egrep' or `fgrep' is deprecated.\n"
                + "		With no FILE, or when FILE is -, read standard input.  If less than two FILEs\n"
                + "		are given, assume -h.  Exit status is 0 if any line was selected, 1 otherwise;\n"
                + "		if any error occurs and -q was not given, the exit status is 2.\n"
                + "\n"
                + "		Report bugs to: bug-grep@gnu.org\n"
                + "		GNU Grep home page: <http://www.gnu.org/software/grep/>\n"
                + "		General help using GNU software: <http://www.gnu.org/gethelp/>\n";
        System.out.print( msg );

    }

    private static void version() {

        System.out.println("Java Grep 0.1");

    }

    public boolean includeFile( final File f ) {

        if( StringUtils.isNotEmpty( includeFilePattern ) ) {

            String nm = FilenameUtils.getName( f.getName() );
            return FilenameUtils.wildcardMatch( nm, includeFilePattern );

        }

        return true;
    }

    public boolean excludeFile( final File f ) {

        if( StringUtils.isNotBlank( excludeFilePattern ) ) {

            String nm = FilenameUtils.getName( f.getName() );
            return FilenameUtils.wildcardMatch( nm, excludeFilePattern );
        }

        if( CollectionUtils.isNotEmpty( excludes ) ) {
            for( String pat : excludes ) {
                String nm = FilenameUtils.getName( f.getName() );
                boolean ex = FilenameUtils.wildcardMatch( nm, pat );
                if( ex ) {
                    return true;
                }
            }
        }

        return false ;

    }

    private boolean excludeDir( final File f ) {

        if( StringUtils.isNotBlank( excludeDirPattern ) ) {
            if( FilenameUtils.wildcardMatch( FilenameUtils.getName( f.getName() ), excludeDirPattern ) ) {
                return true;
            }
        }

        return false;
    }

    private boolean printFileNamesOnly() {

        return printFilesWithoutMatch || printFileNameOnly;
    }




    private long beforeContext = 0;

    private long afterContext = 0;



    private void grepFile( final File file ) {


        long lineNumber = 0;
        long count = 0;
        long byteOffset = 0;
        List< String > beforeContextLines = new LinkedList< String >();
        List< String > afterContextLines = new LinkedList< String >();

        // keep it simple for now
        try( BufferedReader bfr = new BufferedReader( new FileReader( file ), 16000 ) ) {

            for( String line = bfr.readLine(); line != null; line = bfr.readLine() ) {

                lineNumber++;
                Matcher m = grepPattern.matcher( line );
                boolean found = m.find();

                if( ( found && !invertMatch ) ) {
                    count++;
                    if( afterContext > 0 ) {
                        afterContextLines.clear();
                        bfr.mark( 4000 );
                        for( int i = 0; i < afterContext; i++ ) {
                            String s = bfr.readLine();
                            if( s != null ) {
                                afterContextLines.add( s );
                            }
                            else {
                                break;
                            }
                        }
                        bfr.reset();
                    }
                    String match = line;
                    if( printMatchOnly ) {
                        match = m.group();
                    }
                    printMatch( file, match, lineNumber, count, ( byteOffset + m.start() ), beforeContextLines,
                        afterContextLines );
                    if( printFileNameOnly ) {
                        return;
                    }
                    //
                }
                else if( !found && invertMatch ) {
                    count++;
                    printMatch( file, line, lineNumber, count, ( byteOffset + m.start() ), beforeContextLines,
                        afterContextLines );
                    // TODO: this has a slightly different meaning
                    if( printFileNameOnly ) {
                        return;
                    }
                }

                if( ( maxCount != 0 ) && ( count >= maxCount ) ) {
                    break;
                }

                byteOffset += line.getBytes().length;
                byteOffset += 1; // TODO: end of line char - what about DOS/Win32?

                beforeContextLines.add( line );
                if( beforeContextLines.size() > beforeContext ) {
                    beforeContextLines.remove( 0 );
                }
            }
        }
        catch( IOException ioe ) {
            System.out.println( ioe.getMessage() );
        }

        if( count == 0 ) {
            // no matches in file
            if( printFilesWithoutMatch ) {
                printMessage( file.getName() );
            }
        }

        if( printCountOnly ) {
            printMessage( ( file.getName() + ":" + count ) );
        }

    }
}
