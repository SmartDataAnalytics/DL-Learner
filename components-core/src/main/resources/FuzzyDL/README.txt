--------------------------------------------
INSTALLATION
--------------------------------------------

- decompress the folder.

- the "FuzzyDL" folder is the main folder

- set the environment variables 

DYLD_LIBRARY_PATH
LD_LIBRARY_PATH

to the CbcLibxxx libraries

For instance, in the file .profile of your home directory add the appropriately modified version of the following lines

DYLD_LIBRARY_PATH=/Users/straccia/Development/FuzzyDLPackage/Installation/FuzzyDLMacOSX/FuzzyDL/CbcLibppc:/Users/straccia/Development/FuzzyDLPackage/Installation/FuzzyDLMacOSX/FuzzyDL/CbcLib64:/Users/straccia/Development/FuzzyDLPackage/Installation/FuzzyDLMacOSX/FuzzyDL/CbcLib32:.

LD_LIBRARY_PATH=/Users/straccia/Development/FuzzyDLPackage/Installation/FuzzyDLMacOSX/FuzzyDL/CbcLibppc:/Users/straccia/Development/FuzzyDLPackage/Installation/FuzzyDLMacOSX/FuzzyDL/CbcLib64:/Users/straccia/Development/FuzzyDLPackage/Installation/FuzzyDLMacOSX/FuzzyDL/CbcLib32:.


export DYLD_LIBRARY_PATH
export LD_LIBRARY_PATH




- then you should be ready for the execution on Mac OS X.

--------------------------------------------
EXAMPLE EXECUTION of fuzzyDL
--------------------------------------------

1. to check preliminarily that libraries paths are set correctly execute in the FuzzyDL folder

 ./fuzzyDLcbc 

You should see 

Coin Cbc and Clp Solver version 1.01.00, build Jan  4 2008
CoinSolver takes input from arguments ( - switches to stdin)
Enter ? for list of commands or help
Coin:

control-D to exit

2. Make some tests:
a) java -jar FuzzyDL.jar ./test2.txt

YoungPerson subsumes Minor ? >= 0.6

b) java -jar FuzzyDL.jar ./test.txt

Is audi instance of SportCar ? >= 0.92

c) java -jar FuzzyDL.jar ./young.txt 

Is umberto instance of Minor ? <= 1.0

--------------------------------------------
EXECUTION of fuzzyDL
--------------------------------------------
java -jar FuzzyDL.jar filename

To read in the file. Some parameters are taken from the CONFIG file

To select the semantics, change the 'solver' parameter in the CONFIG file 

z for Zadeh Logic
l for Luaksiewicz Loic
c for Classical Logic


