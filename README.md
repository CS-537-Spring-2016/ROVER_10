#CS537 Rover 10 Project

##Members
* James Sunthonlap
* Adam Berman
* Gang Qu
* Priyank Patel

##Rover Responsibilities:
* Range Extension
* Radiation Gathering
* Wheels

##Group Responsiblities
upload a executable .jar of our Rover by every Friday to CSNS

##ROVER_10.jar build process
* run compile.bat

##Running the ROVER_10.jar
* Be in the root directory of the git repo
* java -cp ROVER_10.jar controlServer.SwarmServer
* java -jar ROVER_10.jar

##Search method
* greedy search algorithm: for each possible move, look at how many tiles will be revealed. take the largest possible number; if all things are equal, move in the direction of the objective.
