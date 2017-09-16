#import library for REST call
import requests
from dicetract import dicetractor

#import library for REST server implementation
from flask import Flask, request, jsonify


from subprocess import call
import json
import os

app = Flask(__name__)

@app.route('/run', methods=['GET', 'POST'])
def runDicetract():
	result = None

#	if (request.args.get('ip') in ['nodmon', 'local', 'remote']):
	result = dicetractor(request.args.get('ip'), request.args.get('port'), request.get_json())
#	else:
#		result = -1
	print result
	#return a string version of the list containing results
	if (result == -1):
		response = jsonify("Error")
		response.status_code = 500
		return response
	elif (result is None):
		response = jsonify("Error")
		response.status_code = 500
		return response

	response = jsonify(result)
	response.status_code = 200
	return response




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
	app.run(host='0.0.0.0', port=5050)
