/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Takeshi Saga
 *
 */

package greta.FlipperDemo.dm.managers;

import ch.qos.logback.classic.util.ContextInitializer;
import eu.aria.util.activemq.SimpleProducerWrapper;
import eu.aria.util.activemq.SimpleReceiverWrapper;
import eu.aria.util.activemq.util.UrlBuilder;
import eu.aria.util.translator.Translator;
import eu.aria.util.translator.api.ActiveMQConnector;
import eu.aria.util.translator.api.AgentFeedback;
import eu.aria.util.translator.api.FileCache;
import eu.aria.util.translator.api.ReplacerGroup;
import greta.FlipperDemo.input.ASRInputManager;
//import greta.FlipperDemo.input.ASRInputManager;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import static java.lang.Thread.sleep;
import java.nio.charset.StandardCharsets;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import java.util.logging.Level;
import java.util.logging.Logger;

import greta.auxiliary.fmlannotator.Model;
import greta.core.intentions.FMLTranslator;
import greta.core.intentions.Intention;
import greta.core.intentions.IntentionPerformer;
import static greta.core.util.CharacterDependentAdapter.getCharacterManagerStatic;
import javax.swing.JComboBox;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.lang.InterruptedException;

import greta.core.util.xml.XML;
import greta.core.util.xml.XMLParser;
import greta.core.util.xml.XMLTree;
import greta.core.util.CharacterManager;
import greta.core.util.id.ID;
import greta.core.util.id.IDProvider;
import greta.core.util.Mode;
import greta.core.util.enums.CompositionType;

// import greta.FlipperDemo.main.FlipperLauncherMain;

/**
 * Created by WaterschootJB on 30-5-2017.
 */
public class FMLManager extends SimpleManager{

    private static org.slf4j.Logger logger = LoggerFactory.getLogger(FMLManager.class.getName());
    public boolean stoppedTalking;
    private String logfile;
    private boolean isPlanning;
    private SimpleProducerWrapper sendAgentData;
    private SimpleProducerWrapper sendDialogTurn;//sends agent's start here and user stopped in NLUManager
    public FMLManager(){
        super();
        this.receivedBMLQueue = new LinkedBlockingQueue<>();
        this.receiverBML = new SimpleReceiverWrapper(UrlBuilder.getUrlTcp(amqHostname,amqPort),amqOutputBML,true);
        this.isPlanning = false;
        setup();
        
       System.out.println("FMLManager started");
        this.sendDialogTurn = new SimpleProducerWrapper(UrlBuilder.getUrlTcp(amqHostname,amqPort),"DialogTurn",true);
        this.sendDialogTurn.init();
        this.sendAgentData = new SimpleProducerWrapper(UrlBuilder.getUrlTcp(amqHostname,amqPort), amqDialog,true);
       this.sendAgentData.init();
    }
    public FMLManager(String host, String port, String senderFmlTopic, String flipperTemplateFolder, String sstTask, String sstTaskIndex){
        super();
        setAmqHostname(host);
        setAmqPort(port);
        setAmqSendTopicName(senderFmlTopic);

//        templateFolderPath = flipperTemplateFolder;
//        sstTask = sstTask;
//        sstTaskIndex = sstTaskIndex;

        this.templateFolderPath = flipperTemplateFolder;
        this.sstTask = sstTask;
        this.sstTaskIndex = sstTaskIndex;
        
        this.receivedBMLQueue = new LinkedBlockingQueue<>();
        this.receiverBML = new SimpleReceiverWrapper(UrlBuilder.getUrlTcp(amqHostname,amqPort),amqOutputBML,true);
        this.isPlanning = false;
        setup();
        
       System.out.println("FMLManager started");
       /* this.sendDialogTurn = new SimpleProducerWrapper(UrlBuilder.getUrlTcp(amqHostname,amqPort),"DialogTurn",true);
        this.sendDialogTurn.init();
        this.sendAgentData = new SimpleProducerWrapper(UrlBuilder.getUrlTcp(amqHostname,amqPort), amqDialog,true);
       this.sendAgentData.init();
    */
    }
    
