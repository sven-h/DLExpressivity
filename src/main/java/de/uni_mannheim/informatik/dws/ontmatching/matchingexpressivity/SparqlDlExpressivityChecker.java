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
    protected String queryTemplate;
    
    public SparqlDlExpressivityChecker(String endoint, String graph) {
        this.endoint = trimIfNotNull(endoint);
        this.graph = trimIfNotNull(graph);
        if(this.graph == null || this.graph.length() == 0){
            this.queryTemplate = sparqlPrefixes + " ASK WHERE{ %s }";
        }else{
            this.queryTemplate = sparqlPrefixes + " ASK FROM <" + this.graph + "> WHERE{ %s }";
        }
    }
    public SparqlDlExpressivityChecker(String endoint) {
        this(endoint, null);
    }

    @Override
    protected void checkConstructs() {
        for(Map.Entry<Construct, List<String>> entry : this.constructToSparqlQuery.entrySet()){
            LOGGER.info("Check for construct {}", entry.getKey());
            for(String s : entry.getValue()){
                String query = String.format(this.queryTemplate, s);
                try (QueryExecution qe = QueryExecutionFactory.sparqlService(this.endoint, query)){
                    if(qe.execAsk()){
                        addConstruct(entry.getKey());
                        break;
                    }
                }
            }
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
