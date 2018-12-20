import java.io.*;
import java.util.*;
import java.lang.*;

public class AutoComplete {
	public static void main(String[] args) throws Exception{
		
		//initialize and populate the tree, dictTree.
		dlb dictTree = new dlb();
		dictTree.addDictToDLB();

		//initialize the user history. Use a specialized prioritized linkedList to do so.
		linkedList userHistory = new linkedList();
		userHistory.initUserText(); 	//read in the previous user history


		//Keep track of the average time. At the end, this will be calculated.
		long totalTime = 0;
		int runCount = 0;
		
		//Main body. Initialize other structures
		Scanner sc = new Scanner(System.in);
		char input = getUserInput(sc);
		String word = "";
		ArrayList<String>suggestions = new ArrayList(); //the suggestions

		//"!" is used to exit the program. "$" ends a word.
		while(input != '!'){
			try{
				word = ""; //word is constructed letter by letter in the while loop below
				while(Character.isLetter(input) || input == '\''){ //allowable characters
					long startTime = System.nanoTime(); //calculate time.
					word = word + input;
					suggestions.clear(); //empty the suggestions list from the last loop

					//Start by looking in the user's history
					userHistory.fillPossibleWords(suggestions, word);
					//if there is room left, need to look in the dictionary.
					if(suggestions.size() < 5){
						//find the location of the node that contains the prefix needed.
						dlbNode currentD = dictTree.findPrefixNode(word);
						if(currentD == null && suggestions.size() == 0){ //prefix not in history or in dictionary
							System.out.print("No predictions were found. Continue entering:\n" + word);
						}
						else if(currentD == null){ //prefix not in dictionary, but in history.
							//print suggestions
							int i = 0;
							for (String y : suggestions){
								i++;
		    					System.out.print("[" + i + "]: " + y + "   ");
		    				}
						}
						else if(currentD.daughter == null){ //means that the prefix exists, but there are no suffixes after.
							//the only suggestion from the dictionary is the current prefix (may have some in history) 
							suggestions.add(word);
							int i = 0;
							for (String y : suggestions){
								i++;
		    					System.out.print("[" + i + "]: " + y + "   ");
		    				}
						}
						else{	//the node has suffixes that can be suggested.
							dictTree.returnPossibleSuffixes(currentD, word,suggestions); //populate the array up to 5 choices.
							int i = 0;
							for (String y :suggestions){
								i++;
		    					System.out.print("[" + i + "]: " + y + "   ");
		    				}
						}
					}
		    		long endTime = System.nanoTime();
	    			System.out.println("\n(" + (endTime-startTime) + " nanoseconds)");
	    			totalTime = totalTime + (endTime-startTime);
	    			runCount++;

	    			input = getUserInput(sc); 
	    			//loop back through if this input is allowable
	    		}
	    		if(input == '$'){	//indicates end of word
	    			System.out.println("Your word is: " + word);

	    			userHistory.add(word);
	    			word = "";
					System.out.println("\nEnter a new word.");
	    		}
				else if("12345".indexOf(input) != -1){ //if input is 1,2,3,4 or 5--this is how the user chooses from options
					int number = Character.getNumericValue(input) -1;
					String z =suggestions.get(number);
					System.out.println("Your word is: " + z);
					userHistory.add(z);
					System.out.print("\nEnter a new word.");
					word = "";
				}	  			
				else if(input == '!'){ //end the program
					break;
				}
				else{
					System.out.println("Unallowed character. Try again.");	
				}
	    		input = getUserInput(sc);
    	   	}catch(Exception e){
    	   		//e.printStackTrace();
    			System.out.println("Try again. Enter a new word.");
    			word = "";
    			input = getUserInput(sc);
    		}
    	}
    	if(runCount != 0){ //check to make sure there were actually words entered to prevent division by zero error
    		System.out.println("Average time: " + ((totalTime) / (runCount)) + " nanoseconds. \nGoodbye!");
    		userHistory.sendToUserText(); //stores the user's history to the file
    	}
	}
	public static char getUserInput(Scanner sc){
		System.out.println("\nEnter your character.");
		char input = sc.next().charAt(0);
		return input;
	}
}




//Define the DLB class and the nodes for dlb.
class dlb{
	dlbNode root = new dlbNode();

