#!/usr/bin/python

import sys
import abc
import re
import logging




class Formula():
	__metaclass__ = abc.ABCMeta
	
	def __init__(self, template_filename, values, values_filename=''):
		#read the template and the values to apply on the template formula

		logging.debug('Formula constructor on %s', template_filename)

		self.__ok = True

		self._template = ''

		if (template_filename != ''):
			try:
				#read the template string from the file
				with open(template_filename, 'r') as fp:
					self._template = fp.readline().strip('\n')
			except Exception, error:
				print 'Error while reading file ' + template_filename + ' ' + str(error)
				self.__ok = False
			
		self._values = []
		if (values_filename != ''):
			try:
				with open(values_filename, 'r') as ff:	
					v = ff.readline().strip('\n')
					while (v != ''):
						self._values.append(v)
						v = ff.readline().strip('\n')
			except Exception, error:
				print 'Error while reading file ' + values_filename
				self.__ok = False
		else:
			self._values = values
	
	def getValues(self):
		return self._values


	#Factory method to get a formula based on (file name of) an event list 
	@abc.abstractmethod
	def _createFormula(self):
		pass
	
	#getter
	def getFormulaFor(self, events_filename, history_filename):
		if (self.__ok):
			return self._createFormula(events_filename, history_filename)


	def typename(self):
		return self.__class__.__name__



class SigmaCounting( Formula ):
	__metaclass__ = abc.ABCMeta

		
	@abc.abstractmethod
	def _relationtype(self):
		pass

	#definition of factory
	def _createFormula(self, events_filename, history_filename):

		# Time window
		K = int(self._values[0])
		sigma_design = float(self._values[1])

		# the events file contains all the events recorded in the associated log. However, for some threads the emit event might not have a correspondent receive event.
		# Therefore, we have to create a formula to determine sigma only for those threads having both the events.

		# Split the events stored in the file into two lists.
		emit_events = []
		receive_events = []
		try:
			with open(events_filename, 'r') as ff:	
				line = ff.readline().strip('\n')
				while (line != ''):
					rexp = re.match(r'thread_(\d)*_(emitting|received).*', line, re.I)
					if (rexp.group(2) == 'emitting' and (line not in emit_events)):
						emit_events.append(line)
					elif (rexp.group(2) == 'received' and (line not in receive_events)):
						receive_events.append(line)
					line = ff.readline().strip('\n')
		except Exception, error:
			print 'Error while reading events file - ' + str(error)
			return None

		# Count the receive events to set the formula constants properly
		# In self.values, the timewindow parameter defines the window of inspection. Count the number of receive events in that period of time.
		# We assume that windows of analysis are of the form [0,endpoint] from 0

		#window_endpoint is a value expressed in milliseconds


		#TODO: this functionality should be implemented in a bigdata manner
		window_endpoint = K*1000
		n_receive = 0
		try:
			with open(history_filename, 'r') as ff:	
				line = ff.readline().strip('\n')
				rexp = re.match(r'(\d*) ; .*', line, re.I)

				# Extract the first timestamp of the log into t_init
				t_init = rexp.group(1)
				t = int(t_init)

				# Keep reading the log file until you find a timestamp greater than the t_init+window_endpoint
				while (line != '') and (t < int(t_init)+window_endpoint):
					rexp = re.match(r'(\d*) ; (?:(thread_(?:\d*)_received)|.*)', line, re.I)

					t = int(rexp.group(1))

					# Count +1 receive if needed
					if (rexp.group(2) != None): 
						n_receive = n_receive + 1

					line = ff.readline().strip('\n')
					
					
		except Exception, error:
			print 'Error while reading history file - ' + str(error)

		formula = None
		
		# Create the formula as conjunction of formulae build based on the template.
		# A subformula is emitted only for threads having both emit and receive event appearing in events file
		for e in emit_events:
			templ = self._template
			if (e.replace("emitting", "received") in receive_events):
				templ = templ.replace('$interval_e', '[0,' + str(window_endpoint) + ']', 1)
				templ = templ.replace('$interval_r', '[0,' + str(window_endpoint) + ']', 1)

				templ = templ.replace('$value_e', str(int(sigma_design * n_receive)), 1)	
				templ = templ.replace('$value_r', str(n_receive), 1)

				#replace	both $relation_x with =
				templ = templ.replace('$relation_e', self._relationtype(), 1)
				templ = templ.replace('$relation_r', self._relationtype_receive(), 1)

				#replace 'emit' and 'receive' with the suitable name
				templ = templ.replace('emit', e, 1)
				templ = templ.replace('receive', e.replace("emitting", "received"), 1)

				if (formula == None):
					formula = templ
				else:
					formula = formula + ' & ' + templ

		return formula


	def _relationtype_receive(self):
		return "="


