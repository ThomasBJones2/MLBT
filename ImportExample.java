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

public class ImportExample {

    Pipe pipe;
    Tree T;
    boolean foldVal;
    int foldNum;
    static int FOLDS = 10;

    public ImportExample() {
        this.pipe = buildPipe();
	this.foldVal = true;
	this.foldNum = 0;
    }

    public void BuildTree(){
	String StimModFilename = "./CogPOTerms/StimulusModality/";

	String[] empty = new String[0];
        InstanceList instancesSM = readDirectory(new File(StimModFilename),empty);

	// here we train our bayesian classifiers...
	Classifier bayesSM = trainClassifierBayes(instancesSM);

	T = new Tree("",bayesSM, "StimulusModality", STChildren());
    }

    //This level of the tree adds the Stimulus type level which checks in on each stimulus modality (check on level above)...
    private Tree [] STChildren(){
	String StimTypeFilename = "./CogPOTerms/StimulusType/";

	Tree[] out = new Tree[7];

	String terminator[] = new String[]{"Auditory","Gustatory",
				"Interoceptive","None",
				"Olfactory","Tactile","Visual"};

	String[] list = new String[1];

	String[] empty = new String[0];

	Tree [] NoChildren = new Tree[0];
	

	//So this line says that we are building a bayesian classifier for stimulus types which has an empty restriction list
	InstanceList ThisLevel = readDirectory(new File(StimTypeFilename),empty);;
	Classifier ThisBayes = trainClassifierBayes(ThisLevel);

	for(int i = 0; i < 7; i ++){
		list[0] = "StimulusModality" + terminator[i];
		//Here, we are saying: train a clasiffier for stimulus type while limiting yourself to only those
		//abstracts which have the terms found in 'list'. 
		ThisLevel = readDirectory(new File(StimTypeFilename),list);
		if(ThisLevel.size() > 0){
			ThisBayes = trainClassifierBayes(ThisLevel);
			out[i] = new Tree (terminator[i], ThisBayes, "StimulusType", RMChildren(list));
		}
		else{
			out[i] = new Tree ("NoExamples", ThisBayes, "StimulusType", NoChildren);
		}
	}
	
	return out;
    }

    private Tree [] RMChildren(String[] inList){

	String RespModFilename = "./CogPOTerms/ResponseModality/";

	Tree[] out = new Tree[41];

	String terminator[] =  new String[]{"3DObjects","Accupuncture",
				"AsianCharacters","BrailleDots",
				"BreathableGas","ChordSequences","Clicks",
				"Digits", "ElectricalStimulation",
				"Faces", "FalseFonts", "FilmClip",
				"FixationPoint", "FlashingCheckerboard",
				"Food","Fractals","Heat","InfraredLaser",
				"Infusion", "Music", "Noise", "None",
				"NonverbalVocalSounds", "NonvocalSounds",
				"Objects", "Odor", "Pain", "Pictures", "Point",
				"PointsofLight", "Pseudowords", "RandomDots",
				"ReversedSpeech", "Shapes", "Syllables", 
				"Symbols", "TactileStimulation", "TMS",
				"Tones", "VibratoryStimulation", "Words"};

	String[] list = new String[2];
	list[0] = inList[0];

	String[] empty = new String[0];

	Tree [] NoChildren = new Tree[0];

	InstanceList ThisLevel = readDirectory(new File(RespModFilename),empty);;
	Classifier ThisBayes = trainClassifierBayes(ThisLevel);

	for(int i = 0; i < 41; i ++){
		list[1] = "StimulusType" + terminator[i];
		ThisLevel = readDirectory(new File(RespModFilename),list);
		if(ThisLevel.size() > 0){
			ThisBayes = trainClassifierBayes(ThisLevel);
			out[i] = new Tree (terminator[i], ThisBayes, "ResponseModality", RTChildren(list));
		}
		else{
			out[i] = new Tree ("NoExamples", ThisBayes, "ResponseModality", NoChildren);
		}
	}
	
	return out;
    }


