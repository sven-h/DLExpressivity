package de.uni_mannheim.informatik.dws.ontmatching.matchingexpressivity;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.util.DLExpressivityChecker;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.semanticweb.owlapi.formats.TurtleDocumentFormat;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLDatatypeRestriction;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLInverseObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.OWLSubDataPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLTransitiveObjectPropertyAxiom;
import org.semanticweb.owlapi.util.Construct;
import static org.semanticweb.owlapi.util.Construct.CONCEPT_COMPLEX_NEGATION;
import static org.semanticweb.owlapi.util.Construct.CONCEPT_UNION;
import static org.semanticweb.owlapi.util.Construct.D;
import static org.semanticweb.owlapi.util.Construct.F;
import static org.semanticweb.owlapi.util.Construct.FULL_EXISTENTIAL;
import static org.semanticweb.owlapi.util.Construct.LIMITED_EXISTENTIAL;
import static org.semanticweb.owlapi.util.Construct.NOMINALS;
import static org.semanticweb.owlapi.util.Construct.Q;
import static org.semanticweb.owlapi.util.Construct.ROLE_COMPLEX;
import static org.semanticweb.owlapi.util.Construct.ROLE_DOMAIN_RANGE;
import static org.semanticweb.owlapi.util.Construct.ROLE_HIERARCHY;
import static org.semanticweb.owlapi.util.Construct.ROLE_INVERSE;
import static org.semanticweb.owlapi.util.Construct.ROLE_REFLEXIVITY_CHAINS;
import static org.semanticweb.owlapi.util.Construct.UNIVERSAL_RESTRICTION;
import org.semanticweb.owlapi.vocab.OWL2Datatype;
import org.semanticweb.owlapi.vocab.OWLFacet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class SPARQLExpressivityCheckerTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SPARQLExpressivityCheckerTest.class);
    private OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
    private OWLDataFactory factory = manager.getOWLDataFactory();
    private OWLClass owlClass = factory.getOWLClass("http://example.com/clazz");
    private OWLClass owlClass2 = factory.getOWLClass("http://example.com/clazz2");
    private OWLIndividual individual = factory.getOWLNamedIndividual("http://exampe.com/individual");
    private OWLIndividual individual2 = factory.getOWLNamedIndividual("http://exampe.com/individual2");
    private OWLObjectProperty owlObjProp = factory.getOWLObjectProperty("http://exampe.com/ObjectProperty");
    private OWLObjectProperty owlObjProp2 = factory.getOWLObjectProperty("http://exampe.com/ObjectProperty2");
    private OWLDataProperty owlDataProp = factory.getOWLDataProperty("http://exampe.com/DataProperty");
    private OWLDataProperty owlDataProp2 = factory.getOWLDataProperty("http://exampe.com/DataProperty2");
    private OWLLiteral owlliteral = factory.getOWLLiteral("literal");
    private OWLLiteral owlliteral2 = factory.getOWLLiteral("literal2");
    private OWLDatatype integer = factory.getOWLDatatype(OWL2Datatype.XSD_INTEGER.getIRI());    
    private OWLDatatypeRestriction datatypeRestriction = factory.getOWLDatatypeRestriction(integer, OWLFacet.MIN_INCLUSIVE, factory.getOWLLiteral(18));
    
    

    private static String prefixDefinition = "@prefix : <http://example.com/>. @prefix owl: <http://www.w3.org/2002/07/owl#>. @prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>. @prefix xsd: <http://www.w3.org/2001/XMLSchema#>.";

    @Test
    void testAllQueries() {
        ModelDlExpressivityChecker c = new ModelDlExpressivityChecker("");
        for (List<String> queries : c.constructToSparqlQuery.values()) {
            for (String query : queries) {
                String fullQuery = String.format("%s ASK WHERE{ %s }",c.sparqlPrefixes, query);
                QueryFactory.create(fullQuery);
            }
        }
    }

    @Test
    void testCheckConstructsAgainstEmptyModel() {
        ModelDlExpressivityChecker c = new ModelDlExpressivityChecker("");
        assertTrue(c.getConstructs().isEmpty(), "Constructs is not empty");
    }
    
    
    
    @Test
    void checkOWLObjectInverseOf() {
        assertConstruct(":x owl:inverseOf :y", Construct.ROLE_INVERSE);
    }
    
    @Test
    void checkOWLDataProperty() {
        assertConstructJena(":x a owl:DatatypeProperty.", Construct.D); //TODO: check OWLAPI
    }
    
    @Test
    void checkOWLDataComplementOf() {
        assertConstructJena(":x owl:datatypeComplementOf :y", Construct.D);
    }
    
    @Test
    void checkOWLDataOneOf() {
        assertConstructJena(":x owl:oneOf (\"Test\"). :x a rdfs:Datatype.", Construct.D);
    }
    
    //@Test
    void checkOWLDatatypeRestriction() {
        //assertConstructJena(":x a rdfs:Datatype. :x owl:onDatatype :y.", Construct.D);
    }
    
    //@Test
    void checkOWLLiteral() {
        //assertConstruct(":x a rdfs:Literal", Construct.D);
    }
    
    //@Test
    void checkOWLFacetRestriction() {
        //TODO:
    }
    
    @Test
    void checkOWLObjectIntersectionOf() {
        String objectIntersectionOf = getOWLAxiomsAsTurtle(factory.getOWLSubClassOfAxiom(owlClass, factory.getOWLObjectIntersectionOf(owlClass, owlClass2)));
        //System.out.println(objectIntersectionOf);
        assertConstruct(objectIntersectionOf, Construct.CONCEPT_INTERSECTION);
        assertConstruct(":x owl:intersectionOf (:y :z)", Construct.CONCEPT_INTERSECTION);
    }
    
    @Test
    void checkOWLObjectUnionOf() {
        String objectUnionOf = getOWLAxiomsAsTurtle(factory.getOWLSubClassOfAxiom(owlClass, factory.getOWLObjectUnionOf(owlClass, owlClass2)));
        //System.out.println(objectUnionOf);
        assertConstruct(objectUnionOf, Construct.CONCEPT_UNION);
        assertConstruct(":x owl:unionOf (:y :z)", Construct.CONCEPT_UNION);
    }
    
    @Test
    void checkOWLObjectComplementOf() {
        String objectComplementOf = getOWLAxiomsAsTurtle(factory.getOWLSubClassOfAxiom(owlClass2, factory.getOWLObjectComplementOf(owlClass)));
        //System.out.println(objectComplementOf);
        assertConstruct(objectComplementOf, Construct.CONCEPT_COMPLEX_NEGATION);
        assertConstruct(":x owl:complementOf :y.", Construct.CONCEPT_COMPLEX_NEGATION);
    }

    @Test
    void checkOWLObjectSomeValuesFrom() {
        //generate();
        assertConstruct(":x rdfs:subClassOf [a owl:Restriction; owl:onProperty :y; owl:someValuesFrom :z].", Construct.FULL_EXISTENTIAL);
        assertConstruct(":x rdfs:subClassOf [a owl:Restriction; owl:onProperty :y; owl:someValuesFrom owl:Thing].", Construct.LIMITED_EXISTENTIAL);

        assertConstructJena(":x a owl:Restriction; owl:onProperty :y; owl:someValuesFrom :z.", Construct.FULL_EXISTENTIAL);
        assertConstructJena(":x a owl:Restriction; owl:onProperty :y; owl:someValuesFrom owl:Thing.", Construct.LIMITED_EXISTENTIAL);
    }

    @Test
    void checkOWLObjectAllValuesFrom() {
        assertConstruct(":x rdfs:subClassOf [a owl:Restriction; owl:onProperty :y; owl:allValuesFrom :z].", Construct.UNIVERSAL_RESTRICTION);
    }

    @Test
    void checkOWLObjectHasValue() {
        assertConstruct(":x rdfs:subClassOf [a owl:Restriction; owl:onProperty :y; owl:hasValue :z].", Construct.FULL_EXISTENTIAL, Construct.NOMINALS);
    }

    @Test
    void checkOWLObjectMinCardinality() {
        //String minCardinalityOnt = getOWLAxiomsAsTurtle(factory.getOWLSubClassOfAxiom(owlClass, factory.getOWLObjectMinCardinality(5, owlObjProp)));
        //assertConstructOwlAPI(minCardinalityOnt, Construct.Q);
        assertConstruct(":y a owl:ObjectProperty. :x rdfs:subClassOf [a owl:Restriction; owl:onProperty :y; owl:minCardinality \"5\"^^xsd:nonNegativeInteger].", Construct.N);
        //assertConstruct(":y a owl:ObjectProperty. :x rdfs:subClassOf [a owl:Restriction; owl:onProperty :y; owl:minCardinality \"5\"^^xsd:nonNegativeInteger; owl:onClass owl:Thing].", Construct.N); // not in standard owl
        //assertConstruct(":y a owl:ObjectProperty. :x rdfs:subClassOf [a owl:Restriction; owl:onProperty :y; owl:minCardinality \"5\"^^xsd:nonNegativeInteger; owl:onClass :z].", Construct.Q);// not in standard owl
        assertConstruct(":y a owl:ObjectProperty. :x rdfs:subClassOf [a owl:Restriction; owl:onProperty :y; owl:minQualifiedCardinality \"5\"^^xsd:nonNegativeInteger; owl:onClass owl:Thing].", Construct.N);
        assertConstruct(":y a owl:ObjectProperty. :x rdfs:subClassOf [a owl:Restriction; owl:onProperty :y; owl:minQualifiedCardinality \"5\"^^xsd:nonNegativeInteger; owl:onClass :z].", Construct.Q);
    }
    
    @Test
    void checkOWLObjectExactCardinality() {        
        assertConstruct(":y a owl:ObjectProperty. :x rdfs:subClassOf [a owl:Restriction; owl:onProperty :y; owl:cardinality \"5\"^^xsd:nonNegativeInteger].", Construct.N);
        assertConstruct(":y a owl:ObjectProperty. :x rdfs:subClassOf [a owl:Restriction; owl:onProperty :y; owl:qualifiedCardinality \"5\"^^xsd:nonNegativeInteger; owl:onClass owl:Thing].", Construct.N);
        assertConstruct(":y a owl:ObjectProperty. :x rdfs:subClassOf [a owl:Restriction; owl:onProperty :y; owl:qualifiedCardinality \"5\"^^xsd:nonNegativeInteger; owl:onClass :z].", Construct.Q);
    }
    
    @Test
    void checkOWLObjectMaxCardinality() {        
        assertConstruct(":y a owl:ObjectProperty. :x rdfs:subClassOf [a owl:Restriction; owl:onProperty :y; owl:maxCardinality \"5\"^^xsd:nonNegativeInteger].", Construct.N);
        assertConstruct(":y a owl:ObjectProperty. :x rdfs:subClassOf [a owl:Restriction; owl:onProperty :y; owl:maxQualifiedCardinality \"5\"^^xsd:nonNegativeInteger; owl:onClass owl:Thing].", Construct.N);
        assertConstruct(":y a owl:ObjectProperty. :x rdfs:subClassOf [a owl:Restriction; owl:onProperty :y; owl:maxQualifiedCardinality \"5\"^^xsd:nonNegativeInteger; owl:onClass :z].", Construct.Q);
    }

    
    @Test
    void checkOWLObjectHasSelf() {        
        assertConstruct(":x rdfs:subClassOf [a owl:Restriction; owl:onProperty :y; owl:hasSelf \"true\"^^xsd:boolean].", Construct.ROLE_COMPLEX);
    }
    
    @Test
    void checkOWLObjectOneOf() {        
        assertConstruct(":x owl:oneOf (:y :z).", Construct.CONCEPT_UNION, Construct.NOMINALS);
    }
    
    @Test
    void checkOWLDataSomeValuesFrom() {  
        //String someValuesFromOnt = getOWLAxiomsAsTurtle(factory.getOWLSubClassOfAxiom(owlClass,factory.getOWLDataSomeValuesFrom(this.owlDataProp, factory.getOWLDatatype("test"))));
        //String someValuesFromOnt = getOWLAxiomsAsTurtle(factory.getOWLSubClassOfAxiom(owlClass,factory.getOWLDataSomeValuesFrom(this.owlDataProp, datatypeRestriction)));
        //System.out.println(someValuesFromOnt);
        assertConstruct(":y a owl:DatatypeProperty. :z a rdfs:Datatype. :x rdfs:subClassOf [a owl:Restriction; owl:onProperty :y; owl:someValuesFrom :z].", Construct.FULL_EXISTENTIAL, Construct.D);
    }
    
    @Test
    void checkOWLDataHasValue() {  
        assertConstruct(":x rdfs:subClassOf [a owl:Restriction; owl:onProperty :y; owl:hasValue \"5\"^^xsd:nonNegativeInteger].", Construct.D);
    }
    
    @Test
    void checkOWLDataMinCardinality() {
        //String minCardinalityOnt = getOWLAxiomsAsTurtle(factory.getOWLSubClassOfAxiom(owlClass, factory.getOWLDataMinCardinality(5, owlDataProp)));
        //System.out.println(minCardinalityOnt);
        assertConstruct(":y a owl:DatatypeProperty. :x rdfs:subClassOf [a owl:Restriction; owl:onProperty :y; owl:minCardinality \"5\"^^xsd:nonNegativeInteger].", Construct.N, Construct.D);
        //assertConstruct(":y a owl:DatatypeProperty. :x rdfs:subClassOf [a owl:Restriction; owl:onProperty :y; owl:minCardinality \"5\"^^xsd:nonNegativeInteger; owl:onDataRange xsd:integer].", Construct.Q, Construct.D); // not in standard owl
        //assertConstruct(":y a owl:DatatypeProperty. :x rdfs:subClassOf [a owl:Restriction; owl:onProperty :y; owl:minCardinality \"5\"^^xsd:nonNegativeInteger; owl:onDataRange rdfs:Literal].", Construct.N, Construct.D);// not in standard owl
        assertConstruct(":y a owl:DatatypeProperty. :x rdfs:subClassOf [a owl:Restriction; owl:onProperty :y; owl:minQualifiedCardinality \"5\"^^xsd:nonNegativeInteger; owl:onDataRange xsd:integer].", Construct.Q, Construct.D);
        assertConstruct(":y a owl:DatatypeProperty. :x rdfs:subClassOf [a owl:Restriction; owl:onProperty :y; owl:minQualifiedCardinality \"5\"^^xsd:nonNegativeInteger; owl:onDataRange rdfs:Literal].", Construct.N, Construct.D);
    }
    
    @Test
    void checkOWLDataExactCardinality() {        
        assertConstruct(":y a owl:DatatypeProperty. :x rdfs:subClassOf [a owl:Restriction; owl:onProperty :y; owl:cardinality \"5\"^^xsd:nonNegativeInteger].", Construct.N, Construct.D);
        assertConstruct(":y a owl:DatatypeProperty. :x rdfs:subClassOf [a owl:Restriction; owl:onProperty :y; owl:qualifiedCardinality \"5\"^^xsd:nonNegativeInteger; owl:onDataRange xsd:integer].", Construct.Q, Construct.D);
        assertConstruct(":y a owl:DatatypeProperty. :x rdfs:subClassOf [a owl:Restriction; owl:onProperty :y; owl:qualifiedCardinality \"5\"^^xsd:nonNegativeInteger; owl:onDataRange rdfs:Literal].", Construct.N, Construct.D);
    }
    
    @Test
    void checkOWLDataMaxCardinality() {        
        assertConstruct(":y a owl:DatatypeProperty. :x rdfs:subClassOf [a owl:Restriction; owl:onProperty :y; owl:maxCardinality \"5\"^^xsd:nonNegativeInteger].", Construct.N, Construct.D);
        assertConstruct(":y a owl:DatatypeProperty. :x rdfs:subClassOf [a owl:Restriction; owl:onProperty :y; owl:maxQualifiedCardinality \"5\"^^xsd:nonNegativeInteger; owl:onDataRange xsd:integer].", Construct.Q, Construct.D);
        assertConstruct(":y a owl:DatatypeProperty. :x rdfs:subClassOf [a owl:Restriction; owl:onProperty :y; owl:maxQualifiedCardinality \"5\"^^xsd:nonNegativeInteger; owl:onDataRange rdfs:Literal].", Construct.N, Construct.D);
    }
    
    @Test
    void checkOWLAsymmetricObjectPropertyAxiom() {        
        assertConstruct(":y a owl:AsymmetricProperty.", Construct.ROLE_COMPLEX);
    }
    
    @Test
    void checkOWLReflexiveObjectPropertyAxiom() {        
        assertConstruct(":y a owl:ReflexiveProperty.", Construct.ROLE_REFLEXIVITY_CHAINS);
    }
    
    @Test
    void checkOWLDisjointClassesAxiom() {        
        assertConstruct(":x owl:disjointWith :y.", Construct.CONCEPT_COMPLEX_NEGATION);
        assertConstruct(":x a owl:AllDisjointClasses. :x owl:members (:y :z).", Construct.CONCEPT_COMPLEX_NEGATION);
    }
    
    @Test
    void checkOWLDataPropertyDomainAxiom() {   
        //String propertydomain = getOWLAxiomsAsTurtle(factory.getOWLObjectPropertyDomainAxiom(owlObjProp, owlClass));
        //System.out.println(propertydomain);
        assertConstruct(":x a owl:DatatypeProperty. :x rdfs:domain :y.", Construct.ROLE_DOMAIN_RANGE, Construct.D);
    }
    
    @Test
    void checkOWLObjectPropertyDomainAxiom() {
        assertConstruct(":x a owl:ObjectProperty. :x rdfs:domain :y.", Construct.ROLE_DOMAIN_RANGE);        
    }
    
    @Test
    void checkOWLEquivalentObjectPropertiesAxiom() {
        assertConstructJena(":x owl:equivalentProperty :y.", Construct.ROLE_HIERARCHY);
        assertConstruct(":x a owl:DatatypeProperty. :y a owl:DatatypeProperty. :x owl:equivalentProperty :y.", Construct.ROLE_HIERARCHY, Construct.D);
    }
    
    @Test
    void checkOWLDifferentIndividualsAxiom() {
        assertConstruct(":x owl:differentFrom :y.", Construct.CONCEPT_COMPLEX_NEGATION, Construct.CONCEPT_UNION, Construct.NOMINALS);
        assertConstruct(":x a owl:AllDifferent. :x owl:members (:y :z).", Construct.CONCEPT_COMPLEX_NEGATION, Construct.CONCEPT_UNION, Construct.NOMINALS);
    }
    
    
    @Test
    void checkOWLDisjointDataPropertiesAxiom() {
        String disjointDataPropertiesAxiom = getOWLAxiomsAsTurtle(factory.getOWLDisjointDataPropertiesAxiom(owlDataProp));
        //System.out.println(disjointDataPropertiesAxiom);
        assertConstruct(disjointDataPropertiesAxiom, Construct.D);
        assertConstruct(":x a owl:DatatypeProperty. :y a owl:DatatypeProperty. :x owl:propertyDisjointWith :y.", Construct.D);
        assertConstruct(":x a owl:AllDisjointProperties . :x owl:members (:y :z). :y a owl:DatatypeProperty.", Construct.D);
    }
    
    @Test
    void checkOWLDisjointObjectPropertiesAxiom() {
        String disjointObjectPropertiesAxiom = getOWLAxiomsAsTurtle(factory.getOWLDisjointObjectPropertiesAxiom(owlObjProp));
        //System.out.println(disjointObjectPropertiesAxiom);
        assertConstruct(disjointObjectPropertiesAxiom, Construct.ROLE_COMPLEX);
        assertConstruct(":x a owl:ObjectProperty. :y a owl:ObjectProperty. :x owl:propertyDisjointWith :y.", Construct.ROLE_COMPLEX);
        assertConstruct(":y a owl:ObjectProperty. :z a owl:ObjectProperty. :x a owl:AllDisjointProperties . :x owl:members (:y :z).", Construct.ROLE_COMPLEX);
    }
    
    
    @Test
    void checkOWLObjectPropertyRangeAxiom() {
        String objectPropertyRange = getOWLAxiomsAsTurtle(factory.getOWLObjectPropertyRangeAxiom(owlObjProp, owlClass));
        //System.out.println(objectPropertyRange);
        assertConstruct(objectPropertyRange, Construct.ROLE_DOMAIN_RANGE);
        assertConstruct(":x rdfs:range :y.", Construct.ROLE_DOMAIN_RANGE);
    }
    
    @Test
    void checkOWLFunctionalObjectPropertyAxiom() {
        String functionalObjectProperty = getOWLAxiomsAsTurtle(factory.getOWLFunctionalObjectPropertyAxiom(owlObjProp));
        //System.out.println(functionalObjectProperty);
        assertConstruct(functionalObjectProperty, Construct.F);
        assertConstruct(":x a owl:FunctionalProperty, owl:ObjectProperty.", Construct.F);
        assertConstructJena(":x a owl:FunctionalProperty.", Construct.F);
    }
    
    @Test
    void checkOWLSubObjectPropertyOfAxiom() {
        String subObjectPropertyOf = getOWLAxiomsAsTurtle(factory.getOWLSubObjectPropertyOfAxiom(owlObjProp, owlObjProp));
        //System.out.println(subObjectPropertyOf);
        assertConstruct(subObjectPropertyOf, Construct.ROLE_HIERARCHY);
        assertConstruct(":x a owl:ObjectProperty. :y a owl:ObjectProperty. :x rdfs:subPropertyOf :y.", Construct.ROLE_HIERARCHY);
        assertConstructJena(":x rdfs:subPropertyOf :y.", Construct.ROLE_HIERARCHY);
    }
    
    @Test
    void checkOWLDisjointUnionAxiom() {
        String disjointUnion = getOWLAxiomsAsTurtle(factory.getOWLDisjointUnionAxiom(owlClass, Arrays.asList(owlClass)));
        //System.out.println(disjointUnion);
        assertConstruct(disjointUnion, Construct.CONCEPT_COMPLEX_NEGATION, Construct.CONCEPT_UNION);
        assertConstruct(":x owl:disjointUnionOf (:y).", Construct.CONCEPT_COMPLEX_NEGATION, Construct.CONCEPT_UNION);
    }
    
    @Test
    void checkOWLSymmetricObjectPropertyAxiom() {
        String symmetricObjectProperty = getOWLAxiomsAsTurtle(factory.getOWLSymmetricObjectPropertyAxiom(owlObjProp));
        //System.out.println(symmetricObjectProperty);
        assertConstruct(symmetricObjectProperty, Construct.ROLE_INVERSE);
        assertConstruct(":x a owl:SymmetricProperty.", Construct.ROLE_INVERSE);
    }
    
    @Test
    void checkOWLDataPropertyRangeAxiom() {
        String dataPropertyRange = getOWLAxiomsAsTurtle(factory.getOWLDataPropertyRangeAxiom(owlDataProp, integer));
        //System.out.println(dataPropertyRange);
        assertConstruct(dataPropertyRange, Construct.ROLE_DOMAIN_RANGE, Construct.D);
        assertConstruct(":x a owl:DatatypeProperty. :x rdfs:range xsd:nonNegativeInteger", Construct.ROLE_DOMAIN_RANGE, Construct.D);
    }
    
    @Test
    void checkOWLFunctionalDataPropertyAxiom() {
        String functionalDataProperty = getOWLAxiomsAsTurtle(factory.getOWLFunctionalDataPropertyAxiom(owlDataProp));
        //System.out.println(functionalDataProperty);
        assertConstruct(functionalDataProperty, Construct.F, Construct.D);
        assertConstruct(":x a owl:FunctionalProperty, owl:DatatypeProperty.", Construct.F, Construct.D);
    }   
    
    @Test
    void checkOWLEquivalentDataPropertiesAxiom() {
        String equivalentData = getOWLAxiomsAsTurtle(factory.getOWLEquivalentDataPropertiesAxiom(owlDataProp, owlDataProp2));
        //System.out.println(equivalentData);
        assertConstruct(equivalentData, Construct.ROLE_HIERARCHY, Construct.D);
        assertConstruct(":x a owl:DatatypeProperty. :y a owl:DatatypeProperty. :x owl:equivalentProperty :y.", Construct.ROLE_HIERARCHY, Construct.D);
    }
        
    @Test
    void checkOWLDataPropertyAssertionAxiom() {
        String dataPropertyAssertion = getOWLAxiomsAsTurtle(factory.getOWLDataPropertyAssertionAxiom(owlDataProp, individual, 1));
        //System.out.println(dataPropertyAssertion);
        assertConstruct(dataPropertyAssertion, Construct.D);
        assertConstruct(":p a owl:DatatypeProperty. :x :p \"test\".", Construct.D);
    }
    
    @Test
    void checkOWLTransitiveObjectPropertyAxiom() {
        String transitiveObjectProperty = getOWLAxiomsAsTurtle(factory.getOWLTransitiveObjectPropertyAxiom(owlObjProp));
        //System.out.println(transitiveObjectProperty);
        assertConstruct(transitiveObjectProperty, Construct.ROLE_TRANSITIVE);
        assertConstruct(":x a owl:TransitiveProperty.", Construct.ROLE_TRANSITIVE);
    }
    
    @Test
    void checkOWLIrreflexiveObjectPropertyAxiom() {
        String irreflexiveObjectProperty = getOWLAxiomsAsTurtle(factory.getOWLIrreflexiveObjectPropertyAxiom(owlObjProp));
        //System.out.println(irreflexiveObjectProperty);
        assertConstruct(irreflexiveObjectProperty, Construct.ROLE_COMPLEX);
        assertConstruct(":x a owl:IrreflexiveProperty.", Construct.ROLE_COMPLEX);
    }    
    
    @Test
    void checkOWLSubDataPropertyOfAxiom() {
        String subDataPropertyOf = getOWLAxiomsAsTurtle(factory.getOWLSubDataPropertyOfAxiom(owlDataProp, owlDataProp));
        //System.out.println(subDataPropertyOf);
        assertConstruct(subDataPropertyOf, Construct.ROLE_HIERARCHY, Construct.D);
        assertConstruct(":x a owl:DatatypeProperty. :y a owl:DatatypeProperty. :x rdfs:subPropertyOf :y.", Construct.ROLE_HIERARCHY, Construct.D);
    }
    
    @Test
    void checkOWLInverseFunctionalObjectPropertyAxiom() {
        String inverseFunctionalObjectProperty = getOWLAxiomsAsTurtle(factory.getOWLInverseFunctionalObjectPropertyAxiom(owlObjProp));
        //System.out.println(inverseFunctionalObjectProperty);
        assertConstruct(inverseFunctionalObjectProperty, Construct.ROLE_INVERSE, Construct.F);
        assertConstruct(":x a owl:InverseFunctionalProperty.", Construct.ROLE_INVERSE, Construct.F);
    }
    
    @Test
    void checkOWLSameIndividualAxiom() {
        String sameIndividual = getOWLAxiomsAsTurtle(factory.getOWLSameIndividualAxiom(individual, individual2));
        System.out.println(sameIndividual);
        assertConstruct(sameIndividual, Construct.NOMINALS);
        assertConstruct(":x owl:sameAs :y.", Construct.NOMINALS);
    }
    
    @Test
    void checkOWLSubPropertyChainOfAxiom() {
        String subPropertyChainOf = getOWLAxiomsAsTurtle(factory.getOWLSubPropertyChainOfAxiom(Arrays.asList(owlObjProp), owlObjProp));
        //System.out.println(subPropertyChainOf);
        assertConstruct(subPropertyChainOf, Construct.ROLE_REFLEXIVITY_CHAINS);
        assertConstruct(":x owl:propertyChainAxiom (:y :z). :y a owl:ObjectProperty. :z a owl:ObjectProperty.", Construct.ROLE_REFLEXIVITY_CHAINS);
    }
    
    @Test
    void checkOWLInverseObjectPropertiesAxiom() {
        String inverseObjectProperties = getOWLAxiomsAsTurtle(factory.getOWLInverseObjectPropertiesAxiom(owlObjProp, owlObjProp2));
        //System.out.println(inverseObjectProperties);
        assertConstruct(inverseObjectProperties, Construct.ROLE_INVERSE);
        assertConstruct(":x owl:inverseOf :y", Construct.ROLE_INVERSE);
    }
    
    //CHECK real ontology
    
    @Test
    void checkRealOntologyPeople() {
        checkRealOntology("http://owl.man.ac.uk/2006/07/sssw/people.owl");
    }
    
    @Test
    void checkRealOntologyCamera() {        
        checkRealOntology("https://protege.stanford.edu/ontologies/camera.owl");
    }
    
    @Test
    void checkRealOntologyKoala() {
        checkRealOntology("https://protege.stanford.edu/ontologies/koala.owl");
    }
    
    @Test
    void checkRealOntologyPizza() {        
        checkRealOntology("https://protege.stanford.edu/ontologies/pizza/pizza.owl");
    }
    
    @Test
    void checkRealOntologyTravel() {        
        checkRealOntology("https://protege.stanford.edu/ontologies/travel.owl");
    }
    
    @Test
    void checkRealOntologyWine() {        
        checkRealOntology("http://www.w3.org/TR/owl-guide/wine.rdf");
    }

    
    
    
    /******************************************************
     * Helper Methods
     ******************************************************/
    
    private void checkRealOntology(String url){
        String onto = loadFromUrl(url);
        assertSameConstructOfRealOntologies(onto);
        //getDLExpressivityCheckerFromString(onto).getConstructs();
    }
    
   
    private String loadFromUrl(String requestURL) {
        try (Scanner scanner = new Scanner(new URL(requestURL).openStream(),StandardCharsets.UTF_8.toString())) {
            scanner.useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next() : "";
        } catch (IOException ex) {return "";}
    }
    
    private void assertSameConstructOfRealOntologies(String ontText) {
        DLExpressivityChecker owlapiChecker = getDLExpressivityCheckerFromString(ontText);
        ModelDlExpressivityChecker jenaChecker = new ModelDlExpressivityChecker(ontText, "RDF/XML");
        assertEquals(owlapiChecker.getConstructs(), jenaChecker.getConstructs());
    }

    private void assertConstruct(String ontText, Construct... c) {
        assertConstructOwlAPI(ontText, c);
        assertConstructJena(ontText, c);
    }

    private void assertConstructOwlAPI(String ontText, Construct... c) {
        String ont = prefixDefinition + ontText;
        DLExpressivityChecker owlapiChecker = getDLExpressivityCheckerFromString(ont);
        assertEquals(Arrays.asList(c), owlapiChecker.getConstructs(), "Constructs " + Arrays.toString(c) + " is not contained in DLExpressivityChecker.");
    }

    private void assertConstructJena(String ontText, Construct... c) {
        String ont = prefixDefinition + ontText;
        ModelDlExpressivityChecker jenaChecker = new ModelDlExpressivityChecker(ont);
        assertEquals(Arrays.asList(c), jenaChecker.getConstructs(), "Constructs " + Arrays.toString(c) + " is not contained in ModelDlExpressivityChecker.");
    }

    private DLExpressivityChecker getDLExpressivityCheckerFromString(String text) {
        OWLOntology owl = getOntologyFromString(text);
        LOGGER.info(owl.toString());
        return new DLExpressivityChecker(Arrays.asList(owl));
    }

    private OWLOntology getOntologyFromString(String text) {
        try {
            return manager.loadOntologyFromOntologyDocument(new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8)));
        } catch (OWLOntologyCreationException ex) {
            return null;
        }
    }

    private void generate() {

        System.out.println(getOWLAxiomsAsTurtle(factory.getOWLSubClassOfAxiom(owlClass, factory.getOWLObjectMinCardinality(5, owlObjProp))));
        /*
        OWLOntology owl = null;
        try {
            owl = manager.createOntology();
        } catch (OWLOntologyCreationException ex) { }

        //owl.addAxiom(factory.getOWLSubClassOfAxiom(factory.getOWLClass("http://test.de/bla"), factory.getOWLObjectSomeValuesFrom(factory.getOWLObjectProperty("http://test.de/test"), factory.getOWLThing())));
        
        owl.addAxiom();
        
        try {
            manager.saveOntology(owl, System.out);
        } catch (OWLOntologyStorageException ex) {
            
        }
         */
    }

    private String getOWLAxiomsAsTurtle(OWLAxiom axiom) {
        TurtleDocumentFormat turtleFormat = new TurtleDocumentFormat();
        OWLOntology owl = null;
        try {
            owl = manager.createOntology();
        } catch (OWLOntologyCreationException ex) {
        }
        owl.addAxiom(axiom);
        try {
            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                manager.saveOntology(owl, turtleFormat, out);
                return new String(out.toByteArray(), StandardCharsets.UTF_8);
            }
        } catch (IOException | OWLOntologyStorageException ex) {
        }
        return "";
    }
}
