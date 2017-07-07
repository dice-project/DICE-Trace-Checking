#import library for REST call
import requests
from dicetract import dicetractor


#import library for REST server implementation
from flask import Flask, request


from subprocess import call
import json
import os

app = Flask(__name__)

@app.route('/run', methods=['GET', 'POST'])
def runDicetract():
	result = None

	if (request.args.get('ip', '') == 'local'):
		result = dicetractor('local', request.get_json())
	else:
		result = dicetractor(url, request.get_json())

	#return a string version of the list containing results
	return ' '.join(result)


@app.route('/clean', methods=['GET'])
def cleanup():

	call(['rm', './topologylog.json'])
	call(['rm','merge.log'])

	currentdir = os.getcwd()

	os.chdir('./logs/')
	call('ls | grep \"\.log\" | xargs rm', shell=True)
	os.chdir(currentdir)
	print 'Removed all logs files\n'

	os.chdir('./results/')
	call('rm *', shell=True)
	os.chdir(currentdir)
	print 'Removed all results files\n'

	os.chdir('./merge/')
	call('rm *', shell=True)
	os.chdir(currentdir)
	print 'Removed all history files\n'

	return 'Clean up done!\n'

@app.route('/user/<username>')
def show_user_profile(username):
    # show the user profile for that user
    return 'User %s' % username

if __name__ == "__main__":
	app.run()
