#!/usr/bin/python

import sys
import abc
import re
import logging




class Formula():
	__metaclass__ = abc.ABCMeta
	
	def __init__(self, template_filename, values, values_filename=''):
		#read the template and the values to apply on the template formula

		self.__ok = True

		self._template = ''

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
	



	#Factory method to get a formula based on (file name of) an event list 
	@abc.abstractmethod
	def _createFormula(self):
		pass
	
	#getter
	def getFormulaFor(self, events_filename, history_filename):
		if (self.__ok):
			return self._createFormula(events_filename, history_filename)

	@abc.abstractmethod
	def typename(self):
		pass



class SigmaCounting( Formula ):
	__metaclass__ = abc.ABCMeta

		
	@abc.abstractmethod
	def _relationtype(self):
		pass

	#definition of factory
	def _createFormula(self, events_filename, history_filename):

		# Time window
		K = int(self._values[0])
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
					rexp = re.match(r'thread_(\d)*_(emit|receive).*', line, re.I)
					if (rexp.group(2) == 'emit' and rexp.group(1) not in emit_events):
						emit_events.append(rexp.group(1))
					elif (rexp.group(2) == 'receive' and rexp.group(1) not in receive_events):
						receive_events.append(rexp.group(1))
					line = ff.readline().strip('\n')
		except Exception, error:
			print 'Error while reading events file - ' + str(error)


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
				rexp = re.match(r'(\d*) ; thread_(\d*)_(emit|receive).*', line, re.I)

				# Extract the first timestamp of the log into t_init
				t_init = rexp.group(1)
				# Count +1 receive if the first event is receive
				if (rexp.group(3) == 'receive'): 
					n_receive = n_receive + 1

				t = int(t_init)
				# Keep reading the log file until you find a timestamp greater than the t_init+window_endpoint
				line = ff.readline().strip('\n')
				while (line != '') and (t < int(t_init)+window_endpoint):
					rexp = re.match(r'(\d*) ; thread_(\d*)_(emit|receive).*', line, re.I)
					t = int(rexp.group(1))
					# Count +1 receive if needed
					if (rexp.group(3) == 'receive'): 
						n_receive = n_receive + 1
					line = ff.readline().strip('\n')
					
		except Exception, error:
			print 'Error while reading history file - ' + str(error)


		formula = ''
		
		# Create the formula as conjunction of formulae build based on the template.
		# A subformula is emitted only for threads having both emit and receive event appearing in events file
		for e in emit_events:
			templ = self._template
			if (e in receive_events):
				templ = templ.replace('$interval_e', '[0,' + str(window_endpoint) + ']', 1)
				templ = templ.replace('$interval_r', '[0,' + str(window_endpoint) + ']', 1)

				templ = templ.replace('$value_e', str(int(sigma_design * n_receive)), 1)	
				templ = templ.replace('$value_r', str(n_receive), 1)

				#replace	both $relation_x with =
				templ = templ.replace('$relation_e', self._relationtype(), 1)
				templ = templ.replace('$relation_r', self._relationtype_receive(), 1)

				#replace 'emit' and 'receive' with the suitable name
				templ = templ.replace('emit', 'thread_'+e+'_emit',1)
				templ = templ.replace('receive', 'thread_'+e+'_replace',1)

				if (formula == ''):
					formula = templ
				else:
					formula = formula + ' & ' + templ

		return formula


	def _relationtype_receive(self):
		return "="


class SigmaCountEQ( SigmaCounting ):

	def typename(self):
		return "sigmaCountEQ"

	def _relationtype(self):
		return "="


class SigmaCountLT( SigmaCounting ):

	def typename(self):
		return "sigmaCountLT"

	def _relationtype(self):
		return ">"


class SigmaCountGT( SigmaCounting ):

	def typename(self):
		return "sigmaCountGT"

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
					rexp = re.match(r'thread_(\d)*_(emit|receive).*', line, re.I)
					if (rexp.group(2) == 'emit' and rexp.group(1) not in emit_events):
						emit_events.append(rexp.group(1))
					elif (rexp.group(2) == 'receive' and rexp.group(1) not in receive_events):
						receive_events.append(rexp.group(1))
					line = ff.readline().strip('\n')
		except Exception, error:
			print 'Error while reading events file - ' + str(error)


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
				rexp = re.match(r'(\d*) ; thread_(\d*)_(emit|receive).*', line, re.I)

				# Extract the first timestamp of the log into t_init
				t_init = rexp.group(1)
				# Count +1 receive if the first event is receive
				if (rexp.group(3) == 'receive'): 
					n_receive = n_receive + 1

				t = int(t_init)
				# Keep reading the log file until you find a timestamp greater than the t_init+window_endpoint
				line = ff.readline().strip('\n')
				while (line != '') and (t < int(t_init)+window_endpoint):
					rexp = re.match(r'(\d*) ; thread_(\d*)_(emit|receive).*', line, re.I)
					t = int(rexp.group(1))
					# Count +1 receive if needed
					if (rexp.group(3) == 'receive'): 
						n_receive = n_receive + 1
					line = ff.readline().strip('\n')
					
		except Exception, error:
			print 'Error while reading history file - ' + str(error)


		formula = ''
		
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

				if (formula == ''):
					formula = templ
				else:
					formula = formula + ' & ' + templ

		return formula


	def _relationtype_receive(self):
		return "="


