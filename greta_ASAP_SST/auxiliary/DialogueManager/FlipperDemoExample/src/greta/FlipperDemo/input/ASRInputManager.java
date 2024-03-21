/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package greta.FlipperDemo.input;

/**
 *
 * @author Jieyeon Woo, Takeshi Saga
 *
 */

import ch.qos.logback.classic.util.ContextInitializer;
import greta.FlipperDemo.dm.managers.FMLManager;
import greta.FlipperDemo.main.FlipperLauncherMain;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ASRInputManager {
    
   
    
    private SpeechInputReceiver  inputReceiver;
    private FlipperLauncherMain singletoneInstance = null;

    private String host = null;
    private String port = null;
    private String gretaASRTopic = null;
    private String gretaInputTopic = null;
    private String msg;

    private Process processAT = null;
    private Process process1 = null;
    private Process process2 = null;
    private Process process3 = null;
    
    private String processAT_command = "python " + System.getProperty("user.dir") + "\\Scripts\\ATcls\\ATclassifyFR.py";

    // private String process1_command = "cmd /c D:\\Takeshi\\SST\\SST_main\\greta_python\\record_with_cd.bat";
    private String process1_command = "cmd /c " + System.getProperty("user.dir") + "\\Scripts\\SST\\record_with_cd.bat";

    // private String process_killer_command = "cmd /c D:\\Takeshi\\SST\\SST_main\\greta_python\\activate_env_kill_ffmpeg.vbs";

//    private String process2_command = "cmd /c D:\\Takeshi\\SST\\SST_main\\greta_python\\eval_feedback.vbs";
    private String process2_command = "cmd /c " + System.getProperty("user.dir") + "\\Scripts\\SST\\activate_env_eval_feedback.bat";
    private int process2_cnt= 0;

    private String log_text_file = System.getProperty("user.dir") + "\\Scripts\\SST\\src_text\\temp.txt";
    
    public boolean init()
    {   
       System.out.println("ASR input manager initialized");
       singletoneInstance = FlipperLauncherMain.getInstance();
       if(singletoneInstance != null){
           System.out.println("jai gayatri mata: asrinput got main singleton instance : "
                   + singletoneInstance.getGretaASRTopic());
       }
       
       host = singletoneInstance.getHost();
       port = singletoneInstance.getPort();
       gretaASRTopic = singletoneInstance.getGretaASRTopic();
       inputReceiver = new SpeechInputReceiver(host, port, gretaASRTopic);

       return true;
    }
    public void initSpeechInputReceiver(String host, String port, String topic){
        inputReceiver = new SpeechInputReceiver(host, port, topic);
    }
   
    public boolean hasMessage(){
        return inputReceiver.hasMessage();
    }
    
    public String getMessage(){
        this.msg = inputReceiver.getMessage();
                 
        return msg;
     }
    
    public void saveMessage(){

        FileWriter filewriter = null;
        try {
            File file = new File(log_text_file);
            filewriter = new FileWriter(file, true);
            filewriter.write("User: ");
            filewriter.write(this.msg);
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
    
    public void startATclsProgram() throws IOException{
        try {
            //this.processAT = new ProcessBuilder("python", System.getProperty("user.dir")+"\\Scripts\\ATcls\\ATclassifyFR.py").redirectErrorStream(true).start();
            String[] splitted_command = this.processAT_command.split(" ");
            this.processAT = new ProcessBuilder(splitted_command).redirectErrorStream(true).start();
        } catch (IOException e) {
            System.out.println(e);
        }
    }
    
    public void endATclsProgram(){
        this.processAT.destroy();
        System.out.println("Python program terminated");
    }
     
    // Check if the received message is an Automatic Thought via ATcls
    public String checkMessageIsAT() throws IOException {
        String isAT="";;
        try {
            BufferedWriter pythonInput = new BufferedWriter(new OutputStreamWriter(this.processAT.getOutputStream() )); //,"ISO-8859-15"));
            BufferedReader pythonOutput = new BufferedReader(new InputStreamReader(this.processAT.getInputStream()));
            
            Thread thread = new Thread(() -> {
                while(this.processAT.isAlive()){
                    try{
                        pythonInput.write(this.msg);
                        pythonInput.newLine();
                        pythonInput.flush();
                    } catch (IOException ex) {
                        System.out.println(ex.getLocalizedMessage());
//                        this.processAT.destroy();
//                        System.out.println("Python program terminated");
                        Logger.getLogger(ASRInputManager.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
            );
            thread.start();
            String output=null;
            StringBuilder sb = new StringBuilder();
            while(this.processAT.isAlive() && (output=pythonOutput.readLine())!=null){
                System.out.println("output: "+output);
                sb.append(output);
            }
            isAT = sb.toString();
            pythonOutput.close();
            pythonInput.close();
     
        } catch (IOException ex) {
            Logger.getLogger(FMLManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("Checked ATcls: " + this.msg + " " + isAT + "\n");
        
        return isAT;
    }

    
    public void startProcess1() throws IOException {
        String[] splitted_command = this.process1_command.split(" ");
        try {
            this.process1 = new ProcessBuilder(splitted_command).redirectErrorStream(true).start();
            outputConsumer(this.process1, false, false);
        } catch (IOException e) {
            System.out.println(e);
        }    
    }
 
    public String checkProcess1() throws IOException {
        
        String result="Recording";
        return result;
    }

    public void endProcess1(){
        
        // This is dummy(legacy) function.
        // Recording killer was moved to inside of processNext function in FMLManager.java
        System.out.println("endProcess1 signal was received in endProcess1 function of ASRInputManager.java");
        
//        try{
//            this.process1.getInputStream().close();
//            this.process1.getOutputStream().close();
//            this.process1.getErrorStream().close();
//            this.process1.destroy();
//            System.out.println("Process1 terminated: " + this.process1_command);            
//        }
//        catch (IOException ex) {
//            Logger.getLogger(FMLManager.class.getName()).log(Level.SEVERE, null, ex);            
//        }
//        
//        // alternative killer
//        String[] splitted_command = this.process_killer_command.split(" ");
//        try {
//            this.process1 = new ProcessBuilder(splitted_command).redirectErrorStream(true).start();
//            System.out.println("Process1 terminated (alternative): " + this.process1_command);            
//        } catch (IOException e) {
//            System.out.println(e);
//        }

    }



    public void startProcess2() throws IOException {
        
        // Implementation was moved to FMLManager.java
        
//        String[] splitted_command = this.process2_command.split(" ");
//        try {
//            this.process2 = new ProcessBuilder(splitted_command).redirectErrorStream(true).start();
//        } catch (IOException e) {
//            System.out.println(e);
//        }    
    }
 
    public String checkProcess2() throws IOException {
        
        // Implementation was moved to FMLManager.java

//        String result="Calculating: " + process2_cnt;
//        process2_cnt = process2_cnt + 1;
//        return result;
        return "";
    }

    public void endProcess2(){
        
        // Implementation was moved to FMLManager.java
        
//        try{
//            this.process1.getInputStream().close();
//            this.process1.getOutputStream().close();
//            this.process1.getErrorStream().close();
//            this.process1.destroy();
//            System.out.println("Process1 terminated: " + this.process1_command);            
//        }
//        catch (IOException ex) {
//            Logger.getLogger(FMLManager.class.getName()).log(Level.SEVERE, null, ex);            
//        }
//        
//        // alternative killer
//        String[] splitted_command = this.process_killer_command.split(" ");
//        try {
//            this.process1 = new ProcessBuilder(splitted_command).redirectErrorStream(true).start();
//            System.out.println("Process1 terminated (alternative): " + this.process1_command);            
//        } catch (IOException e) {
//            System.out.println(e);
//        }

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
