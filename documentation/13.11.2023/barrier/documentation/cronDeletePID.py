import os
import logging
import time

time.sleep(5)
file = open("top.txt")
f = file.readlines()
file.close()
titulTop = f[6]
dataTop = f[7::]
topDict = {}


def logger(pid, cpu, command):
    logging.basicConfig(
        level=logging.DEBUG,
        filename="mylog.log",
        format="%(asctime)s - %(module)s - %(levelname)s - %(funcName)s: %(lineno)d - %(message)s",
        datefmt='%H:%M:%S',
    )
    logging.info(pid + " " + cpu + " " + command)


for dataLine in dataTop:
    line = dataLine.split()
    cpu = line[8]
    cpu = float(cpu.replace(',', '.'))
    if cpu > 90:
        logger(line[0], line[8], line[11])
        os.system("kill " + dataLine.split()[0])
        break
    else:
       # logger("Все", "тихо", "")
        break
