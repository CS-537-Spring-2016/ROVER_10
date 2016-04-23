javac -cp ./libs/* -d ./build ./src/common/*.java ./src/controlServer/*.java ./src/enums/*.java ./src/json/*.java ./src/supportTools/*.java ./src/swarmBots/*.java ./src/testRoverComm/*.java ./src/testUtillities/*.java
xcopy libs build\libs /y
cd build
jar xf ../libs/commons-lang3-3.4.jar
jar xf ../libs/gson-2.3.1.jar
jar xf ../libs/guava-jdk8-1.1.jar
jar xf ../libs/json-simple-1.1.1.jar
rmdir META-INF /s /q
jar cvfm ROVER_10.jar ../jar_manifest.txt *
move ROVER_10.jar ../
cd ..
pause