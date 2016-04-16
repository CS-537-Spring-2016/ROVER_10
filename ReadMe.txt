CS-537 Swarm Rover Project

build process:
javac -cp ./libs/* -d ./build ./src/common/*.java ./src/controlServer/*.java ./src/enums/*.java ./src/json/*.java ./src/build/*.java ./src/supportTools/*.java ./src/swarmBots/*.java ./src/testUtilities/*.java
jar cvf my.jar ./build/*
to run (with example rover 00):
java -cp my.jar;./libs/* controlServer.SwarmServer
java -cp my.jar;./libs/* swarmBots.ROVER_00