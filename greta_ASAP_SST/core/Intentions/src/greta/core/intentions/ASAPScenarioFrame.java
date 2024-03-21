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
package greta.core.intentions;

import greta.core.intentions.FMLFileReader_ASAPScenarioPlayer;
import greta.core.signals.SignalPerformer;
import greta.core.util.CharacterManager;
import java.io.File;
//import java.io.FileWriter;
import java.io.IOException;
//import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
//import javax.xml.parsers.DocumentBuilder;
//import javax.xml.parsers.DocumentBuilderFactory;
//import javax.xml.parsers.ParserConfigurationException;
//import javax.xml.transform.OutputKeys;
//import javax.xml.transform.Transformer;
//import javax.xml.transform.TransformerException;
//import javax.xml.transform.TransformerFactory;
//import javax.xml.transform.dom.DOMSource;
//import javax.xml.transform.stream.StreamResult;
//import org.w3c.dom.Document;
//import org.xml.sax.InputSource;
//import org.xml.sax.SAXException;
import java.util.concurrent.TimeUnit; //for time delay
import java.util.stream.Stream;
//import greta.core.util.time.DefaultTimeController;
//import greta.core.utilx.gui.ASAPScenarioPlayerServer;

/**
 * Inspired by OpenAndLoad in package greta.core.utilx.gui;
 * @author JYW
 */


