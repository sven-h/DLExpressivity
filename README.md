# DLExpressivity
SPARQL queries to check DL Expressivity of Ontology / Endpoint

# How to build
just run `mvn package` and execute `java -jar dl-expressivity-1.0-jar-with-dependencies.jar` in the generated target folder.

# Example call

```
java -jar dl-expressivity-1.0-jar-with-dependencies.jar -e https://dbpedia.org/sparql

INFO  SparqlDlExpressivityChecker:29 - Check for construct LIMEXIST
INFO  SparqlDlExpressivityChecker:29 - Check for construct NEG
INFO  SparqlDlExpressivityChecker:29 - Check for construct RRESTR
INFO  SparqlDlExpressivityChecker:29 - Check for construct Q
INFO  SparqlDlExpressivityChecker:29 - Check for construct (D)
INFO  SparqlDlExpressivityChecker:29 - Check for construct U
INFO  SparqlDlExpressivityChecker:29 - Check for construct R
INFO  SparqlDlExpressivityChecker:29 - Check for construct +
INFO  SparqlDlExpressivityChecker:29 - Check for construct UNIVRESTR
INFO  SparqlDlExpressivityChecker:29 - Check for construct CINT
INFO  SparqlDlExpressivityChecker:29 - Check for construct N
INFO  SparqlDlExpressivityChecker:29 - Check for construct F
INFO  SparqlDlExpressivityChecker:29 - Check for construct E
INFO  SparqlDlExpressivityChecker:29 - Check for construct Rr
INFO  SparqlDlExpressivityChecker:29 - Check for construct C
INFO  SparqlDlExpressivityChecker:29 - Check for construct O
INFO  SparqlDlExpressivityChecker:29 - Check for construct H
INFO  SparqlDlExpressivityChecker:29 - Check for construct I
INFO  Main:46 - expressivity: FL0, EL, ELPLUSPLUS   DL Name:
```