	public void addDictToDLB() throws Exception{
		//Adds the dictionary text file into the DLB.
		File file = new File("dictionary.txt");
		BufferedReader br = new BufferedReader(new FileReader(file));
		String st; 
 		while ((st = br.readLine()) != null) 
    		addValue(st);
    }
	public void addValue(String word){
		//simple add method. If the word is already there, it effectively does nothing.
		dlbNode current = root;
		
		for(int i = 0; i < word.length(); i++){ //for each letter
			if (current.daughter == null){
				current.adddaughter(false, word.charAt(i));
			}
			current = current.daughter;
			while(word.charAt(i) != current.value){
				if(current.sister == null){
					current.addSister(false, word.charAt(i));
				}
				else{
					current = current.sister;	
				}
			}
		}
		current.isValid=true; //even if it is already there, it works
	}
	public dlbNode findPrefixNode(String prefix){
		//returns the node that corresponds to the longest prefix
		dlbNode currentRow = root;
		dlbNode currentInRow = root;
		for(int i = 0; i < prefix.length(); i++){
			if(currentInRow.daughter == null){
				return null;
			}
			currentInRow = currentInRow.daughter;
			currentRow = currentInRow.daughter;
			while(prefix.charAt(i) != currentInRow.value){
				if(currentInRow.sister == null){
					return null;
				}
				currentInRow = currentInRow.sister;
			}
		}
		return currentInRow;
	}
	public ArrayList<String> returnPossibleSuffixes(dlbNode prefixNode, String path, ArrayList<String> thing){
		//need to do a modified DFS
		if(prefixNode.daughter == null){
			thing.add("");
		}
		else{
			if(!thing.contains(path) && prefixNode.isValid){
				thing.add(path);
			}
			helperdfs(prefixNode.daughter, thing, path);
		}
		return thing;
	}
	public Boolean helperdfs(dlbNode node, ArrayList<String> result, String path){
		//helper method to preform DFS starting from node, putting matching results into result,
		//and using path as the letters leading up to the current node
		dlbNode current = node;
		do{
			if(!result.contains(path + current.value) && current.isValid){
				result.add(path + current.value);
				if(result.size() >= 5){ //indicates done searching since we must find 5 matches
					return true;
				}
			}
			else{ //we must continue on, since we aren't at the end of the string
				if(current.daughter != null){
					if (helperdfs(current.daughter, result, path + current.value)){
						return true;
					}
				}
			}
			current = current.sister;
		}while(current != null);
		return false;
	}
}
class dlbNode{
	char value;
	Boolean isValid;
	dlbNode sister;
	dlbNode daughter;

	public dlbNode(){}
	public dlbNode(Boolean _isValid, char _value){
		isValid = _isValid;
		value = _value;
	}

	public void addSister(Boolean _isValid, char _value){
		sister = new dlbNode(_isValid, _value);
	}
	public void adddaughter(Boolean _isValid, char _value){
		daughter = new dlbNode(_isValid, _value);
	}
	public void adddaughter(){
		daughter = new dlbNode();
	}
}



//Creating linkedList structure for use in user history:
class listNode{
	String value;
	int freq;
	listNode next;
	public listNode(){}
	public listNode(String _value, int _freq){
		value = _value;
		freq = _freq;
	}
}
class linkedList{
	listNode root = new listNode();

	public void sendToUserText() throws IOException{
		//writes the user history of the program to user_history.txt, overwriting
		BufferedWriter writer = new BufferedWriter(new FileWriter("user_history.txt"));
		writer.write("");
		listNode current = root;
		while(current.next != null){
			current = current.next;
			//System.out.println("Writing " + current.value);
			writer.append(current.value + "," + current.freq + "\n");
		}

		writer.close();

	}
	public void initUserText() throws IOException{
		//looks in user_history.txt  (generated by sendToUserText() in previous program runs)and adds this to the queue.
		// If no file exists, nothing happens.
		try{
			BufferedReader in = new BufferedReader(new FileReader("user_history.txt"));
			String x = in.readLine();
			listNode current = root;
			while(x != null){
				String[] stuff = x.split(",");
				String word = stuff[0];
				int frequency = Integer.parseInt(stuff[1]);
				current.next = new listNode(word, frequency);
				current = current.next;

				x = in.readLine();
				}
			in.close();
		}
		catch(Exception FileNotFoundException){
			//do nothing then
		}
	}
	public void fillPossibleWords(ArrayList result, String prefix){
		//modifies result in-place to add any possible auto-completes that work.
		//prefix contains the letters up to the current node
		listNode current = root;
		while(current.next != null && result.size() < 5){
			current = current.next;
			if(current.value.startsWith(prefix)){
				result.add(current.value);
			}
		}
	}
	public void add(String value){
		//adds node to linked list, sorted by frequency.
		listNode loc = findLocationByValue(value);
		if(loc == null){
			listNode current = root;
			while(current.next!= null){
				current = current.next;
			}
			current.next = new listNode(value, 1);
		}
		else if(loc == root.next){
			loc.freq++;
		}
		else{
			loc.freq ++;
			listNode current = root;
			while(current.next != loc && current.next != null){
				current = current.next;
				if(current.freq < loc.freq){
					break;
				}
			}
			if(current.freq < loc.freq){
				swap(current, loc);
			}
		}
	}

	private listNode findLocationByValue(String value){
		//search method to find the location of a word.
		listNode current = root;
		while(current.next != null){
			current = current.next;
			if(current.value.equals(value)){
				return current;
			}
		}
		return null; //means not found
	}
	private void swap(listNode a, listNode b){ //assume a is closer to the root than b
		//swap's the values within a and b. No references are changed.
		String tempValue = a.value;
		int tempFreq = a.freq;

		a.value = b.value;
		a.freq = b.freq;

		b.value = tempValue;
		b.freq = tempFreq;
	}
	public void printList(){
		//method to print out the user history, for debugging purposes
		listNode current = root;
		while(current.next != null){
			current = current.next;
			System.out.println(current.value + " " + current.freq);
		}
	}
}
