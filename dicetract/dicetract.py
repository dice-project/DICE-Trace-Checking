#!/usr/bin/python

import sys
import os
import logging
import json
import abc
import re
import math
import glob
#from app import *
import requests
from subprocess import call, Popen, PIPE, check_output
from io import BufferedReader


# DICE-TraCT definitions and classes 
# ----------------------------------
from solvers.tcrunner import TCRunner
from logMerger.logmerger import Merger
# ----------------------------------




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


class ConverterNodesItemBis( AbstractConverter ):
	
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

	def getDescriptor(self):
		return self.__d


class VisitableDMonQuery( AbstractDMonQuery ):
	__metaclass__ = abc.ABCMeta
	
	def __init__(self, query):
		self._query = query
	
	@abc.abstractmethod
	def getRegExp(self, visitor):
		pass



class DMonQuery( VisitableDMonQuery ):

	def translate(self, translators):
		return self._query.translate(translators)

	def getRegExp(self, visitor):
		return visitor.visit(self)		

	def getDescriptor(self):
		return self._query.getDescriptor()

	def getTCInstance(self, visitor):
		return visitor.visit(self)	



class VisitorDMonQuery():
	__metaclass__ = abc.ABCMeta
	
	def __init__(self):
		pass
	
	@abc.abstractmethod
	def visit(self, query):
		pass
	

class VisitorClassSigmaSpoutRate( VisitorDMonQuery ):

	def visit(self, query):
		assert (type(query) is DMonQuery), "VisitorClassSigmaSpoutRate received an object to visit not of type DMonQuery"
		d = query.getDescriptor()

		flag_spoutrate = False
		flag_sigma = False

		for e in d["nodes"]:
			if (e["parameter"] == 'sigma'):
				flag_sigma = True
			if (e["parameter"] == 'avg_emit_rate'):
				flag_spoutrate = True
	
		if (flag_sigma and flag_spoutrate):
			return 're_exclamation_spoutrate_sigma_thread.json'
		elif (flag_sigma):
			return 're_exclamation_sigma_thread.json'
		elif (flag_spoutrate):
			return 're_exclamation_spoutrate_thread.json'



class VisitorDescriptorInstance( VisitorDMonQuery ):
# This class modifies the TC instance received from the user in order to produce a TC instance that is ready for dicetract

	def visit(self, query):
		assert (type(query) is DMonQuery), "VisitorDescriptorInstance received an object to visit not of type DMonQuery"
		d = query.getDescriptor()

		for e in d["nodes"]:
			if (e["parameter"] == 'avg_emit_rate'):
				e["parameter"] = 'spoutrate'

		return d
	


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






