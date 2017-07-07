#!/usr/bin/python

import sys
import os
import logging
import json
import abc
import re
import math
from merge import Merger
from formula import SigmaEQ, IdleTimeEQ
from subprocess import call, Popen, PIPE, check_output

# abstract DMon query class
class AbstractDMonQuery():
	__metaclass__ = abc.ABCMeta
	
	def __init__(self, descriptor):
		pass
	
	@abc.abstractmethod
	def translate():
		pass


# abstract converter for an item of the json to be parsed
class AbstractConverter():
	__metaclass__ = abc.ABCMeta

	@abc.abstractmethod
	def convert():
		pass


# converter for json item "nodes" which is a list of trace checking node descriptors
#	"nodes": [
#		{
#			"name": "boltA",
#			"parameter": "sigma",
#			"timewindow": "3600",
#			"inputrate": "100",
#			"method": "counting",
#			"relation": "<"
#		}
#	],
class ConverterNodesItem( AbstractConverter ):
	
	@classmethod
	def convert(cls, item):
		s = []
		for e in item:
			s = s + [{'name':e['name'], 'timewindow':e['timewindow']}]
		return s



class SimpleDMonQuery( AbstractDMonQuery ):
	
	def __init__(self, descriptor):
		self.__d = descriptor

	#translator is a dictionary of Converter where keys are json nameid and values are Converter
	def translate(self, translators):
		s = {}
		for e in self.__d:
			if (e == 'topologyname'): s.update({'topologyname': self.__d[e]})
			elif (e == 'nodes'): s.update({'nodes':translators[e].convert(self.__d[e])})
			elif (e == 'formulae'): pass
			else: pass

		#we should return a more complex object to allow an instance of AbstractDMonConnector (in restGet()) to determine the actions required to retrieve from DMon the correct subset of workerlogs
		return json.dumps(s)



class AbstractDMonConnector():
	__metaclass__ = abc.ABCMeta	

	def __init__(self, url):
		self.__url = url	

	def getURL(self):
		return self.__url	
	
	def checkDMonOn():
		pass

	@abc.abstractmethod
	def restGet():
		pass

	@abc.abstractmethod
	def restPost():
		pass



class LocalConnector( AbstractDMonConnector ):
	
	def __init__(self, path):
		AbstractDMonConnector.__init__(self, path)
		self.__ok = True


	def checkDMonOn(self):
		try:
			with open(self.getURL(), 'r') as f:
				self.__ok = True
		except IOError, error:
			logging.error('Connection to ' + self.getURL() + ' failed' )
			self.__ok = False

		return self.__ok


	def restGet(self, jsonscript):
		if (self.checkDMonOn()):
			with open(self.getURL(), 'r') as f:
				try:
					return json.load(f)
				except Exception, error:
					return None

	def restPost():
		pass


