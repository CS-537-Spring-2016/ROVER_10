#CS537 Rover 10 Project

###Members
James Sunthonlap
Adam Berman
Gang Qu
Priyank Patel

###Rover Responsibilities:
Range Extension
Radiation Gathering
Wheels

###Must
upload a executable .jar of our Rover  by every Friday to CSNS

###ROVER_10.jar build process
* navigate into the root directory of the git repo
* create a folder called build if not exists
* javac -cp ./libs/* -d ./build ./src/common/*.java ./src/controlServer/*.java ./src/enums/*.java ./src/json/*.java ./src/supportTools/*.java ./src/swarmBots/*.java ./src/testRoverComm/*.java ./src/testUtillities/*.java
* cd build
* jar cvf ROVER_10.jar *
* move ROVER_10.jar ../
* cd ..

###Running the jar file
* Be in the root directory of the git repo
* java -cp ROVER_10.jar;./libs/* controlServer.SwarmServer
* java -cp ROVER_10.jar;./libs/* swarmBots.ROVER_10

###Search method
greedy search algorithm: for each possible move, look at how many tiles will be revealed. take the largest possible number; if all things are equal, move in the direction of the objective.
