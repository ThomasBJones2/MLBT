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

public class SingleAbstract {

    Pipe pipe;
    Tree T;
    boolean foldVal;
    int foldNum;
    static int FOLDS = 10;
    float BayesianThreshold = 0;
    static int NUMTREES = 120;

    public SingleAbstract() {
        this.pipe = buildPipe();
	this.foldVal = true;
	this.foldNum = 0;
    }

    public void BuildTree(String[] Order){
	
		
	
	String[] empty = new String[0];

	T = TreeRecurse(empty, empty, 0, "", Order)[0];

    }


    private Tree[] TreeRecurse(String [] inList, String[] terminator, int depth, String parentCategory, String[] Order){

	Tree[] out = new Tree[terminator.length];
	String FileName = "";
	String[] terminatorOut = new String[0];
	String LocalName = "";

	if(Order[depth].equals("SM")){

		LocalName = "StimulusModality";

		FileName = "./CogPOTerms/StimulusModality/";

		terminatorOut = new String[]{"Auditory","Gustatory",
				"Interoceptive","None",
				"Olfactory","Tactile","Visual"};
	}

	else if(Order[depth].equals("ST")){

		LocalName = "StimulusType";

		FileName = "./CogPOTerms/StimulusType/";

		terminatorOut =  new String[]{"3DObjects","Accupuncture",
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
	}

	else if(Order[depth].equals("RM")){

		LocalName = "ResponseModality";

		FileName = "./CogPOTerms/ResponseModality/";

		terminatorOut =  new String[]{"Facial",
				"Foot","Hand","None","Ocular"};
	}

	else if(Order[depth].equals("RT")){

		LocalName = "ResponseType";

		FileName = "./CogPOTerms/ResponseType/";

		terminatorOut =  new String[]{"ButtonPress","FingerTapping",
				"Flexion","Grasp",
				"manipulate","None","Saccades",
				"Speech"};
	}


	else if(Order[depth].equals("I")){
		LocalName = "Instructions";
		
		FileName = "./CogPOTerms/Instructions/";

		terminatorOut = new String[]{"Attend","Count","Detect","Discriminate",
				"Encode","Fixate","Generate","Imagine",
				"Move","Name","None","Passive","Read",
				"Recall","Repeat","Sing","Smile","Track"};

	}

	String[] list = new String[4];

	String[] empty = new String[0];

	Tree [] NoChildren = new Tree[0];

	//So this line says that we are building a bayesian classifier for stimulus types which has an empty restriction list
	InstanceList ThisLevel = readDirectory(new File(FileName),empty);;
	Classifier ThisBayes = trainClassifierBayes(ThisLevel);

	for(int i = 0; i < terminator.length; i ++){
		for(int j = 0; j < depth - 1; j ++){
			list[j] = inList[j];
		}
		for(int j = depth - 1; j < 4; j ++)
			list[j] = parentCategory + terminator[i];			

		//Here, we are saying: train a clasiffier for stimulus type while limiting yourself to only those
		//abstracts which have the terms found in 'list'. 
		ThisLevel = readDirectory(new File(FileName),list);
//		System.out.print(ThisLevel.size()+"\n");
		if(ThisLevel.size() > 0 && depth < 4){
			ThisBayes = trainClassifierBayes(ThisLevel);
			out[i] = new Tree (terminator[i], ThisBayes, LocalName, 
				TreeRecurse(list, terminatorOut, depth + 1, LocalName, Order));
		}
		else if(ThisLevel.size() > 0 && depth == 4){
			ThisBayes = trainClassifierBayes(ThisLevel);
			out[i] = new Tree(terminator[i], ThisBayes, LocalName, NoChildren);
		}
		else{
			out[i] = new Tree ("NoExamples", ThisBayes, LocalName, NoChildren);
		}
	}

	if(depth == 0){
		Tree[] NewOut = new Tree[1];
		NewOut[0] = new Tree("", ThisBayes, LocalName, TreeRecurse(empty, terminatorOut, depth + 1, LocalName, Order));
		return NewOut;
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
                             new TxtFilter(".txt", FilterPhrases),
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
    public void printLabelings(Tree T, String abstractName, int depth, String Tag) throws IOException {

    	Tree [] children;
	//InstanceList testInstances = importer.readDirectory(new File(TestSetFilename), empty);eList

        Labeling labeling = T.getClassifier().classify(abstractName).getLabeling();
        // print the labels with their weights in descending order (ie best first) 
    	/******************************************************************************* 
    	 * added by Jiawei, an example shows how to directly pull the correct labels
    	 *******************************************************************************/
    	if (depth == 0) {

		String [] Correct = new String[1];
		Correct[0] = Tag;
    		for (String correct : Correct) {
    			if (!correct.startsWith(T.getTitle()))
    				continue;
    			//System.out.print(correct+" ");
    			correct = correct.replaceAll(T.getTitle(), "");
    			System.out.print(correct+" ");
    			children = T.getChildren();
    			for(int child = 0; child < children.length; child++)
        			if(correct.equals(children[child].getValue()))
        				printLabelings(children[child], abstractName, depth + 1, Tag);
    		}
    		//System.out.print("\n");
    		return;
    	}
    	/*******************************************************************************/
    	else{                
        for (int rank = 0; rank < labeling.numLocations(); rank++){
        	if(labeling.getValueAtRank(rank) > BayesianThreshold){
        		//fos[index].write(T.getTitle()+labeling.getLabelAtRank(rank).toString()+" ");
        		System.out.print(T.getTitle()+labeling.getLabelAtRank(rank).toString()+" ");
        		children = T.getChildren();
        		for(int child = 0; child < children.length; child++)
        			if(labeling.getLabelAtRank(rank).toString().equals(children[child].getValue()))
        				printLabelings(children[child], abstractName, depth + 1, Tag);
        	}	
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

    public static void main (String[] args) throws IOException {
	String TestSetFilename = "./CogPOTerms/Abstracts/";
	String OutFileName = "./CogPOTerms/TestAbstracts";
	String FoldFileName = "./CogPOTerms/FoldAbstracts/";
	Scorer3 MyScorer = new Scorer3();
	String[] newargs = new String[5];

        SingleAbstract importer = new SingleAbstract();

	System.out.print("Currently on tree: " + args[0] +
				" " + args[1] +
				" " + args[2] +
				" " + args[3] +
				" " + args[4] + "\n");

	String[] empty = new String[0];

	String[] Builder = new String[5];
	Builder[0] = args[0];
	Builder[1] = args[1];
	Builder[2] = args[2];
	Builder[3] = args[3];
	Builder[4] = args[4];

	String tag = args[6];
	String AbFile = args[7];

	importer.BayesianThreshold = Float.parseFloat(args[5]);
	importer.BuildTree(Builder);


	System.out.print("Tree Built\n");
	//Here I print out the results from my classifier...

	importer.printLabelings(importer.T, AbFile, 0, tag);
}

}


