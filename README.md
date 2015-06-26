My Contiki Code
======

#Powertrace
Printing power profile

- Add to Makefile
APP += powertrace 
- Add to source file
include "powertrace.h"
- To print power profile every 10 second
powertrace_start(CLOCK_SECOND * 10);

#IP-Sensor-Collect
Collect data from a light sensor, sending to a sink node and visualize it in a gray scale panel of a Java application run on the host computer 
##Prepare
- Put in the same folder: serialdump-windows.exe, cygwin1.dll, IPSensorCollect.jar (for Windows)
- Check Serial port for the sink node, it should be similar to /dev/comX

##Test sink node
- Run serial-dump to test: 
serialdump-windows -b115200 /dev/comX

##Run the application
/>java -jar IPSensorCollect.jar /dev/comX