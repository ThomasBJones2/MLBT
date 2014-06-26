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


public  class Scorer {

    Pipe pipe;
    int foldNum;
    static int FOLDS = 10;

    public Scorer() {
        pipe = buildPipe();
	foldNum = 0;
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
                             new TxtFilter(".txt", FilterPhrases, false, this.foldNum),
                             FileIterator.LAST_DIRECTORY);

        // Construct a new instance list, passing it the pipe
        //  we want to use to process instances.
        InstanceList instances = new InstanceList(pipe);

        // Now process each instance provided by the iterator.
        instances.addThruPipe(iterator);

        return instances;
    }

public String cleanString (String in){
		String [] check = in.split("/");
		return check[check.length-1];	
	}

public boolean check(String in, String[] inlist){
	boolean out = false;
		for(int i = 0; i < inlist.length; i ++){
			if(in.equals(inlist[i]))
				out = true;
		}
	return out;	
}

public double stdev(double[] in, double avg){
	double Accum = 0;
	for(int i = 0; i < FOLDS; i ++)
		Accum += (in[i] - avg)*(in[i] - avg);
	Accum = Accum/((double)FOLDS - 1.0);

	return Math.sqrt(Accum);
}

public static void main(String[] args) throws IOException{
	Scorer score = new Scorer();

	String CheckFileName = "./CogPOTerms/TaggedAbstracts/";
	
	String OutFileName = "./CogPOTerms/TestAbstracts";

	String[] empty = new String[0];

	String SMterminator[] = new String[]{"Auditory","Gustatory",
				"Interoceptive","None",
				"Olfactory","Tactile","Visual"};

	String STterminator[] = new String[]{"3DObjects","Accupuncture",
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

	String Iterminator[] = new String[]{"Attend","Count","Detect","Discriminate",
				"Encode","Fixate","Generate","Imagine",
				"Move","Name","None","Passive","Read",
				"Recall","Repeat","Sing","Smile","Track"};

	String RMterminator[] = new String[]{"Facial",
				"Foot","Hand","None","Ocular"};

	String RTterminator[] = new String[]{"ButtonPress","FingerTapping",
				"Flexion","Grasp",
				"manipulate","None","Saccades",
				"Speech"};

	//Here I am grabbing the correct values.


	double[][] SMDen = new double[FOLDS][SMterminator.length + 1];
	double[][] SMNum = new double[FOLDS][SMterminator.length + 1];

	double[][] STDen = new double[FOLDS][STterminator.length + 1];
	double[][] STNum = new double[FOLDS][STterminator.length + 1];

	double[][] RMDen = new double[FOLDS][RMterminator.length + 1];
	double[][] RMNum = new double[FOLDS][RMterminator.length + 1];

	double[][] RTDen = new double[FOLDS][RTterminator.length + 1];
	double[][] RTNum = new double[FOLDS][RTterminator.length + 1];

	double[][] IDen = new double[FOLDS][Iterminator.length + 1];
	double[][] INum = new double[FOLDS][Iterminator.length + 1];


	for(int k = 0; k < FOLDS; k++){
	score.foldNum = k;

	InstanceList testInstances = score.readDirectory(new File(OutFileName+score.foldNum+"/"), empty);


	//=============================================
	// Here I do the scoring for stimulus modality
	//============================================= 


	for(int i = 0; i < SMterminator.length; i ++){
		for(int index = 0; index < testInstances.size(); index ++){
			char [] in = new char[1000];
			char [] in2 = new char[1000];
	    		String AbstractName = score.cleanString(testInstances.get(index).getName().toString());
	    		FileReader fis = new FileReader(new File(CheckFileName+AbstractName));
	    		fis.read(in);
			String [] Correct = (new String(in)).split(" ");
			fis.close();
			fis = new FileReader(new File(OutFileName + score.foldNum + "/" +AbstractName));
			fis.read(in2);
			String [] Guess = (new String(in2)).split(" ");
			fis.close();
			if(score.check("StimulusModality" + SMterminator[i], Correct))
				SMDen[k][i] ++;
			if(score.check("StimulusModality" + SMterminator[i], Guess)){
				SMDen[k][i] ++;
			}
			if(score.check("StimulusModality" + SMterminator[i], Guess) && 
				score.check("StimulusModality" + SMterminator[i], Correct)){
				SMNum[k][i] ++;
			}	
		}
		if(k == FOLDS-1){
			double out = 0;
			double stdev = 0;
			double [] stdhelp = new double[FOLDS];
			for(int l = 0; l < FOLDS; l ++){
				if(SMDen[l][i] > 0){
					stdhelp[l] = (2.0*SMNum[l][i]/SMDen[l][i]);
					out += (2.0*SMNum[l][i]/SMDen[l][i]);
				}
				else
					stdhelp[l] = 0;
			}
			out = out/FOLDS;
			stdev = score.stdev(stdhelp , out);
			System.out.print(SMterminator[i] + ": " + out + " +/-" + stdev + "\n");
		}
		SMNum[k][SMterminator.length] += SMNum[k][i];
		SMDen[k][SMterminator.length] += SMDen[k][i];
		
	}
	if(k == FOLDS-1){
		double out = 0;
		double stdev = 0;
		double [] stdhelp = new double[FOLDS];
		for(int l = 0; l < FOLDS; l ++){
			if(SMDen[l][SMterminator.length] > 0){
				stdhelp[l] = (2.0*SMNum[l][SMterminator.length]/SMDen[l][SMterminator.length]);
				out += (2.0*SMNum[l][SMterminator.length]/SMDen[l][SMterminator.length]);
			}
			else
				stdhelp[l] = 0;
				
		}
		out = out/FOLDS;
		stdev = score.stdev(stdhelp , out);
		System.out.print("StimulusModality" + ": " + out + " +/-" + stdev + "\n\n");
	}

	//=============================================
	// Here I do the scoring for stimulus type
	//============================================= 


	for(int i = 0; i < STterminator.length; i ++){
		for(int index = 0; index < testInstances.size(); index ++){
			char [] in = new char[1000];
			char [] in2 = new char[1000];
	    		String AbstractName = score.cleanString(testInstances.get(index).getName().toString());
	    		FileReader fis = new FileReader(new File(CheckFileName+AbstractName));
	    		fis.read(in);
			String [] Correct = (new String(in)).split(" ");
			fis.close();
			fis = new FileReader(new File(OutFileName + score.foldNum + "/" +AbstractName));
			fis.read(in2);
			String [] Guess = (new String(in2)).split(" ");

/*			System.out.println();
			System.out.print(OutFileName + score.foldNum + "/" +AbstractName + "\n");
			for(int t = 0; in[t] != 0; t ++)
				System.out.print(in2[t]);
			System.out.println();
			for(int t = 0; t < Guess.length; t ++)
				System.out.print(Guess[t] + " ");
			System.out.println();*/

			fis.close();
			if(score.check("StimulusType" + STterminator[i], Correct))
				STDen[k][i] ++;
			if(score.check("StimulusType" + STterminator[i], Guess)){
				STDen[k][i] ++;
			}
			if(score.check("StimulusType" + STterminator[i], Guess) && 
				score.check("StimulusType" + STterminator[i], Correct)){
				STNum[k][i] ++;
			}	
		}
		if(k == FOLDS-1){
			double out = 0;
			double stdev = 0;
			double [] stdhelp = new double[FOLDS];
			for(int l = 0; l < FOLDS; l ++){
				if(STDen[l][i] > 0){				
					stdhelp[l] = (2.0*STNum[l][i]/STDen[l][i]);
					out += (2.0*STNum[l][i]/STDen[l][i]);
				}
				else
					stdhelp[l] = 0;
			}
			out = out/FOLDS;
			stdev = score.stdev(stdhelp , out);
			System.out.print(STterminator[i] + ": " + out + " +/-" + stdev + "\n");
		}
		STNum[k][STterminator.length] += STNum[k][i];
		STDen[k][STterminator.length] += STDen[k][i];
	}
	if(k == FOLDS-1){
		double out = 0;
		double stdev = 0;
		double [] stdhelp = new double[FOLDS];
		for(int l = 0; l < FOLDS; l ++){
			if(STDen[l][STterminator.length] > 0){
				stdhelp[l] = (2.0*STNum[l][STterminator.length]/STDen[l][STterminator.length]);
				out += (2.0*STNum[l][STterminator.length]/STDen[l][STterminator.length]);
			}
			else
				stdhelp[l] = 0;
		}
		out = out/FOLDS;
		stdev = score.stdev(stdhelp , out);
		System.out.print("StimulusType" + ": " + out + " +/-" + stdev + "\n\n");
	}


	//=============================================
	// Here I do the scoring for Response Modality
	//============================================= 


	for(int i = 0; i < RMterminator.length; i ++){
		for(int index = 0; index < testInstances.size(); index ++){
			char [] in = new char[1000];
			char [] in2 = new char[1000];
	    		String AbstractName = score.cleanString(testInstances.get(index).getName().toString());
	    		FileReader fis = new FileReader(new File(CheckFileName+AbstractName));
	    		fis.read(in);
			String [] Correct = (new String(in)).split(" ");
			fis.close();
			fis = new FileReader(new File(OutFileName + score.foldNum + "/" +AbstractName));
			fis.read(in2);
			fis.close();
			String [] Guess = (new String(in2)).split(" ");
			if(score.check("ResponseModality" + RMterminator[i], Correct))
				RMDen[k][i] ++;
			if(score.check("ResponseModality" + RMterminator[i], Guess)){
				RMDen[k][i] ++;
			}
			if(score.check("ResponseModality" + RMterminator[i], Guess) && 
				score.check("ResponseModality" + RMterminator[i], Correct)){
				RMNum[k][i] ++;
			}	
		}
		if(k == FOLDS-1){
			double out = 0;
			double stdev = 0;
			double [] stdhelp = new double[FOLDS];
			for(int l = 0; l < FOLDS; l ++){
				if(RMDen[l][i] > 0){
					stdhelp[l] = (2.0*RMNum[l][i]/RMDen[l][i]);
					out += (2.0*RMNum[l][i]/RMDen[l][i]);
				}
				else
					stdhelp[l] = 0;
			}
			out = out/FOLDS;
			stdev = score.stdev(stdhelp , out);
			System.out.print(RMterminator[i] + ": " + out + " +/-" + stdev + "\n");
		}
		RMNum[k][RMterminator.length] += RMNum[k][i];
		RMDen[k][RMterminator.length] += RMDen[k][i];
	}
	if(k == FOLDS-1){
		double out = 0;
		double stdev = 0;
		double [] stdhelp = new double[FOLDS];
		for(int l = 0; l < FOLDS; l ++){
			if(RMDen[l][RMterminator.length] > 0){
				stdhelp[l] = (2.0*RMNum[l][RMterminator.length]/RMDen[l][RMterminator.length]);
				out += (2.0*RMNum[l][RMterminator.length]/RMDen[l][RMterminator.length]);
			}
			else
				stdhelp[l] = 0;
		}
		out = out/FOLDS;
		stdev = score.stdev(stdhelp , out);
		System.out.print("ResponseModality" + ": " + out + " +/-" + stdev + "\n\n");
	}

	//=============================================
	// Here I do the scoring for Response Type
	//============================================= 

	for(int i = 0; i < RTterminator.length; i ++){
		for(int index = 0; index < testInstances.size(); index ++){
			char [] in = new char[1000];
			char [] in2 = new char[1000];
	    		String AbstractName = score.cleanString(testInstances.get(index).getName().toString());
	    		FileReader fis = new FileReader(new File(CheckFileName+AbstractName));
	    		fis.read(in);
			String [] Correct = (new String(in)).split(" ");
			fis.close();
			fis = new FileReader(new File(OutFileName + score.foldNum + "/" +AbstractName));
			fis.read(in2);
			String [] Guess = (new String(in2)).split(" ");
			fis.close();
			if(score.check("ResponseType" + RTterminator[i], Correct))
				RTDen[k][i] ++;
			if(score.check("ResponseType" + RTterminator[i], Guess)){
				RTDen[k][i] ++;
			}
			if(score.check("ResponseType" + RTterminator[i], Guess) && 
				score.check("ResponseType" + RTterminator[i], Correct)){
				RTNum[k][i] ++;
			}	
		}
		if(k == FOLDS-1){
			double out = 0;
			double stdev = 0;
			double [] stdhelp = new double[FOLDS];
			for(int l = 0; l < FOLDS; l ++){
				if(RTDen[l][i] > 0){
					stdhelp[l] = (2.0*RTNum[l][i]/RTDen[l][i]);
					out += (2.0*RTNum[l][i]/RTDen[l][i]);
				}
				else 
					stdhelp[l] = 0;
			}
			out = out/FOLDS;
			stdev = score.stdev(stdhelp , out);
			System.out.print(RTterminator[i] + ": " + out + " +/-" + stdev + "\n");
		}
		RTNum[k][RTterminator.length] += RTNum[k][i];
		RTDen[k][RTterminator.length] += RTDen[k][i];
	}
	if(k == FOLDS-1){
		double out = 0;
		double stdev = 0;
		double [] stdhelp = new double[FOLDS];
		for(int l = 0; l < FOLDS; l ++){
			if(RTDen[l][RTterminator.length] > 0){
				stdhelp[l] = (2.0*RTNum[l][RTterminator.length]/RTDen[l][RTterminator.length]);
				out += (2.0*RTNum[l][RTterminator.length]/RTDen[l][RTterminator.length]);
			}
			else
				stdhelp[l] = 0;
		}
		out = out/FOLDS;
		stdev = score.stdev(stdhelp , out);
		System.out.print("ResponseType" + ": " + out + " +/-" + stdev + "\n\n");
	}

	//=============================================
	// Here I do the scoring for Instructions
	//============================================= 

	for(int i = 0; i < Iterminator.length; i ++){
		for(int index = 0; index < testInstances.size(); index ++){
			char [] in = new char[1000];
			char [] in2 = new char[1000];
	    		String AbstractName = score.cleanString(testInstances.get(index).getName().toString());
	    		FileReader fis = new FileReader(new File(CheckFileName+AbstractName));
	    		fis.read(in);
			String [] Correct = (new String(in)).split(" ");
			fis.close();
			fis = new FileReader(new File(OutFileName + score.foldNum + "/" +AbstractName));
			fis.read(in2);
			String [] Guess = (new String(in2)).split(" ");
			fis.close();
			if(score.check("Instructions" + Iterminator[i], Correct))
				IDen[k][i] ++;
			if(score.check("Instructions" + Iterminator[i], Guess)){
				IDen[k][i] ++;
			}
			if(score.check("Instructions" + Iterminator[i], Guess) && 
				score.check("Instructions" + Iterminator[i], Correct)){
				INum[k][i] ++;
			}	
		}
		if(k == FOLDS-1){
			double out = 0;
			double stdev = 0;
			double [] stdhelp = new double[FOLDS];
			for(int l = 0; l < FOLDS; l ++){
				if(IDen[l][i] > 0){
					stdhelp[l] = (2.0*INum[l][i]/IDen[l][i]);
					out += (2.0*INum[l][i]/IDen[l][i]);
				}
				else
					stdhelp[l] = 0;
			}
			out = out/FOLDS;
			stdev = score.stdev(stdhelp , out);
			System.out.print(Iterminator[i] + ": " + out + " +/-" + stdev + "\n");
		}
		INum[k][Iterminator.length] += INum[k][i];
		IDen[k][Iterminator.length] += IDen[k][i];
	}
	if(k == FOLDS-1){
		double out = 0;
		double stdev = 0;
		double [] stdhelp = new double[FOLDS];
		for(int l = 0; l < FOLDS; l ++){
			if(IDen[l][Iterminator.length] > 0){
				stdhelp[l] = (2.0*INum[l][Iterminator.length]/IDen[l][Iterminator.length]);
				out += (2.0*INum[l][Iterminator.length]/IDen[l][Iterminator.length]);
			}
			else
				stdhelp[l] = 0;
		}
		out = out/FOLDS;
		stdev = score.stdev(stdhelp , out);
		System.out.print("Instructions" + ": " + out + " +/-" + stdev + "\n\n");
	}
	}
    }
}
