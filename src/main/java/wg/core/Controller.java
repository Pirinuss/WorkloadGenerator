package wg.core;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import wg.workload.Workload;

public class Controller {

	private static Workload workload;
	private static Executor executor = new Executor();
	private static WorkloadParser workloadParser = new WorkloadParser();
	private static Result result;
	
	public static void main(String[] args) {
		workload = parseCommands(args);
		if (workload != null) {
			result = executor.executeWorkload(workload);
			result.printResponses();
		}
		System.exit(0);
	}
	
	/**
	 * Generates input options, parses the arguments to one of this options
	 * and executes it.
	 * @param args The arguments of the console command
	 * @return workload The resulting workload of the JSON file parsing
	 */
	private static Workload parseCommands(String[] args) {
		CommandLineParser parser = new DefaultParser();
		Options options = new Options();
	    Option packageOption = Option.builder("h").longOpt("help").hasArg(false).build();
	    Option classOption = Option.builder("f").longOpt("file").hasArg(true).build();
	    options.addOption(packageOption);
	    options.addOption(classOption);
	    try {
	    	CommandLine cmd = parser.parse(options, args);
	    	if (cmd.hasOption("f")) {
	    		workload = workloadParser.parseWorkload(cmd.getOptionValue("f"));
	    		return workload;
	    	} else if (cmd.hasOption("h")) {
	    		printToConsole("help");
	    	} else {
	    		printToConsole("noInput");
	    	}
	    } catch (ParseException e) {
	    	System.err.println("Errror while parsing arguments");
	    	e.printStackTrace();
	    }
		return null;
	}
	
	/**
	 * Prints a text to the console for a given option
	 * @param text The option
	 */
	private static void printToConsole(String text) {
		if (text.equals("help")) {
			//TODO Hilfstext auf Konsole ausgeben
		}
		if (text.equals("noInput")) {
			System.out.println("Use -f [filepath] to insert a file!");
		}
	}
	
}