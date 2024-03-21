/*
 * This file is part of Greta.
 *
 * Greta is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Greta is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Greta.  If not, see <https://www.gnu.org/licenses/>.
 *
 */
package greta.core.utilx.gui;

import com.illposed.osc.MessageSelector;
import com.illposed.osc.OSCBadDataEvent;
import com.illposed.osc.OSCMessageEvent;
import com.illposed.osc.OSCMessageListener;
import com.illposed.osc.OSCPacketEvent;
import com.illposed.osc.OSCPacketListener;
import com.illposed.osc.messageselector.OSCPatternAddressMessageSelector;
import com.illposed.osc.transport.udp.OSCPortIn;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author Andre-Marie Pez
 */
public final class Watson extends javax.swing.JFrame {

    /** Creates new form OpenAndLoad */
    public Watson() {
        initComponents();
    }


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jFileChooser1 = new javax.swing.JFileChooser();
        jFileChooser1.setCurrentDirectory(new File("./"));
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        textArea1 = new java.awt.TextArea();
        jCheckBox1 = new javax.swing.JCheckBox();

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)), "Watson", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 14))); // NOI18N

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel1.setText("Text Received");

        jCheckBox1.setText("Enable Watson");
        jCheckBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(textArea1, javax.swing.GroupLayout.DEFAULT_SIZE, 356, Short.MAX_VALUE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jCheckBox1)
                            .addComponent(jLabel1))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addComponent(jCheckBox1)
                .addGap(37, 37, 37)
                .addComponent(textArea1, javax.swing.GroupLayout.PREFERRED_SIZE, 167, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(67, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(32, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jCheckBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox1ActionPerformed
        Thread thread = new Thread() {
		@Override
		public void run() {
			try {
				SocketAddress socketAddress = new InetSocketAddress("127.0.0.1",9100) ;
                            OSCPortIn receiver = new OSCPortIn(socketAddress);
				//SocketAddress socketAddress1 = receiver.getRemoteAddress();
				MessageSelector messageSelector = new OSCPatternAddressMessageSelector("/unity/watson");
				OSCMessageListener messageListener = new OSCMessageListener() {
					
					@Override
					public void acceptMessage(OSCMessageEvent arg0) {
                                                try {
                                                    // TODO Auto-generated method stub
                                                    //System.out.println("message recieved");
                                                    //System.out.println("[INFO]:"+arg0.getMessage().getArguments());
                                                    System.out.println("RECEIVED MESSAGE");
                                                    String obj =arg0.getMessage().getArguments().toString().replace("[","").replace("]","");
                                                    String construction="<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?>\n" +
                                                            "<fml-apml>\n<bml>"+
                                                            "\n<speech id=\"s1\" language=\"english\" start=\"0.0\" text=\"\" type=\"SAPI4\" voice=\"marytts\" xmlns=\"\">"+
                                                            "\n<description level=\"1\" type=\"gretabml\"><reference>tmp/from-fml-apml.pho</reference></description>";
                                                    System.out.println("greta.core.intentions.FMLFileReader.TextToFML()");
                                                    int i=1;
                                                    String [] text =obj.split(" ");
                                                    for(int j=0;j<text.length;j++){
                                                        construction=construction+"\n<tm id=\"tm"+i+"\"/>"+text[j].replace("ISIR","Ezer");
                                                        i++;
                                                    }
                                                    i=i-1;
                                                    construction=construction+"\n</speech>\n</bml>\n<fml>\n";
                                                    if(obj.contains("Welcome")){
                                                        System.out.println("[INFO]1");
                                                        construction=construction+"<performative id=\"rp1\" type=\"greet\" start=\"0\" end=\"s1:tm"+2+"\" importance=\"1.0\"/>\n";
                                                        construction=construction+"<emotion id=\"e1\" type=\"joyStrong\" start=\"s1:tm1\" end=\"s1:tm:"+5+"\" importance=\"1.0\"/>";
                                                    }else if(obj.contains("ISIR?")){
                                                        System.out.println("[INFO]2");
                                                        construction=construction+"<beat id=\"rp1\" type=\"pos\" start=\"0\" end=\"s1:tm"+6+"\" importance=\"1.0\"/>\n";     
                                                        construction=construction+"<emotion id=\"e1\" type=\"joyStrong\" start=\"s1:tm1\" end=\"s1:tm:"+6+"\" importance=\"1.0\"/>";
                                                    }
                                                    else if(obj.contains("robots")){
                                                        System.out.println("[INFO]3");
                                                        construction=construction+"<adjectival id=\"rp1\" type=\"adj\" start=\"0\" end=\"s1:tm"+5+"+2\" importance=\"1.0\"/>\n"; 
                                                        construction=construction+"<emotion id=\"e1\" type=\"joyStrong\" start=\"s1:tm1\" end=\"s1:tm:"+5+"\" importance=\"1.0\"/>";
                                                    }
                                                    else if(obj.contains("robot")){
                                                        System.out.println("[INFO]4");
                                                        construction=construction+"<beat id=\"rp1\" type=\"pos\" start=\"s1:tm1\" end=\"s1:tm"+6+"+2\" importance=\"1.0\"/>\n"; 
                                                        construction=construction+"<emotion id=\"e1\" type=\"joyStrong\" start=\"s1:tm1\" end=\"s1:tm:"+6+"\" importance=\"1.0\"/>";
                                                    }
                                                    else if(obj.contains("video")){
                                                        System.out.println("[INFO]5");
                                                        construction=construction+"<deictic id=\"rp1\" type=\"ind\" start=\"s1:tm1\" end=\"s1:tm"+6+"+2\" importance=\"1.0\"/>\n";
                                                        construction=construction+"<emotion id=\"e1\" type=\"joyStrong\" start=\"s1:tm1\" end=\"s1:tm:"+6+"\" importance=\"1.0\"/>";
                                                    }
                                                    else if (obj.contains("Bye")){
                                                        System.out.println("[INFO]6");
                                                        construction=construction+"<iconic id=\"rp1\" type=\"byebye\" start=\"0\" end=\"s1:tm"+(i-1)+"\" importance=\"1.0\"/>\n";
                                                        construction=construction+"<emotion id=\"e1\" type=\"joyStrong\" start=\"s1:tm1\" end=\"s1:tm:"+(i-1)+"\" importance=\"1.0\"/>";
                                                    
                                                    }construction=construction+ "</fml>\n</fml-apml>";
                                                    DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
                                                    DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
                                                    Document document = docBuilder.parse(new InputSource(new StringReader(construction)));
                                                    TransformerFactory transformerFactory = TransformerFactory.newInstance();
                                                    Transformer transformer = transformerFactory.newTransformer();
                                                    DOMSource source = new DOMSource(document);
                                                    FileWriter writer = new FileWriter(new File(System.getProperty("user.dir")+"\\filename.xml"));
                                                    StreamResult result = new StreamResult(writer);
                                                    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                                                    transformer.transform(source, result);
                                                    send(System.getProperty("user.dir")+"\\filename.xml");
                                                } catch (ParserConfigurationException ex) {
                                                    Logger.getLogger(Watson.class.getName()).log(Level.SEVERE, null, ex);
                                                  } catch (SAXException ex) {
                                                Logger.getLogger(Watson.class.getName()).log(Level.SEVERE, null, ex);
                                            } catch (IOException ex) {
                                                Logger.getLogger(Watson.class.getName()).log(Level.SEVERE, null, ex);
                                            } catch (TransformerException ex) {
                                                Logger.getLogger(Watson.class.getName()).log(Level.SEVERE, null, ex);
                                            }
                                                
                                                    
                                        }
				};
				OSCPacketListener listener = new OSCPacketListener() {
					
					@Override
					public void handlePacket(OSCPacketEvent arg0) {
						// TODO Auto-generated method stub
						//System.out.println("[INFO_4]:recieved");
						//System.out.println(arg0.getSource().toString());
						//System.out.println(arg0.getPacket().toString());
					}
					
					@Override
					public void handleBadData(OSCBadDataEvent arg0) {
						// TODO Auto-generated method stub
						
					}
				};

				receiver.getDispatcher().addListener(messageSelector, messageListener);
				receiver.addPacketListener(listener);
				receiver.startListening();
				if (receiver.isListening())
					System.out.println("Server is listening");
				receiver.run();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println("error " + e);
			}
		}
	};

	thread.start();

    }//GEN-LAST:event_jCheckBox1ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JFileChooser jFileChooser1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel2;
    private java.awt.TextArea textArea1;
    // End of variables declaration//GEN-END:variables


    private Method loadMethod;
    private Object loader;

    protected void send(String fileName) {
        if(fileName==null || fileName.isEmpty()) return ;
        if(loadMethod!=null){
            try {
                loadMethod.invoke(loader, fileName);
            }
            catch (InvocationTargetException ex) {
                ex.getCause().printStackTrace();
            }
            catch (Exception ex) {
                System.err.println("Can not invoke method load(String) on "+loader.getClass().getCanonicalName());
            }
        }
        else{
            System.out.println("load is null");
        }
    }

    public void setLoader(Object loader){
        System.out.println("greta.core.utilx.gui.OpenAndLoad.setLoader()");
        this.loader = loader;
        try {
            loadMethod = loader.getClass().getMethod("load", String.class);
        } catch (Exception ex) {
            System.err.println("Can not find method load(String) in "+loader.getClass().getCanonicalName());
        }
        try {
            Method getFileFilterMethod = loader.getClass().getMethod("getFileFilter");
            final java.io.FileFilter ff = (java.io.FileFilter) getFileFilterMethod.invoke(loader);
            jFileChooser1.removeChoosableFileFilter(jFileChooser1.getAcceptAllFileFilter());
            jFileChooser1.setAcceptAllFileFilterUsed(true);

        } catch (Exception ex) {}
    }
    
}