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
import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.ISynsetID;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.item.Pointer;
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
import greta.auxiliary.activemq.TextReceiver;
import greta.auxiliary.activemq.WhiteBoard;
import greta.core.intentions.FMLTranslator;
import greta.core.intentions.Intention;
import greta.core.intentions.IntentionEmitter;
import greta.core.intentions.IntentionPerformer;
import greta.core.intentions.PseudoIntentionPitchAccent;
import greta.core.intentions.PseudoIntentionSpeech;
import static greta.core.util.CharacterDependentAdapter.getCharacterManagerStatic;
import greta.core.util.CharacterManager;
import greta.core.util.Mode;
import greta.core.util.enums.CompositionType;
import greta.core.util.id.ID;
import greta.core.util.id.IDProvider;
import greta.core.util.time.SynchPoint;
import greta.core.util.time.TimeMarker;
import greta.core.util.xml.XML;
import greta.core.util.xml.XMLParser;
import greta.core.util.xml.XMLTree;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;
import org.xml.sax.SAXException;

/**
 *
 * @author Donatella Simonetti
 */

// processText is called when speech is controlled with ActiveMQ broker (e.g. Flipper dialogue manager, ActiveMQ send signal from python, etc.)

public class FMLReceiver_MeaningMiner extends TextReceiver implements IntentionEmitter{

    private ArrayList<IntentionPerformer> performers;
    private XMLParser fmlParser;
    private CharacterManager cm;

    private ImageSchemaExtractor ISExtractor;

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

    private Process process0 = null;
//    private String server_activation_command = "python ./Scripts/spaCy/spacy_server.py";
    private String server_activation_command = "./Scripts/spaCy/activate_py37_spacy_server.bat";
    
    // Currently, update button doesn't work in NVBG_MM_controller.
    // If you want to change language, change MM_Language variable in CharacterManager.java in Util.jar and re-compile, then launch again.
    private String lang = null;

    private Process process1 = null;
//    private String parser_command = "python ./Scripts/spaCy/spacy_parser.py"; // stand alone version
    private String parser_command = "python ./Scripts/spaCy/spacy_client.py"; // server-client version
//    private String parser_command = "./bin/Scripts/run_parser.bat"; // if run in conda virtual environment; make sure to edit env name
//    private String lang = "EN";
        
    private String parsed_sentence_path = "./parsed_sentences.txt";    
    
    public FMLReceiver_MeaningMiner(CharacterManager cm) throws InterruptedException {

        this(WhiteBoard.DEFAULT_ACTIVEMQ_HOST,
                WhiteBoard.DEFAULT_ACTIVEMQ_PORT,
                "greta.FML",cm);

        this.cm = cm;
        lang = this.cm.get_MM_Language();

        //this.imgSchmext = new ImageSchemaExtractor(this.cm);
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

        if(this.cm.get_use_MM()){
            this.ISExtractor = new ImageSchemaExtractor(this.cm);
        }
        
//        //Load the parser chunkerModel from the stanford parser
//        lp = LexicalizedParser.loadModel(STANFORDPARSER);
//
//        //Load the Tree Bank for english
//        tlp = lp.treebankLanguagePack(); // a PennTreebankLanguagePack for English
//        if (tlp.supportsGrammaticalStructures()) {
//            gsf = tlp.grammaticalStructureFactory();
//        }
//
//        //Load the OpenNLP Chunker
//        InputStream modelIn = null;
//        ChunkerModel chunkerModel = null;
//        try {
//            modelIn = new FileInputStream(OPENNLPCHUNKER);
//            chunkerModel = new ChunkerModel(modelIn);
//        } catch (FileNotFoundException ex) {
//            Logger.getLogger(ImageSchemaExtractor.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (IOException ex) {
//            Logger.getLogger(ImageSchemaExtractor.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        chunker = new ChunkerME(chunkerModel);
        
        server_activation_command = server_activation_command + " " + lang;
        String[] splitted_command = server_activation_command.split(" ");
        try {
            System.out.println(splitted_command);
            process0 = new ProcessBuilder(splitted_command).redirectErrorStream(true).start();
            System.out.println("### spaCy parser (language): " + lang);
            outputConsumer(process0, false, true);
        } catch (IOException e) {
            System.out.println(e);
        }

    }