    private boolean agentTalking = false;
    private String outputBML = "";
    private String amqSendTopicName = null; // "greta.input.FML";
    private String amqFeedbackTopicName = "greta.output.feedback.BML";
    private String amqOutputBML = "greta.output.BML";
    private String amqPort = null;
    private String amqHostname = null; // 192.168.0.1
    private String amqDialog = "dialog";
    private String replacerConfigPath = "Common\\Data\\FlipperResources\\data\\a-config.json";
    private String templateFolderPath = null; //"Common/Data/FlipperResources/fmltemplates";
    private boolean showFMLGUI = false;
    private boolean showFallbackGUI = true;
    private boolean disableAMQ = false;
    LinkedList<QueueableBehaviour> behaviourQueue = new LinkedList<>();
    ActiveMQConnector activeMQConnector;
    private boolean isConnected;
    private SimpleReceiverWrapper receiverBML;
    private BlockingQueue<String> receivedBMLQueue = null;
    
    private String sstTask = null;
    private String sstTaskIndex = null;
    
//    private String lang = null;
    private String lang = "FR";
    
    // System.getProperty("user.dir")+"\\Scripts\\ATcls\\ATclassifyFR.py"
    // " + System.getProperty("user.dir") + "\Scripts\SST\
    
    private String alternative_xml_file1 = "AgentSpeech_startEval";
    private String alternative_xml_file2 = "AgentSpeech_end";
//    private String process_killer_command = "cmd /c D:\\Takeshi\\SST\\SST_main\\greta_python\\activate_env_kill_ffmpeg.vbs";
//    private String process_killer_command = "cmd /c " + System.getProperty("user.dir") + "\\Scripts\\SST\\activate_env_kill_ffmpeg.vbs";
    private String process_killer_command = "cmd /c " + System.getProperty("user.dir") + "\\Scripts\\SST\\activate_env_kill_ffmpeg.bat";
    private Process process1 = null;
    
    private boolean evaluationFinished = false;
//    private String copy_command = "cmd /c D:\\Takeshi\\SST\\SST_main\\greta_python\\activate_env_copy.bat";
    private String copy_command = "cmd /c " + System.getProperty("user.dir") + "\\Scripts\\SST\\activate_env_copy.bat";
    private Process process2 = null;

//    private String evaluation_command = "cmd /c D:\\Takeshi\\SST\\SST_main\\greta_python\\activate_env_eval_feedback.bat " + sstTask;
    private String evaluation_command = "cmd /c " + System.getProperty("user.dir") + "\\Scripts\\SST\\activate_env_eval_feedback.bat ";
    private Process process3 = null;

//    private String feedback_visualization_command = "cmd /c D:\\Takeshi\\SST\\SST_main\\greta_python\\activate_env_viewer_primary.bat";
    private String feedback_visualization_command = "cmd /c " + System.getProperty("user.dir") + "\\Scripts\\SST\\activate_env_viewer_secondary.bat";
    private Process process4 = null;
    
    private Model real;
    private JComboBox languageComboBox;
//    private String result_json_file = "D:\\Takeshi\\SST\\SST_main\\greta_python\\result_for_viewer\\result.json";
    private String result_json_file = System.getProperty("user.dir") + "\\Scripts\\SST\\result_for_viewer\\result.json";
    
    private String log_text_file = System.getProperty("user.dir") + "\\Scripts\\SST\\src_text\\temp.txt";
    
    private CharacterManager charactermanager;
    private ArrayList<IntentionPerformer> performers = new ArrayList<IntentionPerformer>();
    
    // private FlipperLauncherMain flipper_launcher_obj;
    
    public void setAmqHostname(String host){
        amqHostname = host;
    }
    public void setAmqSendTopicName(String senderFmlTopic){
        amqSendTopicName = senderFmlTopic;
    }
    public void setAmqPort(String port){
        amqPort = port;
    }
    @Override
    public void process(){
        processNext();
        super.process();
    }
    Map<String, Translator> translators = new HashMap<>();

