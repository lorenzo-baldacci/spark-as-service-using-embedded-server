# Exposing Inverted Index services through rest API
This application exposes rest APIs of search engine based on Inverted Index. It uses Spark 2.3 as distributed engine and Akka HTTP for the rest API.

The engine allow both batch indexing and streaming. It leverages spark's in-memory data structures for a fast indexing and retrieval.
When the engine starts, its index is empty and incrementally grows when processing batches of files or when new papers are submitted through the API.
Moreover its index can be persisted in a physical storage and loaded back to allow the engine to be stopped and restarted.


## 1. Exposed services
Once the application starts it exposes an http page so the user can check through a browser that the engine is up and running. 
- **index papers from file**: retrieves papers from file, process them in bulk, add to the index
- **index single paper**: process the given paper and add it to the index
- **persist index**: save the index in a permanent storage
- **retrieve persisted index**: substitutes the index with one stored previously
- **query the index**: returns the list of papers where the word is found

## 2. What's in scope and what is not:
The application is composed of 3 main packages: *web*, *search* and *utils*. 
1. The package *web* deals with the rest services and the integration to internal functionalities. This is where internal data structures should be translated into a http-friendly manner (e.g. json).
I left this translation out of scope along with unit tests.
2. The package *search* holds the core functionalities of the indexing and search engine. I added basic unit tests for this package.
Out of scope are the problems related to the re-indexing of a paper and I just naively managed the duplicated entry o a paper (i.e. submitting the same paper multiple times). In this exercise I also did not deal with data quality related issues in text (e.g. stopWords, synonyms, etc)
3. I dropped in the package *utils* al those functionalities that I needed to upport the app but where not big enough to create their own package. It is worth mentioning that any I/O related functionalities sit here. Again I have left out of scope the unit test of this package.

## 3. How to call the services
1. **homepage** - GET - http://host:port
2. **index papers from file** - POST - http://host:port/indexPapersFromFile
3. **index single paper** - POST - http://host:port/indexPaper/PAPER_ID|ABSTRACT (note that paper id and abstrct are concatenated with a pipe)
4. **persist index** - POST - http://host:port/persistIndex
5. **retrieve persisted index** - POST - http://host:port/retrievePersistedIndex
6. **query the index** - GET - http://host:port/getPapers/WORD_TO_SEARCH

## 4. Building
It uses *Scala 2.11*, *Spark 2.3* and *Akka-Http 10.0.9*
```markdown
mvn clean install
```
## 5. Execution
We can start our application as stand-alone jar like this:
```markdown
mvn exec:java
```
### 5.1 Configuration and cmd-line-args
The application uses the following configuration parameters:
```init
spark.master = local
spark.appname = spark-search-engine
akka.http.port = 8001
papers.folder = /path/to/paper/csv/files/
storage.folder = /path/to/app/index/storage/
storage.file.name = invertedIndexStorage.csv
```
You can change the default configurations directly on the *application.conf* file.
Optionally, you can provide configuration params like spark-master, akka-port etc from command line. To see the list of configurable params, just type:
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
