#!/usr/bin/env groovy

/*
 * PROTOTYPE FOR BPIPE CONFIG SCRIPT
 */

// CFR: http://jameswilliams.be/blog/entry/240
//The JANSI project allows you to use Java to print colored text and use console effects like blinking and bolding. 
// As opposed to having to bundle a jar I used @Grab to automagically download the dependency.
@Grapes(
    @Grab(group='org.fusesource.jansi', module='jansi', version='1.8')
)
import org.fusesource.jansi.AnsiConsole
import static org.fusesource.jansi.Ansi.*
import static org.fusesource.jansi.Ansi.Color.*

class BpipeConfig
{
    final static String DEFAULT_HELP = """
        bpipe-config [OPTIONS] [pipeline_name] ...

    """.stripIndent().trim()

    // will be updated by gradle
    final static String version = "alpha"
    final static String builddate = System.currentTimeMillis()

    // GLOBAL VARS
    public static String working_dir = System.getProperty("user.dir")
    public static Map env = System.getenv()
    public static String BPIPE_GFU_HOME = env['BPIPE_GFU_HOME']

    static void main( String [] args)
    {
        def bpipe_config = new BpipeConfig()

        // ANSI COLORS
        AnsiConsole.systemInstall()

        def cli = new CliBuilder(usage: DEFAULT_HELP, posix: true, width: 80, stopAtNonOption: false)

        cli.h( longOpt: 'help'      , 'usage information'             , required: false          )
        cli.v( longOpt: 'verbose'   , 'verbose mode'                  , required: false          )
        cli.p( longOpt: 'pipelines' , 'list available pipelines'      , required: false          )
        cli.m( longOpt: 'email'     , 'user email'                    , required: false, args: 1 )

        OptionAccessor opt = cli.parse(args)

        if (!opt) { System.exit(1) }

        String versionInfo = "\nbpipe-config Version $version built on ${new Date(Long.parseLong(builddate))}\n"

        // print usage if -h, --help, or no argument is given
        if(opt.h || !opt.arguments().isEmpty()) {
            println versionInfo
            cli.usage()
            println "\n"
            System.exit(1)
        }

        // List pipelines
        if (opt.p)
        {
            println versionInfo
            listPipelines()
            print "\n"
            System.exit(0)
        }

        // validate email
        if (opt.m && !validateEmail(opt.m))
        {
            println "\n$opt.m doesn't appear to be a valid email\n"
            cli.usage()
            println "\n"
            System.exit(1)
        }

        // TESTS
        println "Verbose is $opt.v"
        if (opt.m) println "Mail is $opt.m"
        
        println "Working directory: $working_dir"
        println "BPIPE_GFU_HOME = $BPIPE_GFU_HOME"
        println "ARGS: $args"
        
        println "Number of args: ${opt.arguments().size()} : ${opt.arguments()}"
    }

    private static void listPipelines()
    {
        println "Available pipelines:\n"
        new File("$BPIPE_GFU_HOME/pipelines").eachDirRecurse() 
        {
            dir -> dir.eachFileMatch(~/.*.groovy/)
            {
                file -> println getPipeInfo(file)
            }
        }
    }

    private static String getPipeInfo(File file)
    {
        def name = file.getName().replaceFirst(~/\.[^\.]+$/, '')
        
        // TODO take description
        file.eachLine {

        }
        
        name = ansi().a(Attribute.INTENSITY_BOLD).a("\t$name").reset().a(" : description")
        return name
    }

    private static boolean validateEmail(String email)
    {
        def emailPattern = /[_A-Za-z0-9-]+(\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\.[A-Za-z0-9]+)*(\.[A-Za-z]{2,})/
        return email ==~ emailPattern
    }
}