class SigmaCountEQ( SigmaCounting ):

	def _relationtype(self):
		return "="


class SigmaCountLT( SigmaCounting ):

	def _relationtype(self):
		return ">"


class SigmaCountGT( SigmaCounting ):

	def _relationtype(self):
		return "<"



class SigmaAverage( Formula ):
	__metaclass__ = abc.ABCMeta

		
	@abc.abstractmethod
	def _relationtype(self):
		pass

	#definition of factory
	def _createFormula(self, events_filename, history_filename):

		# Time window
		K = int(self._values[0])
		# Subwindow
		h = int(self._values[1])
		sigma_design = float(self._values[2])

		# the events file contains all the events recorded in the associated log. However, for some threads the emit event might not have a correspondent receive event.
		# Therefore, we have to create a formula to determine sigma only for those threads having both the events.

		# Split the events stored in the file into two lists.
		emit_events = []
		receive_events = []
		try:
			with open(events_filename, 'r') as ff:	
				line = ff.readline().strip('\n')
				while (line != ''):
					rexp = re.match(r'thread_(\d)*_(emitting|received).*', line, re.I)
					if (rexp.group(2) == 'emitting' and (line not in emit_events)):
						emit_events.append(line)
					elif (rexp.group(2) == 'received' and (line not in receive_events)):
						receive_events.append(line)
					line = ff.readline().strip('\n')
		except Exception, error:
			print 'Error while reading events file - ' + str(error)
			return None


		# Count the receive events to set the formula constants properly
		# In self.values, the timewindow parameter defines the window of inspection. Count the number of receive events in that period of time.
		# We assume that windows of analysis are of the form [0,endpoint] from 0

		#window_endpoint is a value expressed in milliseconds


		#TODO: this functionality should be implemented in a bigdata manner
		window_endpoint = K*1000
		n_receive = 0
		try:
			with open(history_filename, 'r') as ff:	
				line = ff.readline().strip('\n')
				rexp = re.match(r'(\d*) ; .*', line, re.I)

				# Extract the first timestamp of the log into t_init
				t_init = rexp.group(1)
				t = int(t_init)

				# Keep reading the log file until you find a timestamp greater than the t_init+window_endpoint
				while (line != '') and (t < int(t_init)+window_endpoint):
					rexp = re.match(r'(\d*) ; (?:(thread_(?:\d*)_received)|.*)', line, re.I)

					t = int(rexp.group(1))

					# Count +1 receive if needed
					if (rexp.group(2) != None): 
						n_receive = n_receive + 1

					line = ff.readline().strip('\n')
					
					
		except Exception, error:
			print 'Error while reading history file - ' + str(error)

		formula = None
		
		# Create the formula as conjunction of formulae build based on the template.
		# A subformula is emitted only for threads having both emit and receive event appearing in events file
		for e in emit_events:
			templ = self._template
			if (e in receive_events):
				templ = templ.replace('$interval_e', '[0,' + str(window_endpoint) + ']', 1)
				templ = templ.replace('$interval_r', '[0,' + str(window_endpoint) + ']', 1)

				templ = templ.replace('$subinterval_e', '[0,' + str(h) + ']', 1)
				templ = templ.replace('$subinterval_r', '[0,' + str(h) + ']', 1)

				templ = templ.replace('$value_e', str(int(sigma_design * n_receive)), 1)	
				templ = templ.replace('$value_r', str(n_receive), 1)

				#replace	both $relation_x with =
				templ = templ.replace('$relation_e', self._relationtype(), 1)
				templ = templ.replace('$relation_r', self._relationtype_receive(), 1)

				#replace 'emit' and 'receive' with the suitable name
				templ = templ.replace('emit', 'thread_'+e+'_emit',1)
				templ = templ.replace('receive', 'thread_'+e+'_replace',1)

				if (formula == None):
					formula = templ
				else:
					formula = formula + ' & ' + templ

		return formula


	def _relationtype_receive(self):
		return "="


class SigmaAverageEQ( SigmaAverage ):

	def _relationtype(self):
		return "="


class SigmaAverageLT( SigmaAverage ):

	def _relationtype(self):
		return ">"


class SigmaAverageGT( SigmaAverage ):

	def _relationtype(self):
		return "<"



