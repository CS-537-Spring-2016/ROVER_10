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
javac -cp ./libs/* -d ./build ./src/common/*.java ./src/controlServer/*.java ./src/enums/*.java ./src/json/*.java ./src/build/*.java ./src/supportTools/*.java ./src/swarmBots/*.java ./src/testUtilities/*.java
jar cvf my.jar ./build/*
to run (with example rover 00):
java -cp my.jar;./libs/* controlServer.SwarmServer
java -cp my.jar;./libs/* swarmBots.ROVER_00
