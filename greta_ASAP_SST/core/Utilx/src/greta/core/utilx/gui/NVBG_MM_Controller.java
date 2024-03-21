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

import greta.core.util.CharacterManager;
import greta.core.util.speech.Speech;
import greta.core.util.speech.TTS;

import greta.core.signals.gesture.GestureSignal;
import greta.core.signals.SpeechSignal;
import greta.core.signals.FaceSignal;
import greta.core.signals.GazeSignal;
import greta.core.signals.HeadSignal;
import greta.core.signals.ShoulderSignal;
import greta.core.signals.SpineSignal;
import greta.core.signals.TorsoSignal;

/**
 *
 * @author Andre-Marie Pez
 */
public class NVBG_MM_Controller extends javax.swing.JFrame {

    private CharacterManager cm;

    /** Creates new form TTSController */
    public NVBG_MM_Controller(CharacterManager cm) {
        this.cm = cm;
        initComponents();
        
        // use CharacterManager default
        doMM.setSelected(this.cm.get_use_MM());
        doNVBG.setSelected(this.cm.get_use_NVBG());
        
        MM_GestureSignal.setSelected(this.cm.get_MM_GestureSignal());
        MM_FaceSignal.setSelected(this.cm.get_MM_FaceSignal());
        MM_GazeSignal.setSelected(this.cm.get_MM_GazeSignal());
        MM_HeadSignal.setSelected(this.cm.get_MM_HeadSignal());
        MM_ShoulderSignal.setSelected(this.cm.get_MM_ShoulderSignal());
        MM_TorsoSignal.setSelected(this.cm.get_MM_TorsoSignal());

        MM_Multilingual_experimental.setSelected(this.cm.get_MM_Multilingual());
        
    }

    private void updateNVBG_MM_Options(){
            
            if(doNVBG.isSelected()){
                System.out.println("greta.core.utilx.gui.NVBG_MM_Controller: NVBG turned on");
                this.cm.set_use_NVBG(true); 
            }
            else{
                System.out.println("greta.core.utilx.gui.NVBG_MM_Controller: NVBG turned off");
                this.cm.set_use_NVBG(false); 
            }
            
            if(doMM.isSelected()){
                System.out.println("greta.core.utilx.gui.NVBG_MM_Controller: meaning minor turned on");
                this.cm.set_use_MM(true);            
            }
            else{
                System.out.println("greta.core.utilx.gui.NVBG_MM_Controller: meaning minor turned off");
                this.cm.set_use_MM(false); 
            }

            if(MM_GestureSignal.isSelected()){
                System.out.println("greta.core.utilx.gui.NVBG_MM_Controller: meaning minor - GestureSignal turned on");
                this.cm.set_MM_GestureSignal(true);            
            }
            else{
                System.out.println("greta.core.utilx.gui.NVBG_MM_Controller: meaning minor - GestureSignal turned off");
                this.cm.set_MM_GestureSignal(false); 
            }
            
            if(MM_FaceSignal.isSelected()){
                System.out.println("greta.core.utilx.gui.NVBG_MM_Controller: meaning minor - FaceSignal turned on");
                this.cm.set_MM_FaceSignal(true);            
            }
            else{
                System.out.println("greta.core.utilx.gui.NVBG_MM_Controller: meaning minor - FaceSignal turned off");
                this.cm.set_MM_FaceSignal(false); 
            }

            if(MM_GazeSignal.isSelected()){
                System.out.println("greta.core.utilx.gui.NVBG_MM_Controller: meaning minor - GazeSignal turned on");
                this.cm.set_MM_GazeSignal(true);            
            }
            else{
                System.out.println("greta.core.utilx.gui.NVBG_MM_Controller: meaning minor - GazeSignal turned off");
                this.cm.set_MM_GazeSignal(false); 
            }

            if(MM_HeadSignal.isSelected()){
                System.out.println("greta.core.utilx.gui.NVBG_MM_Controller: meaning minor - HeadSignal turned on");
                this.cm.set_MM_HeadSignal(true);            
            }
            else{
                System.out.println("greta.core.utilx.gui.NVBG_MM_Controller: meaning minor - HeadSignal turned off");
                this.cm.set_MM_HeadSignal(false); 
            }

            if(MM_ShoulderSignal.isSelected()){
                System.out.println("greta.core.utilx.gui.NVBG_MM_Controller: meaning minor - ShoulderSignal turned on");
                this.cm.set_MM_ShoulderSignal(true);            
            }
            else{
                System.out.println("greta.core.utilx.gui.NVBG_MM_Controller: meaning minor - ShoulderSignal turned off");
                this.cm.set_MM_ShoulderSignal(false); 
            }

            if(MM_TorsoSignal.isSelected()){
                System.out.println("greta.core.utilx.gui.NVBG_MM_Controller: meaning minor - TorsoSignal turned on");
                this.cm.set_MM_TorsoSignal(true);            
            }
            else{
                System.out.println("greta.core.utilx.gui.NVBG_MM_Controller: meaning minor - TorsoSignal turned off");
                this.cm.set_MM_TorsoSignal(false); 
            }

            if(MM_Multilingual_experimental.isSelected()){
                System.out.println("greta.core.utilx.gui.NVBG_MM_Controller: meaning minor - Multilingual turned on");
                this.cm.set_MM_Multilingual(true);            
            }
            else{
                System.out.println("greta.core.utilx.gui.NVBG_MM_Controller: meaning minor - Multilingual turned off");
                this.cm.set_MM_Multilingual(false); 
            }            
            
            
            
            System.out.println("greta.core.utilx.gui.NVBG_MM_Controller: language - " + MM_Language_TextField.getText().trim());
            this.cm.set_MM_Language(MM_Language_TextField.getText().trim());

            
    }


