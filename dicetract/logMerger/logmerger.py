#!/usr/bin/python

import sys, getopt
import heapq
import re
import logging
import json
import os

inputfile = ''
outputfile = ''



class LabeledQueue():
#Priority queue which returns the priority element only if the queue contains elements labeled with all the labels passed in the constructor.
#An internal dictionary keeps the counting of elements for all the labels

	def __init__(self, elem_labels):
		#queue
		self.__queue = []
		#dictionary to keep the label counting
		self.__labels = {}

		logging.debug('Creating dictionary for labels')
		[self.__labels.update({label:0}) for label in elem_labels]
		logging.debug('Created LabeledQueue for labels '+ str(elem_labels))


	def put(self, key, element, label):
		#add tuple (key,element)
		try:
			heapq.heappush(self.__queue, (key,(element,label)))
		except str_msg:
			print "Error while adding element" + str( (key,(element,label)) ) + " in queue :" + self.__queue
		#update counting
		self.__labels[label] = self.__labels[label] + 1
		logging.debug('Queue put: ' + 'key=' + key + ' value=' + str( (element,label) ))

	def get(self):
		#if all the labels are in the queue then return the smallest one; otherwise return None
		if (0 in self.__labels.values()):
			logging.debug('Some labels are not in the queue')
			return None
			
		#get the smallest key
		el = heapq.heappop(self.__queue)
		logging.debug('Queue get:' + str(el))
		#update counting
		self.__labels[el[1][1]] = self.__labels[el[1][1]] - 1
		return el

	def canGet(self):
		return (0 not in self.__labels.values()) and (len(self.__queue)>0)



