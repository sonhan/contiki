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