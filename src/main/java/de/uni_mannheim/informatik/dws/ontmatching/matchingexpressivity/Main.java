package de.uni_mannheim.informatik.dws.ontmatching.matchingexpressivity;

import java.util.Collection;
import java.util.stream.Collectors;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.semanticweb.owlapi.util.Languages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
    
    public static void main(String[] args){
        Options options = new Options();

        Option endpoint = new Option("e", "enpoint", true, "Endpoint as URL");
        endpoint.setRequired(true);
        options.addOption(endpoint);

        Option graph = new Option("g", "graph", true, "Graph as URL");
        graph.setRequired(false);
        options.addOption(graph);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("dl-expressivity", options);
            System.exit(1);
        }
        
        SparqlDlExpressivityChecker checker = new SparqlDlExpressivityChecker(cmd.getOptionValue("enpoint"), cmd.getOptionValue("graph"));
        
        Collection<Languages> dlLangs = checker.expressibleInLanguages();
        String exp = dlLangs.stream().map(Enum::name).collect(Collectors.joining(", "));
        LOGGER.info("expressivity: " + exp + "\tDL Name:" + checker.getDescriptionLogicName());        
    }
}