class Merger():
    
	def __init__(self, topologylog_descriptor):
		self.__nodes = []
		self.__mergeOK = True
		self.__mapNodes = {}
		self.__queues = {}
		self.__mapWorkers = {}
		self.__history_files = {}
		self.__events = {}
		self.__event_files = {}

		try:

			#now read all the following lines matching the file name abc.log followed by the list of contained nodes
			for d in topologylog_descriptor['logs']:
				self.__nodes = self.__nodes + [str(d['nodename'])]
				self.__mapNodes.update({ d['nodename']: map(lambda x: str(x), d['logs'].split(',')) })

	
			logging.info('Dictionary map completed for nodes')

			logging.debug('Building priority queues dictionary')
			#Create a dictionary of priority queues for each element of the topology (either spout or bolt)
			#Each priority queue is a LabeledQueue with values in _mapNodes
			#We assume that all the nodes defined in the list :spouts and :bolts are tracked in at least one log file
			#[self._queues.update({element: LabeledQueue(self._mapNodes[element])}) for element in spouts+bolts]
			for node in self.__nodes:
				t = ()
				try:
					t = self.__mapNodes[node]
				except Exception:
					logging.warning('Node ' + str(node) + ' not found in ' + topology_file + '. Verify the topology file. Continuing with empty list for ' + str(node))
					print 'Node ' + str(node) + ' not found in ' + topology_file + '. Verify the topology file. Continuing with empty list for ' + str(node)
				e = LabeledQueue(t)
				self.__queues.update({node:e})
				logging.debug('Added labeled Queue ' + str(e) + ' for node ' + str(node))
			logging.debug('Priority queues dictionary' + str(self.__queues))


			#for all the spouts and bolts create a file for the history indexed in the dictionary __history_files
			if not os.path.exists(r'./merge'):
				os.makedirs(r'./merge')
			[self.__history_files.update({node:open('./merge/'+node+'.his', 'w+')}) for node in self.__nodes]
			#for all the spouts and bolts create a file for the events appearing in the logs indexed in the dictionary __event_files
			[self.__event_files.update({node:open('./merge/'+node+'.eve', 'w+')}) for node in self.__nodes]

			logging.info('Node files created for ' + str(self.__nodes))


		except Exception, err_msg:
			print "Error while creating Merger: %s" % str(err_msg)
			self.__mergeOK = False
        

	#getter to check whether merge was ok
	def mergeOK(self):
		return self.__mergeOK

	#merge function
	def merge(self, worker_files, rexp_file):
		try:
			#get the regular expression to parse the logs from re.txt
			with open(rexp_file, 'r') as f:
				rexpjson = json.load(f)

			numberOfGroups = rexpjson['numberOfgroups']
			valuePositions = rexpjson['valuePositions']
			keyPositions = rexpjson['keyPositions']
			nodePosition = rexpjson['nodePosition']
			rexp = rexpjson['regexp']

			logging.debug('Regular expression set on: ' + rexp)

         #open all the log files (workers)
			logFiles = {}
			#[logFiles.update({f.split('/')[-1]: open(f, 'r')}) for f in worker_files]
			[logFiles.update({f: open(f, 'r')}) for f in worker_files]
			logging.info('Log files opened')
			logging.debug(logFiles)

			while (len(worker_files) and self.__mergeOK):
				#for all the collected logs until all get eof 
				for f in worker_files:
					#remove from f the folder and get the file name
					filename = f #.split('/')[-1]
					#folder = f[0:f.find(filename)]
					#read a line
					line = logFiles[filename].readline().strip('\n')
					logging.debug('Raw line from file ' + f + ': ' + line)
		
					#if line from f is EOF then remove the file from the list 
					if (line == ''):
						worker_files.remove(f)
					else:
						#extract from line the timestamp and the node name    
						re_match = re.match(rexp, line, re.I)
						logging.debug(re_match)
						#if (re_match is None):
						#	logging.error('Regular expression extraction failed on line ' + line)
						#	self.__mergeOK = False
						#else:
						if (re_match is not None):
							logging.debug(re_match.groups())
							#extract the key
							key = ''
							for e in keyPositions: 
								key = key + re_match.group(int(e))

							#extract the value
							value = ''
							for e in valuePositions: 
								if (value != '' and (re_match.group(int(e)) is not None)):
									value = value + '_'
								if (re_match.group(int(e)) is not None):
									value = value + re_match.group(int(e)).lower()
							logging.debug('Extracted data from log: ' + f + ' key: ' + key + ' value: ' + value)
			
							value = value.replace('-', '_')

							nodeName = re_match.group(nodePosition)
							#add the entry in the proper queue
							if (self.__queues.has_key(nodeName)): 
								self.__queues[nodeName].put(key,value,filename)
								logging.debug('Pushed ' + key + ', ' + value + ' into ' + nodeName)

							#add the event in the dictionary of events
							if (self.__events.has_key(nodeName)): 
								if (value not in self.__events[nodeName]):
									self.__events[nodeName].append(value)
							else:
								self.__events[nodeName] = [value]


						#write, in the proper output file, all the possible elements in the queues based on the order
						for q in self.__queues:
							while (self.__queues[q].canGet()):
								logging.debug('Queue '+str(q) + ' can get')
								# get the smallest key
								smallest = self.__queues[q].get()
								logging.debug(str(smallest))		
								self.__history_files[q].write(str(smallest[0]) + ' ; ' + str(smallest[1][0]) + '\n')
								logging.debug('Write on ' + str(q)+'.his' + ' element ' + str(smallest))

			logging.info('Writing event files')
			for node in self.__nodes:
				if (self.__events.has_key(node)):
					for e in self.__events[node]:
						self.__event_files[node].write(e + '\n')

			logging.debug('Closing file ...')
		   # close all the open files
			[logFiles[f].close() for f in logFiles]
			[self.__history_files[f].close() for f in self.__history_files]
			[self.__event_files[f].close() for f in self.__event_files]   
			logging.debug('Closing files')
			logging.info('File merge completed')

		except Exception, err_msg:
			print "Error while merging: %s" % str(err_msg)
			logging.error('Error while merging: %s', str(err_msg))
			self.__mergeOK = False
        

def main():
	doMerge = True
	#command line check
	try:
		#define the logger. Set logging level based on -log= value
		n = getattr(logging, sys.argv[sys.argv.index("-log")+1].upper())
		logging.basicConfig(filename='merge.log', filemode='w', level=n)
	except Exception, error:
		#if no -log is defined then consider logging.INFO
		logging.basicConfig(filename='merge.log', filemode='w', level=logging.INFO)
		logging.warning('Something wrong when defining the logging level. Default value INFO set...')

	try:
		log_files = sys.argv[sys.argv.index("-f")+1:sys.argv.index("-t")]
		topology_file = sys.argv[sys.argv.index("-t")+1]
		rexp_file = sys.argv[sys.argv.index("-r")+1]
	except Exception, error:
		logging.error('Wrong command line definition not compliant with merge.py -f [lists_of_logs] -t [topology_file] -r [regexp_file] {-log }')
		doMerge = False


	if (doMerge):
		with open(topology_file, 'r') as fp:
			logging.info('Topology file open')
			topologylog_json = json.load(fp)

		merger = Merger(topologylog_json)	
		merger.merge(log_files, rexp_file)
		if (merger.mergeOK()):
			logging.info('Apparently everthing went good')
		else:
			logging.info('Merge finished with errors')
	else:
		print 'Something was wrong. No merge run!'

if __name__ == '__main__':
    main()




