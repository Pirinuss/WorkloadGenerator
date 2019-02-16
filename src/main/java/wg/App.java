package wg;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import wg.Execution.Executor;
import wg.Execution.WorkloadExecutionException;
import wg.Execution.WorkloadResult;
import wg.workload.Workload;
import wg.workload.parser.WorkloadParser;
import wg.workload.parser.WorkloadParserException;

public class App {

	private static final Executor executor = new Executor();
	private static final WorkloadParser workloadParser = new WorkloadParser();
	private static Workload workload;
	private static WorkloadResult result;
	private static final Logger log = LoggerFactory.getLogger(App.class);
	private static String path;
	private static boolean printInDetail = false;

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
		log.info("Start Workload Generator");
		parseCommands(args);
		if (path != null) {
			try {
				workload = workloadParser.parseWorkload(path);
				result = executor.executeWorkload(workload);
			} catch (WorkloadParserException e) {
				throw new WorkloadGeneratorException(
						"Error while parsing workload", e);
			} catch (WorkloadExecutionException e) {
				throw new WorkloadGeneratorException(
						"Error while executing workload!", e);
			}
			result.printResponses(printInDetail);
		}
		System.exit(0);
		log.info("Stop Workload Generator");
	}

	/**
	 * Generates input options, parses the arguments to one of this options and
	 * executes it.
	 * 
	 * @param args
	 *            The arguments of the console command
	 */
	private static void parseCommands(String[] args) {
		CommandLineParser parser = new DefaultParser();
		Options options = new Options();
		Option fileOption = Option.builder("f").longOpt("file").hasArg(true)
				.build();
		options.addOption(fileOption);
		Option printOption = Option.builder("v").build();
		options.addOption(fileOption);
		options.addOption(printOption);
		try {
			CommandLine cmd = parser.parse(options, args);
			if (cmd.hasOption("f")) {
				path = cmd.getOptionValue("f");
				if (cmd.hasOption("v")) {
					printInDetail = true;
				}
			} else {
				System.out.println("Use -f [filepath] to insert a file!");
			}
		} catch (ParseException e) {
			log.error("Errror while parsing arguments");
			e.printStackTrace();
		}
	}

}