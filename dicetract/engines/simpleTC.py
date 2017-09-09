#!/usr/bin/python

import sys
import abc
import re
import logging
from formulaHandler.formula import *
import os




class SimpleTC():
	
	def __init__(self):
		pass	


	def run(self, nodename, formula):

		# Time window
		K = int(formula.getValues()[0])

		#TODO: this functionality should be implemented in a bigdata manner
		window_endpoint = K*1000
		n_receive = 0
		n_emit = 0

		logfile = os.path.join("./merge", nodename+".his")

		try:
			with open(logfile, 'r') as ff:	

				filesize = os.path.getsize(logfile)

				line = ff.readline().strip('\n')
				rexp = re.match(r'(\d*) ; .*', line, re.I)

				# Extract the first timestamp of the log into t_init
				t_init = rexp.group(1)
				t = int(t_init)

				# Keep reading the log file until you find a timestamp greater than the t_init+window_endpoint
				while (line != '') and (t < int(t_init)+window_endpoint):
					contentread = len(line)
					#print "\r"+ str(float(contentread)/int(filesize)*100) + "%"
					#sys.stdout.write("\r step Processing file ... %d" % float(contentread)/int(filesize))

					rexp = re.match(r'(\d*) ; (?:(thread_(?:\d*)_received)|(thread_(?:\d*)_emitting))', line, re.I)

					t = int(rexp.group(1))

					# Count +1 receive if needed
					if (rexp.group(2) != None): 
						n_receive = n_receive + 1
					elif (rexp.group(3) != None): 
						n_emit = n_emit + 1
					else:
						raise Exception("Unexpected events in history file" + logfile)

					line = ff.readline().strip('\n')	
					t_last = rexp.group(1)
							
		except Exception, error:
			print 'Error while reading history file - ' + str(error)
			raise Exception("Error while reading history file - " + str(error))


		if (not os.path.exists("./results")):
			os.makedirs("./results")

		with open(os.path.join("./results", formula.typename()+nodename+".res"), 'w') as ff:
			if (type(formula) is SigmaQuantitative):
				ff.write(str(float(n_receive)/n_emit))

			elif (type(formula) is SpoutRateQuantitative):
				if ((int(t_last)-int(t_init))<window_endpoint):
					ff.write(str(1000*float(n_emit)/(int(t_last)-int(t_init))))
				else:
					ff.write(str(1000*float(n_emit)/window_endpoint))

			else:
				raise Exception("Simple Trace Checker was called with an unknown parameter")









