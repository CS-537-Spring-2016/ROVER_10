CS-537 Swarm Rover Project

Rover functionality:
range extesion,
radiation
wheels

Members:
James Sunthonlap
Adam Berman
Gang Qu
Priyank Patel

Goal: upload a executable .jar of our Rover  by every Friday

manual build process:
javac -cp ./libs/* -d ./build ./src/common/*.java ./src/controlServer/*.java ./src/enums/*.java ./src/json/*.java ./src/supportTools/*.java ./src/swarmBots/*.java ./src/testUtillities/*.java
cd build
jar cvf my.jar *
cd ..
cp build/my.jar my.jar
to run (with example rover 00):
java -cp my.jar;./libs/* controlServer.SwarmServer
java -cp my.jar;./libs/* swarmBots.ROVER_00

greedy search algorithm: for each possible move, look at how many tiles will be revealed. take the largest possible number; if all things are equal, move in the direction of the objective.