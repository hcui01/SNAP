
import sys
import os
import matplotlib.pyplot as plt
import numpy as np
import time

domains=["crossing_traffic", "traffic", "sysadmin"]
instances=1+np.arange(12)

for domain in domains:
    for inst in instances:
        os.system("./run_SNAP "+domain+"_inst_pomdp__"+str(inst)+" 2323")
        
