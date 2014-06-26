import cc.mallet.classify.*;
import cc.mallet.util.*;
import cc.mallet.types.*;
import java.io.*;
import java.util.*;
import java.util.regex.*;

import cc.mallet.pipe.*;
import cc.mallet.pipe.iterator.*;
import cc.mallet.types.*;
import cc.mallet.pipe.Pipe;
import java.net.URI;

public class LSA {

    Pipe pipe;
    boolean foldVal;
    int foldNum;
    static int FOLDS = 10;
    double BayesianThreshold = 0;

    public LSA() {
        pipe = buildPipe();
	this.foldVal = true;
	this.foldNum = 0;
    }

    public Pipe buildPipe() {
        ArrayList pipeList = new ArrayList();

        // Read data from File objects
        pipeList.add(new Input2CharSequence("UTF-8"));

        // Regular expression for what constitutes a token.
        //  This pattern includes Unicode letters, Unicode numbers, 
        //   and the underscore character. Alternatives:
        //    "\\S+"   (anything not whitespace)
        //    "\\w+"    ( A-Z, a-z, 0-9, _ )
        //    "[\\p{L}\\p{N}_]+|[\\p{P}]+"   (a group of only letters and numbers OR
        //                                    a group of only punctuation marks)
        Pattern tokenPattern =
            Pattern.compile("[\\p{L}\\p{N}_]+");

        // Tokenize raw strings
        pipeList.add(new CharSequence2TokenSequence(tokenPattern));

        // Normalize all tokens to all lowercase
        pipeList.add(new TokenSequenceLowercase());

        // Remove stopwords from a standard English stoplist.
        //  options: [case sensitive] [mark deletions]
        pipeList.add(new TokenSequenceRemoveStopwords(false, false));

        // Rather than storing tokens as strings, convert 
        //  them to integers by looking them up in an alphabet.
        pipeList.add(new TokenSequence2FeatureSequence());

        // Do the same thing for the "target" field: 
        //  convert a class label string to a Label object,
        //  which has an index in a Label alphabet.
        pipeList.add(new Target2Label());

        // Now convert the sequence of features to a sparse vector,
        //  mapping feature IDs to counts.
        pipeList.add(new FeatureSequence2FeatureVector());

        // Print out the features and the label
 //       pipeList.add(new PrintInputAndTarget());

        return new SerialPipes(pipeList);
    }

    public InstanceList readDirectory(File directory, String[] FilterPhrases) {
        return readDirectories(new File[] {directory}, FilterPhrases);
    }

    public InstanceList readDirectories(File[] directories, String[]  FilterPhrases) {
        
        // Construct a file iterator, starting with the 
        //  specified directories, and recursing through subdirectories.
        // The second argument specifies a FileFilter to use to select
        //  files within a directory.
        // The third argument is a Pattern that is applied to the 
        //   filename to produce a class label. In this case, I've 
        //   asked it to use the last directory name in the path.
        FileIterator iterator =
            new FileIterator(directories,
                             new TxtFilter(".txt", FilterPhrases, this.foldVal, this.foldNum),
                             FileIterator.LAST_DIRECTORY);


        // Construct a new instance list, passing it the pipe
        //  we want to use to process instances.
        InstanceList instances = new InstanceList(pipe);

        // Now process each instance provided by the iterator.
        instances.addThruPipe(iterator);

        return instances;
    }

    public Classifier trainClassifierBayes(InstanceList trainingInstances) {

        // Here we use a maximum entropy (ie polytomous logistic regression)                               
        //  classifier. Mallet includes a wide variety of classification                                   
        //  algorithms, see the JavaDoc API for details.                                                   

        ClassifierTrainer trainer = new NaiveBayesTrainer();
        return trainer.train(trainingInstances);
    }


    public Classifier trainClassifierLSA(InstanceList trainingInstances) {

        // Here we use a maximum entropy (ie polytomous logistic regression)                               
        //  classifier. Mallet includes a wide variety of classification                                   
        //  algorithms, see the JavaDoc API for details.       

        //NaiveBayesTrainer.Factory trainer1 = new NaiveBayesTrainer.Factory();

        //ClassifierTrainer trainer = new BaggingTrainer(trainer1);
        ClassifierTrainer trainer = new TopicTrainer();
        return trainer.train(trainingInstances);
    }


    public Classifier loadClassifier(File serializedFile)
        throws FileNotFoundException, IOException, ClassNotFoundException {

        // The standard way to save classifiers and Mallet data                                            
        //  for repeated use is through Java serialization.                                                
        // Here we load a serialized classifier from a file.                                               

        Classifier classifier;

        ObjectInputStream ois =
            new ObjectInputStream (new FileInputStream (serializedFile));
        classifier = (Classifier) ois.readObject();
        ois.close();

        return classifier;
    }