class LocalDMonConnector( AbstractDMonConnector ):
	
	def __init__(self, url):
		AbstractDMonConnector.__init__(self, url)
		self.__ok = True
		self.logfiles = []
		self.logFolder = './logs'


	def checkDMonOn(self):
		return self.__ok


	def __getLogParserRegexp(self, rexp_descriptor):
		with open(rexp_descriptor, 'r') as f:
			rexpjson = json.load(f)

		return rexpjson


	def getLogFolder(self):
		return self.logFolder


	def __extractNodes(self, jsonscript):
		l = []

		#print jsonscript
		descriptor = json.loads(jsonscript)

		for e in descriptor["nodes"]:
			l.append(e["name"])
	
		return l


	def restGet(self, jsonscript, regexp_descriptor):
	# This method, based on the type of jsonscript (specifing the kind of analysis to carry out on the nodes), retrives the workerlogs from DMon that are useful to run TC for the specified parameter under analysis

		topology_descriptor = None

		if (self.checkDMonOn()):

			#identify the workerlog to be dowloaded from Dmon (assumption, only one file allowed)
			workerlogToCheck = self.whichLog(jsonscript)

			self.dmonRestGetStormLog(workerlogToCheck)

			output_tar = None
			# After downloading from Dmon, untar the workerlogs file
			# remove the original tar and untar the included file which contains logs
			output_tar = check_output(['tar', '-xvf', os.path.join(self.getLogFolder(),workerlogToCheck), '--directory', self.getLogFolder()])

			if (output_tar is None or output_tar == ''):
				raise RuntimeError("tar file from DMON is empty")

			#os.remove(os.path.join(self.getLogFolder(), workerlogToCheck))
			#os.rename(os.path.join(self.getLogFolder(), output_tar.strip('\n')), os.path.join(self.getLogFolder(), workerlogToCheck))

			output_tar = check_output(['tar', '-xvf', os.path.join(self.getLogFolder(),workerlogToCheck), '--directory', self.getLogFolder()])

			#print output_tar
			if (len(output_tar)>0):
				topology_descriptor = {'topologyname':'Atopology', 'logs':[]}

				rexpjson = self.__getLogParserRegexp(regexp_descriptor)				
				nodePosition = rexpjson['nodePosition']
				rexp = rexpjson['regexp']

				# From the json descriptor, get the list of nodes to check
				nodesToCheck = self.__extractNodes(jsonscript)

				#set variable logfiles containing the list of log files extracted from the tar file recieved from DMon
				#self.logfiles = output_tar.split() --- NOT PORTABLE
				self.logfiles = [os.path.basename(x) for x in glob.glob("./logs/*.log")]
				print "\nTar file contains the following logs:"
				for e in self.logfiles:
					print "\t-", e

				for log_file in self.logfiles:
					try:
						with open('./logs/' + log_file, 'r') as f:
							#extract a line from the current log file
							line = f.readline().strip('\n')

							list_of_nodes = []

							while (line != ''): 
								#match the regexp to parse the line
								re_match = re.match(rexp, line, re.I)
								if (re_match):

									#verify if the node name is already defined topology_descriptor and add the current log name to its list of workers
									nodeName = re_match.group(nodePosition) 
									if ( (not nodeName in list_of_nodes) and (nodeName in nodesToCheck) ):
										#add nodeName to the list of nodes (related to the current log file) already processed and marked with the current log_file
										list_of_nodes.append(nodeName)
										#look for node into topology_descriptor (nodeName might be already there as it might have been spawned over many workers, so its name is alredy in the dictionary)
										found = False;
										for e in topology_descriptor['logs']:
											if (e['nodename'] == nodeName):
												e['logs'].append(log_file)
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

		if (topology_descriptor is None):
			return None


		print '\033[93m' + '\nLogging information of the current topology:' + '\033[0m'
		print '---------------------------------------------'
		print 'Nodes to analyse:'
		for e in nodesToCheck:
			print '\t- ' + '\033[1m' + e + '\033[0m'
		print 'Descriptor:'
		print json.dumps(topology_descriptor, sort_keys=True, indent=4, separators=(',', ':'))

		return topology_descriptor




	def getlogfiles(self, withdir):
		# This method returns the list of (already dowlonded) file containing the workerlogs including their path in the local machine
		l = []
		for e in self.logfiles:
			if (withdir == True):
				l.append(self.getLogFolder() + '/' + e)
			else:
				l.append(e)
		return l



	def whichLog(self, jsonscript):
		#This method is in charge of selecting the proper workerlog file to be downloaded from Dmon, based on the requirement in the jsonscript parameter
		#specifying the kind of trace checking analysis to execute - the time window to consider.

		#get the json depscriptor containing the list of the available workerlogs
		responseDmonquery = self.availableStormLogs()

		# Here put your best method to select the proper workerlog
		log = max(responseDmonquery)
		print "\nThe latest available log is", log

		return log



	def availableStormLogs(self):
		#This method calls GET /v1/overlord/storm/logs/ of DMon to retrieve the json list of available workerlogs
		#The list is of the form 
		#	{
		#		"StormLogs": [
		#			"workerlogs_2017-07-12-10:40:57.tar",
		#			"workerlogs_2017-07-12-10:36:34.tar",
		#			"workerlogs_2017-07-12-11:03:36.tar",
		#			"workerlogs_2017-07-11-10:07:51.tar",
		#			"workerlogs_2017-07-11-15:27:42.tar"
		#		]
		#	}
		# the last entry is the current available log


		# address of local deployment
		url = self.getURL() + '/dmon/v1/overlord/storm/logs'

		print '\033[93m' + "Checking available logs on " + '\033[0m' + '\033[1m' + url + '\033[0m'
 		response = requests.get(url)
		r = response.json()
		print "Found:"
		for e in r["StormLogs"]:
			print "\t-" + '\033[1m' + url + '\033[0m'

		# return the list of logs in DMon
		return r["StormLogs"]


	def dmonRestGetStormLog(self, workerlogToCheck):
		#This method calls GET /v1/overlord/storm/logs/{workerlogs} of DMon
		# address of local deployment
		url = self.getURL() + '/dmon/v1/overlord/storm/logs/'


		print "Call DMON to download", workerlogToCheck

		response = requests.get(url + workerlogToCheck, stream=True)
		with open(os.path.join(self.getLogFolder(),workerlogToCheck), 'wb') as fd:
			for chunk in response.iter_content(chunk_size=256):
				fd.write(chunk)		

		print "Worker log file written to", os.path.join(self.getLogFolder(),workerlogToCheck)

		#return the last entry
		return

	def restPost(self):
		pass


