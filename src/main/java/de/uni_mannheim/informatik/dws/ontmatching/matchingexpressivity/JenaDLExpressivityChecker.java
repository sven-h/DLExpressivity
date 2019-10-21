package de.uni_mannheim.informatik.dws.ontmatching.matchingexpressivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.semanticweb.owlapi.util.Construct;
import org.semanticweb.owlapi.util.Languages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class JenaDLExpressivityChecker {
    //https://www.w3.org/TR/owl2-mapping-to-rdf/
    private static final Logger LOGGER = LoggerFactory.getLogger(JenaDLExpressivityChecker.class);
    
    protected static String owl(String fragement){
        return String.format("<http://www.w3.org/2002/07/owl#%s>", fragement);        
    }
    protected static String rdfs(String fragement){
        return String.format("<http://www.w3.org/2000/01/rdf-schema#%s>", fragement);
    }
    
    protected static String queryForRdfsType(String type){
        return String.format("?x %s %s.", rdfs("type"), rdfs(type));
    }
    
    protected static String queryForOwlType(String type){
        return String.format("?x %s %s.", rdfs("type"), owl(type));
    }
    
    protected static String queryForOwlPredicate(String predicate){
        return String.format("?x %s ?y.", owl(predicate));
    }
    protected static String queryForRdfsPredicate(String predicate){
        return String.format("?x %s ?y.", rdfs(predicate));
    }
    
    protected String sparqlPrefixes = "PREFIX owl:<http://www.w3.org/2002/07/owl#> PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>";
       
    protected Map<Construct, List<String>> constructToSparqlQuery = createConstructToSparqlQuery();    
    protected Map<Construct, List<String>> createConstructToSparqlQuery(){        
        Map<Construct, List<String>> m = new HashMap<>();
        addQuery(m, Construct.ROLE_INVERSE,
                "?x owl:inverseOf ?y.", //OWLObjectInverseOf 
                "?x a owl:SymmetricProperty.", //OWLSymmetricObjectPropertyAxiom
                "?x a owl:InverseFunctionalProperty." //OWLInverseFunctionalObjectPropertyAxiom
        );
        addQuery(m, Construct.D,
            "?x rdf:type owl:DatatypeProperty.",//OWLDataProperty
            "?x owl:datatypeComplementOf ?y.",//OWLDataComplementOf
            "?x owl:oneOf ?y. ?y rdf:first ?z. FILTER(isLiteral(?z))", //OWLDataOneOf
            "?x owl:onDatatype ?y. ?x owl:withRestrictions ?z.", //OWLDatatypeRestriction 
            //"?s ?p ?o. FILTER(isLiteral(?o))"//OWLLiteral
            //OWLFacetRestriction
            "?x owl:hasValue ?y. FILTER(isLiteral(?y))"//OWLDataHasValue
            //"?x a owl:AllDisjointProperties. ?x owl:members ?y. ?y rdf:first ?z. ?z a owl:DatatypeProperty."//OWLDisjointDataPropertiesAxiom -> "?x owl:propertyDisjointWith ?y. ?x a owl:DatatypeProperty. ?y a owl:DatatypeProperty." NOT NEED BECAUSE DatatypeProperty is already there
        );
        addQuery(m, Construct.CONCEPT_INTERSECTION,
            "?x owl:intersectionOf ?y."//OWLObjectIntersectionOf
        );
        
        addQuery(m, Construct.CONCEPT_UNION,
            "?x owl:unionOf ?y.", //OWLObjectUnionOf
            "?x owl:oneOf ?y. ?y rdf:first ?z. FILTER(isURI(?z))", //OWLObjectOneOf
            "?x owl:differentFrom ?y.", "?x a owl:AllDifferent. ?x owl:members ?y.", //OWLDifferentIndividualsAxiom
            "?x owl:disjointUnionOf ?y." //OWLDisjointUnionAxiom
        );
        
        addQuery(m, Construct.ATOMIC_NEGATION
            //OWLObjectComplementOf not used because OWLObjectComplementOf is always atomic.
        );
        
        addQuery(m, Construct.CONCEPT_COMPLEX_NEGATION,
            "?x owl:complementOf ?y.", //OWLObjectComplementOf (is always atomic)
            "?x owl:disjointWith ?y.", "?x a owl:AllDisjointClasses. ?x owl:members ?y.", //OWLDisjointClassesAxiom
            "?x owl:differentFrom ?y.", "?x a owl:AllDifferent. ?x owl:members ?y.", //OWLDifferentIndividualsAxiom
            "?x owl:disjointUnionOf ?y." //OWLDisjointUnionAxiom
        );
        
        addQuery(m, Construct.FULL_EXISTENTIAL,
            "?x owl:someValuesFrom ?y. FILTER(?y != owl:Thing).", //OWLObjectSomeValuesFrom
            "?x owl:hasValue ?y. FILTER(isURI(?y))" //OWLObjectHasValue
        );
        
        addQuery(m, Construct.LIMITED_EXISTENTIAL,
            "?x owl:someValuesFrom owl:Thing." //OWLObjectSomeValuesFrom
        );
        
        addQuery(m, Construct.UNIVERSAL_RESTRICTION,
            "?x owl:allValuesFrom ?y." //OWLObjectAllValuesFrom
        );
        
        addQuery(m, Construct.NOMINALS,
            "?x owl:hasValue ?y. FILTER(isURI(?y))", //OWLObjectHasValue
            "?x owl:oneOf ?y. ?y rdf:first ?z. FILTER(isURI(?z))", //OWLObjectOneOf
            "?x owl:differentFrom ?y.", "?x a owl:AllDifferent.", //OWLDifferentIndividualsAxiom
            "?x owl:sameAs ?y." //OWLSameIndividualAxiom
        );
        
        addQuery(m, Construct.Q,
            "?x owl:minQualifiedCardinality ?y; owl:onClass ?z. FILTER(?z != owl:Thing).", //OWLObjectMinCardinality
            "?x owl:minQualifiedCardinality ?y; owl:onDataRange ?z. FILTER(?z != rdfs:Literal).", //OWLDataMinCardinality
            
            "?x owl:qualifiedCardinality ?y; owl:onClass ?z. FILTER(?z != owl:Thing).", //OWLObjectExactCardinality
            "?x owl:qualifiedCardinality ?y; owl:onDataRange ?z. FILTER(?z != rdfs:Literal).", //OWLDataExactCardinality
            
            "?x owl:maxQualifiedCardinality ?y; owl:onClass ?z. FILTER(?z != owl:Thing).", //OWLObjectMaxCardinality
            "?x owl:maxQualifiedCardinality ?y; owl:onDataRange ?z. FILTER(?z != rdfs:Literal)." //OWLDataMaxCardinality
        );
        addQuery(m, Construct.N,
            "?x owl:minCardinality ?y.", //OWLObjectMinCardinality  and Data
            "?x owl:minQualifiedCardinality ?y; owl:onClass owl:Thing.", //OWLObjectMinCardinality
            "?x owl:minQualifiedCardinality ?y; owl:onDataRange rdfs:Literal.", //OWLDataMinCardinality
            
            "?x owl:cardinality ?y.", //OWLObjectExactCardinality and Data
            "?x owl:qualifiedCardinality ?y; owl:onClass owl:Thing.", //OWLObjectExactCardinality
            "?x owl:qualifiedCardinality ?y; owl:onDataRange rdfs:Literal.", //OWLDataExactCardinality
            
            "?x owl:maxCardinality ?y.", //OWLObjectExactCardinality and Data
            "?x owl:maxQualifiedCardinality ?y; owl:onClass owl:Thing.", //OWLObjectMaxCardinality
            "?x owl:maxQualifiedCardinality ?y; owl:onDataRange rdfs:Literal." //OWLDataMaxCardinality
        );
        
        addQuery(m, Construct.ROLE_COMPLEX,
            "?x owl:hasSelf ?y.", //OWLObjectHasSelf
            "?x a owl:AsymmetricProperty.", //OWLAsymmetricObjectPropertyAxiom
            "?x owl:propertyDisjointWith ?y. ?x a owl:ObjectProperty. ?y a owl:ObjectProperty.", "?x a owl:AllDisjointProperties . ?x owl:members ?y. ?y rdf:first ?z. ?z a owl:ObjectProperty.",//OWLDisjointObjectPropertiesAxiom
            "?x a owl:IrreflexiveProperty."
        );
        
        addQuery(m, Construct.ROLE_REFLEXIVITY_CHAINS,
            "?x a owl:ReflexiveProperty.", //OWLReflexiveObjectPropertyAxiom
            "?x owl:propertyChainAxiom ?y." //OWLSubPropertyChainOfAxiom  -> "?y rdf:first ?z. ?z a owl:ObjectProperty." not neccessary
        );
        
        addQuery(m, Construct.ROLE_DOMAIN_RANGE,
            "?x rdfs:domain ?y.", //OWLDataPropertyDomainAxiom and object
            "?x rdfs:range ?y." //OWLDataPropertyRangeAxiom and object
        );
        
        addQuery(m, Construct.ROLE_HIERARCHY,
            "?x owl:equivalentProperty ?y.", //OWLEquivalentObjectPropertiesAxiom
            "?x rdfs:subPropertyOf ?y." //OWLSubObjectPropertyOfAxiom
        );
        
        addQuery(m, Construct.F,
            "?x a owl:FunctionalProperty.", //OWLFunctionalObjectPropertyAxiom
            "?x a owl:InverseFunctionalProperty." //OWLInverseFunctionalObjectPropertyAxiom
        );
        
        addQuery(m, Construct.ROLE_TRANSITIVE,
            "?x a owl:TransitiveProperty." //OWLTransitiveObjectPropertyAxiom
        );
        
        return m;
    }   
    
    
    private static void addQuery(Map<Construct, List<String>> map, Construct c, String... queries){
        if(map.containsKey(c))
            throw new IllegalArgumentException("Key is already in map");
        map.put(c, Arrays.asList(queries));        
    }
    
    protected abstract void checkConstructs();
    
    private Set<Construct> constructs;
    
    
    //methods from DLExpressivityChecker
    
    /**
     * @return Collection of Languages that include all constructs used in the ontology. Each
     *         language returned allows for all constructs found and has no sublanguages that also
     *         allow for all constructs found. E.g., if FL is returned, FL0 and FLMNUS cannot be
     *         returned.
     */
    public Collection<Languages> expressibleInLanguages() {
        return Arrays.stream(Languages.values()).filter(this::minimal).collect(Collectors.toList());
    }

    /**
     * @param l language to check
     * @return true if l is minimal, i.e., all sublanguages of l cannot represent all the constructs
     *         found, but l can.
     */
    public boolean minimal(Languages l) {
        if (!l.components().containsAll(getOrderedConstructs())) {
            // not minimal because it does not cover the constructs found
            return false;
        }
        return Arrays.stream(Languages.values()).filter(p -> p.isSubLanguageOf(l))
            .noneMatch(this::minimal);
    }

    /**
     * @param l language to check
     * @return true if l is sufficient to express the ontology, i.e., if all constructs found in the
     *         ontology are included in the language
     */
    public boolean isWithin(Languages l) {
        return l.components().containsAll(getOrderedConstructs());
    }

    /**
     * @param c construct to check
     * @return true if the matched constructs contain c.
     */
    public boolean has(Construct c) {
        return getOrderedConstructs().contains(c);
    }
        
    /**
     * @return ordered constructs
     */
    public List<Construct> getConstructs() {
        return new ArrayList<>(getOrderedConstructs());
    }

    /**
     * @return DL name
     */
    public String getDescriptionLogicName() {
        return getOrderedConstructs().stream().map(Object::toString).collect(Collectors.joining());
    }

    protected Set<Construct> getOrderedConstructs() {
        if (constructs == null) {
            constructs = new TreeSet<>();
            checkConstructs();
        }
        Construct.trim(constructs);
        return constructs;
    }

    protected void addConstruct(Construct c) {
        if (constructs == null) {
            constructs = new TreeSet<>();
        }
        // Rr+I = R + I
        if (c == Construct.ROLE_INVERSE && constructs.contains(Construct.ROLE_REFLEXIVITY_CHAINS)) {
            constructs.add(c);
            constructs.remove(Construct.ROLE_REFLEXIVITY_CHAINS);
            constructs.add(Construct.ROLE_COMPLEX);
        } else if (c == Construct.ROLE_REFLEXIVITY_CHAINS && constructs.contains(Construct.ROLE_INVERSE)) {
            constructs.add(Construct.ROLE_COMPLEX);
        } else {
            constructs.add(c);
        }
    }
}
