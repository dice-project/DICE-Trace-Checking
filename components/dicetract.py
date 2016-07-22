#!/usr/bin/python

import sys
import os
import logging
import json
import abc
from merge3 import Merger
from formula import SigmaEQ, IdleTimeEQ
from subprocess import call

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
			s = s + [{'name':e['name']}] + [{'timewindow':e['timewindow']}]
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


class RunnableTCInstance():
	
	def __init__(self, nodename, formula):
		self.__nodename = nodename
		self.__formula = formula

	def run(self):

		#build the formula based on the events in the node events file
		theformula = self.__formula.getFormulaFor('./merge/'+self.__nodename + '.eve')

		#write the formula file
		with open(self.__formula.typename() + self.__nodename + '.sol', 'w') as fo:
			fo.write(theformula)

#spark-submit --class it.polimi.krstic.MTLMapReduce.SparkHistoryCheck --master spark://localhost:7077 --executor-memory 4g --executor-cores 2 --num-executors 1 ../mtlmapreduce/target/MTLMapReduce-0.0.1-SNAPSHOT-jar-with-dependencies.jar ../mtlmapreduce/examples/traces/trace1 ../mtlmapreduce/examples/formulae/G/P01 outzazaza --reader spark -d


		print '| ******** Run TC on ' + self.__nodename + ' ********* |\n'
		print '| History file: ' + '/home/bersani/Tools/DICE-WP4/dicestractor/merge/'+self.__nodename+'.his'
		print '| Formula file: ' + '/home/bersani/Tools/DICE-WP4/dicestractor/'+self.__formula.typename()+self.__nodename+'.sol'
		print '| **************************************************** |\n'
		
		call (['/home/bersani/Tools/spark-1.6.2-bin-hadoop2.6/bin/spark-submit', '--class', 'it.polimi.krstic.MTLMapReduce.SparkHistoryCheck', '--master', 'spark://localhost:7077',  '--executor-memory', '4g', '--executor-cores', '2', '--num-executors', '1', '/home/bersani/Tools/mtlmapreduce/target/MTLMapReduce-0.0.1-SNAPSHOT-jar-with-dependencies.jar', '/home/bersani/Tools/DICE-WP4/dicestractor/merge/'+self.__nodename+'.his', '/home/bersani/Tools/DICE-WP4/dicestractor/'+self.__formula.typename()+self.__nodename+'.sol', self.__formula.typename()+self.__nodename+'.res', '--reader', 'spark', '-l'])



class TCRunner():
		
	template_file_sigma = './templates/sigma_1.tmp'
	template_file_idletime = './templates/idleTime.tmp'
	
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

			if (node['parameter'] == 'sigma' and node['type'] == 'bolt'):
				# check that the design value of sigma is equal to the real value of sigma
				if (node['relation'] == '='):
					#create the list of values to build the formula
					values = [ node['timewindow'], node['designvalue']*node['inputrate']*node['timewindow'], node['timewindow'], node['inputrate']*node['timewindow'] ]

					#define the formula
					formula = SigmaEQ(self.template_file_sigma, values)
				elif (node['relation'] == '<'):
					pass
				elif (node['relation'] == '>'):
					pass
				else: 
					pass

			elif (node['parameter'] == 'idleTime' and node['type'] == 'spout'):
				values = [ node['min'], node['max'] ]
				formula = IdleTimeEQ(self.template_file_idletime, values)

			else:
				print 'Unknown parameter type: ' + node['parameter'] 
	
			self.__i = self.__i + 1
		
			# retun a RunnableTCInstance specifying the node and the formula
			return RunnableTCInstance(node['name'], formula)
		else:
			raise StopIteration()




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
	connector = LocalConnector('./topologylog.json')
	logsdescriptor = connector.restGet(rest_jsonquery)

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
	


