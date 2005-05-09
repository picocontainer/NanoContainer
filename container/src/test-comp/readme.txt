This component is deliberately developerd outside of maven control. 
It is important that it is not a magically imported dependancy 
for when the unit tests run. Reason ? The test is testing that a 
classloader can be created and introduce a new component.

- Paul