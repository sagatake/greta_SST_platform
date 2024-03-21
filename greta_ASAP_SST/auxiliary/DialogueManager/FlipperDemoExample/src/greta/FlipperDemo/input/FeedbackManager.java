/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package greta.FlipperDemo.input;

/**
 *
 * @author Barnge
 */

import greta.FlipperDemo.main.FlipperLauncherMain;

public class FeedbackManager {

   private FeedbackReceiver  inputReceiver;
   private FlipperLauncherMain singletoneInstance = null;

   private String host = null;
   private String port = null;
   private String gretaTemplateTopic = null;


   public FeedbackManager(String gretaTemplateTopic) {

        this.gretaTemplateTopic = gretaTemplateTopic;
    }


   public boolean init()
   {   System.out.println("Template input manager initialized");
       singletoneInstance = FlipperLauncherMain.getInstance();
       if(singletoneInstance != null){
           System.out.println("jai gayatri mata: Template input got main singleton instance : ");
       }

       host = singletoneInstance.getHost();
       port = singletoneInstance.getPort();
       gretaTemplateTopic = this.gretaTemplateTopic;
       System.out.println(gretaTemplateTopic);
       inputReceiver = new FeedbackReceiver(host, port, gretaTemplateTopic);



       return true;
   }
   public void initFeedbackReceiver(String host, String port, String topic){
       inputReceiver = new FeedbackReceiver(host, port, topic);
   }

    public boolean hasMessage(){
        return inputReceiver.hasMessage();
    }

     public String getMessage(){
         return inputReceiver.getMessage();
     }
}