    public FMLReceiver_MeaningMiner(String host, String port, String topic, CharacterManager cm) throws InterruptedException {
        super(host, port, topic);
        this.cm = cm;
        performers = new ArrayList<IntentionPerformer>();
        fmlParser = XML.createParser();
        fmlParser.setValidating(false);

        if(this.cm.get_use_MM()){
            this.ISExtractor = new ImageSchemaExtractor(this.cm);
        }
    }

    @Override
    protected void onMessage(String content, Map<String, Object> properties) {
        
        System.out.println("### content of onMessage() in greta.auxiliary.MeaningMinor.FMLReceiver_MeaningMiner.java ###");
        System.out.println(content);
        System.out.println("### content of onMessage() in greta.auxiliary.MeaningMinor.FMLReceiver_MeaningMiner.java ###");
        
        // parse the fml message
        XMLTree fml = fmlParser.parseBuffer(content);
        //XMLTree bmlxml = fmlParser.parseBuffer(content.toString());
        XMLTree pitchxml = XML.createTree("speech");

        List<Intention> intentions = FMLTranslator.FMLToIntentions(fml,this.cm);
//        List<Signal> signals = BMLTranslator.BMLToSignals(bml,this.cm);

        String fml_id = "";
        List<int[]> listPitchAccent = new ArrayList<>();

        if (fml == null) {
            return;
        }

        Mode mode = FMLTranslator.getDefaultFMLMode();
        if (fml.hasAttribute("composition")) {
            mode.setCompositionType(fml.getAttribute("composition"));
        }
        if (fml.hasAttribute("reaction_type")) {
            mode.setReactionType(fml.getAttribute("reaction_type"));
        }
        if (fml.hasAttribute("reaction_duration")) {
            mode.setReactionDuration(fml.getAttribute("reaction_duration"));
        }
        if (fml.hasAttribute("social_attitude")) {
            mode.setSocialAttitude(fml.getAttribute("social_attitude"));
        }
        if(fml.hasAttribute("id")){
            fml_id = fml.getAttribute("id");
        }else{
            fml_id = "fml_1";
        }

        /// Edit here: start ////////////////////////////////////////////////////////////////////////////

//        for (XMLTree fmlchild : fml.getChildrenElement()) {
//            // store the bml id in the Mode class
//            if (fmlchild.isNamed("bml")) {
//                if(fmlchild.hasAttribute("id")){
//                    mode.setBml_id(fmlchild.getAttribute("id"));
//                }
//                for (XMLTree bmlchild : fmlchild.getChildrenElement()){
//                    if (bmlchild.isNamed("speech")){
//                        for (XMLTree child : bmlchild.getChildrenElement()){
//                            if (child.isNamed("description")) {
//                                bmlchild.removeChild(child);
//                            }else if(child.isNamed("pitchaccent")){
//                                pitchxml.addChild(child); // add the pitchaccent in a XML tree specific
//                            }
//                        }
//                    }
//                }
//            }
//        }
//
//        String plaintext = "";
//        // take the speech elements end translate them in a simple text
//        /*plaintext = bmlxml.toString().replaceAll("<[^>]+>", "");
//        plaintext = plaintext.trim().replaceAll(" +", " "); // delete multiple spaces*/
//
//        List<Intention> intentions = FMLTranslator.FMLToIntentions(fml, this.cm);
//        
//        
//        System.out.println("greta.auxiliary.MeaningMiner.FMLReceiver_MeaningMiner: use_MM : " + this.cm.get_use_MM());
//        if(this.cm.get_use_MM()){
//
//            PseudoIntentionSpeech speech = new PseudoIntentionSpeech(this.cm);
//
//            // set if the timemearker name start from tmO or tm1
//            int startfrom = 0;
//            List<TimeMarker> justcheck = speech.getTimeMarkers();
//            String st = justcheck.get(1).getName();
//            if (st.lastIndexOf('0') != -1){
//                startfrom = 0;
//            }else if(st.lastIndexOf('1') != -1){
//                startfrom = 1;
//            }
//
//            // take the PseudoIntentionSpeech in order to put a timemarker for each word
//            for (Intention intent : intentions){
//                if (intent instanceof PseudoIntentionSpeech){
//                    speech.addSpeechElement(((PseudoIntentionSpeech) intent).getSpeechElements()); // take the utterances
//                    //speech.setLanguage(((PseudoIntentionSpeech) intent).getLanguage()); // the language
//                    speech.setId(((PseudoIntentionSpeech) intent).getId());
//                    //speech.setReference(((PseudoIntentionSpeech) intent).getReference());
//                    //speech.addPhonems(((PseudoIntentionSpeech) intent).getPhonems());
//                    intentions.remove(intent);
//                    break;
//                }
//            }
//
//            HashMap<String, String> wordandTimeMarker = new HashMap<String, String>();
//
//
//            int numWords = 0;
//            if (speech != null){
//                for (Object word : speech.getSpeechElements()){
//                    if (word instanceof ArrayList){
//                        for(Object array_obj : (ArrayList) word){
//                            if(array_obj instanceof String){
//                                String w =(String) array_obj;
//                                if (!w.equals(" ")){
//                                    plaintext += (String) array_obj;
//
//                                    String utterence = (String) array_obj;
//                                    utterence.trim();
//                                    //utterence.replace(" +", " ");
//                                    List<String> words = Arrays.asList(utterence.split(" "));
//                                    numWords += words.size() - 1;
//
//                                }
//                            }else if (array_obj instanceof TimeMarker){
//                                String actualTM = ((TimeMarker) array_obj).getName();
//                                wordandTimeMarker.put(actualTM, "tm"+numWords);
//
//                            }
//                        }
//                        break;
//                    }
//                }
//            }
//
//            // create the intention for the Newspeech
//            PseudoIntentionSpeech newSpeech = createSpeechIntention(plaintext, wordandTimeMarker);
//            newSpeech.setLanguage(speech.getLanguage());
//            newSpeech.setId(speech.getId());
//            newSpeech.setReference(speech.getReference());
//            newSpeech.getStart().addReference("start");
//
//            //add the markers in a list
//            List<TimeMarker> tm_list = new ArrayList<TimeMarker>();
//            for (Object obj: newSpeech.getSpeechElements()){
//                if(obj instanceof ArrayList){
//                    String last_tm_name = "";
//                    for (Object array_obj : (ArrayList) obj){
//                        if(array_obj instanceof TimeMarker){
//                            if(!((TimeMarker) array_obj).getName().equals(last_tm_name)){
//                                tm_list.add((TimeMarker) array_obj);
//                                last_tm_name = ((TimeMarker) array_obj).getName();
//                            }
//                        }
//                    }
//                }
//            }
//
//            // create a bml xmltree with the new speech and timemarker to put as input for the meaningMiner computation
//            XMLTree treebml = toXML(newSpeech); // speech child
//
//            //newSpeech set timemarkers list
//            newSpeech.setMarkers(tm_list);
//
//            // update the timemarker for each intention according the newSpeech markers
//            for(Intention intens : intentions){
//
//                TimeMarker end = intens.getEnd();
//                TimeMarker start = intens.getStart();
//                int[] strt_end = new int[2];
//                int counter = 0;
//
//                List<TimeMarker> list_tm = new ArrayList<TimeMarker>();
//                list_tm.add(end);
//                list_tm.add(start);
//                for (TimeMarker m : list_tm){
//                    List<SynchPoint> list_sypoint = m.getReferences();
//
//                    double offset_synchpnt = m.getReferences().get(0).getOffset();
//
//                    String targetname = list_sypoint.get(0).getTargetName();
//                    // index of number in the string
//                    int column = targetname.indexOf(":");// example s1:tm2
//                    String nametm = targetname.substring(column + 1, targetname.length());
//                    // TODO intentions have correct timemarkers
//                    String new_nametm = wordandTimeMarker.get(nametm);
//                    m.removeReferences();
//                    String newtm = targetname.substring(0,targetname.indexOf(":") + 1) + new_nametm;
//                    m.addReference(newtm);
//                    m.getReferences().get(0).setOffset(offset_synchpnt);
//
//                    int nmb = Integer.parseInt(new_nametm.substring(2, new_nametm.length()));
//
//                    strt_end[counter] = nmb;
//                    counter++;
//                }
//
//                if (intens instanceof PseudoIntentionPitchAccent){
//                    Arrays.sort(strt_end);
//                    int[] pitchAccent = {strt_end[0], strt_end[1]};
//                    listPitchAccent.add(pitchAccent);
//                }
//            }
//
//            // create the input for the MeaningMiner computation
//            XMLTree bmlRoot = XML.createTree("bml");
//            bmlRoot.addChild(treebml); // bml part where after each word is set a timemarker
//
//            //MeaningMiner
//            String input = bmlRoot.toString();
//            List<Intention> newIntentionsfromSpeech = processText(input, listPitchAccent);
//
//            // add the intentions found with the mining miner to the others
//            if (newIntentionsfromSpeech.size() > 0){
//                for (int i = 0; i < newIntentionsfromSpeech.size(); i++)
//                if (newIntentionsfromSpeech.get(i) instanceof PseudoIntentionSpeech){
//                    PseudoIntentionSpeech spc = (PseudoIntentionSpeech) newIntentionsfromSpeech.get(i);
//                    spc.setId(speech.getId());
//                    intentions.add(spc);
//                }else{
//                    intentions.add(newIntentionsfromSpeech.get(i));
//                }
//            }
//        }

        if(this.cm.get_use_MM()){
            try {
                //MEANING MINER TREATMENT START
                List<Intention> intention_list;
                intention_list = this.ISExtractor.processText_2(fml.toString());
                intentions.addAll(intention_list);
                //MEANING MINER TREATMENT END
            } catch (TransformerException ex) {
                Logger.getLogger(FMLReceiver_MeaningMiner.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ParserConfigurationException ex) {
                Logger.getLogger(FMLReceiver_MeaningMiner.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SAXException ex) {
                Logger.getLogger(FMLReceiver_MeaningMiner.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(FMLReceiver_MeaningMiner.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        
        /// Edit here: end ////////////////////////////////////////////////////////////////////////////

        Object contentId = null;
        if (fml.hasAttribute("id")) {
            contentId = fml.getAttribute("id");
        }
        else {
            contentId = properties.get("content-id");
        }

        ID id = IDProvider.createID(contentId == null ? "FMLReceiver" : contentId.toString());
        id.setFmlID(fml_id); // add the fml id
        for (IntentionPerformer performer : performers) {
            performer.performIntentions(intentions, id, mode);
        }
    }

    public PseudoIntentionSpeech createSpeechIntention(String plaintext, HashMap<String, String> wordandTimeMarker){
        PseudoIntentionSpeech pis = new PseudoIntentionSpeech(this.cm);

        ArrayList<String> listofSpeechElement = new ArrayList<String>();
        int lastcharposition = 0;
        boolean reading_word = false;
        // read each caracter
        for (int i=0; i < plaintext.length(); i++){
            if (plaintext.charAt(i) == ' ' || plaintext.charAt(i) == '.' || plaintext.charAt(i) == ';' || plaintext.charAt(i) == ',' || plaintext.charAt(i) == ':' || plaintext.charAt(i) == '!' || plaintext.charAt(i) == '?' ||
                               plaintext.charAt(i) == '\n' || plaintext.charAt(i) == '\"' || plaintext.charAt(i) == '(' || plaintext.charAt(i) == ')' || plaintext.charAt(i) == '/'){

                if (i!=0 && reading_word){
                    listofSpeechElement.add(plaintext.substring(lastcharposition, i)); // add the special caracter
                }
                //char c = plaintext.charAt(i);
                if (plaintext.charAt(i) != ' ' && plaintext.charAt(i) != '\n'){
                    listofSpeechElement.add(String.valueOf(plaintext.charAt(i))); // add the special caracter
                }
                lastcharposition = i+1;
                reading_word = false;
            }else{
                reading_word = true;
            }
        }

        // create a PseudoIntentionSpeech with the new speechElement and timemarker
        ArrayList<Object> speechelement = new ArrayList<Object>();
        int counter = 0;
        for (String str : listofSpeechElement){
            speechelement.add(new TimeMarker("tm"+counter)); //add the TimeMarker
            speechelement.add(str);// add the word
            counter++;
        }
        speechelement.add(new TimeMarker("tm"+counter)); //add last TimeMarker
        pis.addSpeechElement(speechelement);
        return pis;
    }

    //Insert a new xml imageschema in the XML tree. This object will be completed later or even deleted if no Image Schema could be put there.
    private XMLTree createXMLImageSchema(XMLTree fmlRoot, int countImageSchema, int countTimeMarker) {
        XMLTree imageschema = fmlRoot.createChild("imageschema");
        imageschema.setAttribute("importance", "1.0");
        imageschema.setAttribute("id", "im_" + countImageSchema);
        imageschema.setAttribute("start", "s1:tm" + (countTimeMarker) + "-0.2");

        return imageschema;
    }

    // Set the Image Schema to this xml imageschema. It checks if this was the root of the sentence to decide if it should be the main of the current ideational unit.
    private void setImageSchemaType(XMLTree imageschema, String imageRef, String pos, XMLTree ideationalUnit, String dep, Integer indexWord) {

        imageschema.setAttribute("type", imageRef);
        imageschema.setAttribute("indexword", indexWord.toString());
        imageschema.setAttribute("POSroot", pos);

//        if (dep.equals("root")) {
        if (dep.equals("ROOT")) {
            ideationalUnit.setAttribute("main", imageschema.getAttribute("id"));
        }
    }    

    public XMLTree toXML( PseudoIntentionSpeech newSpeech){
        XMLTree toReturn;

            toReturn = XML.createTree("speech");
            toReturn.setAttribute("id", newSpeech.getId());
            toReturn.setAttribute("language", newSpeech.getLanguage());
            toReturn.setAttribute("voice","marytts");
            toReturn.setAttribute("type", "SAPI4");
            TimeMarker start = newSpeech.getMarkers().get(0);
            SynchPoint synchRef = start.getFirstSynchPointWithTarget();
            if(synchRef != null) {
                toReturn.setAttribute("start", Double.toString(synchRef.getValue()));
            }
            else{
                if(start.concretizeByReferences()) {
                    toReturn.setAttribute("start", ""+start.getValue());
                }
            }
            String ref = newSpeech.getReference();
            if(ref != null) {
                toReturn.setAttribute("ref", ref);
            }
            for(int i=1; i< newSpeech.getSpeechElements().size()-1; ++i){ //skip start and end
                if (newSpeech.getSpeechElements().get(i) instanceof ArrayList){
                    ArrayList arraylist = (ArrayList) newSpeech.getSpeechElements().get(i);
                    for (Object obj : arraylist){
                        if(obj instanceof String) {
                            toReturn.addText((String)obj);
                        }
                        else{
                            XMLTree tm = toReturn.createChild("tm");
                            tm.setAttribute("id", ((TimeMarker)obj).getName());
                                // time ?
                        }
                    }

                }

            }

        return toReturn;
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

    //Return the Image Schemas that can be found for this Synset (meaning).
    //This is a recursive function that starts at the synset of the word,
    //looks for Image Schemas and then continue up the tree by following the hypernyms (more global meaning) of the current set
    private Set<String> getImageSchemas(ISynset synset, int depth) {

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
        //return toReturn;
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
        performers.add(ip);
    }

    @Override
    public void removeIntentionPerformer(IntentionPerformer performer) {
        performers.remove(performer);
    }

    public List<Intention> processText(String input, List<int[]> listPitchAccent) {

        System.out.println("greta.auxiliary.MeaningMiner.FMLReceiver_MeaningMiner.processText_2()");
        
        //System.out.println(input);
        XMLParser xmlParser = XML.createParser();
        XMLTree inputXML = xmlParser.parseBuffer(input);
//        List<int[]> listPitchAccent = new ArrayList<>();
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
//                    listPitchAccent.add(pitchAccent);
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
        tagFreeInput = tagFreeInput.trim().replaceAll("  +", "");
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
//        StringReader sr = new StringReader(tagFreeInput);

        //*********   FIRST WE START BY AUGMENTING THE TEXT ***************
        //prepare the XML structure to store the speech in a FML-APML way
        XMLTree fmlApmlRoot = XML.createTree("fml-apml");
        XMLTree bmlRoot = fmlApmlRoot.addChild(inputXML);
        XMLTree fmlRoot = fmlApmlRoot.createChild("fml");
        
        //Call spaCy parser
//        String sentences = "test";
        
        parser_command = parser_command + " " + lang + " \"" + tagFreeInput + "\"";
        String[] splitted_command = parser_command.split(" ");
        try {
            System.out.println(splitted_command);
            process1 = new ProcessBuilder(splitted_command).redirectErrorStream(true).start();
            System.out.println("### spaCy parser (language): " + lang);
            System.out.println("### spaCy parser (sentence): " + tagFreeInput);
            outputConsumer(process1, true, true);
        } catch (IOException e) {
            System.out.println(e);
        }

//        StringBuilder contentBuilder = new StringBuilder();
//        try (Stream<String> stream = Files.lines(Paths.get(parsed_sentence_path), StandardCharsets.UTF_8)) {
//          stream.forEach(s -> contentBuilder.append(s).append("\n"));
//        } catch (IOException e) {
//          //handle exception
//        }
//        String sentences = contentBuilder.toString();
//        System.out.println(sentences);
//        String[] sentenceArray = sentences.split("\n");
//        for(String sentence:sentenceArray){
//            System.out.println(sentence);
//        }

        
        String[] sentenceArray = new String[]{};
        try{
            List<String> sentenceList = Files.readAllLines(Paths.get(parsed_sentence_path), StandardCharsets.UTF_8);
            sentenceArray = sentenceList.stream().toArray(String[]::new);
        } catch (IOException e) {
            System.out.println(e);
        }

        for(String sentence:sentenceArray){
            System.out.println(sentence);
        }
                
        //split by sentences, for each sentences:
//        for (List<HasWord> sentence : new DocumentPreprocessor(sr)) {
        for(String sentence:sentenceArray){
            System.out.println(sentence);
            String[] origSentence_wordArray = sentence.split("#");
            String origSentence = origSentence_wordArray[0];
            String[] wordArray = origSentence_wordArray[1].split("/");
            
            imageSchemasGenerated.clear();
            boolean hasVerb = false;
            boolean afterVerb = false;
            boolean negation = false;

            Set<String> imageSchemas = new HashSet<>();
//            Tree parse = lp.apply(sentence);
//            parse.pennPrint();

            //System.out.println();
//            List<TypedDependency> tdl = null;

            //retrieve the grammatical dependencies
//            if (gsf != null) {
//                GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
//                tdl = gs.typedDependenciesCCprocessed();
//                System.out.println(tdl);
//                System.out.println();
//            }

            //Prepare the ideational unit structure
            XMLTree ideationalUnit = fmlRoot.createChild("ideationalunit");
            ideationalUnit.setAttribute("id", "id_" + countIdeationalUnit++);
            ideationalUnit.setAttribute("importance", "1.0");
            ideationalUnit.setAttribute("start", "s1:tm" + countTimeMarkers + "-0.2");
            ideationalUnit.setAttribute("end", "s1:tm" + (countTimeMarkers + sentence.length() - 1) + "-0.2");
            String[] listToken = new String[sentence.length()];
            String[] listPos = new String[sentence.length()];
            
            //A first loop that checks if there is a verb in the sentence and prepare the sentence for chunking
            for (int i = 0; i < wordArray.length; i++) {
                
                //retrieve the word and its grammar posTag
//                CoreLabel cl = (CoreLabel) sentence.get(i);
//                String type = cl.tag();
//                System.out.println("### ImageSchemaExtractor :");
//                for(String wordProperty:wordArray){
//                    System.out.println(wordProperty);
//                }
                String[] wordProperty = wordArray[i].split(" ");
                String orig = wordProperty[0];
                String lemma = wordProperty[1];
                String pos = wordProperty[2];
                String dep = wordProperty[3];
                String chunktag = wordProperty[4];

                listToken[i] = orig;
                listPos[i] = pos;
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
            for (int i = 0; i < wordArray.length; i++) {

                String[] wordProperty = wordArray[i].split(" ");
                String value = wordProperty[0];
                String lemma = wordProperty[1];
                String posTag = wordProperty[2];
                String dep = wordProperty[3];
                String chunktag = wordProperty[4];

                imageSchemas.clear();

                //mapping spaCy pos to wordnet POS
                //this part can act as a filter
                POS pos = null;
                switch (posTag) {
                    //noun singular or mass
                    case "NOUN":
                        pos = POS.NOUN;
                        break;
                    //pronoun
                    case "PRON":
                        pos = POS.NOUN;
                        break;
                    //proper noun
                    case "PROPN":
                        pos = POS.NOUN;
                        break;
                    //Verb
                    case "VERB":
                        pos = POS.VERB;
                        break;
                    //Adjective
                    case "ADJ":
                        pos = POS.ADJECTIVE;
                        break;
                    //particle
                    case "PART":
                        pos = POS.ADVERB;
                        break;
                    //adverb
                    case "ADV":
                        pos = POS.ADVERB;
                        break;
                    default:
                        pos = null;
                        break;
                }

                //begin wordnet code
                List<String> stems = new ArrayList<>();

                //We retrieve the stems for the word.
//                if (pos == POS.NOUN || pos == POS.ADJECTIVE || pos == POS.VERB || pos == POS.ADVERB) {
//                    stems = wns.findStems(value, pos);
//                }
                stems.add(lemma);

                //check if we are at the verb
//                if (pos == POS.VERB || chunktag[i].equals("B-VP")) {
                if (pos == POS.VERB || chunktag.equals("B-VP")) {
                    afterVerb = true;
                }

                if (!hasVerb || (hasVerb && afterVerb)) {
                    //for each stem
                    for (String stem : stems) {
                        //we retrieve the word from wordnet
//                        System.out.println(stem);
//                        System.out.println(pos);
                        try{
                            IIndexWord idxWord = dict.getIndexWord(stem, pos);
                            if (idxWord != null) {

                                //we retrieve the synset
                                ISynset synset = this.simplifiedLesk(idxWord, origSentence);
                                /*System.out.println("Stem : " + stem + " POS:" + pos);
                                for (IWordID idw : idxWord.getWordIDs()) {
                                    System.out.println("ID : " + idw.getSynsetID().getOffset());
                                }
                                System.out.println(stem + " id:" + synset.getOffset());*/
                                //THE IMPORTANT PART : we retrieve the image schemas for this synset
                                Set<String> imscSet = getImageSchemas(synset, 10);

                                imageSchemas.addAll(imscSet);

                            }
                        } catch (Exception e) {
                            System.out.println(e);
                        }
                    }

                    //Depending on the chunk, I will construct the image schema differently
                    //If we begin a new chunk (the B tag), I either delete an empty previous Image Schema (created as a placeholder) or
                    //if the previous is not empty, I close it with an end tag and I open a new one (with createXMLImageSchema).
                    if (chunktag.startsWith("B")) {

                        if (previousImageSchema != null) {
                            if (previousImageSchema.getAttribute("type") == "") {
                                fmlRoot.removeChild(previousImageSchema);
                                previousImageSchema = createXMLImageSchema(fmlRoot, countImageSchema++, countSentenceMarkers + i - 1);

                            } else {
                                if (!imageSchemas.isEmpty()) {
                                    previousImageSchema.setAttribute("end", "s1:tm" + (countSentenceMarkers + i - 1) + "-0.2");
                                    imageSchemasGenerated.add(previousImageSchema);
                                    previousImageSchema = createXMLImageSchema(fmlRoot, countImageSchema++, countSentenceMarkers + i - 1);

                                } else {
                                    //This is used so the Image Schema coming from a previous chunk can span unto the next chunk until there is a new Image Schema.
                                    previousImageSchema.setAttribute("previous", "true");
                                }
                            }
                        } else {
                            previousImageSchema = createXMLImageSchema(fmlRoot, countImageSchema++, countSentenceMarkers + i - 1);

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
                            previousImageSchema = createXMLImageSchema(fmlRoot, countImageSchema++, countSentenceMarkers + i - 1);
                            //Check if the Image Schema was coming from a previous chunk
                        } else if (previousImageSchema.getAttribute("previous") == "true" && !imageSchemas.isEmpty()) {

                            previousImageSchema.setAttribute("end", "s1:tm" + (countSentenceMarkers + i - 1) + "-0.2");
                            imageSchemasGenerated.add(previousImageSchema);
                            previousImageSchema = createXMLImageSchema(fmlRoot, countImageSchema++, countSentenceMarkers + i - 1);

                        }
                        if (!imageSchemas.isEmpty()) {
                            switch (chunktag) {
                                case "I-NP": {
                                    if (previousImageSchema.getAttribute("POSroot") == "") {
                                        setImageSchemaType(previousImageSchema, imageSchemas.iterator().next().toLowerCase(), pos.toString(), ideationalUnit, dep, i);
                                    } else {
                                        if (previousImageSchema.getAttribute("POSroot").equals(POS.NOUN.toString())) {
                                            setImageSchemaType(previousImageSchema, imageSchemas.iterator().next().toLowerCase(), pos.toString(), ideationalUnit, dep, i);

                                        }
                                        if (pos.equals(POS.ADJECTIVE) || isWithinPitchAccent(listPitchAccent, i) || pos.equals(POS.NOUN) && !previousImageSchema.getAttribute("POSroot").equals(POS.ADJECTIVE.toString())) {
                                            if (previousImageSchema.getAttribute("type") == "") {
                                                fmlRoot.removeChild(previousImageSchema);
                                            } else {
                                                previousImageSchema.setAttribute("end", "s1:tm" + (countSentenceMarkers + i - 1) + "-0.2");
                                                imageSchemasGenerated.add(previousImageSchema);
                                            }
                                            XMLTree imageschema = createXMLImageSchema(fmlRoot, countImageSchema++, countSentenceMarkers + i - 1);
                                            setImageSchemaType(imageschema, imageSchemas.iterator().next().toLowerCase(), pos.toString(), ideationalUnit, dep, i);
                                            previousImageSchema = imageschema;
                                        }
                                    }
                                    break;
                                }

                                case "I-VP": {
                                    if (previousImageSchema.getAttribute("POSroot") == "") {
                                        setImageSchemaType(previousImageSchema, imageSchemas.iterator().next().toLowerCase(), pos.toString(), ideationalUnit, dep, i);
                                    } else {
                                        if (previousImageSchema.getAttribute("POSroot").equals(POS.VERB.toString())) {
                                            setImageSchemaType(previousImageSchema, imageSchemas.iterator().next().toLowerCase(), pos.toString(), ideationalUnit, dep, i);
                                        }
                                        if (pos.equals(POS.ADVERB) || isWithinPitchAccent(listPitchAccent, i) || pos.equals(POS.VERB) && !previousImageSchema.getAttribute("POSroot").equals(POS.ADVERB.toString())) {
                                            if (previousImageSchema.getAttribute("type") == "") {
                                                fmlRoot.removeChild(previousImageSchema);
                                            } else {
                                                previousImageSchema.setAttribute("end", "s1:tm" + (countSentenceMarkers + i - 1) + "-0.2");
                                                imageSchemasGenerated.add(previousImageSchema);
                                            }
                                            XMLTree imageschema = createXMLImageSchema(fmlRoot, countImageSchema++, countSentenceMarkers + i - 1);
                                            setImageSchemaType(imageschema, imageSchemas.iterator().next().toLowerCase(), pos.toString(), ideationalUnit, dep, i);
                                            previousImageSchema = imageschema;
                                        }
                                    }
                                    break;
                                }

                                default: { //I-PRT, I-INTJ, I-SBAR, I-ADJP, I-ADVP, I-PP
                                    if (previousImageSchema.getAttribute("POSroot") == "") {
                                        setImageSchemaType(previousImageSchema, imageSchemas.iterator().next().toLowerCase(), pos.toString(), ideationalUnit, dep, i);

                                    } else {
                                        if (previousImageSchema.getAttribute("type") == "") {
                                            fmlRoot.removeChild(previousImageSchema);
                                        } else {
                                            previousImageSchema.setAttribute("end", "s1:tm" + (countSentenceMarkers + i - 1) + "-0.2");
                                            imageSchemasGenerated.add(previousImageSchema);
                                        }
                                        XMLTree imageschema = createXMLImageSchema(fmlRoot, countImageSchema++, countSentenceMarkers + i - 1);
                                        setImageSchemaType(imageschema, imageSchemas.iterator().next().toLowerCase(), pos.toString(), ideationalUnit, dep, i);
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
                                previousImageSchema.setAttribute("end", "s1:tm" + (countSentenceMarkers + i - 1) + "-0.2");
                                imageSchemasGenerated.add(previousImageSchema);
                            }

                        }
                        previousImageSchema = null;
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

        System.out.println(fmlApmlRoot.toString());

        //TO INTENTIONS
        List<Intention> intentions = FMLTranslator.FMLToIntentions(fmlApmlRoot, this.cm);

        for (IntentionPerformer ip : intentionsPerformers) {
            ip.performIntentions(intentions, IDProvider.createID("MeaningMiner"), new Mode(CompositionType.blend));
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
