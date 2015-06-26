IPSensorCollect
===

#Prepare
put in the same folder
serialdump-windows.exe
cygwin1.dll
gateway.jar
#Check Serial port for the sink node /dev/comX

#Run serial-dump to test
serialdump-windows -b115200 /dev/comX

#Run the gateway
/>java -jar IPSensorCollect /dev/comX
