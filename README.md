# Exposing text search services through rest APIs
This application exposes a search engine based on [Inverted Index](https://en.wikipedia.org/wiki/Inverted_index) through 
rest APIs. This use case also comprises the integration with a public open dataset of academic papers and indexing/retrieval 
of papers. The search engine allow:
- indexing scientific papers (abstract only) 
- searching papers given a word (paper ids are returned)
- persisting and restoring its internal index to/from a physical location
- automatically refreshing its index from a public data set

The engine allows both batch indexing and streaming. It leverages spark's in-memory data structures for a fast retrieval 
and higher capacity. When the engine starts, its index is empty and incrementally grows by processing batches of files or 
submitting new papers directly through the API. The application allows to refresh the index automatically from 
[here](https://www.kaggle.com/ahmaurya/iclr2017reviews/data). Finally, its index can be persisted in a physical 
storage and loaded back into memory so to allow the service to start and stop.

Any service provided by the engine has been made available through rest APIs.

## 1. What is in scope and what is not:
The application code and architecture is not meant to be production-ready. It is composed of a module that provides 
core functionalities and a light layer of http services on top of that. As per the core functionalities, the code 
is covered with basic tests and it has been designed in a more functional fashion. Tests and error handling are missing 
for the external layers (I/O to physical storage, rest APIs, and integration with Kaggle).

In some more details, the application is composed of 3 packages: *web*, *search* and *utils*. 
1. The package *web* deals with the rest services and their integration to internal functionalities. This is where 
internal data structures should be translated into a http-friendly manner (e.g. json). I left this translation out 
of scope along with unit tests.
2. The package *search* holds the core functionalities of the indexing and search engine. I added here basic unit tests.
Out of scope are the problems related to re-indexing, de-duplication and text quality (i.e. stopWords, synonyms, etc)
3. I dropped in the package *utils* al those functionalities needed by the app which are not big enough to create 
other packages by their own. It is worth mentioning that any I/O and network related functionality is implemented here 
here and, again, I have not spent the lot of time needed for error handling and unit tests.

## 2. Exposed services
Once the application starts it exposes an home page so the user can check whether the engine is correctly up and 
running. The home page is available at *http://host:port* where *host* and *port* can be configured beforehand 
using the *application.conf* file.

Follow the list of services and how to call them:

**indexPapersFromFile** 

Retrieves papers from file, process them in bulk. It adds indexed abtracts and paper ids to the internal index. 
File name and path should be configured beforehand using the *application.conf* file.

*http://host:port/indexPapersFromFile* (post)

**indexPaper** 

Processes the provided paper id and abstract and adds it to the index.
This allow adding new papers from external streaming application straight into memory and ready to be retrieved.

*http://host:port/indexPaper/PAPER_ID|ABSTRACT* (post, paper id and abstrct are concatenated with a pipe character '|')

**persistIndex**

Persists the current index to a physical location for later retrieval.
File name and path should be configured beforehand using the *application.conf* file.

*http://host:port/persistIndex* (post)

**retrievePersistedIndex**

Replaces the current index with the new one retrieved from the physical location.
File name and path should be configured beforehand using the *application.conf* file.

*http://host:port/retrievePersistedIndex* (post)

**getPapers**

Returns the list of papers where the provided word is found.

*http://host:port/getPapers/WORD_TO_SEARCH* (get)

## 3. Technology stack
*Scala 2.11*, *Spark 2.3*, *Akka-Http 10.0.9* and Kaggle command line

## 4. How to run the application
You will need your environment ready to compile with *maven* and have *[Kaggle](https://github.com/Kaggle/kaggle-api)* 
command line installed.

### 4.1 Build
```markdown
mvn clean install
```
### 4.2 Execution
We can start our application as stand-alone jar like this:
```markdown
mvn exec:java
```
### 4.3 Configuration and cmd-line-args
The application uses the following configuration parameters:
```init
spark.master = local
spark.appname = spark-search-engine
akka.http.port = 8001
papers.folder = /path/to/paper/csv/files/
storage.folder = /path/to/app/index/storage/
storage.file.name = invertedIndexStorage.csv
??? kaggle things here ???
```
You can change the default configurations directly on the *application.conf* file.
Optionally, you can provide configuration params like spark-master, akka-port etc from command line. To see the list of 
configurable params, just type:
```markdown
mvn exec:java -Dexec.args="--help" 
OR 
mvn exec:java -Dexec.args=“-h"
```

```ini
This application exposes rest APIs of search engine based on Inverted Index. It uses Spark 2.3 as distributed engine and Akka HTTP for the rest API.
So, this application needs config params like AkkaWebPort to bind to, SparkMaster
and SparkAppName

Usage: spark-submit jarname.jar [options]
  Options:
  -h, --help
  -m, --master <master_url>                    spark://host:port, mesos://host:port, yarn, or local. Default: $sparkMasterDef
  -n, --name <name>                            A name of your application. Default: $sparkAppNameDef
  -p, --akkaHttpPort <portnumber>              Port where akka-http is binded. Default: $akkaHttpPortDef
  -f, --paperFolder <paper_folder>             Folder in which papers can be processed in batch. Default: $papersFolderDef
  -s, --storageFolder <storage_folder>         Folder in which the application will persist the inverted index. Default: $storageFolder
```

## 4. Credits
I started this project by branching from a framework ready to host spark services and expose through rest APIs.
The original framework is available [here](https://github.com/spoddutur/spark-as-service-using-embedded-server).
The code went then through refactoring, core functionalities tests and implementation, i/o integration, 
kaggle integration, upgrade of scala, spark and akka http libraries, and other bits and pieces.
