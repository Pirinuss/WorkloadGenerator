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
	private static Executor executor;
	private static WorkloadParser workloadParser = new WorkloadParser();
	
	public static void main(String[] args) {
		workload = parseCommands(args);
		if (workload != null) {
			//executor.executeWorkload(workload);
		}
	}
	
	private static Workload parseCommands(String[] args) {
		//Generierung der Eingabeoptionen
		CommandLineParser parser = new DefaultParser();
		Options options = new Options();
	    Option packageOption = Option.builder("h").longOpt("help").hasArg(false).build();
	    Option classOption = Option.builder("f").longOpt("file").hasArg(true).build();
	    options.addOption(packageOption);
	    options.addOption(classOption);
	    //Verarbeiten der Konsoleneingabe
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
	
	private static void printToConsole(String text) {
		if (text.equals("help")) {
			//TODO Hilfstext auf Konsole ausgeben
		}
		if (text.equals("noInput")) {
			//TODO Text für fehlende Parameter auf Konsole ausgeben
		}
	}
	
}