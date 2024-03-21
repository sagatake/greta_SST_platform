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
package greta.core.animation.performer;

/**
 *
 * @author Adrien Maudet
 */
public class BodyNoiseGui extends javax.swing.JFrame {

    BodyAnimationNoiseGenerator bodyheadnoise;
    /**
     * Creates new form BodyNoiseGui
     */
    public BodyNoiseGui() {
        initComponents();

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        headlabel = new javax.swing.JLabel();
        torsolabel = new javax.swing.JLabel();
        lowerBodyLabel = new javax.swing.JLabel();
        enableLabel = new javax.swing.JLabel();
        headCheckBox1 = new javax.swing.JCheckBox();
        torsoCheckBox2 = new javax.swing.JCheckBox();
        lowerbodyCheckBox3 = new javax.swing.JCheckBox();
        Torsovalue = new javax.swing.JTextField();
        Headvalue = new javax.swing.JTextField();
        lowerbodyvalue = new javax.swing.JTextField();
        intensitylable = new javax.swing.JLabel();

        headlabel.setText("Head");

        torsolabel.setText("Torso");

        lowerBodyLabel.setText("Lower Body");

        enableLabel.setText("Enable");

        headCheckBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                headCheckBox1ActionPerformed(evt);
            }
        });

        torsoCheckBox2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                torsoCheckBox2ActionPerformed(evt);
            }
        });

        lowerbodyCheckBox3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                lowerbodyCheckBox3ActionPerformed(evt);
            }
        });

        Torsovalue.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TorsovalueActionPerformed(evt);
            }
        });

        Headvalue.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                HeadvalueActionPerformed(evt);
            }
        });

        lowerbodyvalue.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                lowerbodyvalueActionPerformed(evt);
            }
        });

        intensitylable.setText("Intensity");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(headlabel)
                    .addComponent(torsolabel)
                    .addComponent(lowerBodyLabel))
                .addGap(24, 24, 24)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(headCheckBox1)
                    .addComponent(torsoCheckBox2)
                    .addComponent(lowerbodyCheckBox3)
                    .addComponent(enableLabel))
                .addGap(28, 28, 28)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(Torsovalue, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 43, Short.MAX_VALUE)
                        .addComponent(Headvalue, javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(lowerbodyvalue))
                    .addComponent(intensitylable))
                .addContainerGap(207, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(intensitylable, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(enableLabel))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(headCheckBox1)
                            .addComponent(headlabel))
                        .addGap(29, 29, 29)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(torsoCheckBox2)
                            .addComponent(torsolabel))
                        .addGap(30, 30, 30)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lowerBodyLabel)
                            .addComponent(lowerbodyCheckBox3)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(Headvalue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(30, 30, 30)
                        .addComponent(Torsovalue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(31, 31, 31)
                        .addComponent(lowerbodyvalue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(136, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void headCheckBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_headCheckBox1ActionPerformed
        if(bodyheadnoise != null){
            if (this.headCheckBox1.isSelected()){// if checked
                bodyheadnoise.setUseHead(true);
            }else{
                bodyheadnoise.setUseHead(false);
            }
        }
    }//GEN-LAST:event_headCheckBox1ActionPerformed

    private void torsoCheckBox2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_torsoCheckBox2ActionPerformed
       if(bodyheadnoise != null){
            if (this.torsoCheckBox2.isSelected()){// if checked
                bodyheadnoise.setUseTorso(true);
            }else{
                bodyheadnoise.setUseTorso(false);
            }
        }
    }//GEN-LAST:event_torsoCheckBox2ActionPerformed

    private void lowerbodyCheckBox3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lowerbodyCheckBox3ActionPerformed
        if(bodyheadnoise != null){
            if (this.lowerbodyCheckBox3.isSelected()){// if checked
                bodyheadnoise.setUseLowerBody(true);
            }else{
                bodyheadnoise.setUseLowerBody(false);
            }
        }
    }//GEN-LAST:event_lowerbodyCheckBox3ActionPerformed

    private void HeadvalueActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_HeadvalueActionPerformed
        updateHeadvalue();
    }//GEN-LAST:event_HeadvalueActionPerformed

    private void TorsovalueActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TorsovalueActionPerformed
        updateTorsovalue();
    }//GEN-LAST:event_TorsovalueActionPerformed

    private void lowerbodyvalueActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lowerbodyvalueActionPerformed
        updateLowerBodyvalue();
    }//GEN-LAST:event_lowerbodyvalueActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(BodyNoiseGui.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(BodyNoiseGui.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(BodyNoiseGui.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(BodyNoiseGui.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new BodyNoiseGui().setVisible(true);
            }
        });
    }

    public void setGui(BodyAnimationNoiseGenerator node){
        this.bodyheadnoise = node;
        readIntensityvalues();
        CheckIfEnable();

    }

    private void readIntensityvalues() {
        if(bodyheadnoise!=null){
            this.Headvalue.setText(Double.toString(bodyheadnoise._intensityHead));
            this.Torsovalue.setText(Double.toString(bodyheadnoise._intensityTorso));
            this.lowerbodyvalue.setText(Double.toString(bodyheadnoise.step));
        }
    }

    private void CheckIfEnable() {
        if(bodyheadnoise!=null){
            this.headCheckBox1.setSelected(bodyheadnoise._useHead);
            this.torsoCheckBox2.setSelected(bodyheadnoise._useTorso);
            this.lowerbodyCheckBox3.setSelected(bodyheadnoise._useLowerBody);
        }
    }

    public void updateHeadvalue(){
        if(bodyheadnoise!=null){
            bodyheadnoise.setIntensityHead(valueOf(Headvalue, Double.parseDouble(Headvalue.getText())));
        }
    }

    public void updateTorsovalue(){
        if(bodyheadnoise!=null){
            bodyheadnoise.setIntensityTorso(valueOf(Torsovalue, Double.parseDouble(Torsovalue.getText())));
        }
    }

    public void updateLowerBodyvalue(){
        if(bodyheadnoise!=null){
            bodyheadnoise.setStep(valueOf(lowerbodyvalue, Double.parseDouble(lowerbodyvalue.getText())));
        }
    }

    private double valueOf(javax.swing.JTextField field, double defaultValue){
        try{
            return Double.parseDouble(field.getText());
        }
        catch(Throwable t){}
        field.setText(""+defaultValue);
        return defaultValue;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField Headvalue;
    private javax.swing.JTextField Torsovalue;
    private javax.swing.JLabel enableLabel;
    private javax.swing.JCheckBox headCheckBox1;
    private javax.swing.JLabel headlabel;
    private javax.swing.JLabel intensitylable;
    private javax.swing.JLabel lowerBodyLabel;
    private javax.swing.JCheckBox lowerbodyCheckBox3;
    private javax.swing.JTextField lowerbodyvalue;
    private javax.swing.JCheckBox torsoCheckBox2;
    private javax.swing.JLabel torsolabel;
    // End of variables declaration//GEN-END:variables


}
