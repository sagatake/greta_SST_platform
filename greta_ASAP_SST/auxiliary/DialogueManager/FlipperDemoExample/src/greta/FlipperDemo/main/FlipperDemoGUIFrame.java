 /*
 * This file is part of the auxiliaries of Greta.
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
package greta.FlipperDemo.main;

import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class FlipperDemoGUIFrame extends JFrame  {

	/**
	 * 
	 */

	
	private JLabel connectedLabel;
	private JTextField hostTextField;
	private JTextField portTextField;
	private JTextField requestTopicTextField;
	private JTextField responseTopicTextField;
        private JTextField flipperPropertyResTextField;
        private JTextField flipperTemplateFolderTextField;
	private JTextField sstTaskTextField;
	private JTextField sstTaskIndexTextField;
//	private JTextField sstLanguageTextField;
	private JButton resetConnectionButton;
	private FlipperLauncherMain flipperDemoMain= null;
        
        //not implemented
	private JButton resetButton;
        
        
      //private ActiveMQConnector groupBehaviorMain = null;
      //	 private SpeechRecognizer sr = null;
	public FlipperDemoGUIFrame() {
		this.initComponents();
	}
	
	private void initComponents() {
            Container contentPane = this.getContentPane();

            JPanel overallPanel = new JPanel();
            JPanel buttonPanel = new JPanel();
            
            contentPane.add(overallPanel);

            overallPanel.setLayout(new BoxLayout(overallPanel, BoxLayout.Y_AXIS));
            buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));

            /*JPanel connectedPanel = new JPanel();
            connectedPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
            connectedPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
            this.connectedLabel = new JLabel("");
            connectedPanel.add(this.connectedLabel);
            this.setConnectionLabel(false);
            overallPanel.add(connectedPanel);
            */
            JPanel hostPanel = new JPanel();
            hostPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 0));
            hostPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
            hostPanel.add(new JLabel("Host"));
            this.hostTextField = new JTextField("", 50);
            hostPanel.add(this.hostTextField);
            overallPanel.add(hostPanel);

            JPanel portPanel = new JPanel();
            portPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 0));
            portPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
            portPanel.add(new JLabel("Port"));
            this.portTextField = new JTextField("", 50);
            portPanel.add(this.portTextField);
            overallPanel.add(portPanel);

            JPanel requesttopicPanel = new JPanel();
            requesttopicPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 0));
            requesttopicPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
            requesttopicPanel.add(new JLabel("Request Topic"));
            this.requestTopicTextField = new JTextField("", 50);
            requesttopicPanel.add(this.requestTopicTextField);
            overallPanel.add(requesttopicPanel);

            JPanel responseTopicPanel = new JPanel();
            responseTopicPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 0));
            responseTopicPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
            responseTopicPanel.add(new JLabel("Response Topic"));
            this.responseTopicTextField = new JTextField("", 50);
            responseTopicPanel.add(this.responseTopicTextField);
            overallPanel.add(responseTopicPanel);

            JPanel flipperPropertyPanel = new JPanel();
            flipperPropertyPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 0));
            flipperPropertyPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
            flipperPropertyPanel.add(new JLabel("Flipper Property File"));
            this.flipperPropertyResTextField = new JTextField("", 90);
            flipperPropertyPanel.add(this.flipperPropertyResTextField);
            overallPanel.add(flipperPropertyPanel);

            JPanel templateFolderPanel = new JPanel();
            templateFolderPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 0));
            templateFolderPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
            templateFolderPanel.add(new JLabel("Flipper Template Folder"));
            this.flipperTemplateFolderTextField = new JTextField("", 90);
            templateFolderPanel.add(this.flipperTemplateFolderTextField);
            overallPanel.add(templateFolderPanel);

            JPanel sstTaskPanel = new JPanel();
            sstTaskPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 0));
            sstTaskPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
            sstTaskPanel.add(new JLabel("SST task [TELL,LISTEN,ASK,DECLINE]"));
            this.sstTaskTextField = new JTextField("", 10);
            sstTaskPanel.add(this.sstTaskTextField);
            overallPanel.add(sstTaskPanel);

            JPanel sstTaskIndexPanel = new JPanel();
            sstTaskIndexPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 0));
            sstTaskIndexPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
            sstTaskIndexPanel.add(new JLabel("SST task index [ex) 01]"));
            this.sstTaskIndexTextField = new JTextField("", 10);
            sstTaskIndexPanel.add(this.sstTaskIndexTextField);
            overallPanel.add(sstTaskIndexPanel);            

