package de.uni_mannheim.informatik.dws.ontmatching.matchingexpressivity;

import java.util.List;
import java.util.Map;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.semanticweb.owlapi.util.Construct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SparqlDlExpressivityChecker extends JenaDLExpressivityChecker{
    private static final Logger LOGGER = LoggerFactory.getLogger(SparqlDlExpressivityChecker.class);
    
    protected String endoint;
    protected String graph;
    
    public SparqlDlExpressivityChecker(String endoint, String graph) {
        this.endoint = trimIfNotNull(endoint);
        this.graph = trimIfNotNull(graph);
    }
    public SparqlDlExpressivityChecker(String endoint) {
        this(endoint, null);
    }

    @Override
    protected void checkConstructs() {
        for(Map.Entry<Construct, List<String>> entry : this.constructToSparqlQuery.entrySet()){
            LOGGER.info("Check for construct {}", entry.getKey());
            for(String s : entry.getValue()){
                try (QueryExecution qe = QueryExecutionFactory.sparqlService(this.endoint, getQuery(s))){
                    if(qe.execAsk()){
                        addConstruct(entry.getKey());
                        break;
                    }
                }
            }
        }
    }
    
    protected String getQuery(String queryPart){
        if(graph == null|| graph.length() == 0){
            return String.format("%s ASK FROM <%s> WHERE{ %s }", sparqlPrefixes, graph, queryPart);
        }else{
            return String.format("%s ASK WHERE{ %s }", sparqlPrefixes, queryPart);
        }
    }

    public String getEndoint() {
        return endoint;
    }

    public String getGraph() {
        return graph;
    }
    
    private String trimIfNotNull(String text){
        if(text == null)
            return null;
        return text.trim();
    }
}
