# UBT Wrapper for full-text searching (FTS) Vs Regular expressions (regex)



## Overview

**UBTWrapperFulltextVsRegex**, is a rerun and extension for the benchmark [LUBMft](http://www.l3s.de/~minack/rdf-fulltext-benchmark/). It benchmarks Full-text and regex queires though the [Lehigh University Benchmark](http://swat.cse.lehigh.edu/projects/lubm/) on different triple stores (4Store, Bigdata, Jena TDB, Owlim, Sesame, Stardog, Virtuoso and Yars). YARS is particularly not maintained any more.



## compiling

    mvn clean install  
    mvn install -DskipTests=false \\ if tests fails, skip tests  
    mvn dependency:copy-dependencies
The above will not run tests, skip tests with:  
    mvn install -DskipTests=false  
This may not work for all triple stores, so you may cd each one and do the above cmd.  
This will download all dependances in to path target/dependency which will be used when runing the scripts.  
The file: "config.kb.tdblucene" contains the configrations for each triple store, the data path should point to the dir where is should be at ../../LUBM/LUBM...
and all rdf should be in RDF/XML formate and should have the prefix "University" to be processed.  

Then, either edit/run the file bulkTest.sh for all triple stores, or  
cd to the chosen store and Run:  

    sh ./load_ubt_*.sh  // for loading data into a seprate directory within the project.  
    sh ./query_ubt_*.sh  // for querying the tripe store from the file "config.query.lubm-fulltext.sparql.larq"  

Find the result in a text file start with Result and suffixed with date and time.  


## Results

Chick the different xlsx files for results obtained from running the experment on my machine.


## licensing

This work is licensed under the GNU General Public License (GPL). 

  

## Support
Check my profile for my email.  
Helping is not guaranteed, asking is never a problem :)
