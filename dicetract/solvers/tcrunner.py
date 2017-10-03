import logging


# DICE-TraCT definitions and classes 
# ----------------------------------
from tcsolvers import SimpleTCSolver, SparkTCSolver
from formulaHandler.formula import SigmaCountEQ, SigmaCountLT, SigmaCountGT, SigmaAverageEQ, SigmaAverageLT, SigmaAverageGT, SpoutRateCountEQ, SpoutRateCountLT, SpoutRateCountGT, SpoutRateAverageEQ, SpoutRateAverageLT, SpoutRateAverageGT, SimpleTCWrapper
# ----------------------------------

class TCRunner():
		
	template_file_sigma_averge = './templates/sigma_average.tmp'
	template_file_sigma_counting = './templates/sigma_counting.tmp'	
	template_file_spoutrate_average = './templates/spoutrate_average.tmp'
	template_file_spoutrate_counting = './templates/spoutrate_counting.tmp'		

	
	def __init__(self, tc_descriptor):
		self.__descriptor = tc_descriptor
		self.__i = 0
		self.__nodes = self.__descriptor['nodes']

		# Declaration of Chain-Of-Responsibility handlers
		self.sparkTC = SparkTCSolver()
		self.simpleTC = SimpleTCSolver()
	
		self.sparkTC.setSuccessor(self.simpleTC)


	def __iter__(self):
		return self

	def next(self):
		 #logging.info('Trace Checking task activated %s', self.__nodes[self.__i])

		if (self.__i < len(self.__nodes)):
			node = self.__nodes[self.__i]

			formula = None
			flag = True

			if (node['parameter'] == 'sigma' and node['type'] == 'bolt'):
				# check that the design value of sigma is equal to the real value of sigma

				if (node['method'] == 'average'):

					#create the list of values to build the formula
					values = [ node['timewindow'], node['timesubwindow'], node['designvalue'] ]

					if (node['relation'] == '='):
						#define the formula
						formula = SigmaAverageEQ(self.template_file_sigma_average, values)
					elif (node['relation'] == '<'):
						#define the formula
						formula = SigmaAverageLT(self.template_file_sigma_average, values)
					elif (node['relation'] == '>'):
						#define the formula
						formula = SigmaAverageGT(self.template_file_sigma_average, values)
					else: 
						raise Exception("No runnable TC instance created. Possible cause: parameter name is not correct and properly defined")
				elif (node['method'] == 'counting'):			
	
					#create the list of values to build the formula
					values = [ node['timewindow'], node['designvalue'] ]

					if (node['relation'] == '='):
						#define the formula
						formula = SigmaCountEQ(self.template_file_sigma_counting, values)
					elif (node['relation'] == '<'):
						#define the formula
						formula = SigmaCountLT(self.template_file_sigma_counting, values)
					elif (node['relation'] == '>'):
						#define the formula
						formula = SigmaCountGT(self.template_file_sigma_counting, values)
					else: 
						raise Exception("No runnable TC instance created. Possible cause: parameter name is not correct and properly defined")
				else:
					raise Exception("No runnable TC instance created. Possible cause: parameter name is not correct and properly defined")


			elif (node['parameter'] == 'spoutrate' and node['type'] == 'spout'):
				if (node['method'] == 'average'):
					
					#create the list of values to build the formula
					values = [ node['timewindow'], node['timesubwindow'], node['designvalue'] ]

					if (node['relation'] == '='):
						#define the formula
						formula = SpoutRateAverageEQ(self.template_file_spoutrate_average, values)
					elif (node['relation'] == '<'):
						#define the formula
						formula = SpoutRateAverageLT(self.template_file_spoutrate_average, values)
					elif (node['relation'] == '>'):
						#define the formula
						formula = SpoutRateAverageGT(self.template_file_spoutrate_average, values)
					else: 
						raise Exception("No runnable TC instance created. Possible cause: parameter name is not correct and properly defined")
				elif (node['method'] == 'counting'):		
	
					#create the list of values to build the formula
					values = [ node['timewindow'], node['designvalue'] ]

					if (node['relation'] == '='):
						#define the formula
						formula = SpoutRateCountEQ(self.template_file_spoutrate_counting, values)
					elif (node['relation'] == '<'):
						#define the formula
						formula = SpoutRateCountLT(self.template_file_spoutrate_counting, values)
					elif (node['relation'] == '>'):
						#define the formula
						formula = SpoutRateCountGT(self.template_file_spoutrate_counting, values)
					else: 
						raise Exception("No runnable TC instance created. Possible cause: parameter name is not correct and properly defined")
				else:
					raise Exception("No runnable TC instance created. Possible cause: parameter name is not correct and properly defined")

			else:
				print 'Unknown parameter type: ' + node['parameter'] 
				flag = False
	
			self.__i = self.__i + 1

			logging.debug("Created formula %s", formula)
			
	
			# Wrapping formula object into a SigmaQuantitative or SpoutRateQuantitative
			# This is for bypassing Spark TC until bugs are fixed
			formula = SimpleTCWrapper.transform(formula)
			logging.debug("Created a wrapper formula %s", formula)

			logging.debug("Ready to create a RunnableTCInstance...")
			# return a RunnableTCInstance specifying the node and the formula
			if (flag):
				return self.sparkTC.getRunnableTCInstance(node['name'], formula)
			else:
				raise Exception("No runnable TC instance created. An error occurred when the formula to be analyzed was being created.")
		else:
			raise StopIteration()