class SpoutRateCounting( Formula ):
		
	#definition of factory
	def _createFormula(self, events_filename, history_filename):

		# Time window
		K = int(self._values[0])
		spoutrate_design = float(self._values[1])

		logging.debug('Spout Rate formula: reading event file to produce Soloist formula')
		# Split the events stored in the file into two lists.
		emit_events = []
		try:
			with open(events_filename, 'r') as ff:	
				line = ff.readline().strip('\n')
				while (line != ''):
					emit_events.append(line)
					line = ff.readline().strip('\n')
		except Exception, error:
			print 'Error while reading events file: ' + str(error)
			return None

		logging.info('Spout Rate formula: event file read correctly - %s', str(emit_events))

		window_endpoint = K*1000

		formula = None
		
		# Create the formula as conjunction of formulae build based on the template.
		# A subformula is emitted only for threads having both emit and receive event appearing in events file
		for e in emit_events:
			templ = self._template
			templ = templ.replace('$interval', '[0,' + str(window_endpoint) + ']', 1)

			templ = templ.replace('$value', str(int(spoutrate_design * window_endpoint)), 1)	

			#replace	both $relation_x with =
			templ = templ.replace('$relation', self._relationtype(), 1)

			#replace 'emit' and 'receive' with the suitable name
			templ = templ.replace('emit', e, 1)

			if (formula == None):
				formula = templ
			else:
				formula = formula + ' & ' + templ

		logging.info('Defined spout Rate formula: %s', formula)

		return formula

class SpoutRateCountEQ( SpoutRateCounting ):

	def _relationtype(self):
		return "="


class SpoutRateCountLT( SpoutRateCounting ):

	def _relationtype(self):
		return "<"


class SpoutRateCountGT( SpoutRateCounting ):

	def _relationtype(self):
		return ">"



class SpoutRateAverage( Formula ):
		
	#definition of factory
	def _createFormula(self, events_filename, history_filename):

		# Time window
		K = int(self._values[0])
		# Subwindow
		h = int(self._values[1])
		spoutrate_design = float(self._values[2])

		logging.debug('Spout Rate formula: reading event file to produce Soloist formula')
		# Split the events stored in the file into two lists.
		emit_events = []
		try:
			with open(events_filename, 'r') as ff:	
				line = ff.readline().strip('\n')
				while (line != ''):
					emit_events.append(line)
					line = ff.readline().strip('\n')
		except IOError, error:
			print 'Error reading events file: ' + str(error)
			return None

		logging.info('Spout Rate formula: event file read correctly - %s', str(emit_events))

		window_endpoint = K*1000

		formula = None
		
		# Create the formula as conjunction of formulae build based on the template.
		# A subformula is emitted only for threads having both emit and receive event appearing in events file
		for e in emit_events:
			templ = self._template
			templ = templ.replace('$interval', '[0,' + str(window_endpoint) + ']', 1)

			templ = templ.replace('$value', str(int(spoutrate_design * window_endpoint)), 1)	

			templ = templ.replace('$subinterval', '[0,' + str(h) + ']', 1)

			#replace	both $relation_x with =
			templ = templ.replace('$relation', self._relationtype(), 1)

			#replace 'emit' and 'receive' with the suitable name
			templ = templ.replace('emit', e, 1)

			if (formula == None):
				formula = templ
			else:
				formula = formula + ' & ' + templ

		return formula

class SpoutRateAverageEQ( SpoutRateAverage ):

	def _relationtype(self):
		return "="


class SpoutRateAverageLT( SpoutRateAverage ):

	def _relationtype(self):
		return "<"


class SpoutRateAverageGT( SpoutRateAverage ):

	def _relationtype(self):
		return ">"


class SigmaQuantitative( Formula ):

	def __init__(self, template, values):
		Formula.__init__(self, "", values)

	#definition of factory
	def _createFormula(self, events_filename, history_filename):
		pass

class SpoutRateQuantitative( Formula ):

	def __init__(self, template, values):
		Formula.__init__(self, "", values)

	#definition of factory
	def _createFormula(self, events_filename, history_filename):
		pass


class SimpleTCWrapper():

	@staticmethod
	def transform(formula):
		if ( (type(formula) is SigmaCountEQ) or
			  (type(formula) is SigmaCountGT) or
			  (type(formula) is SigmaCountLT) or
			  (type(formula) is SigmaAverageEQ) or
			  (type(formula) is SigmaAverageGT) or
			  (type(formula) is SigmaAverageLT) ):
			return SigmaQuantitative("", formula.getValues())
		elif( (type(formula) is SpoutRateCountEQ) or
			   (type(formula) is SpoutRateCountGT) or
			   (type(formula) is SpoutRateCountLT) or
			   (type(formula) is SpoutRateAverageEQ) or
			   (type(formula) is SpoutRateAverageGT) or
			   (type(formula) is SpoutRateAverageLT) ):
			return SpoutRateQuantitative("", formula.getValues())