class LocalConnectorGeneratingDescriptor( AbstractDMonConnector ):
	
	def __init__(self, path):
		AbstractDMonConnector.__init__(self, path)
		self.__ok = True
		self.logfiles = []
		self.logFolder = './logs'


	def checkDMonOn(self):
		return self.__ok


	def __getLogParserRegexp(self, rexp_descriptor):
		with open(rexp_descriptor, 'r') as f:
			rexpjson = json.load(f)

		return rexpjson


	def __getlogFolder(self):
		return self.logFolder

	def restGet(self, jsonscript, regexp_descriptor):
	# This method, based on the type of jsonscript (specifing the kind of analysis to carry out on the nodes, retrives the workerlogs from DMon that are useful to run TC for the specified parameter under analysis


		if (self.checkDMonOn()):

			#identify the workerlog to be dowloaded from Dmon (assumption, only one file allowed)
			workerlogToCheck = self.whichLog(jsonscript)

			self.DmonRestGetStormLog(workerlogToCheck, self.__getlogFolder())
		
			# After downloading from Dmon, untar the workerlogs file
			output_tar = check_output(['tar', '-xvf', self.__getlogFolder() + '/' + workerlogToCheck, '--directory', self.__getlogFolder()])
			#print output_tar
			if (len(output_tar)>0):
				topology_descriptor = {'topologyname':'Atopology', 'logs':[]}

				rexpjson = self.__getLogParserRegexp(regexp_descriptor)				
				nodePosition = rexpjson['nodePosition']
				rexp = rexpjson['regexp']

				#set variable logfiles containing the list of log files extracted from the tar file recieved from DMon
				self.logfiles = output_tar.split()

				for log_file in self.logfiles:
					try:
						with open('./logs/' + log_file, 'r') as f:
							#extract a line from the currrent log file
							line = f.readline().strip('\n')

							list_of_nodes = []

							while (line != ''): 
								#match the regexp to parse the line
								re_match = re.match(rexp, line, re.I)
								#verify if the node name is already defined topology_descriptor and add the current log name to its list of workers
								nodeName = re_match.group(nodePosition) 
								if (not (nodeName in list_of_nodes)):
									#add nodeName to the list of nodes (related to the current log file) already processed and marked with the current log_file
									list_of_nodes.append(nodeName)
									#look for node into topology_descriptor (nodeName might be already there as it might have been spawned over many workers, so its name is alredy in the dictionary)
									found = False;
									for e in topology_descriptor['logs']:
										if (e['nodename'] == nodeName):
											e['logs'] = e['logs'] + ',' + log_file
											found = True
									if (not found):
										topology_descriptor['logs'].append({"nodename": nodeName, "logs": log_file})
								#extract a line from the currrent log file
								line = f.readline().strip('\n')

					except IOError, error:
						logging.error('IO error')
				
				try:
					with open('./topologylog.json', 'w') as f:
						f.write(json.dumps(topology_descriptor))
				except IOError, error:
					logging.error('IO error')	

		return topology_descriptor




	def getlogfiles(self, withdir):
		# This method returns the list of (already dowlonded) file containing the workerlogs including their path in the local machine
		l = []
		for e in self.logfiles:
			if (withdir == True):
				l.append(self.__getlogFolder() + '/' + e)
			else:
				l.append(e)
		return l



	def whichLog(self, jsonscript):
		#This method is in charge of selecting the proper workerlog file to be downloaded from Dmon, based on the requirement in the jsonscript parameter
		#specifying the kind of trace checking analysis to execute - the time window to consider.

		#get the json depscriptor containing the list of the available workerlogs
		responseDmonquery = self.availableStormLogs()

		# Here put your best method to select the proper workerlog
		log = responseDmonquery["StormLogs"][len(responseDmonquery)]


		return log



	def availableStormLogs(self):
		#This method calls GET /v1/overlord/storm/logs/ of DMon to retrieve the json list of available workerlogs
		return json.loads('{"StormLogs": ["workerlogs_2016-12-08-17:32:53.tar","workerlogs.tar"]}')


	def DmonRestGetStormLog(self, workerlogToCheck, logFolder):
		#This method calls GET /v1/overlord/storm/logs/{workerlogs} of DMon
		pass

	def restPost(self):
		pass


class RemoteDmonConnector( AbstractDMonConnector ):
	
	def __init__(self, path):
		AbstractDMonConnector.__init__(self, path)
		self.__ok = True


	def checkDMonOn(self):
		return self.__ok


	def restGet(self, jsonscript):
		pass
	def restPost():
		pass


class RunnableTCInstance():
	
	def __init__(self, nodename, formula):
		self.__nodename = nodename
		self.__formula = formula

	def run(self):

		#build the formula based on the events in the node events file
		theformula = self.__formula.getFormulaFor('./merge/'+self.__nodename + '.eve', './merge/'+self.__nodename + '.his')

		#write the formula file
		with open('./results/' + self.__formula.typename() + self.__nodename + '.sol', 'w') as fo:
			fo.write(theformula)


