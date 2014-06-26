NB:
	clear
	clear
	javac -cp ".:mallet.jar:mallet-deps.jar:./*" BasicClassifier.java
	javac -cp ".:mallet.jar:mallet-deps.jar:./*" ImportExample.java
	javac -cp ".:mallet.jar:mallet-deps.jar:./*" ImportExample2.java
	javac -cp ".:mallet.jar:mallet-deps.jar:./*" ImportExample3.java
	javac -cp ".:mallet.jar:mallet-deps.jar:./*" ImportExample4.java
	javac -cp ".:mallet.jar:mallet-deps.jar:./*" Scorer.java
	javac -cp ".:mallet.jar:mallet-deps.jar:./*" DT.java
	javac -cp ".:mallet.jar:mallet-deps.jar:./*" SingleAbstract.java
	javac -cp ".:mallet.jar:mallet-deps.jar:./*" LSA.java

createNums:
	rm Results.dat
	java -cp ".:mallet.jar:mallet-deps.jar:./*" BasicClassifier
	@echo "These number are for: the basic classifier" >> Results.dat
	java -cp ".:mallet.jar:mallet-deps.jar:./*" Scorer >> Results.dat
	java -cp ".:mallet.jar:mallet-deps.jar:./*" ImportExample2 I SM ST RM RT .1
	@echo "These number are for: the unaided tree I SM ST RM RT" >> Results.dat
	java -cp ".:mallet.jar:mallet-deps.jar:./*" Scorer >> Results.dat
	java -cp ".:mallet.jar:mallet-deps.jar:./*" ImportExample2 SM ST RM RT I .1
	@echo "These number are for: the unaided tree SM ST RM RT I" >> Results.dat
	java -cp ".:mallet.jar:mallet-deps.jar:./*" Scorer >> Results.dat
	java -cp  ".:mallet.jar:mallet-deps.jar:./*" ImportExample2 ST SM RM RT I .1
	@echo "These number are for: the unaided tree ST SM RM RT I" >> Results.dat
	java -cp ".:mallet.jar:mallet-deps.jar:./*" Scorer >> Results.dat
	java -cp ".:mallet.jar:mallet-deps.jar:./*" ImportExample2 RM SM ST RT I .1
	@echo "These number are for: the unaided tree RM SM ST RT I" >> Results.dat
	java -cp ".:mallet.jar:mallet-deps.jar:./*" Scorer >> Results.dat
	java -cp ".:mallet.jar:mallet-deps.jar:./*" ImportExample2 RT SM ST RM I .1
	@echo "These number are for: the unaided tree RT SM ST RM I" >> Results.dat
	java -cp ".:mallet.jar:mallet-deps.jar:./*" Scorer >> Results.dat

run:
	java -cp ".:mallet.jar:mallet-deps.jar:./*" ImportExample 
	java -cp ".:mallet.jar:mallet-deps.jar:./*" Scorer

run2:
	java -cp ".:mallet.jar:mallet-deps.jar:./*" ImportExample2 I ST SM RT RM .1
	java -cp ".:mallet.jar:mallet-deps.jar:./*" Scorer

run3: 
	java -cp ".:mallet.jar:mallet-deps.jar:./*" ImportExample3 I ST SM RT RM .1

run4:
	java -cp ".:mallet.jar:mallet-deps.jar:./*" ImportExample4 I ST SM RT RM .1	

SingleAbstract:
	java -cp ".:mallet.jar:mallet-deps.jar:./*" SingleAbstract SM ST RM RT I .1 StimulusModalityNone ./CogPOTerms/Abstracts/1402966.txt

runDT:
	java -cp ".:mallet.jar:mallet-deps.jar:./*" DT SM ST I RM RT .1
	java -cp ".:mallet.jar:mallet-deps.jar:./*" Scorer
	
runscorer:
	java -cp ".:mallet.jar:mallet-deps.jar:./" Scorer

runbasic:
	java -cp ".:mallet.jar:mallet-deps.jar:./*" BasicClassifier .1
	java -cp ".:mallet.jar:mallet-deps.jar:./*" Scorer

runLSA:
	java -cp ".:mallet.jar:mallet-deps.jar:./*" LSA .1
	java -cp ".:mallet.jar:mallet-deps.jar:./*" Scorer

clean:
	rm *.class
	rm blah.out
	rm blah.mallet
