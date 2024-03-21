/*
 * This file is part of the auxiliaries of Greta.
 *
 * Greta is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Greta is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Greta.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package greta.auxiliary.MeaningMiner;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.IIndexWordID;
import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.ISynsetID;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.item.Pointer;
import edu.mit.jwi.item.Synset;
import edu.mit.jwi.item.SynsetID;
import edu.mit.jwi.morph.WordnetStemmer;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.SentenceUtils;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.TypedDependency;
import greta.core.intentions.FMLTranslator;
import greta.core.intentions.Intention;
import greta.core.intentions.IntentionEmitter;
import greta.core.intentions.IntentionPerformer;
import greta.core.util.CharacterDependent;
import static greta.core.util.CharacterDependentAdapter.getCharacterManagerStatic;
import greta.core.util.CharacterManager;
import greta.core.util.Mode;
import greta.core.util.enums.CompositionType;
import greta.core.util.id.IDProvider;
import greta.core.util.xml.XML;
import greta.core.util.xml.XMLParser;
import greta.core.util.xml.XMLTree;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.util.concurrent.TimeUnit;

/**
 *
 * This class is the core of the Meaning Miner project. It acts as a text
 * analyzer that extracts Image Schemas from the String input and build an XML
 * FML file out of it with Image Schemas type of intention
 *
 * @author Brian Ravenet
 */

// processText, processText2 is called when speech is controlled with FML Reader (e.g. direct input with FML files)

public class ImageSchemaExtractor implements MeaningMinerModule, IntentionEmitter, CharacterDependent {

    private CharacterManager charactermanager;

    private ArrayList<IntentionPerformer> intentionsPerformers = new ArrayList<>();
    private DictionnarySynsetImageSchema dictSynsetToImageSchema = new DictionnarySynsetImageSchema();
    private DictionnaryImageSchemaGesture dictImageSchemaToGesture = new DictionnaryImageSchemaGesture();
    private String WORDNETDICTPATH = "./Common/Data/wordnet30/dict";
    private String STANFORDPARSER = "./Common/Data/stanfordparser/englishPCFG.ser.gz";
    private String OPENNLPCHUNKER = "./Common/Data/opennlp/en-chunker.bin";
    private ChunkerME chunker = null;
    private WordnetStemmer wns;
    private LexicalizedParser lp;
    private TreebankLanguagePack tlp;
    private GrammaticalStructureFactory gsf;
    private IDictionary dict;
    
//    private Process process0 = null;
//    private String server_activation_command = "python ./Scripts/spaCy/spacy_server.py";

    private String lang = null;
    
    private Integer InitDelay = 7;
    
    private Process process1 = null;
    private Process process2 = null;
//    private String parser_command = "./Scripts/spaCy/run_parser_server.bat " + lang + " 'This is test sentence.'"; // stand alone version
//    private String parser_server_command = "./Scripts/spaCy/activate_py37_spacy_server.bat " + lang; // server-client version, server
    private String parser_server_command = "./Scripts/spaCy/activate_py37_spacy_server.bat "; // server-client version, server
    private String parser_client_command = "python ./Scripts/spaCy/spacy_client.py"; // server-client version, client
    private String parsed_sentence_path = "./parsed_sentences.txt";
    
    // Word Sense Disambiguation
    private Process process3 = null;
    private Process process4 = null;
//    private String wsd_server_command = "./Scripts/wsd/activate_py37_wsd.bat " + lang; // server-client version
    private String wsd_server_command = "./Scripts/wsd/activate_py37_wsd.bat "; // server-client version
    private String wsd_client_command = "python ./Scripts/wsd/wordnet_sentence-embed_client.py"; // server-client version
    private String wsd_output_path = "./wsd_output.txt";
    

