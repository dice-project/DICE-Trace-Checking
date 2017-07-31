import abc
import logging
import os
from subprocess import call


# DICE-TraCT definitions and classes 
# ----------------------------------
from engines.simpleTC import SimpleTC 
from formulaHandler.formula import SigmaCountEQ, SigmaCountLT, SigmaCountGT, SigmaAverageEQ, SigmaAverageLT, SigmaAverageGT, SpoutRateCountEQ, SpoutRateCountLT, SpoutRateCountGT, SpoutRateAverageEQ, SpoutRateAverageLT, SpoutRateAverageGT, SimpleTCWrapper, SigmaQuantitative, SpoutRateQuantitative
# ----------------------------------



class TCSolver():
	__metaclass__ = abc.ABCMeta
	
	def __init__(self):
		self.successor = None
	
	@abc.abstractmethod
	def canProcess(self, formula):
		pass

	@abc.abstractmethod
	def run(self):
		pass

	@abc.abstractmethod
	def getResult(self):
		pass

	@abc.abstractmethod
	def setTCInstance(self, node, formula):
		pass

	def setSuccessor(self, successor):
		self.successor = successor

	def getRunnableTCInstance(self, node, formula):
		if (self.canProcess(formula)):
			self.setTCInstance(node, formula)
			return self
		else:
			return self.successor.getRunnableTCInstance(node, formula)


class SparkTCSolver( TCSolver ):
	
	def __init__(self, nodename=None, formula=None):
		self.__nodename = nodename
		self.__formula = formula

	def setTCInstance(self, nodename, formula):
		self.__nodename = nodename
		self.__formula = formula


	def canProcess(self, formula):
		if ( (type(formula) is SigmaCountEQ) or
			  (type(formula) is SigmaCountGT) or
			  (type(formula) is SigmaCountLT) or
			  (type(formula) is SigmaAverageEQ) or
			  (type(formula) is SigmaAverageGT) or
			  (type(formula) is SigmaAverageLT) or
			  (type(formula) is SpoutRateCountEQ) or
			  (type(formula) is SpoutRateCountGT) or
			  (type(formula) is SpoutRateCountLT) or
			  (type(formula) is SpoutRateAverageEQ) or
			  (type(formula) is SpoutRateAverageGT) or
			  (type(formula) is SpoutRateAverageLT) ):
			return True
		else:
			return False

	def run(self):

		logging.debug('Reading event list file %s.eve', self.__nodename)
		logging.debug('Class formula %s', self.__formula.__class__)
		#build the formula based on the events in the node events file
		theformula = self.__formula.getFormulaFor('../merge/'+self.__nodename + '.eve', '../merge/'+self.__nodename + '.his')

		if (theformula is None):
			logging.error('Formula definition went wrong!')
			raise Exception("No formula created")
	
		logging.info('Event list file read properly')

		logging.debug('Writing formula based on events in %s', self.__nodename + '.eve')
		logging.debug('Formula: %s', theformula)
		#write the formula file
		with open('../results/' + self.__formula.typename() + self.__nodename + '.sol', 'w') as fo:
			fo.write(theformula)
		logging.info('Formula written on %s', self.__nodename + '.sol')

#spark-submit --class it.polimi.krstic.MTLMapReduce.SparkHistoryCheck --master spark://localhost:7077 --executor-memory 4g --executor-cores 2 --num-executors 1 ../mtlmapreduce/target/MTLMapReduce-0.0.1-SNAPSHOT-jar-with-dependencies.jar ../mtlmapreduce/examples/traces/trace1 ../mtlmapreduce/examples/formulae/G/P01 outzazaza --reader spark -d


		print '| ******** Run TC on ' + self.__nodename + ' ********* |'
		print '| History file: ' + './results/'+self.__nodename+'.his'
		print '| Formula file: ' + './results/'+self.__formula.typename()+self.__nodename+'.sol'
		print '| **************************************************** |\n'
		
		logging.debug('Calling external TC engine')
		call (['../spark-submit', '--class', 'it.polimi.krstic.MTLMapReduce.SparkHistoryCheck', '--master', 'spark://lap-bersani:7077',  '--executor-memory', '4g', '--executor-cores', '2', '--num-executors', '1', './MTLMapReduce.jar', './merge/'+self.__nodename+'.his', './results/' + self.__formula.typename()+self.__nodename+'.sol', './results/' + self.__formula.typename()+self.__nodename+'.res', '--reader', 'spark', '-l'])
		logging.info('Finished Trace Checking task')


	
	def getResult(self):
		r_json = {"node": None, "result": None}
		r = ""
		try:
			with open('./results/' + self.__formula.typename() + self.__nodename + '.res-f1', 'r') as fo:
				r = fo.readline()
		except IOError, err:
			print 'Uuppps...Spark apparently did not work properly'
			logging.error('Error while opening result file produced by Spark Trace Checker: %s', str(err))
			return None

		r_json["node"] = self.__nodename
		r_json["result"] = r

		return r_json


class SimpleTCSolver( TCSolver ):

	def __init__(self, nodename=None, formula=None):
		self.__nodename = nodename
		self.__formula = formula
		self.tc = SimpleTC()


	def setTCInstance(self, nodename, formula):
		self.__nodename = nodename
		self.__formula = formula


	def canProcess(self, formula):
		if ( (type(formula) is SigmaQuantitative) or
			  (type(formula) is SpoutRateQuantitative) ):
			return True
		else:
			return False


	def run(self):
		self.tc.run(self.__nodename, self.__formula)

	def getResult(self):
		r_json = {"node": None, "result": None}
		r = ""
		try:
			with open('./results/' + self.__formula.typename() + self.__nodename + '.res', 'r') as fo:
				r = fo.readline()
		except IOError, err:
			print 'Uuppps...Simple Trace Checker apparently did not work properly'
			logging.error('Error while opening result file produced by Simple Trace Checker: %s', str(err))
			return None

		r_json["node"] = self.__nodename
		r_json["result"] = r

		return r_json