class LocalConnector( LocalDMonConnector ):
	
	def __init__(self, path):
		LocalDMonConnector.__init__(self, path)
		self.__ok = True


	# override method
	def checkDMonOn(self):
		return self.__ok


	# override method
	def whichLog(self, jsonscript):
		#This method is in charge of selecting the proper workerlog file to be downloaded from Dmon, based on the requirement in the jsonscript parameter
		#specifying the kind of trace checking analysis to execute - the time window to consider.
		# NOTE: Local call only deals with one fime of name "workerlogs.tar" only

		return "workerlogs.tar"

	# override method
	def availableStormLogs(self):
		# The local call works on a file of name "workerlogs.tar" only
		return "workerlogs.tar"

	# override method
	def dmonRestGetStormLog(self, workerlogToCheck):

		print "No call DMON to download. Logs already in .log/" + workerlogToCheck

		return



class RemoteDmonConnector( LocalDMonConnector ):
	
	def __init__(self, path):
		LocalDMonConnector.__init__(self, path)






def dicetractor(dmon_ip, dmon_port, tc_descriptor):

	#build the rest json query from the TC descriptor by means of the converters (that allow for intepreting the TC descriptor)
	dmon_query = DMonQuery(SimpleDMonQuery(tc_descriptor))
	rest_jsonquery = dmon_query.translate({'nodes':ConverterNodesItem}) 	

	#set the variable specifying the json file including the description of the regural expression used to parse the logs
	rexp_file = dmon_query.getRegExp(VisitorClassSigmaSpoutRate())

	print 'Regexp definition at:', rexp_file

	#query D-mon to get 
	#- topology logs descriptor 
	#- logs

	connector = None
	logdescriptor = None

	try:
		if (dmon_ip == 'nodmon'):
			connector = LocalConnector('')

		elif (dmon_ip == 'local'):
			connector = LocalDMonConnector('http://127.0.0.1:5001')

		elif (dmon_ip == 'remote'):
			connector = RemoteDmonConnector(dmon_ip)

		else:
			connector = LocalDMonConnector(dmon_ip+':'+dmon_port)
	
		logsdescriptor = connector.restGet(rest_jsonquery, rexp_file)
	except Exception, err_msg:
		print "Error while calling DMon: %s" % str(err_msg)
		logging.error('Error while calling Dmon: %s', str(err_msg))
		return -1

	logging.basicConfig(filename='merge.log', filemode='w', level=logging.DEBUG)

	result = []

	if (logsdescriptor):
		#merge logs to run TC
		merger = Merger(logsdescriptor)	

		log_files = connector.getlogfiles(withdir=True)

		merger.merge(log_files, rexp_file)
		
		tc_instances = TCRunner(dmon_query.getTCInstance(VisitorDescriptorInstance()))
		for i in tc_instances:
			try:
				i.run()	
				if (i.getResult()):
					result.append(i.getResult())
				else:
					return -1
			except Exception, err_msg:
				print "Error while calling the Trace Checker: %s" % str(err_msg)
				logging.error('Error while calling the Trace Checker: %s', str(err_msg))
				return -1


	else:
		print "An error occurred when I tried to contact DMon and no info on the topology were correctly retrieved. The Get call on DMon failed..."
		logging.error('An error occurred when I tried to contact DMon and no info on the topology were correctly retrieved. The Get call on DMon failed...')
		return -1

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
	