    public boolean getNVBG(){
        return doNVBG.isSelected();
    }


    public boolean getMM(){
        return doMM.isSelected();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        doNVBG = new greta.core.utilx.gui.ToolBox.LocalizedJCheckBox("NVBG");
        doMM = new greta.core.utilx.gui.ToolBox.LocalizedJCheckBox("Meaning Miner");
        MM_HeadSignal = new greta.core.utilx.gui.ToolBox.LocalizedJCheckBox("HeadSignal");
        MM_FaceSignal = new greta.core.utilx.gui.ToolBox.LocalizedJCheckBox("FaceSignal");
        MM_GazeSignal = new greta.core.utilx.gui.ToolBox.LocalizedJCheckBox("GazeSignal");
        MM_ShoulderSignal = new greta.core.utilx.gui.ToolBox.LocalizedJCheckBox("ShoulderSignal");
        MM_TorsoSignal = new greta.core.utilx.gui.ToolBox.LocalizedJCheckBox("TorsoSignal");
        jSeparator1 = new javax.swing.JSeparator();
        MM_GestureSignal = new greta.core.utilx.gui.ToolBox.LocalizedJCheckBox("GestureSignal");
        jSeparator2 = new javax.swing.JSeparator();
        MM_Multilingual_experimental = new greta.core.utilx.gui.ToolBox.LocalizedJCheckBox("MM_Multilingual_experimental");
        MM_Language_TextField = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        MM_LanguageUpdate = new javax.swing.JButton();

        doNVBG.setSelected(true);
        doNVBG.setContentAreaFilled(false);
        doNVBG.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        doNVBG.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                doNVBGActionPerformed(evt);
            }
        });

        doMM.setSelected(true);
        doMM.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                doMMActionPerformed(evt);
            }
        });

        MM_HeadSignal.setSelected(true);
        MM_HeadSignal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MM_HeadSignalActionPerformed(evt);
            }
        });

        MM_FaceSignal.setSelected(true);
        MM_FaceSignal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MM_FaceSignalActionPerformed(evt);
            }
        });

        MM_GazeSignal.setSelected(true);
        MM_GazeSignal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MM_GazeSignalActionPerformed(evt);
            }
        });

        MM_ShoulderSignal.setSelected(true);
        MM_ShoulderSignal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MM_ShoulderSignalActionPerformed(evt);
            }
        });

        MM_TorsoSignal.setSelected(true);
        MM_TorsoSignal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MM_TorsoSignalActionPerformed(evt);
            }
        });

        MM_GestureSignal.setSelected(true);
        MM_GestureSignal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MM_GestureSignalActionPerformed(evt);
            }
        });

        MM_Multilingual_experimental.setSelected(true);
        MM_Multilingual_experimental.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MM_Multilingual_experimentalActionPerformed(evt);
            }
        });

        MM_Language_TextField.setText("FR");
        MM_Language_TextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MM_Language_TextFieldActionPerformed(evt);
            }
        });

        jLabel1.setText("Language");

        MM_LanguageUpdate.setText("Update");
        MM_LanguageUpdate.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                MM_LanguageUpdateMouseClicked(evt);
            }
        });
        MM_LanguageUpdate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MM_LanguageUpdateActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparator1)
                    .addComponent(jSeparator2)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(MM_Multilingual_experimental)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(MM_FaceSignal)
                                    .addComponent(MM_GazeSignal)
                                    .addComponent(MM_HeadSignal)
                                    .addComponent(doMM))
                                .addGap(109, 109, 109)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(doNVBG)
                                    .addComponent(MM_ShoulderSignal)
                                    .addComponent(MM_GestureSignal)
                                    .addComponent(MM_TorsoSignal))))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(MM_Language_TextField, javax.swing.GroupLayout.DEFAULT_SIZE, 106, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(MM_LanguageUpdate)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(doMM)
                            .addComponent(doNVBG))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(MM_FaceSignal)
                            .addComponent(MM_GestureSignal))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(MM_GazeSignal))
                    .addComponent(MM_ShoulderSignal))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(MM_HeadSignal)
                    .addComponent(MM_TorsoSignal))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(MM_Multilingual_experimental)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(MM_Language_TextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(MM_LanguageUpdate))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void doNVBGActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_doNVBGActionPerformed
        updateNVBG_MM_Options();
    }//GEN-LAST:event_doNVBGActionPerformed

    private void doMMActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_doMMActionPerformed
        updateNVBG_MM_Options();
    }//GEN-LAST:event_doMMActionPerformed

    private void MM_HeadSignalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MM_HeadSignalActionPerformed
        updateNVBG_MM_Options();
    }//GEN-LAST:event_MM_HeadSignalActionPerformed

    private void MM_FaceSignalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MM_FaceSignalActionPerformed
        updateNVBG_MM_Options();
    }//GEN-LAST:event_MM_FaceSignalActionPerformed

    private void MM_GazeSignalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MM_GazeSignalActionPerformed
        updateNVBG_MM_Options();
    }//GEN-LAST:event_MM_GazeSignalActionPerformed

    private void MM_ShoulderSignalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MM_ShoulderSignalActionPerformed
        updateNVBG_MM_Options();
    }//GEN-LAST:event_MM_ShoulderSignalActionPerformed

    private void MM_TorsoSignalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MM_TorsoSignalActionPerformed
        updateNVBG_MM_Options();
    }//GEN-LAST:event_MM_TorsoSignalActionPerformed

    private void MM_GestureSignalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MM_GestureSignalActionPerformed
        updateNVBG_MM_Options();
    }//GEN-LAST:event_MM_GestureSignalActionPerformed

    private void MM_Multilingual_experimentalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MM_Multilingual_experimentalActionPerformed
        updateNVBG_MM_Options();
    }//GEN-LAST:event_MM_Multilingual_experimentalActionPerformed

    private void MM_Language_TextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MM_Language_TextFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_MM_Language_TextFieldActionPerformed

    private void MM_LanguageUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MM_LanguageUpdateActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_MM_LanguageUpdateActionPerformed

    private void MM_LanguageUpdateMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_MM_LanguageUpdateMouseClicked
        // TODO add your handling code here:
        updateNVBG_MM_Options();
    }//GEN-LAST:event_MM_LanguageUpdateMouseClicked

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox MM_FaceSignal;
    private javax.swing.JCheckBox MM_GazeSignal;
    private javax.swing.JCheckBox MM_GestureSignal;
    private javax.swing.JCheckBox MM_HeadSignal;
    private javax.swing.JButton MM_LanguageUpdate;
    private javax.swing.JTextField MM_Language_TextField;
    private javax.swing.JCheckBox MM_Multilingual_experimental;
    private javax.swing.JCheckBox MM_ShoulderSignal;
    private javax.swing.JCheckBox MM_TorsoSignal;
    private javax.swing.JCheckBox doMM;
    private javax.swing.JCheckBox doNVBG;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    // End of variables declaration//GEN-END:variables

}
