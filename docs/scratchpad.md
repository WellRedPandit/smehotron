#Suppress the output when generating "golden" SVRL?#
When executed,
```
./smehotron -c path/to/your/smehotron-config.xml -g
```
displays  the results on screen. Is this needed? 

#On displaying the comparison results#
in \GitHub\smehotron\dist:
```
smehotron -c Dropbox\sasha\smehotron.config.GLOB.windows.xml 
> 
Dropbox\sasha\GLOB-out.xml
```
Let's look at Dropbox\sasha\OSA-Schematron-v1.1-DEV-DROPBOX\_test-suite\input-controls\NoGo\GLOB-statement.svrl:

In it, we can suppress namespaced elements like d:req, d:func, etc.

We can count report/assert statements within a &lt;rule> element, and in the output we could display the value of
```
/svrl:schematron-output/svrl:successful-report/svrl:text
or
/svrl:schematron-output/svrl:failed-assert/svrl:text
```
preceded by rule[@id].

Thus if in the GLOB-statement.svrl we have
```
   <svrl:successful-report test="exists(.)" location="/article[1]/statement[1]">
      <svrl:text>ERROR [glob-test:GLOB220]: 'statement' is not allowed</svrl:text>
   </svrl:successful-report>
```
then in the output we could display something like:
```
driver: Dropbox\sasha\OSA-Schematron-v1.1-DEV-Dropbox\_test-suite\sch-drivers\global-testing.sch
discrep: rule[@id="GLOB220"]/report[1]: ERROR [glob-test:GLOB220]: 'statement' is not allowed
```
which would allow us to trace the location of the discrepancy between the golden and the current svrl.