public class ASAPScenarioFrame extends javax.swing.JFrame {
    private ASAPServer server;
    private boolean vad_state;
    public boolean send_fml_state;
    public boolean send_fml_state_prev;
    public boolean send_fml_state_flag;
    private long time_start_user_speaking;
    private long time_duration_user_speaking;
    private boolean reset_scenario;
    public CharacterManager cm;
//    private DefaultTimeController time = new DefaultTimeController();
//    private ArrayList<SignalPerformer> signal_performers = new ArrayList<SignalPerformer>();
    
    
    /** Creates new form ScenarioLauncherFrame */
    public ASAPScenarioFrame() {
        initComponents();
        server = new ASAPServer();
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
        jPanel1 = new javax.swing.JPanel();
        sendButton = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        port = new java.awt.TextField();
        jLabel2 = new javax.swing.JLabel();
        address = new java.awt.TextField();
        connexion = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Scenario Player", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 24))); // NOI18N
        jPanel1.setName("frame1"); // NOI18N

        sendButton.setText("Send");
        sendButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sendButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(sendButton)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(sendButton, javax.swing.GroupLayout.DEFAULT_SIZE, 28, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Socket parameters", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 18))); // NOI18N

        jLabel1.setText("Port");

        port.setText("4400");
        port.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                portActionPerformed(evt);
            }
        });

        jLabel2.setText("Address");

        address.setText("192.168.100.37");
        address.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addressActionPerformed(evt);
            }
        });

        connexion.setText("Enable");
        connexion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                connexionActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addComponent(jLabel1))
                        .addGap(43, 43, 43)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addComponent(address, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(82, Short.MAX_VALUE))
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addComponent(port, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(connexion)
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(connexion)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(31, 31, 31)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addComponent(address, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(port, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void portActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_portActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_portActionPerformed

    private void addressActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addressActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_addressActionPerformed

    private void connexionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_connexionActionPerformed
        // TODO add your handling code here:

        if (connexion.isSelected()) {
            try {
                server.setAddress(address.getText());
                server.setPort(port.getText());
                System.out.println("greta.core.utilx.gui.ASAPScenarioPlayerFrame:" + server.port + "   " + server.address);
                server.startConnection();
                
                Thread r1 = new Thread() {
                    @Override
                    public void run() {
                        try {
//                            String line;
                            while(true) {
                                String line2=server.receiveMessage();
                                if(line2!=null && line2.length()>0) {
//                                    System.out.println("CLIENT:"+line2);
                                    loadVAD(line2);
                                }
                            }
                               
                        } catch (IOException ex) {
                            Logger.getLogger(ASAPScenarioFrame.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                };
                Thread r2 = new Thread() {
                    @Override
                    public void run() {
                        String tmp = null;
                        server.getOut().print(tmp);
                    }
                };
                
                r1.start();
                r2.start();
            } catch (IOException ex) {
                Logger.getLogger(ASAPScenarioFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        } else {
            System.out.println("greta.core.utilx.gui.ASAPScenarioPlayerFrame.connexionActionPerformed() UNCHECKED");
            try {
                // r2.join();
                server.setStop(true);
                server.stopConnection();
            } catch (IOException ex) {
                Logger.getLogger(ASAPScenarioFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_connexionActionPerformed

    private void sendButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sendButtonActionPerformed

        Thread r0 = new Thread() {
            @Override
            public void run() {
                reset_scenario = true;
                while(true){
                    System.out.println("send_fml_state: "+send_fml_state);
                    if (send_fml_state && send_fml_state_flag && (TimeUnit.NANOSECONDS.toMillis(time_duration_user_speaking)>=1500)) {
                        // Wait for 4sec and if the user is still not speaking send fml and check that the user VAD time is longer than 1.5sec (user talked for at least 1sec)
                        try {
                            TimeUnit.MILLISECONDS.sleep(5000);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(ASAPScenarioFrame.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        if (send_fml_state) {
                            send(".\\Examples\\ASAPScenario\\ASAPScenario0.xml", reset_scenario);
                            send_fml_state_flag = false;
                            reset_scenario = false;
                        }
                    }
                }
            }
        };
        r0.start();
    }//GEN-LAST:event_sendButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private java.awt.TextField address;
    private javax.swing.JCheckBox connexion;
    private javax.swing.JFileChooser jFileChooser1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel4;
    private java.awt.TextField port;
    private javax.swing.JButton sendButton;
    // End of variables declaration//GEN-END:variables

    
//    public void isIsAnimationRunning(boolean isRunning) {
//        isAnimationRunning = isRunning;
//    }
    
    public void loadVAD(String line_data) {
        vad_state = Boolean.parseBoolean(line_data);
        send_fml_state_prev = send_fml_state;
        if (vad_state) {
//            System.out.println("VAD detected");
            send_fml_state = false;
            send_fml_state_flag = false;
            if(send_fml_state_prev) {
                //User started talking
                time_start_user_speaking = System.nanoTime();
            }
        } else {
//            System.out.println("VAD not detected");
            send_fml_state = true;
            if(!send_fml_state_prev) {
                //User stopped talking
                send_fml_state_flag = true;
                time_duration_user_speaking = time_start_user_speaking = System.nanoTime();
            } else {
                send_fml_state_flag = false;
            }
        }

//        System.out.println("fml state: "+send_fml_state);
    }
    

    private Method loadMethod;
    private Object loader;

    protected void send(String fileName, boolean reset_scenario) {
        if(fileName==null || fileName.isEmpty()) return ;
        if(loadMethod!=null){
            try {
                loadMethod.invoke(loader, fileName, reset_scenario);
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
            loadMethod = loader.getClass().getMethod("load", String.class, boolean.class);
        } catch (Exception ex) {
            System.err.println("Can not find method load(String) in "+loader.getClass().getCanonicalName());
        }
        try {
            Method getFileFilterMethod = loader.getClass().getMethod("getFileFilter");
            final java.io.FileFilter ff = (java.io.FileFilter) getFileFilterMethod.invoke(loader);
            jFileChooser1.removeChoosableFileFilter(jFileChooser1.getAcceptAllFileFilter());
            jFileChooser1.setAcceptAllFileFilterUsed(true);
            //jFileChooser1.addChoosableFileFilter(new javax.swing.filechooser.FileFilter(){

              //  @Override
                //public boolean accept(File f) {
                  //  return f.isDirectory() || ff.accept(f);
                //}

                //@Override
                //public String getDescription() {
                  //  return OpenAndLoad.this.loader.getClass().getSimpleName()+" Files";
                //}
            //});

        } catch (Exception ex) {}
    }
}