import sys
import random
import json


def main():
	nodes = sys.argv[sys.argv.index("-nodes")+1]
	sigma = float(sys.argv[sys.argv.index("-sigma")+1])
	mu = float(sys.argv[sys.argv.index("-mean")+1])
	n_samples = int(sys.argv[sys.argv.index("-n")+1])
	seed = int(sys.argv[sys.argv.index("-seed")+1])
	delta = int(sys.argv[sys.argv.index("-delta")+1])
	outputname = sys.argv[sys.argv.index("-out")+1]

	with open(nodes, 'r') as f:
		dic = json.load(f)

	yY = 2016
	mM = 10
	dD = 16
	hh = 12
	mm = 10
	ss = 0
	ms = 0

	random.seed(int(seed))

	with open(outputname, 'w') as f:
		for i in range(1,n_samples):
			node = random.choice(dic.keys())
			thread = random.choice(dic[node][1].keys())
			event = dic[node][1][thread]
		
			yY_str = str(yY)
			mM_str = str(mM)
			dD_str = str(dD)
			if (hh/10 == 0):
				hh_str = '0'+str(hh)
			else:
				hh_str = str(hh)
			if (mm/10 == 0):
				mm_str = '0'+str(mm)
			else:
				mm_str = str(mm)
			if (ss/10 == 0):
				ss_str = '0'+str(ss)
			else:
				ss_str = str(ss)
			if (ms/100 == 0):
				if (ms/10 == 0):
					ms_str = '00'+str(ms)
				else:
					ms_str = '0'+str(ms)
			else:
				ms_str = str(ms)


			f.write(yY_str + '-' + mM_str + '-' + dD_str + 'T' + hh_str + ':' + mm_str + ':' + ss_str + '.' + ms_str + '+0000' + ' [' + thread + '-' + node + '] bla.bla [INFO] ' + event + ' {tuple}\n')

			if (dic[node][0]=='bolt'):
				if (event == 'emit'):
					dic[node][1][thread] ='receive'
				else:
					dic[node][1][thread] = 'emit'

			v = abs(int(random.gauss(mu,sigma)*delta))
			ms_new = (ms + v)%1000
			if (ms+v > 1000):
				ss_new = (ss + (ms + v)/1000)%60
				if ((ss + (ms + v)/1000) > 60):
					mm_new = (ss + (ms + v)/1000)%60
					if ((ss + (ms + v)/1000)/60 > 24):
						hh_new = ((ss + (ms + v)/1000)/60)%24
						hh = hh_new
					mm = mm_new
				ss = ss_new
			ms = ms_new


if (__name__ == '__main__'):
	main()
