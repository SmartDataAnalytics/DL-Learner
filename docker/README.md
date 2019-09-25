# Docker image for DL-Learner
This is a docker version of the DL-Learner! It is assambled from [the latest release](https://github.com/SmartDataAnalytics/DL-Learner/releases) of the framework.

## Getting started
DL-Learner provide a `cli` interface which is the main command line interface to run learning tasks with the DL-Learner framework. The script takes a configuration file as argument and starts the configured learning algorithm. The DL-Learner release already contains a collection of examples. You can find them in the examples/ folder.

In order to run such intefrace via docker, use the following command:
```
make run
```
When the comand line intefrace it done you will be able to run the learning tasks offered by the DL-Learner framework. As it is described on the DL-Learner manual, you could run one of the examples by just doing: 

```sh
bin/cli examples/father.conf
```

The second executable in the bin/ folder is the `enrichment` script. This script infers new schema axioms based on the instance data of a given SPARQL endpoint. The enrichment can be performed for a particular resource (i.e. a class or a property) or for the whole dataset. To get suggestions for new schema axioms concerning the property <http://dbpedia.org/ontology/currency> you would run :

```sh
bin/enrichment -e http://dbpedia.org/sparql -g http://dbpedia.org -r http://dbpedia.org/ontology/currency
```

### Executing Examples From CLI
It is also possible to execute the examples from the command line. To run the father example you can type: 

```
make run-example
```

We recommend to read the [manual](http://dl-learner.org/Resources/Documents/manual.pdf) when using DL-Learner for the first time.
