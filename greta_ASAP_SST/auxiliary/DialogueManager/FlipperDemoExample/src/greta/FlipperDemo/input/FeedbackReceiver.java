package greta.FlipperDemo.input;
import greta.auxiliary.activemq.Receiver;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonValue;
import java.util.concurrent.TimeUnit; //for time delay

public class FeedbackReceiver extends Receiver<String> {

	private BlockingQueue<String> messages = new LinkedBlockingQueue();
  public boolean is_talking = false;

    public FeedbackReceiver(String host, String port, String topic){
        super(host, port, topic);


    }
    public FeedbackReceiver(){

      super("localhost", "61616", "semaine.callback.output.feedback");

    }


    /**
     * Initialize the TemplateInputReceiver
     * @return true if successfully started
     */
    public boolean init(){

       return true;

    }



    /**
     * Stop the TemplateInputReceiver
     * @return
     */
    public boolean stop(){

        return true;
    }

    /**
     * Start the TemplateInputReceiver (do not call after initialization)
     * @return if succeeded
     */
    public boolean start(){

        return true;
    }

    /**
     * General method for if it has transcribed speech
     * @return true if a result is final
     */
    public boolean hasMessage(){
        return messages.size() != 0;
    }

    /**
     * General method for retrieving transcribed speech
     * @return the callback from TemplateInputReceiver
     */
    public String getMessage(){
		try {
            return messages.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "";
    }


    @Override
    protected void onMessage(String message, Map<String, Object> map) {

            JsonReader jr = Json.createReader(new StringReader(message));
            JsonObject jo = jr.readObject();
            JsonString type = jo.getJsonString("type");

            System.out.println(" feedback received **: "+ jo.toString());
            String cleanType = type.toString().replaceAll("\"", "");
			System.out.println("New cleanType" + cleanType);

            if (cleanType.equals("start")){
              is_talking = true;
			  System.out.println("Here Start");
            }
            if (cleanType.equals("end") & !jo.containsKey("timeMarker_id")){
				try {
					TimeUnit.SECONDS.sleep(2);
				} catch (InterruptedException ex) {
					System.out.println("error: delay");
				}
              is_talking = false;
            }
			messages.add(String.valueOf(is_talking));
    }

    @Override
    protected String getContent(Message message)  {

		String msg =null;
            try {
                msg = ((TextMessage) message).getText();
            } catch (JMSException ex) {
               // Logger.getLogger(MessageReceiver.class.getName()).log(Level.SEVERE, null, ex);
               System.out.println("cought exception in getContent : "+ ex.toString());
            }
		return msg;
    }

    @Override
    protected void onConnectionStarted() {
    	super.onConnectionStarted();
    }

}
