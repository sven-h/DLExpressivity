package de.uni_mannheim.informatik.dws.ontmatching.matchingexpressivity;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.semanticweb.owlapi.util.Construct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author shertlin
 */
public class ModelDlExpressivityChecker extends JenaDLExpressivityChecker{
    private static final Logger LOGGER = LoggerFactory.getLogger(ModelDlExpressivityChecker.class);
    
    protected Model m;
    
    public ModelDlExpressivityChecker(Model m) {
        this.m = m;
    }
    public ModelDlExpressivityChecker(String ontologyAsText, String language) {
        this.m = ModelFactory.createDefaultModel();
        this.m.read(new ByteArrayInputStream(ontologyAsText.getBytes(StandardCharsets.UTF_8)), null, language);
    }
    public ModelDlExpressivityChecker(String ontologyAsText) {
        this(ontologyAsText, "Turtle");
    }

    @Override
    protected void checkConstructs() {
        for(Map.Entry<Construct, List<String>> entry : this.constructToSparqlQuery.entrySet()){
            LOGGER.info("Check for construct {} ({})", entry.getKey(), entry.getKey().name());
            for(String s : entry.getValue()){
                String query = String.format("%s ASK WHERE{ %s }",sparqlPrefixes, s);
                try (QueryExecution qe = QueryExecutionFactory.create(query, m)){
                    if(qe.execAsk()){
                        addConstruct(entry.getKey());
                        LOGGER.info("Check for construct {} - True", entry.getKey());
                        break;
                    }
                }
            }
        }
        
    }
}