    /**
     * DEPRACATED: OLD FLIPPER
     * @param params
     * @param paramArrays
     */
    public void setParams(Map<String, String> params, Map<String, String[]> paramArrays){
        if(!params.containsKey("fml_template_folder")){
            System.err.println("Manager "+ name + "("+id+") must provide a FML template folder containing only FML templates: 'fml_template_folder'.");
        }else{
            templateFolderPath = params.get("fml_template_folder");
        }
//        if(!params.containsKey("sstTask")){
//            System.err.println("Manager "+ name + "("+id+") must provide sstTask: 'sstTask'.");
//        }else{
//            sstTask = params.get("sstTask");
//        }
        if(!params.containsKey("replacer_config_path")){
            System.err.println("Manager "+ name + "("+id+") "
                    + "must provide a path for the replacer config file: 'replacer_config_path'.");
        }else{
            replacerConfigPath = params.get("replacer_config_path");
        }
        if(params.containsKey("amq_hostname")){
            amqHostname = params.get("amq_hostname");
        }
        if(params.containsKey("amq_port")){
            amqPort = params.get("amq_port");
        }
        if(params.containsKey("amq_feedback_topic_name")){
            amqFeedbackTopicName = params.get("amq_feedback_topic_name");
        }
        if(params.containsKey("amq_send_topic_name")){
            amqSendTopicName = params.get("amq_send_topic_name");
        }
        if(params.containsKey("amq_full_bml_feedback")){
            amqOutputBML = params.get("amq_full_bml_feedback");
        }
        if(params.containsKey("show_fml_gui")){
            showFMLGUI = Boolean.parseBoolean(params.get("show_fml_gui"));
        }
        if(params.containsKey("show_fallback_gui")){
            showFallbackGUI = Boolean.parseBoolean(params.get("show_fallback_gui"));
        }
        if(params.containsKey("disable_amq")){
            disableAMQ = Boolean.parseBoolean(params.get("disable_amq"));
        }
        setup();
    }

    ReplacerGroup replacerGroup;
    static long lastTimeStamp = System.currentTimeMillis();
    static int fmlId = 0;

    public static Say agentSay;

    public void setLogfile(String s){
        this.logfile = s;
    }

    HashMap<String, File> templates = new HashMap<>();