    public ImageSchemaExtractor(CharacterManager cm) throws InterruptedException {
        
        System.out.println("greta.auxiliary.MeaningMiner.ImageSchemaExtractor: initialization start");
        
        setCharacterManager(cm);
        
        // Currently, update button doesn't work in NVBG_MM_controller.
        // If you want to change language, change MM_Language variable in CharacterManager.java in Util.jar and re-compile, then launch again.
        lang = this.charactermanager.get_MM_Language();
        parser_server_command = parser_server_command + lang;
        wsd_server_command = wsd_server_command + lang;
        
        //load the JWI wordnet classes
        URL url = null;
        try {
            url = new URL("file", null, WORDNETDICTPATH);
        } catch (MalformedURLException e) {
            System.out.println(e.toString());
        }
        if (url == null) {
            return;
        }

        // construct the dictionary object and open it
        dict = new Dictionary(url);
        try {
            dict.open();
        } catch (IOException ex) {
            Logger.getLogger(ImageSchemaExtractor.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        //Prepare the stemmer
        wns = new WordnetStemmer(dict);
        //Load the parser chunkerModel from the stanford parser
        lp = LexicalizedParser.loadModel(STANFORDPARSER);

        //Load the Tree Bank for english
        tlp = lp.treebankLanguagePack(); // a PennTreebankLanguagePack for English
        if (tlp.supportsGrammaticalStructures()) {
            gsf = tlp.grammaticalStructureFactory();
        }

        //Load the OpenNLP Chunker
        InputStream modelIn = null;
        ChunkerModel chunkerModel = null;
        try {
            modelIn = new FileInputStream(OPENNLPCHUNKER);
            chunkerModel = new ChunkerModel(modelIn);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ImageSchemaExtractor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ImageSchemaExtractor.class.getName()).log(Level.SEVERE, null, ex);
        }
        chunker = new ChunkerME(chunkerModel);
        
        ////////////////////////////////////////////////
        /// For multilingual processing              ///
        ////////////////////////////////////////////////
        String[] splitted_command = parser_server_command.split(" ");
        try {
            process1 = new ProcessBuilder(splitted_command).redirectErrorStream(true).start();
            outputConsumer(process1, false, true); //block = false, output = true
            System.out.println("### Parser server was activated from greta.auxiliary.MeaningMiner.ImageSchemaExtractor.");
        } catch (IOException e) {
            System.out.println(e);
        }
        splitted_command = wsd_server_command.split(" ");
        try {
            process3 = new ProcessBuilder(splitted_command).redirectErrorStream(true).start();
            outputConsumer(process3, false, true); //block = false, output = true
            System.out.println("### WSD(word sense disambiguation) server was activated from greta.auxiliary.MeaningMiner.ImageSchemaExtractor.");
        } catch (IOException e) {
            System.out.println(e);
        }
        System.out.println("-> Waiting for parser/WSD initialization : " + InitDelay.toString() + " sec.");
        TimeUnit.SECONDS.sleep(InitDelay);            
        
        System.out.println("greta.auxiliary.MeaningMiner.ImageSchemaExtractor: initialization completed");        
        
    }

    /**
     * process an input string and split it by sentences first, then, for each
     * sentence, it will use the stanford lexical parser to identify the
     * grammatical role of each word, the OpenNLP for the chunks, and will use
     * WordNet to retrieve a semantic context for these words to be used to
     * extract Image Schemas. Using all these informations, it will build an FML
     * document containing Ideational Units and Image Schema type of gestures
     * where invariant are defined.
     *
     * @param input the input string to be processed
     */
    @Override
    public void processText(String input) {
        
        System.out.println("greta.auxiliary.MeaningMiner.ImageSchemaExtractor.processText()");
        
        try {
            List<Intention> intentions = processText_2(input);
        } catch (TransformerException | ParserConfigurationException | SAXException | IOException ex) {
            Logger.getLogger(ImageSchemaExtractor.class.getName()).log(Level.SEVERE, null, ex);
        }

    }    
    
    //Insert a new xml imageschema in the XML tree. This object will be completed later or even deleted if no Image Schema could be put there.
    private XMLTree createXMLImageSchema(XMLTree fmlRoot, int countImageSchema, int countTimeMarker) {
        XMLTree imageschema = fmlRoot.createChild("imageschema");
        imageschema.setAttribute("importance", "1.0");
        imageschema.setAttribute("id", "im_" + countImageSchema);
        imageschema.setAttribute("start", "s1:tm" + (countTimeMarker) + "-0.2");

        return imageschema;
    }

//    //Set the Image Schema to this xml imageschema. It checks if this was the root of the sentence to decide if it should be the main of the current ideational unit.
//    private void setImageSchemaType(XMLTree imageschema, String imageRef, String pos, XMLTree ideationalUnit, List<TypedDependency> tdl, Integer indexWord) {
//
//        imageschema.setAttribute("type", imageRef);
//        imageschema.setAttribute("indexword", indexWord.toString());
//        imageschema.setAttribute("POSroot", pos);
//
//        for (TypedDependency td : tdl) {
//            if (td.dep().index() == indexWord) {
//                if (td.reln().toString().equals("root")) {
//                    ideationalUnit.setAttribute("main", imageschema.getAttribute("id"));
//                }
//                break;
//            }
//        }
//    }

    // Set the Image Schema to this xml imageschema. It checks if this was the root of the sentence to decide if it should be the main of the current ideational unit.
    private void setImageSchemaType_multilingual(XMLTree imageschema, String imageRef, String pos, XMLTree ideationalUnit, String dep, Integer indexWord) {

        imageschema.setAttribute("type", imageRef);
        imageschema.setAttribute("indexword", indexWord.toString());
        imageschema.setAttribute("POSroot", pos);

//        if (dep.equals("root")) {
        if (dep.equals("ROOT")) {
            ideationalUnit.setAttribute("main", imageschema.getAttribute("id"));
        }
    }

    private void setImageSchemaType_english(XMLTree imageschema, String imageRef, String pos, XMLTree ideationalUnit, List<TypedDependency> tdl, Integer indexWord) {

        imageschema.setAttribute("type", imageRef);
        imageschema.setAttribute("indexword", indexWord.toString());
        imageschema.setAttribute("POSroot", pos);

        for (TypedDependency td : tdl) {
            if (td.dep().index() == indexWord) {
                if (td.reln().toString().equals("root")) {
                    ideationalUnit.setAttribute("main", imageschema.getAttribute("id"));
                }
                break;
            }
        }
    }
    
    //Return the Image Schemas that can be found for this Synset (meaning).
    //This is a recursive function that starts at the synset of the word,
    //looks for Image Schemas and then continue up the tree by following the hypernyms (more global meaning) of the current set
    private Set<String> getImageSchemas(ISynset synset, int depth) {
        
        // What we need for this function if we don't use synset object
        // - POS, synset id, method to get related synset

        Set<String> toReturn = new HashSet<>();
        switch (synset.getPOS()) {
            case NOUN:
                if (dictSynsetToImageSchema.getImageSchemasForNoun(synset.getID().toString()) != null) {
                    toReturn.addAll(dictSynsetToImageSchema.getImageSchemasForNoun(synset.getID().toString()));
                }
                break;
            case VERB:
                if (dictSynsetToImageSchema.getImageSchemasForVerb(synset.getID().toString()) != null) {
                    toReturn.addAll(dictSynsetToImageSchema.getImageSchemasForVerb(synset.getID().toString()));
                }
                break;
            case ADJECTIVE:
                if (dictSynsetToImageSchema.getImageSchemasForAdjective(synset.getID().toString()) != null) {
                    toReturn.addAll(dictSynsetToImageSchema.getImageSchemasForAdjective(synset.getID().toString()));
                }
                break;
            case ADVERB:
                if (dictSynsetToImageSchema.getImageSchemasForAdverb(synset.getID().toString()) != null) {
                    toReturn.addAll(dictSynsetToImageSchema.getImageSchemasForAdverb(synset.getID().toString()));
                }
                break;
            default:
                break;
        }
        List<ISynsetID> relatedSynset = synset.getRelatedSynsets(Pointer.HYPERNYM);
        //FOR NOW WE STOP AS SOON AS WE FIND ONE IMAGE SCHEMA. If I remove the toReturn.size()>0,
        //we will continue as long as there is an hypernym to this synset
        if (relatedSynset.isEmpty() || depth <= 0 || toReturn.size() > 0) {
            return toReturn;
        } else {
            ISynset next = dict.getSynset(relatedSynset.get(0));
            toReturn.addAll(getImageSchemas(next, depth - 1));
            return toReturn;
        }
    }

    //Perform the simplified Lesk algorithm for Word disambiguation.
    //It looks up in the WordNet dictionnary the glossary of each meaning for the word.
    //The meaning that has more word in common with the current context is the selected meaning
    public ISynset simplifiedLesk(IIndexWord idxWord, String context) {
        if (idxWord != null && idxWord.getWordIDs().size() > 0 && context != null) {
            ISynset bestSense = dict.getWord(idxWord.getWordIDs().get(0)).getSynset();
            int maxOverlap = 0;
            String[] contextArray = context.split(" ");
            for (IWordID otherSense : idxWord.getWordIDs()) {
                IWord word = dict.getWord(otherSense);
                String[] glossArray = word.getSynset().getGloss().split(" ");
                int overlap = 0;
                for (String cont : contextArray) {
                    if (cont.length() < 4) {
                        continue;
                    }
                    for (String glos : glossArray) {
                        if (glos.length() < 4) {
                            continue;
                        }
                        if (cont.toLowerCase().equals(glos.toLowerCase())) {
                            overlap++;
                        }
                    }
                }
                if (overlap > maxOverlap) {
                    maxOverlap = overlap;
                    bestSense = word.getSynset();
                }
            }
            return bestSense;
        }
        return null;
    }

    private boolean isWithinPitchAccent(List<int[]> listPitchAccent, int indexWord) {

        for (int[] pa : listPitchAccent) {
            if (indexWord >= pa[0] && indexWord <= pa[1]) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void addIntentionPerformer(IntentionPerformer ip) {
        intentionsPerformers.add(ip);
    }

    @Override
    public void removeIntentionPerformer(IntentionPerformer ip) {
        intentionsPerformers.remove(ip);
    }

    @Override
    public void onCharacterChanged() {

    }

    /**
     * @return the characterManager
     */
    @Override
    public CharacterManager getCharacterManager() {
        if(charactermanager==null)
            charactermanager = CharacterManager.getStaticInstance();
        return charactermanager;
    }

    /**
     * @param characterManager the characterManager to set
     */
    @Override
    public void setCharacterManager(CharacterManager characterManager) {
        if(this.charactermanager!=null)
            this.charactermanager.remove(this);
        this.charactermanager = characterManager;
        //characterManager.add(this);
    }
    public List<Intention> processText_2(String input) throws TransformerConfigurationException, TransformerException, ParserConfigurationException, SAXException, IOException {
        
        List<Intention> intentions;
        if(charactermanager.get_MM_Multilingual()){
            intentions = processText_2_multilingual(input);
        }
        else{
            intentions = processText_2_english(input);        
        }
        
        return intentions;
    }
        
    @SuppressWarnings("empty-statement")
    public List<Intention> processText_2_multilingual(String input) throws TransformerConfigurationException, TransformerException, ParserConfigurationException, SAXException, IOException {
        
        System.out.println("greta.auxiliary.MeaningMiner.ImageSchemaExtractor.processText_2(): start");
        
        long t1 = System.currentTimeMillis();
        
        //System.out.println(input);
        XMLParser xmlParser = XML.createParser();
        XMLTree inputXML = xmlParser.parseBuffer(input);
        List<int[]> listPitchAccent = new ArrayList<>();
        //remove the description tag that creates trouble with the parser and
        //retrieve the pitch accent for future access

        for (XMLTree xmltbml : inputXML.getChildren()) {
            for (XMLTree xmlt : xmltbml.getChildren()) {
                if (xmlt.isNamed("description")) {
                    xmltbml.removeChild(xmlt);
                }
                if (xmlt.isNamed("pitchaccent")) {
                    int start = Integer.parseInt(xmlt.getAttribute("start").replace("s1:tm", ""));
                    int end = Integer.parseInt(xmlt.getAttribute("end").replace("s1:tm", ""));
                    int[] pitchAccent = {start, end};
                    listPitchAccent.add(pitchAccent);
                }
            }
        }
        //Get rid of the xml tags for text processing
        String tagFreeInput = inputXML.toString().replaceAll("<[^>]+>", "");
        tagFreeInput = tagFreeInput.replace("tmp/from-fml-apml.pho", "");
        List<XMLTree> imageSchemasGenerated = new ArrayList<>();

        int countTimeMarkers = 0;
        int countSentenceMarkers = 0; // # of current processed words
        int countIdeationalUnit = 0;
        int countImageSchema = 0;

        //prepare the reader for our value
        tagFreeInput = tagFreeInput.trim().replace(" +", " ");
        String tmp = tagFreeInput;
        tagFreeInput = "";
        for(String line:tmp.split("\n")){
//            System.out.println("### delete odd space: " + line);
            if(!line.isEmpty()){
                tagFreeInput = tagFreeInput + line + "\n";
//                System.out.println("deleted");
//                System.out.println(tagFreeInput);
            }
        }
        tagFreeInput = tagFreeInput.replace(",", "_");

        //*********   FIRST WE START BY AUGMENTING THE TEXT ***************
        //prepare the XML structure to store the speech in a FML-APML way
        XMLTree fmlApmlRoot = XML.createTree("fml-apml");
        XMLTree bmlRoot = fmlApmlRoot.addChild(inputXML);
        XMLTree fmlRoot = fmlApmlRoot.createChild("fml");
        
        long t2 = System.currentTimeMillis();
        
        //Call spaCy parser

//        parser_client_command = parser_client_command + " " + lang + " \"" + tagFreeInput + "\"";
        ArrayList<String> parser_splitted_command = new ArrayList<>();
        String[] splitted_tmp = parser_client_command.split(" ");
        parser_splitted_command.add(splitted_tmp[0]);
        parser_splitted_command.add(splitted_tmp[1]);
//        parser_splitted_command.add(parser_client_command);
        parser_splitted_command.add(lang);
        parser_splitted_command.add(tagFreeInput);
        try {
            System.out.println("### parser_splitted_command:");
            System.out.println(parser_splitted_command);
            process2 = new ProcessBuilder(parser_splitted_command).redirectErrorStream(true).start();
            outputConsumer(process2, true, true);
        } catch (IOException e) {
            System.out.println(e);
        }

        long t3 = System.currentTimeMillis();

        String[] sentenceArray = new String[]{};
        try{
            List<String> sentenceList = Files.readAllLines(Paths.get(parsed_sentence_path), StandardCharsets.UTF_8);
            sentenceArray = sentenceList.stream().toArray(String[]::new);
        } catch (IOException e) {
            System.out.println(e);
        }

        long t4 = System.currentTimeMillis();

        System.out.println("### Parsed sentences: start");
        for(String sentence:sentenceArray){
            System.out.println(sentence);
        }
        System.out.println("### Parsed sentences: end");
                
        //split by sentences, for each sentences:
//        for (List<HasWord> sentence : new DocumentPreprocessor(sr)) {
        String[] wsd_sentenceArray = new String[]{};
        String[][] sentenceSeparatedSynsetIDArray = new String[sentenceArray.length][];
        for(int j=0; j<sentenceArray.length;j++){
            
            String sentence = sentenceArray[j];
            System.out.println("Input sentence: " + sentence);
            String[] origSentence_wordArray = sentence.split("#");
            String origSentence = origSentence_wordArray[0];
            String[] wordArray = origSentence_wordArray[1].split("/");
            
            imageSchemasGenerated.clear();
            boolean hasVerb = false;
            boolean afterVerb = false;
            boolean negation = false;

            Set<String> imageSchemas = new HashSet<>();


//            String[] listToken = new String[sentence.length()];
//            String[] listPos = new String[sentence.length()];
            
            //A first loop that checks if there is a verb in the sentence and prepare the sentence for chunking
            for (int i = 0; i < wordArray.length; i++) {
                
                String[] wordProperty = wordArray[i].split(" ");
                String orig = wordProperty[0];
                String lemma = wordProperty[1];
                String pos = wordProperty[2];
                String dep = wordProperty[3];
                String chunktag = wordProperty[4];

//                listToken[i] = orig;
//                listPos[i] = pos;
//                if (type != null && (type.equals("VB") || type.equals("VBD") || type.equals("VBG") || type.equals("VBN") || type.equals("VBZ") || type.equals("VBP"))) {
                if (pos != null && (pos.equals("VERB"))) {
                    hasVerb = true;
                }

            }
            
            //retrieve the BIO (begin inside out) tags for the chunks
//            String chunktag[] = chunker.chunk(listToken, listPos);
            XMLTree previousImageSchema = createXMLImageSchema(fmlRoot, countImageSchema++, countSentenceMarkers);

            //XMLTree imageSchema = fmlRoot.createChild("imageschema");
            //imageSchema.setAttribute("id", "id_" + countImageSchema++);
            //The second loop
            ArrayList<String> lemmaArray = new ArrayList<>();
            ArrayList<String> posSingleLetterArray = new ArrayList<>();
            for (int i = 0; i < wordArray.length; i++) {

                String[] wordProperty = wordArray[i].split(" ");
                String value = wordProperty[0];
                String lemma = wordProperty[1];
                String posTag = wordProperty[2];
                String dep = wordProperty[3];
                String chunktag = wordProperty[4];
                
                String posSingleLetter;

                imageSchemas.clear();

                //mapping spaCy pos to wordnet POS
                //this part can act as a filter
                POS pos = null;
                switch (posTag) {
                    //noun singular or mass
                    case "NOUN":
                        pos = POS.NOUN;
                        posSingleLetter = "n";
                        break;
                    //pronoun
                    case "PRON":
                        pos = POS.NOUN;
                        posSingleLetter = "n";
                        break;
                    //proper noun
                    case "PROPN":
                        pos = POS.NOUN;
                        posSingleLetter = "n";
                        break;
                    //Verb
                    case "VERB":
                        pos = POS.VERB;
                        posSingleLetter = "v";
                        break;
                    //Adjective
                    case "ADJ":
                        pos = POS.ADJECTIVE;
                        posSingleLetter = "a";
                        break;
                    //particle
                    case "PART":
                        pos = POS.ADVERB;
                        posSingleLetter = "r";
                        break;
                    //adverb
                    case "ADV":
                        pos = POS.ADVERB;
                        posSingleLetter = "r";
                        break;
                    default:
                        pos = null;
                        posSingleLetter = "";
                        break;
                }

                //begin wordnet code
                List<String> stems = new ArrayList<>();

                //We retrieve the stems for the word.
                stems.add(lemma);

                //check if we are at the verb
                if (pos == POS.VERB || chunktag.equals("B-VP")) {
                    afterVerb = true;
                }

                if (!hasVerb || (hasVerb && afterVerb)) {
                    //for each stem (actually, always only one stem though)
                    for (String stem : stems) {
                        //we retrieve the word from wordnet
//                        System.out.println(stem);
//                        System.out.println(pos);
                        lemmaArray.add(lemma);
                        posSingleLetterArray.add(posSingleLetter);
                    }
                }
                else{
                    lemmaArray.add("_");
                    posSingleLetterArray.add("_");
                }
            }

            try{
                ArrayList<String> wsd_splitted_command = new ArrayList<>();
                splitted_tmp = wsd_client_command.split(" ");
                wsd_splitted_command.add(splitted_tmp[0]);
                wsd_splitted_command.add(splitted_tmp[1]);
                wsd_splitted_command.add(lang);
                wsd_splitted_command.add(origSentence);
                wsd_splitted_command.add(lemmaArray.toString());
                wsd_splitted_command.add(posSingleLetterArray.toString());
                try {
                    System.out.println(wsd_splitted_command);
                    process4 = new ProcessBuilder(wsd_splitted_command).redirectErrorStream(true).start();
                    System.out.println("### wsd parser (language): " + lang);
                    System.out.println("### wsd parser (sentence): " + origSentence);
                    System.out.println("### wsd parser (stem): " + lemmaArray.toString());
                    System.out.println("### wsd parser (pos): " + posSingleLetterArray.toString());
                    outputConsumer(process4, true, true);

                    wsd_sentenceArray = new String[]{};
                    try{
                        List<String> sentenceList = Files.readAllLines(Paths.get(wsd_output_path), StandardCharsets.UTF_8);
                        wsd_sentenceArray = sentenceList.stream().toArray(String[]::new);
                    } catch (IOException e) {
                        System.out.println(e);
                    }
                } catch (IOException e) {
                    System.out.println(e);
                }

            } catch (Exception e) {
                System.out.println(e);
            }
            
            System.out.println("### WSD line ###");
            for(String wsd_line:wsd_sentenceArray){
                System.out.println(wsd_line);
            }
            
            System.out.println("### WSD result: SynsetID (String) ###");
            String wsd_result = wsd_sentenceArray[0];
            wsd_result = wsd_result.replace(" ", "");
            wsd_result = wsd_result.replace("[", "");
            wsd_result = wsd_result.replace("]", "");
            wsd_result = wsd_result.replace("'", "");
            System.out.println(wsd_result);
            
            String[] wsdStringArray = wsd_result.split(",");
            
//            // Filter empty component
//            List<String> filteredWSDList = new ArrayList<>();
//            boolean noComponent = true;
//            for(String comp:wsdStringArray){
//                if(!comp.isEmpty()){
//                    filteredWSDList.add(comp);
//                    noComponent = false;
//                }
//            }
//            wsdStringArray = filteredWSDList.toArray(new String[0]);
//            System.out.println(wsdStringArray.length);

            System.out.println("### WSD result: SynsetID (StringArray) ###");
            System.out.println(wsdStringArray.length);
            for(String wsd_synsetID:wsdStringArray){
                System.out.println(wsd_synsetID);
            }
            
            sentenceSeparatedSynsetIDArray[j] = wsdStringArray;
            
            System.out.println("##########################################################");
            System.out.println("##########################################################");
            System.out.println("##########################################################");
        }
        
        for(int n=0; n<sentenceSeparatedSynsetIDArray.length;n++){
            System.out.println(sentenceSeparatedSynsetIDArray[n].length);
        }
        
        long t5 = System.currentTimeMillis();
        System.out.println("processText_2_multilingual:");
        System.out.println(t5-t1);

        XMLTree previousImageSchema = createXMLImageSchema(fmlRoot, countImageSchema++, countSentenceMarkers);
        
        wsd_sentenceArray = new String[]{};
        for(int sentenceIndex=0; sentenceIndex < sentenceArray.length;sentenceIndex++){

            String sentence = sentenceArray[sentenceIndex];
            System.out.println("Input sentence: " + sentence);
            String[] origSentence_wordArray = sentence.split("#");
            String origSentence = origSentence_wordArray[0];
            String[] wordArray = origSentence_wordArray[1].split("/");
            
            imageSchemasGenerated.clear();
            boolean hasVerb = false;
            boolean afterVerb = false;
            boolean negation = false;

            Set<String> imageSchemas = new HashSet<>();

            System.out.println("### Ideationalunit");
            System.out.println(countTimeMarkers);
            System.out.println(origSentence.length());
            System.out.println(sentence.length());
            
            //Prepare the ideational unit structure
            XMLTree ideationalUnit = fmlRoot.createChild("ideationalunit");
            ideationalUnit.setAttribute("id", "id_" + countIdeationalUnit++);
            ideationalUnit.setAttribute("importance", "1.0");
            ideationalUnit.setAttribute("start", "s1:tm" + countTimeMarkers + "-0.2");
            ideationalUnit.setAttribute("end", "s1:tm" + (countTimeMarkers + origSentence.length() - 1) + "-0.2");
            
////            for(String[] SynsetIDArray:sentenceSeparatedSynsetIDArray){
////                for(int SynsetIDIndex = 0; SynsetIDIndex < SynsetIDArray.length; SynsetIDIndex++){
//
//            for(int SynsetIDIndex = 0; SynsetIDIndex < sentenceSeparatedSynsetIDArray[sentenceIndex].length; SynsetIDIndex++){
//                    
//                    if(sentenceSeparatedSynsetIDArray[sentenceIndex][SynsetIDIndex].isEmpty()){
//                        continue;
//                    }
//                    
//                    ISynsetID tmp_synsetID;
//                    tmp_synsetID = SynsetID.parseSynsetID(sentenceSeparatedSynsetIDArray[sentenceIndex][SynsetIDIndex]);
//                    ISynset synset = dict.getSynset(tmp_synsetID);
//
//                    Set<String> imscSet = getImageSchemas(synset, 10);
//                    System.out.println("Synset Added: " + synset.getID().toString());
//                    imageSchemas.addAll(imscSet);            
//            }
////                }
////            }
            
            for (int wordIndex = 0; wordIndex < wordArray.length; wordIndex++) {

                String[] wordProperty = wordArray[wordIndex].split(" ");
                String value = wordProperty[0];
                String lemma = wordProperty[1];
                String posTag = wordProperty[2];
                String dep = wordProperty[3];
                String chunktag = wordProperty[4];

                String posSingleLetter;
                imageSchemas.clear();
                
                for(int n=0;n<sentenceSeparatedSynsetIDArray[sentenceIndex].length;n++){
                    System.out.println("SynsetIDArray " + n + " " + sentenceSeparatedSynsetIDArray[sentenceIndex][n]);
                }
                for(int n=0;n<wordArray.length;n++){
                    System.out.println("wordArray " + n + " " + wordArray[n]);
                }
                
                System.out.println(sentenceSeparatedSynsetIDArray.length);
                System.out.println(sentenceIndex);
                System.out.println(sentenceSeparatedSynsetIDArray[sentenceIndex].length);
                System.out.println(wordArray.length);
                System.out.println(wordIndex);
                System.out.println();
                
                //mapping spaCy pos to wordnet POS
                //this part can act as a filter
                POS pos = null;
                switch (posTag) {
                    //noun singular or mass
                    case "NOUN":
                        pos = POS.NOUN;
                        posSingleLetter = "n";
                        break;
                    //pronoun
                    case "PRON":
                        pos = POS.NOUN;
                        posSingleLetter = "n";
                        break;
                    //proper noun
                    case "PROPN":
                        pos = POS.NOUN;
                        posSingleLetter = "n";
                        break;
                    //Verb
                    case "VERB":
                        pos = POS.VERB;
                        posSingleLetter = "v";
                        break;
                    //Adjective
                    case "ADJ":
                        pos = POS.ADJECTIVE;
                        posSingleLetter = "a";
                        break;
                    //particle
                    case "PART":
                        pos = POS.ADVERB;
                        posSingleLetter = "r";
                        break;
                    //adverb
                    case "ADV":
                        pos = POS.ADVERB;
                        posSingleLetter = "r";
                        break;
                    default:
                        pos = null;
                        posSingleLetter = "";
                        break;
                }

                try{
                    if(!sentenceSeparatedSynsetIDArray[sentenceIndex][wordIndex].isEmpty() && (pos != null)){
                        ISynsetID tmp_synsetID;
                        tmp_synsetID = SynsetID.parseSynsetID(sentenceSeparatedSynsetIDArray[sentenceIndex][wordIndex]);
                        ISynset synset = dict.getSynset(tmp_synsetID);

                        Set<String> imscSet = getImageSchemas(synset, 10);
                        System.out.println("Synset Added: " + synset.getID().toString() + " " + synset.getPOS().toString());
                        imageSchemas.addAll(imscSet);
                    }
                    else{
                        continue;
                    }
                }catch(Exception e){
                    System.out.println(e);
                    continue;
                }

                
                

                
//                System.out.println("### CHECK ###");
//                System.out.println(sentenceSeparatedSynsetIDArray.length);
//                System.out.println(sentenceIndex);
//                System.out.println(sentenceSeparatedSynsetIDArray[sentenceIndex].length);
//                System.out.println(wordIndex);
//                System.out.println(wordArray.length);
//                System.out.println(wordArray[wordIndex]);
                
                String tmp_SynsetID = "";
                try{
                    tmp_SynsetID = sentenceSeparatedSynsetIDArray[sentenceIndex][wordIndex];
                }catch(Exception e){
                    System.out.println(e);
                    continue;
                }
                
                if(tmp_SynsetID != ""){

                    try{

                        //Depending on the chunk, I will construct the image schema differently
                        //If we begin a new chunk (the B tag), I either delete an empty previous Image Schema (created as a placeholder) or
                        //if the previous is not empty, I close it with an end tag and I open a new one (with createXMLImageSchema).
                        if (chunktag.startsWith("B")) {

                            if (previousImageSchema != null) {
                                if (previousImageSchema.getAttribute("type") == "") {
                                    fmlRoot.removeChild(previousImageSchema);
                                    previousImageSchema = createXMLImageSchema(fmlRoot, countImageSchema++, countSentenceMarkers + wordIndex - 1);

                                } else {
                                    if (!imageSchemas.isEmpty()) {
                                        previousImageSchema.setAttribute("end", "s1:tm" + (countSentenceMarkers + wordIndex - 1) + "-0.2");
                                        imageSchemasGenerated.add(previousImageSchema);
                                        previousImageSchema = createXMLImageSchema(fmlRoot, countImageSchema++, countSentenceMarkers + wordIndex - 1);

                                    } else {
                                        //This is used so the Image Schema coming from a previous chunk can span unto the next chunk until there is a new Image Schema.
                                        previousImageSchema.setAttribute("previous", "true");
                                    }
                                }
                            } else {
                                previousImageSchema = createXMLImageSchema(fmlRoot, countImageSchema++, countSentenceMarkers + wordIndex - 1);

                            }
                            //If an Image Schema is identified for this word, I insert it
                            if (!imageSchemas.isEmpty()) {
                                String imageRef = imageSchemas.iterator().next().toLowerCase();
                                previousImageSchema.setAttribute("type", imageRef);
                                previousImageSchema.setAttribute("POSroot", pos.toString());
                            }

                        }
                        //If I am within a chunk, I should just update the existing previous Image Schema that
                        //has been created for the whole chunk during the B case, OR create a new one in case of multiple instance of a noun in a NP or verb in a VP
                        if (chunktag.startsWith("I")) {
                            if (previousImageSchema == null) {
                                previousImageSchema = createXMLImageSchema(fmlRoot, countImageSchema++, countSentenceMarkers + wordIndex - 1);
                                //Check if the Image Schema was coming from a previous chunk
                            } else if (previousImageSchema.getAttribute("previous") == "true" && !imageSchemas.isEmpty()) {

                                previousImageSchema.setAttribute("end", "s1:tm" + (countSentenceMarkers + wordIndex - 1) + "-0.2");
                                imageSchemasGenerated.add(previousImageSchema);
                                previousImageSchema = createXMLImageSchema(fmlRoot, countImageSchema++, countSentenceMarkers + wordIndex - 1);

                            }
                            if (!imageSchemas.isEmpty()) {
                                switch (chunktag) {
                                    case "I-NP": {
                                        if (previousImageSchema.getAttribute("POSroot") == "") {
                                            setImageSchemaType_multilingual(previousImageSchema, imageSchemas.iterator().next().toLowerCase(), pos.toString(), ideationalUnit, dep, wordIndex);
                                        } else {
                                            if (previousImageSchema.getAttribute("POSroot").equals(POS.NOUN.toString())) {
                                                setImageSchemaType_multilingual(previousImageSchema, imageSchemas.iterator().next().toLowerCase(), pos.toString(), ideationalUnit, dep, wordIndex);

                                            }
                                            if (pos.equals(POS.ADJECTIVE) || isWithinPitchAccent(listPitchAccent, wordIndex) || pos.equals(POS.NOUN) && !previousImageSchema.getAttribute("POSroot").equals(POS.ADJECTIVE.toString())) {
                                                if (previousImageSchema.getAttribute("type") == "") {
                                                    fmlRoot.removeChild(previousImageSchema);
                                                } else {
                                                    previousImageSchema.setAttribute("end", "s1:tm" + (countSentenceMarkers + wordIndex - 1) + "-0.2");
                                                    imageSchemasGenerated.add(previousImageSchema);
                                                }
                                                XMLTree imageschema = createXMLImageSchema(fmlRoot, countImageSchema++, countSentenceMarkers + wordIndex - 1);
                                                setImageSchemaType_multilingual(imageschema, imageSchemas.iterator().next().toLowerCase(), pos.toString(), ideationalUnit, dep, wordIndex);
                                                previousImageSchema = imageschema;
                                            }
                                        }
                                        break;
                                    }

                                    case "I-VP": {
                                        if (previousImageSchema.getAttribute("POSroot") == "") {
                                            setImageSchemaType_multilingual(previousImageSchema, imageSchemas.iterator().next().toLowerCase(), pos.toString(), ideationalUnit, dep, wordIndex);
                                        } else {
                                            if (previousImageSchema.getAttribute("POSroot").equals(POS.VERB.toString())) {
                                                setImageSchemaType_multilingual(previousImageSchema, imageSchemas.iterator().next().toLowerCase(), pos.toString(), ideationalUnit, dep, wordIndex);
                                            }
                                            if (pos.equals(POS.ADVERB) || isWithinPitchAccent(listPitchAccent, wordIndex) || pos.equals(POS.VERB) && !previousImageSchema.getAttribute("POSroot").equals(POS.ADVERB.toString())) {
                                                if (previousImageSchema.getAttribute("type") == "") {
                                                    fmlRoot.removeChild(previousImageSchema);
                                                } else {
                                                    previousImageSchema.setAttribute("end", "s1:tm" + (countSentenceMarkers + wordIndex- 1) + "-0.2");
                                                    imageSchemasGenerated.add(previousImageSchema);
                                                }
                                                XMLTree imageschema = createXMLImageSchema(fmlRoot, countImageSchema++, countSentenceMarkers + wordIndex- 1);
                                                setImageSchemaType_multilingual(imageschema, imageSchemas.iterator().next().toLowerCase(), pos.toString(), ideationalUnit, dep, wordIndex);
                                                previousImageSchema = imageschema;
                                            }
                                        }
                                        break;
                                    }

                                    default: { //I-PRT, I-INTJ, I-SBAR, I-ADJP, I-ADVP, I-PP
                                        if (previousImageSchema.getAttribute("POSroot") == "") {
                                            setImageSchemaType_multilingual(previousImageSchema, imageSchemas.iterator().next().toLowerCase(), pos.toString(), ideationalUnit, dep, wordIndex);

                                        } else {
                                            if (previousImageSchema.getAttribute("type") == "") {
                                                fmlRoot.removeChild(previousImageSchema);
                                            } else {
                                                previousImageSchema.setAttribute("end", "s1:tm" + (countSentenceMarkers + wordIndex- 1) + "-0.2");
                                                imageSchemasGenerated.add(previousImageSchema);
                                            }
                                            XMLTree imageschema = createXMLImageSchema(fmlRoot, countImageSchema++, countSentenceMarkers + wordIndex- 1);
                                            setImageSchemaType_multilingual(imageschema, imageSchemas.iterator().next().toLowerCase(), pos.toString(), ideationalUnit, dep, wordIndex);
                                            previousImageSchema = imageschema;
                                        }
                                        break;
                                    }
                                }
                            }
                        }

                        if (chunktag.startsWith("O")) {
                            if (previousImageSchema != null) {
                                if (previousImageSchema.getAttribute("type") == "") {
                                    fmlRoot.removeChild(previousImageSchema);
                                } else {
                                    previousImageSchema.setAttribute("end", "s1:tm" + (countSentenceMarkers + wordIndex - 1) + "-0.2");
                                    imageSchemasGenerated.add(previousImageSchema);
                                }

                            }
                            previousImageSchema = null;
                        }
                    }
                    catch(Exception e){
                        System.out.println("ImageSchemaExtractor: " + e);
                        previousImageSchema = null;
                        continue;
                    }
                }

                negation = posTag.equalsIgnoreCase("DT") && value.equalsIgnoreCase("no") || posTag.equalsIgnoreCase("RB") && value.equalsIgnoreCase("not");
                //System.out.println(negation);

            }
            countSentenceMarkers += wordArray.length;
            if (previousImageSchema != null) {
                if (previousImageSchema.getAttribute("type").equals("")) {
                    fmlRoot.removeChild(previousImageSchema);
                } else {
                    previousImageSchema.setAttribute("end", "s1:tm" + (countSentenceMarkers - 1) + "-0.2");
                    imageSchemasGenerated.add(previousImageSchema);
                }

            }

            //If no root could be found,
            if (!ideationalUnit.hasAttribute("main")) {
                //If no root could be found, delete the ideational unit.
                if (imageSchemasGenerated.size() > 0) {
                    ideationalUnit.setAttribute("main", imageSchemasGenerated.get(0).getAttribute("id"));
                } else {
                    fmlRoot.removeChild(ideationalUnit);
                }
            }

        }

        long t6 = System.currentTimeMillis();
        
        long preparation = t2 - t1;
        long parsing = t3 - t2;
        long result_load = t4 - t3;
        long wsd = t5 - t4;
        long gesture_selection = t6 - t5;
        long total = t6-t1;
        System.out.println("Duration: Preparation " + preparation + " milli sec.");
        System.out.println("Duration: Parsing " + parsing + " milli sec.");
        System.out.println("Duration: Result loading " + result_load + " milli sec.");
        System.out.println("Duration: word sense disambiguation " + wsd + " milli sec.");
        System.out.println("Duration: Gesture selection " + gesture_selection + " milli sec.");
        System.out.println("Duration: Total " + total + " milli sec.");

        try{
            String fmlApmlRoot_v1=fmlApmlRoot.toString().replace("<fml-apml>","").replace("<fml>","").replace("</fml>","").replace("</fml-apml>","");
            fmlApmlRoot_v1= fmlApmlRoot_v1.replace("</bml>","</bml>\n<fml>").replace("?>", "?>\n<fml-apml>").replace(":tm0",":tm1")+"\n</fml>\n</fml-apml>";
            System.out.println(fmlApmlRoot_v1);
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document document = docBuilder.parse(new InputSource(new StringReader(fmlApmlRoot_v1)));
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(document);
            FileWriter writer = new FileWriter(new File(System.getProperty("user.dir")+"\\fml_output_mm.xml"));
            StreamResult result = new StreamResult(writer);
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(source, result);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(ImageSchemaExtractor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(ImageSchemaExtractor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ImageSchemaExtractor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerConfigurationException ex) {
            Logger.getLogger(ImageSchemaExtractor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerException ex) {
            Logger.getLogger(ImageSchemaExtractor.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        //TO INTENTIONS
        List<Intention> intentions = FMLTranslator.FMLToIntentions(fmlApmlRoot, charactermanager);

        for (IntentionPerformer ip : intentionsPerformers) {
            ip.performIntentions(intentions, IDProvider.createID("MeaningMiner"), new Mode(CompositionType.blend));
        }
        
        System.out.println("greta.auxiliary.MeaningMiner.ImageSchemaExtractor.processText_2(): end");
        
//        List<Intention> intentions = new ArrayList<Intention>();
        return intentions;
    }

    public List<Intention> processText_2_english(String input) throws TransformerConfigurationException, TransformerException, ParserConfigurationException, SAXException, IOException {
        //System.out.println(input);
        XMLParser xmlParser = XML.createParser();
        XMLTree inputXML = xmlParser.parseBuffer(input);
        List<int[]> listPitchAccent = new ArrayList<>();
        //remove the description tag that creates trouble with the parser and
        //retrieve the pitch accent for future access

        for (XMLTree xmltbml : inputXML.getChildren()) {
            for (XMLTree xmlt : xmltbml.getChildren()) {
                if (xmlt.isNamed("description")) {
                    xmltbml.removeChild(xmlt);
                }
                if (xmlt.isNamed("pitchaccent")) {
                    int start = Integer.parseInt(xmlt.getAttribute("start").replace("s1:tm", ""));
                    int end = Integer.parseInt(xmlt.getAttribute("end").replace("s1:tm", ""));
                    int[] pitchAccent = {start, end};
                    listPitchAccent.add(pitchAccent);
                }
            }
        }
        //Get rid of the xml tags for text processing
        String tagFreeInput = inputXML.toString().replaceAll("<[^>]+>", "");
        List<XMLTree> imageSchemasGenerated = new ArrayList<>();

        int countTimeMarkers = 0;
        int countSentenceMarkers = 0;
        int countIdeationalUnit = 0;
        int countImageSchema = 0;

        //prepare the reader for our value
        StringReader sr = new StringReader(tagFreeInput);

        //*********   FIRST WE START BY AUGMENTING THE TEXT ***************
        //prepare the XML structure to store the speech in a FML-APML way
        XMLTree fmlApmlRoot = XML.createTree("fml-apml");
        XMLTree bmlRoot = fmlApmlRoot.addChild(inputXML);
        XMLTree fmlRoot = fmlApmlRoot.createChild("fml");

        //split by sentences, for each sentences:
        for (List<HasWord> sentence : new DocumentPreprocessor(sr)) {
            imageSchemasGenerated.clear();
            boolean hasVerb = false;
            boolean afterVerb = false;
            boolean negation = false;

            Set<String> imageSchemas = new HashSet<>();
            Tree parse = lp.apply(sentence);
            parse.pennPrint();

            //System.out.println();
            List<TypedDependency> tdl = null;
            //retrieve the grammatical dependencies
            if (gsf != null) {
                GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
                tdl = gs.typedDependenciesCCprocessed();
                System.out.println(tdl);
                System.out.println();
            }
            
            //Prepare the ideational unit structure
            XMLTree ideationalUnit = fmlRoot.createChild("ideationalunit");
            ideationalUnit.setAttribute("id", "id_" + countIdeationalUnit++);
            ideationalUnit.setAttribute("importance", "1.0");
            ideationalUnit.setAttribute("start", "s1:tm" + countTimeMarkers + "-0.2");
            ideationalUnit.setAttribute("end", "s1:tm" + (countTimeMarkers + sentence.size() - 1) + "-0.2");
            String[] listToken = new String[sentence.size()];
            String[] listPos = new String[sentence.size()];
            //A first loop that checks if there is a verb in the sentence and prepare the sentence for chunking
            for (int i = 0; i < sentence.size(); i++) {
                //retrieve the word and its grammar posTag
                CoreLabel cl = (CoreLabel) sentence.get(i);
                String type = cl.tag();

                listToken[i] = cl.originalText();
                listPos[i] = cl.tag();
                if (type != null && (type.equals("VB") || type.equals("VBD") || type.equals("VBG") || type.equals("VBN") || type.equals("VBZ") || type.equals("VBP"))) {
                    hasVerb = true;
                }

            }
            //retrieve the BIO (begin inside out) tags for the chunks
            String chunktag[] = chunker.chunk(listToken, listPos);
            XMLTree previousImageSchema = createXMLImageSchema(fmlRoot, countImageSchema++, countSentenceMarkers);

            //XMLTree imageSchema = fmlRoot.createChild("imageschema");
            //imageSchema.setAttribute("id", "id_" + countImageSchema++);
            //The second loop
            for (int i = 0; i < sentence.size(); i++) {
                CoreLabel cl = (CoreLabel) sentence.get(i);
                imageSchemas.clear();

                String posTag = cl.tag();
                String value = cl.originalText();

                //mapping PENN from stanford to wordnet POS
                //this part can act as a filter
                POS pos = null;
                switch (posTag) {
                    //noun singular or mass
                    case "NN":
                        pos = POS.NOUN;
                        break;
                    //noun plural
                    case "NNS":
                        pos = POS.NOUN;
                        break;
                    //Verb
                    case "VB":
                        pos = POS.VERB;
                        break;
                    //Verb past tense
                    case "VBD":
                        pos = POS.VERB;
                        break;
                    //Verb gerondif
                    case "VBG":
                        pos = POS.VERB;
                        break;
                    //Verb past participle
                    case "VBN":
                        pos = POS.VERB;
                        break;
                    //Verb 3rd person singular present (s in english)
                    case "VBZ":
                        pos = POS.VERB;
                        break;
                    //Verb singular present other person (without the s)
                    case "VBP":
                        pos = POS.VERB;
                        break;
                    //Adjective
                    case "JJ":
                        pos = POS.ADJECTIVE;
                        break;
                    //Adjective Comparative
                    case "JJR":
                        pos = POS.ADJECTIVE;
                        break;
                    //Adjective Superlative
                    case "JJS":
                        pos = POS.ADJECTIVE;
                        break;
                    //adverb
                    case "RB":
                        pos = POS.ADVERB;
                        break;
                    //adverb comparative
                    case "RBR":
                        pos = POS.ADVERB;
                        break;
                    //adverb superlative
                    case "RBS":
                        pos = POS.ADVERB;
                        break;
                    //particle
                    case "RP":
                        pos = POS.ADVERB;
                        break;
                    //DEBUG : this is unsafe, just to keep some particular words in check
                    case "IN":
                        pos = POS.ADVERB;
                        break;
                    default:
                        pos = null;
                        break;
                }

                //begin wordnet code
                List<String> stems = new ArrayList<>();

                //We retrieve the stems for the word.
                if (pos == POS.NOUN || pos == POS.ADJECTIVE || pos == POS.VERB || pos == POS.ADVERB) {
                    stems = wns.findStems(value, pos);
                }

                //check if we are at the verb
                if (pos == POS.VERB || chunktag[i].equals("B-VP")) {
                    afterVerb = true;
                }

                if (!hasVerb || (hasVerb && afterVerb)) {
                    //for each stem
                    for (String stem : stems) {
                        //we retrieve the word from wordnet
                        IIndexWord idxWord = dict.getIndexWord(stem, pos);
                        if (idxWord != null) {

                            //we retrieve the synset
                            ISynset synset = this.simplifiedLesk(idxWord, SentenceUtils.listToOriginalTextString(sentence));
                            /*System.out.println("Stem : " + stem + " POS:" + pos);
                            for (IWordID idw : idxWord.getWordIDs()) {
                                System.out.println("ID : " + idw.getSynsetID().getOffset());
                            }
                            System.out.println(stem + " id:" + synset.getOffset());*/
                            //THE IMPORTANT PART : we retrieve the image schemas for this synset
                            Set<String> imscSet = getImageSchemas(synset, 10);

                            imageSchemas.addAll(imscSet);

                        }
                    }

                    //Depending on the chunk, I will construct the image schema differently
                    //If we begin a new chunk (the B tag), I either delete an empty previous Image Schema (created as a placeholder) or
                    //if the previous is not empty, I close it with an end tag and I open a new one (with createXMLImageSchema).
                    if (chunktag[i].startsWith("B")) {

                        if (previousImageSchema != null) {
                            if (previousImageSchema.getAttribute("type") == "") {
                                fmlRoot.removeChild(previousImageSchema);
                                previousImageSchema = createXMLImageSchema(fmlRoot, countImageSchema++, countSentenceMarkers + cl.index() - 1);

                            } else {
                                if (!imageSchemas.isEmpty()) {
                                    previousImageSchema.setAttribute("end", "s1:tm" + (countSentenceMarkers + cl.index() - 1) + "-0.2");
                                    imageSchemasGenerated.add(previousImageSchema);
                                    previousImageSchema = createXMLImageSchema(fmlRoot, countImageSchema++, countSentenceMarkers + cl.index() - 1);

                                } else {
                                    //This is used so the Image Schema coming from a previous chunk can span unto the next chunk until there is a new Image Schema.
                                    previousImageSchema.setAttribute("previous", "true");
                                }
                            }
                        } else {
                            previousImageSchema = createXMLImageSchema(fmlRoot, countImageSchema++, countSentenceMarkers + cl.index() - 1);

                        }
                        //If an Image Schema is identified for this word, I insert it
                        if (!imageSchemas.isEmpty()) {
                            String imageRef = imageSchemas.iterator().next().toLowerCase();
                            previousImageSchema.setAttribute("type", imageRef);
                            previousImageSchema.setAttribute("POSroot", pos.toString());
                        }

                    }
                    //If I am within a chunk, I should just update the existing previous Image Schema that
                    //has been created for the whole chunk during the B case, OR create a new one in case of multiple instance of a noun in a NP or verb in a VP
                    if (chunktag[i].startsWith("I")) {
                        if (previousImageSchema == null) {
                            previousImageSchema = createXMLImageSchema(fmlRoot, countImageSchema++, countSentenceMarkers + cl.index() - 1);
                            //Check if the Image Schema was coming from a previous chunk
                        } else if (previousImageSchema.getAttribute("previous") == "true" && !imageSchemas.isEmpty()) {

                            previousImageSchema.setAttribute("end", "s1:tm" + (countSentenceMarkers + cl.index() - 1) + "-0.2");
                            imageSchemasGenerated.add(previousImageSchema);
                            previousImageSchema = createXMLImageSchema(fmlRoot, countImageSchema++, countSentenceMarkers + cl.index() - 1);

                        }
                        if (!imageSchemas.isEmpty()) {
                            switch (chunktag[i]) {
                                case "I-NP": {
                                    if (previousImageSchema.getAttribute("POSroot") == "") {
                                        setImageSchemaType_english(previousImageSchema, imageSchemas.iterator().next().toLowerCase(), pos.toString(), ideationalUnit, tdl, cl.index());
                                    } else {
                                        if (previousImageSchema.getAttribute("POSroot").equals(POS.NOUN.toString())) {
                                            setImageSchemaType_english(previousImageSchema, imageSchemas.iterator().next().toLowerCase(), pos.toString(), ideationalUnit, tdl, cl.index());

                                        }
                                        if (pos.equals(POS.ADJECTIVE) || isWithinPitchAccent(listPitchAccent, cl.index()) || pos.equals(POS.NOUN) && !previousImageSchema.getAttribute("POSroot").equals(POS.ADJECTIVE.toString())) {
                                            if (previousImageSchema.getAttribute("type") == "") {
                                                fmlRoot.removeChild(previousImageSchema);
                                            } else {
                                                previousImageSchema.setAttribute("end", "s1:tm" + (countSentenceMarkers + cl.index() - 1) + "-0.2");
                                                imageSchemasGenerated.add(previousImageSchema);
                                            }
                                            XMLTree imageschema = createXMLImageSchema(fmlRoot, countImageSchema++, countSentenceMarkers + cl.index() - 1);
                                            setImageSchemaType_english(imageschema, imageSchemas.iterator().next().toLowerCase(), pos.toString(), ideationalUnit, tdl, cl.index());
                                            previousImageSchema = imageschema;
                                        }
                                    }
                                    break;
                                }

                                case "I-VP": {
                                    if (previousImageSchema.getAttribute("POSroot") == "") {
                                        setImageSchemaType_english(previousImageSchema, imageSchemas.iterator().next().toLowerCase(), pos.toString(), ideationalUnit, tdl, cl.index());
                                    } else {
                                        if (previousImageSchema.getAttribute("POSroot").equals(POS.VERB.toString())) {
                                            setImageSchemaType_english(previousImageSchema, imageSchemas.iterator().next().toLowerCase(), pos.toString(), ideationalUnit, tdl, cl.index());
                                        }
                                        if (pos.equals(POS.ADVERB) || isWithinPitchAccent(listPitchAccent, cl.index()) || pos.equals(POS.VERB) && !previousImageSchema.getAttribute("POSroot").equals(POS.ADVERB.toString())) {
                                            if (previousImageSchema.getAttribute("type") == "") {
                                                fmlRoot.removeChild(previousImageSchema);
                                            } else {
                                                previousImageSchema.setAttribute("end", "s1:tm" + (countSentenceMarkers + cl.index() - 1) + "-0.2");
                                                imageSchemasGenerated.add(previousImageSchema);
                                            }
                                            XMLTree imageschema = createXMLImageSchema(fmlRoot, countImageSchema++, countSentenceMarkers + cl.index() - 1);
                                            setImageSchemaType_english(imageschema, imageSchemas.iterator().next().toLowerCase(), pos.toString(), ideationalUnit, tdl, cl.index());
                                            previousImageSchema = imageschema;
                                        }
                                    }
                                    break;
                                }

                                default: { //I-PRT, I-INTJ, I-SBAR, I-ADJP, I-ADVP, I-PP
                                    if (previousImageSchema.getAttribute("POSroot") == "") {
                                        setImageSchemaType_english(previousImageSchema, imageSchemas.iterator().next().toLowerCase(), pos.toString(), ideationalUnit, tdl, cl.index());

                                    } else {
                                        if (previousImageSchema.getAttribute("type") == "") {
                                            fmlRoot.removeChild(previousImageSchema);
                                        } else {
                                            previousImageSchema.setAttribute("end", "s1:tm" + (countSentenceMarkers + cl.index() - 1) + "-0.2");
                                            imageSchemasGenerated.add(previousImageSchema);
                                        }
                                        XMLTree imageschema = createXMLImageSchema(fmlRoot, countImageSchema++, countSentenceMarkers + cl.index() - 1);
                                        setImageSchemaType_english(imageschema, imageSchemas.iterator().next().toLowerCase(), pos.toString(), ideationalUnit, tdl, cl.index());
                                        previousImageSchema = imageschema;
                                    }
                                    break;
                                }
                            }
                        }
                    }
                    if (chunktag[i].startsWith("O")) {
                        if (previousImageSchema != null) {
                            if (previousImageSchema.getAttribute("type") == "") {
                                fmlRoot.removeChild(previousImageSchema);
                            } else {
                                previousImageSchema.setAttribute("end", "s1:tm" + (countSentenceMarkers + cl.index() - 1) + "-0.2");
                                imageSchemasGenerated.add(previousImageSchema);
                            }

                        }
                        previousImageSchema = null;
                    }

                }

                negation = posTag.equalsIgnoreCase("DT") && value.equalsIgnoreCase("no") || posTag.equalsIgnoreCase("RB") && value.equalsIgnoreCase("not");
                //System.out.println(negation);

            }
            countSentenceMarkers += sentence.size();
            if (previousImageSchema != null) {
                if (previousImageSchema.getAttribute("type").equals("")) {
                    fmlRoot.removeChild(previousImageSchema);
                } else {
                    previousImageSchema.setAttribute("end", "s1:tm" + (countSentenceMarkers - 1) + "-0.2");
                    imageSchemasGenerated.add(previousImageSchema);
                }

            }

            //If no root could be found,
            if (!ideationalUnit.hasAttribute("main")) {
                //If no root could be found, delete the ideational unit.
                if (imageSchemasGenerated.size() > 0) {
                    ideationalUnit.setAttribute("main", imageSchemasGenerated.get(0).getAttribute("id"));
                } else {
                    fmlRoot.removeChild(ideationalUnit);
                }
            }

        }

        
        try{
            String fmlApmlRoot_v1=fmlApmlRoot.toString().replace("<fml-apml>","").replace("<fml>","").replace("</fml>","").replace("</fml-apml>","");
            fmlApmlRoot_v1= fmlApmlRoot_v1.replace("</bml>","</bml>\n<fml>").replace("?>", "?>\n<fml-apml>").replace(":tm0",":tm1")+"\n</fml>\n</fml-apml>";
            System.out.println(fmlApmlRoot_v1);
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document document = docBuilder.parse(new InputSource(new StringReader(fmlApmlRoot_v1)));
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(document);
            FileWriter writer = new FileWriter(new File(System.getProperty("user.dir")+"\\fml_output_mm.xml"));
            StreamResult result = new StreamResult(writer);
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(source, result);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(ImageSchemaExtractor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(ImageSchemaExtractor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ImageSchemaExtractor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerConfigurationException ex) {
            Logger.getLogger(ImageSchemaExtractor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerException ex) {
            Logger.getLogger(ImageSchemaExtractor.class.getName()).log(Level.SEVERE, null, ex);
        }
        

        //TO INTENTIONS
        List<Intention> intentions = FMLTranslator.FMLToIntentions(fmlApmlRoot, charactermanager);

        for (IntentionPerformer ip : intentionsPerformers) {
            ip.performIntentions(intentions, IDProvider.createID("MeaningMiner"), new Mode(CompositionType.blend));
        }
        
        for(Intention intention: intentions){
            System.out.println("ImageSchemaExtractor - Intention: " + intention);
        }
        
        return intentions;
    }
    
    private void outputConsumer(Process process, boolean block, boolean output){
        
        BufferedReader logReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        Thread thread = new Thread(
            () -> {
                String logLine = null;
                while (process.isAlive()) {
                    try{
                        logLine = logReader.readLine();
                        if(logLine == null){
                            break;
                        }
                        if(output == true){
                            System.out.println("Script output: " + logLine);
                        }
                    }
                    catch(IOException e){
                        e.printStackTrace();
                    }
                }
            }
        );
        thread.start();
        
//        BufferedReader logReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
//        String logLine = null;
//        while (true) {
//            try{
//                logLine = logReader.readLine();
//                if(logLine == null){
//                    break;
//                }
//                System.out.println("Script output: " + logLine);
//            }
//            catch(IOException e){
//                e.printStackTrace();
//            }
//        }
        
        if(block){
            try{
                process.waitFor();
            }
            catch(InterruptedException e){
                e.printStackTrace();
            }
            
        }
    }

}

