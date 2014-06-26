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

public class ImportExample3 {

    Pipe pipe;
    Tree T;
    boolean foldVal;
    int foldNum;
    static int FOLDS = 10;
    float BayesianThreshold = 0;
    static int NUMTREES = 120;

    public ImportExample3() {
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
public void printLabelings(Tree T, InstanceList testInstances, FileWriter[] fos, int index, String FileName) throws IOException {
		Tree[] children;

            Labeling labeling = T.getClassifier().classify(testInstances.get(index)).getLabeling();

            // print the labels with their weights in descending order (ie best first)                     

            for (int rank = 0; rank < labeling.numLocations(); rank++){
		if(labeling.getValueAtRank(rank) > BayesianThreshold){
			if(NotInFile(FileName, T.getTitle() + labeling.getLabelAtRank(rank).toString()))
				fos[index].write(T.getTitle()+labeling.getLabelAtRank(rank).toString()+" ");
			children = T.getChildren();
			for(int child = 0; child < children.length; child++)
				if(labeling.getLabelAtRank(rank).toString().equals(children[child].getValue()))
					printLabelings(children[child], testInstances, fos, index, FileName);
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
	
	String cat1(int trees){
		if(trees < 24)
			return "SM";
		else if(trees < 48)
			return "ST";
		else if(trees < 72)
			return "RM";
		else if(trees < 96)
			return "RT";
		else
			return "I";
	}


	String cat2(int trees, String in1){
		String[] roulette = new String[]{"SM","ST","RM","RT","I"};
		int j = 0;	
		String[] out = new String[5];	
		for(int i = 0; i < 5; i ++){
			if(!roulette[i].equals(in1)){		
				out[j] = roulette[i];
				j ++;
			}
		}
		int recal = trees % 24;
		if(recal < 6)
			return out[0];
		else if(recal < 12)
			return out[1];
		else if(recal < 18)
			return out[2];
		else 
			return out[3];
	}

	String cat3(int trees, String in1, String in2){
		String[] roulette = new String[]{"SM","ST","RM","RT","I"};
		int j = 0;	
		String[] out = new String[5];	
		for(int i = 0; i < 5; i ++){
			if(!roulette[i].equals(in1) && !roulette[i].equals(in2)){		
				out[j] = roulette[i];
				j ++;
			}
		}
		int recal = trees % 6;
		if(recal < 2)
			return out[0];
		else if(recal < 4)
			return out[1];
		else 
			return out[2];
	}

	String cat4(int trees, String in1, String in2, String in3){
		String[] roulette = new String[]{"SM","ST","RM","RT","I"};
		int j = 0;	
		String[] out = new String[5];	
		for(int i = 0; i < 5; i ++){
			if(!roulette[i].equals(in1) && !roulette[i].equals(in2) && !roulette[i].equals(in3)){		
				out[j] = roulette[i];
				j ++;
			}
		}
		int recal = trees % 2;
		if(recal < 1)
			return out[0];
		else 
			return out[1];
	}

	String cat5(String in1, String in2, String in3, String in4){
		String[] roulette = new String[]{"SM","ST","RM","RT","I"};
		int j = 0;
		String[] out = new String[5];		
		for(int i = 0; i < 5; i ++){
			if(!roulette[i].equals(in1) && !roulette[i].equals(in2) && !roulette[i].equals(in3) && !roulette[i].equals(in4)){		
				out[j] = roulette[i];
				j ++;
			}
		}
		return out[0];
	}

    public static void main (String[] args) throws IOException {
	String TestSetFilename = "./CogPOTerms/Abstracts/";
	String OutFileName = "./CogPOTerms/TestAbstracts";
	String FoldFileName = "./CogPOTerms/FoldAbstracts/";
	Scorer2 MyScorer = new Scorer2();
	String[] newargs = new String[5];
	for(int tree = 0; tree < NUMTREES; tree ++){
		System.out.print("Currently on tree: " + tree + "\n");
		for (int k = 0; k < FOLDS; k ++){	
			System.out.print("Currently on K: " + k + "\n");
			int []K_fold = new int[300];
			Random generator = new Random();

			for(int i = 0; i < 300; i ++){
				K_fold[i] = Math.abs(generator.nextInt()%FOLDS);
			}

        		ImportExample3 importer = new ImportExample3();
			importer.foldNum = k;
			importer.ClearDirectory(OutFileName + importer.foldNum + "/");
			importer.ClearDirectory(FoldFileName + importer.foldNum + "/");
			importer.WriteFold(K_fold);


		newargs[0] = importer.cat1(tree);
		newargs[1] = importer.cat2(tree, newargs[0]);
		newargs[2] = importer.cat3(tree, newargs[0], newargs[1]);
		newargs[3] = importer.cat4(tree, newargs[0], newargs[1], newargs[2]);
		newargs[4] = importer.cat5(newargs[0], newargs[1], newargs[2], newargs[3]);

			System.out.print("Currently on tree: " + newargs[0] +
						" " + newargs[1] +
						" " + newargs[2] +
						" " + newargs[3] +
						" " + newargs[4] + "\n");

			String[] empty = new String[0];

			String[] Builder = new String[5];
			Builder[0] = newargs[0];
			Builder[1] = newargs[1];
			Builder[2] = newargs[2];
			Builder[3] = newargs[3];
			Builder[4] = newargs[4];
			importer.BayesianThreshold = Float.parseFloat(args[5]);
			importer.BuildTree(Builder);

			importer.foldVal = false;

			InstanceList testInstances = importer.readDirectory(new File(TestSetFilename), empty);
			//Here I get the abstract name for the guy I am writing out..;
			FileWriter[] fos = new FileWriter[testInstances.size()];    
			String[] FileNames = new String[testInstances.size()];
			for(int i = 0; i < testInstances.size(); i ++){
				String AbstractName = importer.cleanString(testInstances.get(i).getName().toString());
				fos[i] = new FileWriter(new File(OutFileName + importer.foldNum + "/" + AbstractName));
				FileNames[i] = OutFileName + importer.foldNum + "/" + AbstractName;
			}
			//Here I print out the results from my classifier...
			for(int index = 0; index < testInstances.size(); index++){
				importer.printLabelings(importer.T, testInstances, fos, index, FileNames[index]);
			}

			for(int i = 0; i < testInstances.size(); i ++){
				fos[i].close();
			}
		}	
		MyScorer.score(tree);
	}
    	
}

}


