NanoContainer Distribution

This binary distribution has the following structure:

nanocontainer.bat - the script file to start the container using NanoContainer Booter under Windows
nanocontainer.sh - the script file to start the container using NanoContainer Booter under Unix
composition.* - the composition files
lib/nanocontainer-booter-x.y.jar - the booter
lib/common and lib/hidden - the libs accessible to the booter classloader:
						   the libs in hidden are hidden from common.

By default, the libs required to use Groovy as the composition script are included.
Should one want to use XML or other NanoContainer scripting langauges, 
one may remove from lib/hidden		   

groovy-all*.jar           
				
and replace with libs required by the chosen script.

Likewise, if groovy is not used composition.groovy should be removed or ignored
and replaced with a composition pertinent to the chosen scripting language.

Please refer to the online documentation at http://nanocontainer.org
for full details.




				
				
				