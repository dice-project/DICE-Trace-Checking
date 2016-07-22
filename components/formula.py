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
	def getFormulaFor(self, events_filename):
		if (self.__ok):
			return self._createFormula(events_filename)

	@abc.abstractmethod
	def typename(self):
		pass



class SigmaEQ( Formula ):
	
	def typename(self):
		return 'sigma_eq'
		
	#definition of factory
	def _createFormula(self, events_filename):

		# the events file contains all the events recorded in the associated log. However, for some threads the emit event might not have a correspondent receive event.
		# Therefore, we have to create a formula to determine sigma only for those threads having both the events.

		# Split the events stored in the file into two lists.
		emit_events = []
		receive_events = []
		try:
			with open(events_filename, 'r') as ff:	
				line = ff.readline().strip('\n')
				while (line != ''):
					rexp = re.match(r'thread_(\d)*_(emit|receive)_.*', line, re.I)
					if (rexp.group(2) == 'emit' and rexp.group(1) not in emit_events):
						emit_events.append(rexp.group(1))
					elif (rexp.group(2) == 'receive' and rexp.group(1) not in receive_events):
						receive_events.append(rexp.group(1))
					line = ff.readline().strip('\n')
		except Exception, error:
			print 'Error reading events file' + str(error)

		formula = ''
		
		# Create the formula as conjunction of formulae build based on the template.
		# A subformula is emitted only for threads having both emit and receive event appearing in events file
		for e in emit_events:
			templ = self._template
			if (e in receive_events):
				for v in self._values:
					templ = templ.replace('$n', str(v), 1)

				#replace	both $o with =
				templ = templ.replace('$o', '=', 2)
				
				#replace 'emit' and 'receive' with the suitable name
				templ = templ.replace('emit', 'thread_'+e+'_emit',1)
				templ = templ.replace('receive', 'thread_'+e+'_replace',1)

				if (formula == ''):
					formula = templ
				else:
					formula = formula + ' & ' + templ

		return formula



class IdleTimeEQ( Formula ):
	
	def typename(self):
		return 'idleTime'
		
	#definition of factory
	def _createFormula(self, events_filename):

		# the events file contains all the events recorded in the associated log. However, for some threads the emit event might not have a correspondent receive event.
		# Therefore, we have to create a formula to determine sigma only for those threads having both the events.

		# Split the events stored in the file into two lists.
		emit_events = []
		receive_events = []
		try:
			print events_filename
			with open(events_filename, 'r') as ff:	
				line = ff.readline().strip('\n')
				while (line != ''):
					rexp = re.match(r'thread_(\d)*_emit', line, re.I)
					if (rexp.group(1) not in emit_events):
						emit_events.append(rexp.group(1))
					line = ff.readline().strip('\n')
		except Exception, error:
			print 'Error reading events file: ' + str(error)
		
		formula = ''
		
		# Create the formula as conjunction of formulae build based on the template.
		# A subformula is emitted only for threads having both emit and receive event appearing in events file
		for e in emit_events:
			templ = self._template
			#replace 'emit' and 'receive' with the suitable name
			templ = templ.replace('emit', 'thread_'+e+'_emit',3)
			
			templ = templ.replace('$a', str(self._values[0]), 1)
			templ = templ.replace('$b', str(self._values[1]), 1)

			if (formula == ''):
				formula = templ
			else:
				formula = '( ' + formula + ' & ' + templ + ' )'
				#formula = '('+ templ + ')'

		return formula





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