#spark-submit --class it.polimi.krstic.MTLMapReduce.SparkHistoryCheck --master spark://localhost:7077 --executor-memory 4g --executor-cores 2 --num-executors 1 ../mtlmapreduce/target/MTLMapReduce-0.0.1-SNAPSHOT-jar-with-dependencies.jar ../mtlmapreduce/examples/traces/trace1 ../mtlmapreduce/examples/formulae/G/P01 outzazaza --reader spark -d


		print '| ******** Run TC on ' + self.__nodename + ' ********* |\n'
		print '| History file: ' + '/home/bersani/Tools/DICE-WP4/dicestractor/merge/'+self.__nodename+'.his'
		print '| Formula file: ' + '/home/bersani/Tools/DICE-WP4/dicestractor/'+self.__formula.typename()+self.__nodename+'.sol'
		print '| **************************************************** |\n'
		
		call (['./spark-submit', '--class', 'it.polimi.krstic.MTLMapReduce.SparkHistoryCheck', '--master', 'spark://lap-bersani:7077',  '--executor-memory', '4g', '--executor-cores', '2', '--num-executors', '1', './MTLMapReduce.jar', './merge/'+self.__nodename+'.his', './results/' + self.__formula.typename()+self.__nodename+'.sol', './results/' + self.__formula.typename()+self.__nodename+'.res', '--reader', 'spark', '-l'])


	def getResult(self):
		r = ""
		try:
			with open('./results/' + self.__formula.typename() + self.__nodename + '.res-f1', 'r') as fo:
				r = fo.readline()
		except IOError, err:
			print 'Uuppps...Spark apparently did not work properly'
			logging.error('Error while opening result file produced by Spark Trace Checker: %s', str(err))
		return r


class TCRunner():
		
	template_file_spoutrate = ""
	template_file_sigma = ""
	
	def __init__(self, tc_descriptor):
		self.__descriptor = tc_descriptor
		self.__i = 0
		self.__nodes = self.__descriptor['nodes']

	def __iter__(self):
		return self

	def next(self):
		if (self.__i < len(self.__nodes)):
			node = self.__nodes[self.__i]

			formula = None
			flag = True

			if (node['parameter'] == 'sigma' and node['type'] == 'bolt'):
				# check that the design value of sigma is equal to the real value of sigma
	

				if (node['method'] == 'average'):
					self.template_file_sigma = './templates/sigma_average.tmp'

					#create the list of values to build the formula
					values = [ node['timewindow'], node['timesubwindow'], node['designvalue'] ]

					if (node['relation'] == '='):
						#define the formula
						formula = SigmaAverageEQ(self.template_file_sigma, values)
					elif (node['relation'] == '<'):
						#define the formula
						formula = SigmaAverageLT(self.template_file_sigma, values)
					elif (node['relation'] == '>'):
						#define the formula
						formula = SigmaAverageGT(self.template_file_sigma, values)
					else: 
						raise Exception("No runnable TC instance created. Possible cause: parameter name is not correct and properly defined")
				elif (node['method'] == 'counting'):
					template_file_sigma = './templates/sigma_counting.tmp'				
	
					#create the list of values to build the formula
					values = [ node['timewindow'], node['designvalue'] ]

					if (node['relation'] == '='):
						#define the formula
						formula = SigmaCountEQ(self.template_file_sigma, values)
					elif (node['relation'] == '<'):
						#define the formula
						formula = SigmaCountLT(self.template_file_sigma, values)
					elif (node['relation'] == '>'):
						#define the formula
						formula = SigmaCountGT(self.template_file_sigma, values)
					else: 
						raise Exception("No runnable TC instance created. Possible cause: parameter name is not correct and properly defined")
				else:
					raise Exception("No runnable TC instance created. Possible cause: parameter name is not correct and properly defined")


			elif (node['parameter'] == 'spoutrate' and node['type'] == 'spout'):
				if (node['method'] == 'average'):
					self.template_file_spoutrate = './templates/spoutrate_average.tmp'

					#create the list of values to build the formula
					values = [ node['timewindow'], node['timesubwindow'], node['designvalue'] ]

					if (node['relation'] == '='):
						#define the formula
						formula = SpoutRateAverageEQ(self.template_file_sigma, values)
					elif (node['relation'] == '<'):
						#define the formula
						formula = SpoutRateAverageLT(self.template_file_sigma, values)
					elif (node['relation'] == '>'):
						#define the formula
						formula = SpoutRateAverageGT(self.template_file_sigma, values)
					else: 
						raise Exception("No runnable TC instance created. Possible cause: parameter name is not correct and properly defined")
				elif (node['method'] == 'counting'):
					template_file_spoutrate = './templates/spoutrate_counting.tmp'				
	
					#create the list of values to build the formula
					values = [ node['timewindow'], node['designvalue'] ]

					if (node['relation'] == '='):
						#define the formula
						formula = SpoutRateCountEQ(self.template_file_sigma, values)
					elif (node['relation'] == '<'):
						#define the formula
						formula = SpoutRateCountLT(self.template_file_sigma, values)
					elif (node['relation'] == '>'):
						#define the formula
						formula = SpoutRateCountGT(self.template_file_sigma, values)
					else: 
						raise Exception("No runnable TC instance created. Possible cause: parameter name is not correct and properly defined")
				else:
					raise Exception("No runnable TC instance created. Possible cause: parameter name is not correct and properly defined")

			else:
				print 'Unknown parameter type: ' + node['parameter'] 
				flag = False
	
			self.__i = self.__i + 1
		
			# return a RunnableTCInstance specifying the node and the formula
			if (flag):
				return RunnableTCInstance(node['name'], formula)
			else:
				raise Exception("No runnable TC instance created.")
		else:
			raise StopIteration()