    public void saveClassifier(Classifier classifier, File serializedFile)
        throws IOException {

        // The standard method for saving classifiers in                                                   
        //  Mallet is through Java serialization. Here we                                                  
        //  write the classifier object to the specified file.                                             

        ObjectOutputStream oos =
            new ObjectOutputStream(new FileOutputStream (serializedFile));
        oos.writeObject (classifier);
        oos.close();
    }

public String cleanString (String in){
	String [] check = in.split("/");
	int i = 0;
	while(!Character.isDigit(check[i].charAt(0))){
		i++;
	}
		
	return check[i];	
}


public boolean NotInFile(String FileName, String check) throws IOException{
	char[] in = new char[1000];
	FileReader fis = new FileReader(new File(FileName));
	fis.read(in);
	String [] Correct = (new String(in)).split(" ");
	fis.close();
	boolean out = true;
	for(int i = 0; i < Correct.length; i ++){
		if(Correct[i].equals(check))
			out = false;
	}
	return out;
}


//This function accepts a boolean classifier, a list of abstracts to be tested, and preappend string called checker. 
//It then prints out the classification provided by the classifier in the Test-Abstracts folder under the correct name
//insuring to preappend the checker value...
public void printLabelings(Classifier inClassifier, String title, InstanceList testInstances, FileWriter[] fos, int index) throws IOException {
	Tree [] children;

//	for(int index = 0; index < testInstances.size(); index++) {

            Labeling labeling = inClassifier.classify(testInstances.get(index)).getLabeling();

            // print the labels with their weights in descending order (ie best first)                     

            for (int rank = 0; rank < labeling.numLocations(); rank++){
		if(labeling.getValueAtRank(rank) > this.BayesianThreshold){
			fos[index].write(title+labeling.getLabelAtRank(rank).toString()+" ");
            }
        }
}

public void ClearDirectory(String Directory){

 	File filedel = new File(Directory);        
        String[] myFiles;      
            if(filedel.isDirectory()){  
                myFiles = filedel.list();  
                for (int i=0; i<myFiles.length; i++) {  
                    File myFile = new File(filedel, myFiles[i]);   
                    myFile.delete();  
                }  
             } 
}

public void WriteFold(int [] K_Fold) throws IOException {

 	File filewrite = new File("./CogPOTerms/FoldAbstracts" + this.foldNum + "/");
	File fileread = new File("./CogPOTerms/Abstracts/");        
        String[] myFiles;      
            if(fileread.isDirectory()){  
                myFiles = fileread.list();  
                for (int i=0; i<myFiles.length; i++) {  
                    FileWriter fos = new FileWriter( new File(filewrite, myFiles[i]));   
                    fos.write((char)(K_Fold[i] + 48));
		    fos.close();  
                }  
             } 

}

public void RunClassification(double threshold)throws IOException{

	String TestSetFilename = "./CogPOTerms/Abstracts/";
	String OutFileName = "./CogPOTerms/TestAbstracts";
	String StimModFilename = "./CogPOTerms/StimulusModality/";
	String StimTypeFilename = "./CogPOTerms/StimulusType/";
	String RespModFilename = "./CogPOTerms/ResponseModality/";
	String RespTypeFilename = "./CogPOTerms/ResponseType/";
	String InstFilename = "./CogPOTerms/Instructions/";
	String FoldFileName = "./CogPOTerms/FoldAbstracts/";


	for (int k = 0; k < FOLDS; k ++){	
		System.out.print("Currently on K: " + k + "\n");
		int []K_fold = new int[300];
		Random generator = new Random();

		for(int i = 0; i < 300; i ++){
			K_fold[i] = Math.abs(generator.nextInt()%FOLDS);
		}

        	pipe = buildPipe();
		this.foldVal = true;

		this.foldNum = k;
		this.ClearDirectory(OutFileName + this.foldNum + "/");
		this.ClearDirectory(FoldFileName + this.foldNum + "/");
		this.WriteFold(K_fold);
		this.BayesianThreshold = threshold; //Float.parseFloat(args[0]);

		String[] empty = new String[0];

        	InstanceList instancesSM = this.readDirectory(new File(StimModFilename),empty);
        	InstanceList instancesST = this.readDirectory(new File(StimTypeFilename),empty);
        	InstanceList instancesRM = this.readDirectory(new File(RespModFilename),empty);
        	InstanceList instancesRT = this.readDirectory(new File(RespTypeFilename),empty);
        	InstanceList instancesI = this.readDirectory(new File(InstFilename),empty);

		Classifier bayesSM = this.trainClassifierLSA(instancesSM);
		Classifier bayesST = this.trainClassifierLSA(instancesST);
		Classifier bayesRM = this.trainClassifierLSA(instancesRM);
		Classifier bayesRT  = this.trainClassifierLSA(instancesRT);
		Classifier bayesI = this.trainClassifierLSA(instancesI);


		this.foldVal = false;
		InstanceList testInstances = this.readDirectory(new File(TestSetFilename), empty);

		//Here I get the abstract name for the guy I am writing out..;
		FileWriter[] fos = new FileWriter[testInstances.size()];    
		for(int i = 0; i < testInstances.size(); i ++){
			String AbstractName = this.cleanString(testInstances.get(i).getName().toString());
			fos[i] = new FileWriter(new File(OutFileName + this.foldNum + "/" +AbstractName));
		}

		//Here I print out the stimulus modality stuff...
		for(int index = 0; index < testInstances.size(); index++){
			this.printLabelings(bayesSM, "StimulusModality", testInstances, fos, index);
			this.printLabelings(bayesST, "StimulusType", testInstances, fos, index);
			this.printLabelings(bayesRM, "ResponseModality", testInstances, fos, index);
			this.printLabelings(bayesRT, "ResponseType", testInstances, fos, index);
			this.printLabelings(bayesI, "Instructions", testInstances, fos, index);
		}

		for(int i = 0; i < testInstances.size(); i ++){
			fos[i].close();
		}
	}	


}

    public static void main (String[] args) throws IOException {
	LSA BC = new LSA();
	BC.RunClassification(Double.parseDouble(args[0]));
    }

}


