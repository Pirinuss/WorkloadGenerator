package wg.core;

import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import wg.util.LogFormatter;
import wg.util.WorkloadValidator;
import wg.workload.Workload;

public class Controller {

	private static final Executor executor = new Executor();
	private static final WorkloadParser workloadParser = new WorkloadParser();
	private static final WorkloadValidator workloadValidator = new WorkloadValidator();
	private static Workload workload;
	private static Result result;
	private static Logger log;
	private static String path;

	/**
	 * The main method. After calling the argument parser it calls the execution of
	 * the workload if a workload got parsed. Afterwards it calls the printing of
	 * the workloads execution results.
	 * 
	 * @param args
	 *            The arguments of the console command
	 */
	public static void main(String[] args) {
		createLogger();
		parseCommands(args);
		if (path != null) {
			workload = workloadParser.parseWorkload(path);
			if (workloadValidator.validateWorkload(workload)) {
				result = executor.executeWorkload(workload);
				result.printResponses();
			}
		}
		System.exit(0);
	}

	/**
	 * Generates input options, parses the arguments to one of this options and
	 * executes it.
	 * 
	 * @param args
	 *            The arguments of the console command
	 */
	private static void parseCommands(String[] args) {
		log.fine("Start parsing commands");
		CommandLineParser parser = new DefaultParser();
		Options options = new Options();
		Option fileOption = Option.builder("f").longOpt("file").hasArg(true).build();
		options.addOption(fileOption);
		try {
			CommandLine cmd = parser.parse(options, args);
			if (cmd.hasOption("f")) {
				path = cmd.getOptionValue("f");
			} else {
				System.out.println("Use -f [filepath] to insert a file!");
			}
		} catch (ParseException e) {
			log.severe("Errror while parsing arguments");
			e.printStackTrace();
		}
	}

	private static void createLogger() {
		log = Logger.getLogger("logfile.txt");
		log.setLevel(Level.ALL);
		log.setUseParentHandlers(false);

		FileHandler logFileHandler = null;
		try {
			logFileHandler = new FileHandler("logfile.txt");
			logFileHandler.setLevel(Level.ALL);
		} catch (Exception e) {
			e.printStackTrace();
		}
		ConsoleHandler consoleHandler = new ConsoleHandler();
		consoleHandler.setLevel(Level.ALL);

		Formatter formatter = new LogFormatter();
		logFileHandler.setFormatter(formatter);
		consoleHandler.setFormatter(formatter);

		log.addHandler(logFileHandler);
		log.addHandler(consoleHandler);
	}

}