def dicetractor(dmon_url, tc_descriptor):

	#build the rest json query from the TC descriptor by means of the converters (that allow for intepreting the TC descriptor)
	dmon_query = SimpleDMonQuery(tc_descriptor)
	rest_jsonquery = dmon_query.translate({'nodes':ConverterNodesItem}) 	
	

	#set the variable specifying the json file including the description of the regural expression used to parse the logs
	rexp_file = 're.json'

	#query D-mon to get 
	#- topology logs descriptor 
	#- logs

	connector = None
	logsdescriptor = None

	if (dmon_url == 'local'):
		connector = LocalConnectorGeneratingDescriptor('')
		logsdescriptor = connector.restGet(rest_jsonquery, rexp_file)
	else:
		connector = RemoteDmonConnector('http://109.231.122.169:5001')
		logsdescriptor = connector.restGet(rest_jsonquery)

	logging.basicConfig(filename='merge.log', filemode='w', level=logging.DEBUG)

	result = []

	if (logsdescriptor):
		#merge logs to run TC
		merger = Merger(logsdescriptor)	

		log_files = connector.getlogfiles(withdir=True)

		merger.merge(log_files, rexp_file)
		
		tc_instances = TCRunner(tc_descriptor)
		for i in tc_instances:
			try:
				i.run()
				result.append(i.getResult())
			except Exception, err_msg:
				print "Error while calling Spark Trace Checker: %s" % str(err_msg)
				logging.error('Error while calling Spark Trace Checker: %s', str(err_msg))

			

	#return the result to the caller (rest method likely)
	return result



def main():

	#get the trace checking descriptor file name
	tc_file = sys.argv[sys.argv.index("-t")+1]

	#read trace checking descriptor file
	with open(tc_file, 'r') as f:
		tc_descriptor = json.load(f)

	#build the rest json file
	dmon_query = SimpleDMonQuery(tc_descriptor)
	rest_jsonquery = dmon_query.translate({'nodes':ConverterNodesItem}) 	


	#query D-mon to get 
	#- topology logs descriptor 
	#- logs

	#connector = LocalConnector('./topologylog.json')
	#logsdescriptor = connector.restGet(rest_jsonquery)


	logging.basicConfig(filename='merge.log', filemode='w', level=logging.DEBUG)

	if (logsdescriptor):
		#merge logs to run TC
		merger = Merger(logsdescriptor)	

		log_files = sys.argv[sys.argv.index("-f")+1:sys.argv.index("-t")]
		rexp_file = sys.argv[sys.argv.index("-r")+1]

		
		merger.merge(log_files, rexp_file)
	
		
		tc_instances = TCRunner(tc_descriptor)
		for i in tc_instances:
			i.run()
			

	#show result

if __name__ == '__main__':
    main()
	