    private Tree [] RTChildren(String[] inList){

	String RespTypeFilename = "./CogPOTerms/ResponseType/";

	Tree[] out = new Tree[5];

	String terminator[] =  new String[]{"Facial",
				"Foot","Hand","None","Ocular"};

	String[] list = new String[3];
	list[0] = inList[0];
	list[1] = inList[1];

	String[] empty = new String[0];

	Tree [] NoChildren = new Tree[0];

	InstanceList ThisLevel = readDirectory(new File(RespTypeFilename),empty);;
	Classifier ThisBayes = trainClassifierBayes(ThisLevel);

	for(int i = 0; i < 5; i ++){
		list[2] = "ResponseModality" + terminator[i];
		ThisLevel = readDirectory(new File(RespTypeFilename),list);
		if(ThisLevel.size() > 0){
			ThisBayes = trainClassifierBayes(ThisLevel);
			out[i] = new Tree (terminator[i], ThisBayes, "ResponseType", IChildren(list));
		}
		else{
			out[i] = new Tree ("NoExamples", ThisBayes, "ResponseType", NoChildren);
		}
	}
	
	return out;
    }



    private Tree [] IChildren(String[] inList){

	String InstFilename = "./CogPOTerms/Instructions/";

	Tree[] out = new Tree[8];

	String terminator[] =  new String[]{"ButtonPress","FingerTapping",
				"Flexion","Grasp",
				"manipulate","None","Saccades",
				"Speech"};

	String[] list = new String[4];
	list[0] = inList[0];
	list[1] = inList[1];
	list[2] = inList[2];

	String[] empty = new String[0];

	Tree [] NoChildren = new Tree[0];

	InstanceList ThisLevel = readDirectory(new File(InstFilename),empty);;
	Classifier ThisBayes = trainClassifierBayes(ThisLevel);

	for(int i = 0; i < 8; i ++){
		list[3] = "ResponseType" + terminator[i];
		ThisLevel = readDirectory(new File(InstFilename),list);
		if(ThisLevel.size() > 0){
			ThisBayes = trainClassifierBayes(ThisLevel);
			out[i] = new Tree (terminator[i], ThisBayes, "Instructions", NoChildren);
		}
		else{
			out[i] = new Tree ("NoExamples", ThisBayes, "Instructions", NoChildren);
		}
	}
	
	return out;
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

//This function accepts a boolean classifier, a list of abstracts to be tested, and preappend string called checker. 
//It then prints out the classification provided by the classifier in the Test-Abstracts folder under the correct name
//insuring to preappend the checker value...
public void printLabelings(Tree T, InstanceList testInstances, FileWriter[] fos, int index) throws IOException {
	Tree [] children;

            Labeling labeling = T.getClassifier().classify(testInstances.get(index)).getLabeling();

            // print the labels with their weights in descending order (ie best first)                     

            for (int rank = 0; rank < labeling.numLocations(); rank++){
		if(labeling.getValueAtRank(rank) > .1){
			fos[index].write(T.getTitle()+labeling.getLabelAtRank(rank).toString()+" ");
			children = T.getChildren();
			for(int child = 0; child < children.length; child++)
				if(labeling.getLabelAtRank(rank).toString().equals(children[child].getValue()))
					printLabelings(children[child], testInstances, fos, index);
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
	
    public static void main (String[] args) throws IOException {
	String TestSetFilename = "./CogPOTerms/Abstracts/";
	String OutFileName = "./CogPOTerms/TestAbstracts";
	String FoldFileName = "./CogPOTerms/FoldAbstracts/";
	for (int k = 0; k < FOLDS; k ++){	
		System.out.print("Currently on K: " + k + "\n");
		int []K_fold = new int[300];
		Random generator = new Random();

		for(int i = 0; i < 300; i ++){
			K_fold[i] = Math.abs(generator.nextInt()%3);
		}

        	ImportExample importer = new ImportExample();
		importer.foldNum = k;
		importer.ClearDirectory(OutFileName + importer.foldNum + "/");
		importer.ClearDirectory(FoldFileName + importer.foldNum + "/");
		importer.WriteFold(K_fold);

		importer.BuildTree();

		String[] empty = new String[0];

		importer.foldVal = false;

		InstanceList testInstances = importer.readDirectory(new File(TestSetFilename), empty);
		//Here I get the abstract name for the guy I am writing out..;
		FileWriter[] fos = new FileWriter[testInstances.size()];    
		for(int i = 0; i < testInstances.size(); i ++){
			String AbstractName = importer.cleanString(testInstances.get(i).getName().toString());
			fos[i] = new FileWriter(new File(OutFileName + importer.foldNum + "/" + AbstractName));
		}
		//Here I print out the results from my classifier...
		for(int index = 0; index < testInstances.size(); index++){
			importer.printLabelings(importer.T, testInstances, fos, index);
		}

		for(int i = 0; i < testInstances.size(); i ++){
			fos[i].close();
		}
	}	
    	
}

}


