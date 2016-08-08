# DICE - Trace Checking Tool 

The DICE Trace Checking Tool (DICE-TraCT) is the component of the DICE framework which executes trace checking analysis of big-data application logs.

The current minimalistic release of DICE-TraCT is composed of the following three modules that can be found in folder ```/components```:

* [dicetract.py](https://github.com/dice-project/DICE-Trace-Checking/blob/master/components/dicetract.py)
* [merger.py](https://github.com/dice-project/DICE-Trace-Checking/blob/master/components/merger.py)
* [formula.py](https://github.com/dice-project/DICE-Trace-Checking/blob/master/components/formula.py)

The functionality of DICE-TraCT is implemented in **dicetract.py**. The script employs the subcomponents **merger.py** and **formula.py** to define the trace checking instance which is solved through an external trace-checker [mtlmapreduce](https://bitbucket.org/krle/mtlmapreduce/src/29b53e3de83b?at=master). **dicetract.py** receives three parameters: a list of log files to analyze, a trace checking instance descriptor and a log descriptor. Based on the property that is specified in trace checking instance descriptor, **dicetract.py** builds (i) a suitable log trace through **merger.py** and (ii) a temporal logic formula through **formula.py**. Finally, it run the trace-checker and collects the outcome.