    /**
     * Sets up the ActiveMQConnectors
     */
    public void setup(){
        if(!disableAMQ){
            replacerGroup = new ReplacerGroup(replacerConfigPath);

            activeMQConnector = new ActiveMQConnector();
            activeMQConnector.setReplacerGroup(replacerGroup);

            activeMQConnector.initialiseSender(amqHostname, amqPort, amqSendTopicName);
            activeMQConnector.initialiseFeedback(amqHostname, amqPort, amqFeedbackTopicName);
            activeMQConnector.initialiseFeedback(amqHostname, amqPort, amqOutputBML);


           System.out.println("BML receiver started");
            receiverBML.start((Message message) ->{
                if(message instanceof TextMessage){
                    try{
                        outputBML = (((TextMessage) message).getText());
                        receiveData(outputBML);
                    }
                    catch(JMSException e){
                        e.printStackTrace();
                    }
                }
            });


            if(showFMLGUI){
                activeMQConnector.showSenderGui(200, 200);
                activeMQConnector.showFeedbackGui(200, 400);
            }

            File templateFolder = new File(templateFolderPath);

            File[] filesInFolder = templateFolder.listFiles();
            for (File cur : filesInFolder) {
                if(cur.getName().endsWith(".xml")){
                    templates.put(cur.getName(), cur);
                }
            }

            FileCache.getInstance().preloadFilesInFolder(templateFolder);
            this.isConnected = true;

            activeMQConnector.addFeedbackListener((AgentFeedback feedback) -> {
                switch (feedback.getType()) {
                    case Start:
                        if(feedback.getId().startsWith("ID")){
                            Message m = this.sendDialogTurn.createTextMessage("agent_start");
                           this.sendDialogTurn.sendMessage(m);

                            this.isPlanning = false;
                           System.out.println("stopped planning!");
                           System.out.println("Agent started playback. ID: " + feedback.getId()+" type: "+feedback.toString());
                            setIsTalking(true);
                            if(agentSay == null)
                                this.agentSay = new Say();
                            this.agentSay.setActorName("Agent");
                            this.agentSay.setTimestamp(System.currentTimeMillis());
                            this.agentSay.setTalking(true);
                            this.agentSay.setLanguage("fr");
                        }
                        break;
                    case End:
                        if(feedback.getId().startsWith("ID")) {

                                Message m = this.sendDialogTurn.createTextMessage("agent_end");
                               this.sendDialogTurn.sendMessage(m);
                           System.out.println("Agent finished. ID: " + feedback.getId() + " type: " + feedback.toString());
                            setIsTalking(false);
                            this.agentSay.setLength(System.currentTimeMillis()-this.agentSay.getTimestamp());
                            this.agentSay.setTalking(false);

                            stoppedTalking = true;
                            try {
                                sleep(500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            if(this.agentSay.getText() != null) {
                                addAgentSay();
                            }
                            stoppedTalking = false;
                        }
                        break;
                    case Other:
                        try{
                        if(feedback.getId().startsWith("ID")){
                            
                           System.out.println("Agent didnt perform. ID: " + feedback.getId()+" type: "+feedback.toString());
                            if(feedback.getTypeString().equals("dead")){
                                setIsTalking(false);

                            }
                            if(feedback.getTypeString().equals("stopped"+" type: "+feedback.toString())){
                                setIsTalking(false);
                            }
                        }
                        }
                        catch (Exception e){
                            //System.out.println(feedback.getType()..toString());
                        }
                    default:
                        try{
                        if(feedback.getTypeString().equals("dead") || feedback.getTypeString().equals("stopped")){
                            logger.debug("Feedback not of DM: " + feedback.getTypeString());
                        }
                        else{
                           System.out.println("Unknown feedback type from GRETA! '" + feedback.getTypeString() + "'"+" type: "+feedback.toString());
                        }
                        }
                        catch(Exception e){
                            //O no, some weird feedback

                        }
                        break;
                }
            });
        }
        
        // Initialize text log file
        FileWriter filewriter = null;
        try {
            File file = new File(log_text_file);
            filewriter = new FileWriter(file);
            filewriter.write("");
            filewriter.close();

        } catch (IOException ex) {
            Logger.getLogger(FMLManager.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                filewriter.close();
            } catch (IOException ex) {
                Logger.getLogger(FMLManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }


    }

    /**
     * Receives a BML string and prints what the agent has actually said.
     * @param outputBML, the BML String
     */
    private void receiveData(String outputBML) {
        //outputBML = outputBML.replaceAll("xmlns","xmlns:xsi");
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        InputSource bml = new InputSource();
        bml.setCharacterStream(new StringReader(outputBML));
        try {
            Document doc = builder.parse(bml);
            logger.debug("Agent data received: " + outputBML);
            this.receivedBMLQueue.add(outputBML);

            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();
            String speech = "";
            try {
                String expression = "/bml/speech/voice";
                XPathExpression expr = xpath.compile(expression);
                speech = (String) expr.evaluate(doc, XPathConstants.STRING);
                speech = speech.replace("\n"," ");
                speech = speech.trim();
            } catch (XPathExpressionException e) {
                e.printStackTrace();
            }
            logger.debug("Agent says: " + speech);
            String language = "";
            try {
                String expression = "string(/bml/speech/@language)";
                XPathExpression expr = xpath.compile(expression);
                language = (String) expr.evaluate(doc, XPathConstants.STRING);
            } catch (XPathExpressionException e) {
                e.printStackTrace();
            }
            if(agentSay == null)
                this.agentSay = new Say();
            this.agentSay.setText(speech);
//            this.agentSay.setLanguage(language);
            this.agentSay.setLanguage("fr");

        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isBMLReceiverConnected(){
        return this.receiverBML.isReceiver();
    }

    public boolean hasReceivedBML(){
        return !this.receivedBMLQueue.isEmpty();
    }

    /**
     * Needs to be updated to a robust check. The XMLTranslator needs an update in order to retrieve the status
     * of the AMQ wrappers.
     * @return if the AMQ for FML sending is still active.
     */
    public boolean isConnected(){
        return isConnected;
    }

    /**
     * Checks if there is still a behaviour in the queue
     * @return true if so
     */
    public boolean hasBehaviour(){
        return !this.behaviourQueue.isEmpty();
    }

    public boolean setIsTalking(boolean isTalking){
        return this.agentTalking = isTalking;
    }

    public boolean getAgentIsTalking(){return this.agentTalking;}

    public boolean showFallbackGUI(){
        return showFallbackGUI;
    }
    
    private void processNext(ArrayList<String> argNames, ArrayList<String> argValues){

        // Flipper wrong encoding (change encoding UTF_8 -> ISO_8859_1)
        // get current working directory
        String dir_curr = System.getProperty("user.dir");
        String FML_name;
        String content;
        
        File f = templates.get(argValues.get(argNames.indexOf("template"))+".xml");
        if((f == null) && (evaluationFinished == false)){
            
            // Try with scenario index 0 (default path)
            String[] temporary_template_name_array = argValues.get(argNames.indexOf("template")).split("_");
            f = templates.get(temporary_template_name_array[0] + "_" + temporary_template_name_array[1] + "_0.xml"); 
            if (f != null){
                FML_name = temporary_template_name_array[0] + "_" + temporary_template_name_array[1] + "_0";
            }
            // If failed to load, alternative files were loaded
            else{
                System.out.println("Template named: "+argValues.get(argNames.indexOf("template")) + " not found!" );
                System.out.println("Alternative file(" + alternative_xml_file1 + ".xml) will be loaded.");
                FML_name = alternative_xml_file1;

                String[] splitted_command = process_killer_command.split(" ");
                try {
                    process1 = new ProcessBuilder(splitted_command).redirectErrorStream(true).start();
                    process1.waitFor();
                    System.out.println("### Recording was killed from FMLManager.java");
                } catch (IOException e) {
                    System.out.println(e);
                } catch (InterruptedException e) {
                    System.out.println(e);
                }
            }
        }
        else if((f == null) && (evaluationFinished == true)){
            System.out.println("Template named: "+argValues.get(argNames.indexOf("template")) + " not found!" );
            System.out.println("Alternative file(" + alternative_xml_file2 + ".xml) will be loaded.");
            FML_name = alternative_xml_file2;
        }
        else{
            // FML template name 
            FML_name = argValues.get(argNames.indexOf("template"));
        }
        
        content = load_xml_file(dir_curr, FML_name);
        if((FML_name == alternative_xml_file1) || (FML_name == alternative_xml_file2)){
            xml2speech(content, argNames, argValues, false);
        }
        else{
            xml2speech(content, argNames, argValues, true);
        }
        
        // start evaluation calculation and feedback generation for the SST session
        if((f == null) && (evaluationFinished == false)){
            
            String[] _ = postProcessSST();
            
            content = load_xml_file(dir_curr, "AgentSpeech_feedback");
            xml2speech(content, argNames, argValues, false);

            evaluationFinished = true;
        }

    }

    private boolean processNext(){
        if(behaviourQueue.peek() != null){
            QueueableBehaviour next = behaviourQueue.poll();
            if(!disableAMQ){
                processNext(next.argNames, next.argValues);
            }
            return true;
        }
        return false;
    }
    
    private void xml2speech(String content, ArrayList<String> argNames, ArrayList<String> argValues, boolean saveLog){

        System.out.println("### xmlString");
        System.out.println(content);
        System.out.println("### xmlString");
        
        String tagFreeInput = content.replaceAll("<[^>]+>", "");
        tagFreeInput = tagFreeInput.replace("tmp/from-fml-apml.pho", "").trim();
        System.out.println("### tagFreeInput");
        System.out.println(tagFreeInput);
        System.out.println("### tagFreeInput");
        
        if(saveLog){
            FileWriter filewriter = null;
            try {
                File file = new File(log_text_file);
                filewriter = new FileWriter(file, true);
                filewriter.write("Agent: ");
                filewriter.write(tagFreeInput);
                filewriter.write("\n");
                filewriter.close();
                
            } catch (IOException ex) {
                Logger.getLogger(FMLManager.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    filewriter.close();
                } catch (IOException ex) {
                    Logger.getLogger(FMLManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
        
        // read the template
        replacerGroup.readString(content);
        
        // generate internal decision
        replacerGroup.generateComponents();

        //Replace attributes and variables
        for(String argName :  argNames){
            String value = argValues.get(argNames.indexOf(argName));
            if(argName.startsWith("var.")){
                argName = argName.substring(4);
                replacerGroup.replaceVar(argName, value);
            }
            else if(argName.startsWith("alt.")){
                argName = argName.substring(4);
                replacerGroup.replaceVar(argName, value);
            }
            else if(argName.startsWith("certainty.")){
                argName = argName.substring(10);
                replacerGroup.replaceAttribute("certainty", argName, value);
            }
            else if(argName.startsWith("emotion.")){
                argName = argName.substring(8); // "em2.type"
                int secondPoint = argName.indexOf(".");
                String eid = argName.substring(0,secondPoint); //"em2"
                argName = argName.substring(secondPoint+1);
                replacerGroup.replaceAttribute(eid, argName, value);
            }
            else if(argName.startsWith("importance.")){
                argName = argName.substring(11);
                replacerGroup.replaceAttribute("importance", argName, value);
            }
            else if(argName.startsWith("voice.")){
                argName = argName.substring(6);
                replacerGroup.replaceAttribute("", argName, value);
            }
            else if(argName.startsWith("fml-apml.")){
                argName = argName.substring(9);
                replacerGroup.replaceAttribute("",argName,value);
            }
        }
        // this will perform the replacements and sends them to the ActiveMQ topic
        if(!disableAMQ){
            replacerGroup.performReplacements("ID_" + fmlId++);
        }
        //setIsTalking(false);
        //lastTimeStamp = System.currentTimeMillis();
    }


    public void queue( ArrayList<String> argNames, ArrayList<String> argValues){
        QueueableBehaviour next = new QueueableBehaviour();
        next.argNames = argNames;
        next.argValues = argValues;
        behaviourQueue.add(next);
    }

    public void setPlanning(Boolean planning) {
       System.out.println("Planning...");
        this.isPlanning = planning;
    }

    private class QueueableBehaviour{
        public ArrayList<String> argNames;
        public ArrayList<String> argValues;
    }

    public void addAgentSay() {
        if(logfile != null) {
            agentSay.setLanguage("fr");
            long endTime = agentSay.getTimestamp() + agentSay.getLength();
            String turn = agentSay.getId() + ";" + agentSay.getTimestamp() + ";" + endTime + ";" + agentSay.getActorName() + ";" + agentSay.getText() +  ";";
            turn +=  agentSay.strategy; //agentSay.restpose + ";" + agentSay.gesture + ";" + agentSay.smile + ";" + agentSay._text;
            /*+ ";"+ agentSay.random + ";"+ agentSay.reward+ ";";
            for(int i =0; i<agentSay.qTable.length;i++)
            {
                turn += agentSay.qTable[i]+ ";";
            }*/
            turn += "\n";
            Charset charset = Charset.forName("UTF-8");
            byte data[] = turn.getBytes(charset);
            Path file = Paths.get(logfile);
            try (OutputStream out = new BufferedOutputStream(
                    Files.newOutputStream(file, CREATE, APPEND))) {
                out.write(data, 0, data.length);
            //    Message m = this.sendAgentData.createTextMessage(turn);
              //  this.sendAgentData.sendMessage(m);
            } catch (IOException e) {
               System.out.println("IOException: %s%n"+ e);
            }
            //this.agentSay = null;

        }
    }

    public String getLastText(){
        if(agentSay == null || agentSay.getText() == null){
            return "";
        }
        return this.agentSay.getText();
    }

    public boolean isPlanning(){
        return this.isPlanning;
    }
    
    private String[] postProcessSST(){
        
        // lang = getCharacterManagerStatic().getValueString("CEREPROC_LANG").split("-")[0].toUpperCase();
//        lang = 
        
        String[] splitted_command = copy_command.split(" ");
        try {
            
            process2 = new ProcessBuilder(splitted_command).redirectErrorStream(true).start();
            

            System.out.println("### Process2 (copy) was activated from FMLManager.java");
            outputConsumer(process2, true, true);
            
        } catch (IOException e) {
            System.out.println(e);
        }
        System.out.println("### Process2 (copy) was completed");
        
        evaluation_command = evaluation_command + sstTask + " " + sstTaskIndex + " " + lang;
        splitted_command = evaluation_command.split(" ");
        try {
            process3 = new ProcessBuilder(splitted_command).redirectErrorStream(true).start();
            System.out.println("### Process3 (evaluation): " + evaluation_command);
            System.out.println("### Process3 (evaluation): " + sstTask);
            System.out.println("### Process3 (evaluation): " + sstTaskIndex);
            System.out.println("### Process3 (evaluation): " + lang);
            System.out.println("### Process3 (evaluation) was activated from FMLManager.java");
            outputConsumer(process3, true, true);
        } catch (IOException e) {
            System.out.println(e);
        }
        System.out.println("### Process3 (evaluation) was completed");
        
//        splitted_command = feedback_visualization_command.split(" ");
//        try {
//            process3 = new ProcessBuilder(splitted_command).redirectErrorStream(true).start();
//            System.out.println("### Process4 (feedback visualization) was activated from FMLManager.java");
//            outputConsumer(process3, false, true);
//        } catch (IOException e) {
//            System.out.println(e);
//        }
//        //System.out.println("### Process3 (feedback visualization) was completed");        
        
        // load json file
        File jsonInputFile = new File(result_json_file);
        InputStream is;
        String positiveFeedback = "";
        String negativeFeedback = "";
        try {
            is = new FileInputStream(jsonInputFile);
            JsonReader reader = Json.createReader(is);
            JsonObject feedback_json = reader.readObject();
            reader.close();
            positiveFeedback = feedback_json.getString("PositiveComment");
            negativeFeedback = feedback_json.getString("NegativeComment");
            System.out.println("### Positive comment: " + positiveFeedback);
            System.out.println("### Negative comment: " + negativeFeedback);
            
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        // Evaluation and feeback pipeline
//        String[] feedback = {"positive feedback", "negative feedback"};
        String feedback = positiveFeedback + "," + negativeFeedback;
        String[] feedbackArray = feedback.split(",");

        System.out.println("### Feedback comment was loaded");
        
        return feedbackArray;
        
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
    
    private String load_xml_file(String dir_curr, String FML_name){
        
        //string FMLfilename with path
        
        // String f_name = dir_curr+"\\Common\\Data\\FlipperResources\\fmltemplates\\"+FML_name+".xml";
        // String f_name = dir_curr+"\\Common\\Data\\FlipperResources\\fmltemplates_SST\\ASK_01\\"+FML_name+".xml";
        // String f_name = dir_curr + flipper_launcher_obj.getflipperTemplateFolderPath() + "\\" + FML_name + ".xml";
        String f_name = templateFolderPath + "\\" + FML_name + ".xml";
        
        Path path_f = Paths.get(f_name);
        String content = "";
        try {

            // read FMLfile with ISO_8859_1 encoding
            // content = FileCache.getInstance().ReadFile(path_f,StandardCharsets.ISO_8859_1);
            
            // Previous code with UTF_8 encoding
            // content = FileCache.getInstance().getFileContent(f);
            
            content = FileCache.getInstance().ReadFile(path_f,StandardCharsets.UTF_8);

            System.out.println("FMLFile content");
            System.out.println(content);
        } catch (IOException ex) {
            Logger.getLogger(FMLManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return content;

    }
    
}
