NanoContainer Distribution

This binary distribution has the following structure:

  nanocontainer.bat - the script file to start the container
                      using NanoContainer Booter under Windows
  nanocontainer.sh  - the script file to start the container
                      using NanoContainer Booter under Unix
  composition.*     - the composition files
  lib/nanocontainer-booter-x.y.jar - the booter
  lib/common        - the libs accessible to all classloaders
  lib/hidden        - the libs that are hidden from the common
                      and component classloaders

By default, the libs required to use Groovy as the composition script are included.
Should one want to use XML or other NanoContainer scripting langauges, 
one may remove from lib/hidden:

  groovy-all*.jar
				
and replace with libs required by the chosen script language (if any)

Likewise, if groovy is not used composition.groovy should be removed or ignored
and replaced with a composition script pertinent to the chosen scripting language.

Please refer to the online documentation at http://www.nanocontainer.org
for full details.




				
				
				