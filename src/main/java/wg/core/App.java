package wg.core;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import wg.WorkloadGeneratorException;
import wg.result.WorkloadResult;
import wg.workload.Workload;
import wg.workload.parser.WorkloadParser;

public class App {

	private static final Executor executor = new Executor();
	private static final WorkloadParser workloadParser = new WorkloadParser();
	private static Workload workload;
	private static WorkloadResult result;
	private static final Logger log = LoggerFactory.getLogger(App.class);
	private static String path;

	/**
	 * The main method. After calling the argument parser it calls the execution
	 * of the workload if a workload got parsed. Afterwards it calls the
	 * printing of the workloads execution results.
	 * 
	 * @param args
	 *            The arguments of the console command
	 * @throws WorkloadExecutionException 
	 */
	public static void main(String[] args) throws WorkloadGeneratorException {
		parseCommands(args);
		if (path != null) {
			workload = workloadParser.parseWorkload(path);
			try {
				result = executor.executeWorkload(workload);
				result.printResponses();
			} catch (WorkloadExecutionException e) {
				throw new WorkloadGeneratorException("Error while executing workload!", e);
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
		log.error("Start parsing commands");
		CommandLineParser parser = new DefaultParser();
		Options options = new Options();
		Option fileOption = Option.builder("f").longOpt("file").hasArg(true)
				.build();
		options.addOption(fileOption);
		try {
			CommandLine cmd = parser.parse(options, args);
			if (cmd.hasOption("f")) {
				path = cmd.getOptionValue("f");
			} else {
				System.out.println("Use -f [filepath] to insert a file!");
			}
		} catch (ParseException e) {
			log.error("Errror while parsing arguments");
			e.printStackTrace();
		}
	}

}