class SigmaAverageEQ( SigmaCounting ):

	def typename(self):
		return "sigmaAverageEQ"

	def _relationtype(self):
		return "="


class SigmaAverageLT( SigmaCounting ):

	def typename(self):
		return "sigmaAverageLT"

	def _relationtype(self):
		return ">"


class SigmaAverageGT( SigmaCounting ):

	def typename(self):
		return "sigmaAverageGT"

	def _relationtype(self):
		return "<"



class SpoutRateCounting( Formula ):
		
	#definition of factory
	def _createFormula(self, events_filename, history_filename):

		# Time window
		K = int(self._values[0])
		spoutrate_design = float(self._values[2])

		# the events file contains all the events recorded in the associated log. However, for some threads the emit event might not have a correspondent receive event.
		# Therefore, we have to create a formula to determine sigma only for those threads having both the events.

		# Split the events stored in the file into two lists.
		emit_events = []
		receive_events = []
		try:
			with open(events_filename, 'r') as ff:	
				line = ff.readline().strip('\n')
				while (line != ''):
					rexp = re.match(r'thread_((\d)*)_emit', line, re.I)
					if (rexp.group(1) not in emit_events):
						emit_events.append(rexp.group(1))
					line = ff.readline().strip('\n')
		except Exception, error:
			print 'Error reading events file: ' + str(error)

		window_endpoint = K*1000

		formula = ''
		
		# Create the formula as conjunction of formulae build based on the template.
		# A subformula is emitted only for threads having both emit and receive event appearing in events file
		for e in emit_events:
			templ = self._template
			if (e in receive_events):
				templ = templ.replace('$interval', '[0,' + str(window_endpoint) + ']', 1)

				templ = templ.replace('$value', str(int(sigma_design * window_endpoint)), 1)	

				#replace	both $relation_x with =
				templ = templ.replace('$relation', self._relationtype(), 1)

				#replace 'emit' and 'receive' with the suitable name
				templ = templ.replace('emit', 'thread_'+e+'_emit',1)

				if (formula == ''):
					formula = templ
				else:
					formula = formula + ' & ' + templ

		return formula

class SpoutRateCountEQ( SpoutRateCounting ):

	def typename(self):
		return "spoutRateCountEQ"

	def _relationtype(self):
		return "="


class SpoutRateCountLT( SigmaCounting ):

	def typename(self):
		return "spoutRateCountLT"

	def _relationtype(self):
		return "<"


class SpoutRateCountGT( SigmaCounting ):

	def typename(self):
		return "spoutRateCountGT"

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

		# the events file contains all the events recorded in the associated log. However, for some threads the emit event might not have a correspondent receive event.
		# Therefore, we have to create a formula to determine sigma only for those threads having both the events.

		# Split the events stored in the file into two lists.
		emit_events = []
		receive_events = []
		try:
			with open(events_filename, 'r') as ff:	
				line = ff.readline().strip('\n')
				while (line != ''):
					rexp = re.match(r'thread_((\d)*)_emit', line, re.I)
					if (rexp.group(1) not in emit_events):
						emit_events.append(rexp.group(1))
					line = ff.readline().strip('\n')
		except Exception, error:
			print 'Error reading events file: ' + str(error)

		window_endpoint = K*1000

		formula = ''
		
		# Create the formula as conjunction of formulae build based on the template.
		# A subformula is emitted only for threads having both emit and receive event appearing in events file
		for e in emit_events:
			templ = self._template
			if (e in receive_events):
				templ = templ.replace('$interval', '[0,' + str(window_endpoint) + ']', 1)

				templ = templ.replace('$value', str(int(sigma_design * window_endpoint)), 1)	

				templ = templ.replace('$subinterval', '[0,' + str(h) + ']', 1)

				#replace	both $relation_x with =
				templ = templ.replace('$relation', self._relationtype(), 1)

				#replace 'emit' and 'receive' with the suitable name
				templ = templ.replace('emit', 'thread_'+e+'_emit',1)

				if (formula == ''):
					formula = templ
				else:
					formula = formula + ' & ' + templ

		return formula

class SpoutRateAverageEQ( SpoutRateCounting ):

	def typename(self):
		return "spoutRateCountEQ"

	def _relationtype(self):
		return "="


class SpoutRateCountLT( SigmaCounting ):

	def typename(self):
		return "spoutRateCountLT"

	def _relationtype(self):
		return "<"


class SpoutRateCountGT( SigmaCounting ):

	def typename(self):
		return "spoutRateCountGT"

	def _relationtype(self):
		return ">"





def main():
	doFormula = True
	#command line check

	try:
		template_file = sys.argv[sys.argv.index("-t")+1]
		values_file = sys.argv[sys.argv.index("-v")+1]
		event_file = sys.argv[sys.argv.index("-f")+1]
	except Exception, error:	
		print 'Wrong command line definition not compliant with formula.py -t [file.tmpl] -v [file.val]'
		doFormula = False

	if (doFormula):
		fml = SigmaEQ(template_file, values_file)
		s = fml.getFormulaFor(event_file)
		with open(template_file.replace('tmpl','mtl'), 'w') as fo:
			fo.write(s)
			

if __name__ == '__main__':
    main()




