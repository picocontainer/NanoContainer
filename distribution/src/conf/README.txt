NanoContainer Distribution

This distribution has the following structure:

nanocontainer.sh - the script file to start the container using NanoContainer Booter
composition.* - the composition files 
lib/nanocontainer-booter-x.y.jar - the booter
lib/common and lib/hidden - the libs accessible to the booter classloader:
						   the libs in hidden are hidden from common.

By default the libs required to use Groovy as the composition script are included.
Should one want to use XML or other NanoContainer scripting langauges, 
one may remove from lib/hidden		   

groovy-*.jar           
antlr-*.jar
asm-*.jar
				
and replace with libs required by the chosen script.

				
				
				