//            JPanel sstLanguagePanel = new JPanel();
//            sstLanguagePanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 0));
//            sstLanguagePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
//            sstLanguagePanel.add(new JLabel("SST Language [ex) EN (FR, JP)]"));
//            this.sstLanguageTextField = new JTextField("", 10);
//            sstLanguagePanel.add(this.sstLanguageTextField);
//            overallPanel.add(sstLanguagePanel);            

            
            JPanel responseConnectionButtonPanel = new JPanel();
            responseConnectionButtonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 0));
            responseConnectionButtonPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
            this.resetConnectionButton = new JButton("Reset Connection");
            this.resetConnectionButton.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 0));
            responseConnectionButtonPanel.add(this.resetConnectionButton);
            buttonPanel.add(responseConnectionButtonPanel);
            this.resetConnectionButton.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent arg0) {
                            FlipperDemoGUIFrame.this.setActiveMqParameters();
                    }

            });

            JPanel responseResetButtonPanel = new JPanel();
            responseResetButtonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 0));
            responseResetButtonPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
            this.resetButton = new JButton("Reset all");
            this.resetButton.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 0));
            responseResetButtonPanel.add(this.resetButton);
            buttonPanel.add(responseResetButtonPanel);
            this.resetButton.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent arg0) {
                            reset();
                    }

            });
            
            overallPanel.add(buttonPanel);

	}
	
	private void setConnectionLabel(boolean status) {
		if (status) {
			this.connectedLabel.setText("Connected");
			this.connectedLabel.setForeground(Color.GREEN);
		} else {
			this.connectedLabel.setText("Not connected");
			this.connectedLabel.setForeground(Color.RED);
		}
	}
	
	private void setActiveMqParameters() {
		String host = this.hostTextField.getText().trim();
		String port = this.portTextField.getText().trim();
		String requestTopic = this.requestTopicTextField.getText().trim();
		String responseTopic = this.responseTopicTextField.getText().trim();
                
                flipperDemoMain.setActiveMqParameters(host, port, requestTopic, responseTopic);
		//this.setConnectionLabel(false);
                
                //setFlipperDemoMain(flipperDemoMain);
		
	}


	public void setFlipperDemoMain(FlipperLauncherMain flipperDemoMain) {
                //this.flipperDemoMain = flipperDemoMain;
		this.flipperDemoMain = FlipperLauncherMain.getInstance();
		
		if (this.flipperDemoMain.getHost() != null) {
			this.hostTextField.setText(this.flipperDemoMain.getHost());
		}
		if (this.flipperDemoMain.getPort() != null) {
			this.portTextField.setText(this.flipperDemoMain.getPort());
		}
		if (this.flipperDemoMain.getGretaASRTopic()!= null) {
			this.requestTopicTextField.setText(this.flipperDemoMain.getGretaASRTopic());
		}
		if (this.flipperDemoMain.getGretaInputTopic()!= null) {
			this.responseTopicTextField.setText(this.flipperDemoMain.getGretaInputTopic());
		}
                if (this.flipperDemoMain.getFlipperPropertyResource()!= null) {
			this.flipperPropertyResTextField.setText(this.flipperDemoMain.getFlipperPropertyResource());
		}
                if (this.flipperDemoMain.getFlipperTemplateFolderPath()!= null) {
			this.flipperTemplateFolderTextField.setText(this.flipperDemoMain.getFlipperTemplateFolderPath());
		}
                if (this.flipperDemoMain.getSSTTask()!= null) {
			this.sstTaskTextField.setText(this.flipperDemoMain.getSSTTask());
		}
                if (this.flipperDemoMain.getSSTTaskIndex()!= null) {
			this.sstTaskIndexTextField.setText(this.flipperDemoMain.getSSTTaskIndex());
		}
//                if (this.flipperDemoMain.getSSTLanguage()!= null) {
//			this.sstLanguageTextField.setText(this.flipperDemoMain.getSSTLanguage());
//		}

		FlipperDemoGUIFrame.this.setActiveMqParameters();
//                flipperDemoMain.setFlipperPropertyResource(this.flipperPropertyResTextField.getText().trim());
//                flipperDemoMain.setFlipperTemplateFolderPath(this.flipperTemplateFolderTextField.getText().trim());
//                flipperDemoMain.setSSTTask(this.sstTaskTextField.getText().trim());
//                flipperDemoMain.setSSTTaskIndex(this.sstTaskIndexTextField.getText().trim());
                flipperDemoMain.init();
	}

	
	public void onConnectionStarted() {
		this.setConnectionLabel(true);
	}
        
        private void reset(){

//            flipperDemoMain.reset();

            flipperDemoMain.stop();
            flipperDemoMain.setFlipperPropertyResource(this.flipperPropertyResTextField.getText().trim());
            flipperDemoMain.setFlipperTemplateFolderPath(this.flipperTemplateFolderTextField.getText().trim());
            flipperDemoMain.setSSTTask(this.sstTaskTextField.getText().trim());
            flipperDemoMain.setSSTTaskIndex(this.sstTaskIndexTextField.getText().trim());
//            flipperDemoMain.setSSTLanguage(this.sstLanguageTextField.getText().trim());
            flipperDemoMain.init();

        }
}
