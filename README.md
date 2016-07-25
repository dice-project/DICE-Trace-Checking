# DICE - Trace Checking Tool 

The DICE Trace Checking Tool (DICE-TraCT) is the component of the DICE framework which executes trace checking analysis of big-data application logs.

The current minimalistic release of DICE-TraCT is composed of the following three modules that can be found in folder ```/components```:

* [dicetract.py](https://github.com/dice-project/DICE-Trace-Checking/blob/master/components/dicetract.py)
* [merger.py](https://github.com/dice-project/DICE-Trace-Checking/blob/master/components/merger.py)
* [formula.py](https://github.com/dice-project/DICE-Trace-Checking/blob/master/components/formula.py)

The functionality of DICE-TraCT is implemented in **dicetract.py**. The script employs the subcomponents **merger.py** and **formula.py** to define the trace checking instance which is solved through an external trace-checker [mtlmapreduce](https://bitbucket.org/krle/mtlmapreduce/src/29b53e3de83b?at=master). **dicetract.py** receives three parameters: a list of log files to analyze, a trace checking instance descriptor and a log descriptor. Based on the property that is specified in trace checking instance descriptor, **dicetract.py** builds (i) a suitable log trace through **merger.py** and (ii) a temporal logic formula through **formula.py**. Finally, it run the trace-checker and collects the outcome.

 

## Dependencies

**dicestract.py** needs a running Apache Spark environment. The trace checking instance that **dicestract.py** builds is run through *spark-submit* script. The launching script (commonly in <SPARK_HOME/sbin>) has to be available system-wide.
Spark can be executed on the local machine without an active HDFS system (or similar).

**merger.py** and **formula.py**  must be placed in the same directory where **dicetract.py** is run (or available through symbolic link, if stored elsewhere).

Folder ```examples/templates``` contains an example of template formula in file [idleTime.tmp](https://github.com/dice-project/DICE-Trace-Checking/blob/master/examples/templates/idleTime.tmp). **dicestract.py** reads templates files in a local subfolder called ```templates/``` (the current release does not rely on a configuration file). To run **dicetract.py** just copy ```example/templates/``` into the **dicetract.py** folder.

To run **dicetract.py**, execute the following command:

```
python dicetract.py 
	-f <list of .log files> 
    -t <json trace checking descriptor> 
    -r <json regular expression descriptor>
```

**dicetract.py** runs a trace checking instance by means of a Spark job submitted to the Spark cluster through *spark-submit*. The java class specified in the *spark-submit* command line is ```MTLMapReduce-0.0.1-SNAPSHOT-jar-with-dependencies.jar``` that can be obtained following the directives at https://bitbucket.org/krle/mtlmapreduce/.

The following command is an example of DICE-TraCT invocation; logs files are stored in a local subfolder ```logs/```.
```
python dicetract.py -f ./logs/w1.log ./logs/w2.log ./logs/w3.log -t tc.json -r re.json
```
Example of log files, of the trace checking and log lines descriptors can be found at the following links:
* [w1.log](https://github.com/dice-project/DICE-Trace-Checking/blob/master/utils/log_generator/w1.log)
* [w2.log](https://github.com/dice-project/DICE-Trace-Checking/blob/master/utils/log_generator/w2.log)
* [w3.log](https://github.com/dice-project/DICE-Trace-Checking/blob/master/utils/log_generator/w3.log)
* [tc.json](https://github.com/dice-project/DICE-Trace-Checking/blob/master/examples/tc.json)
* [re.json](https://github.com/dice-project/DICE-Trace-Checking/blob/master/examples/re.json)

### Log generator
It builds log traces to validate **dicetract.py**. **genlog.py** reads a json file of the form:

```json
{
	"boltA" : 
		[
			"bolt",
			{
				"Thread-1" : "emit",
				"Thread-3" : "emit"
			}
		],
	"spoutB" : 
		[
			"spout",
			{
				"Thread-2" : "emit",
				"Thread-5" : "emit"
			}
		],
    ...
}
```
where the set of components, whose events will occur in the log file produced by **genlog.py**, is specified through a key-valued pair of the form "component_name": [descriptor]. For each component name (e.g., "boltA") the user specifies in the descriptor the list of threads where it is executed.

To run **genlog.py**, execute the following command in the command line:

```
python genlog.py 
	-nodes <descriptor>.json 
    -time 0 
    -sigma 1.0 
    -mean 0.0 
    -n 1000 
    -seed 71 
    -delta 300 
    -out w2.log
```
where *mean* and *sigma* define the mean value and the standard deviation to use for generating the timestamps in the log trace. Parameter *n* is the number of log lines, *seed* is the random seed, *delta* is the delay between any two events in the traces and *out* is the name of the final log file.

Following command is an example of **genlog.py** invocation with the descriptor called log2.json.
```
python genlog.py -nodes log2.json -time 0 -sigma 1.0 -mean 0.0 -n 1000 -seed 71 -delta 300 -out w2.log
```

Log descriptors can be found at:
* [log1.json](https://github.com/dice-project/DICE-Trace-Checking/blob/master/utils/log_generator/log1.json)
* [log2.json](https://github.com/dice-project/DICE-Trace-Checking/blob/master/utils/log_generator/log2.json)
* [log3.json](https://github.com/dice-project/DICE-Trace-Checking/blob/master/utils/log_generator/log3.json)



