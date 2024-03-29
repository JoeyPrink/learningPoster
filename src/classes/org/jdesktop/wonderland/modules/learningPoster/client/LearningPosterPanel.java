/**
 * Open Wonderland
 *
 * Copyright (c) 2011, Open Wonderland Foundation, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * The Open Wonderland Foundation designates this particular file as
 * subject to the "Classpath" exception as provided by the Open Wonderland
 * Foundation in the License file that accompanied this code.
 */

package org.jdesktop.wonderland.modules.learningPoster.client;

import java.util.logging.Logger;
import javax.swing.JEditorPane;

/**
 * Panel to render a learningPoster.
 * @author Bernard Horan
 * @author Jon Kaplan
 */
public class LearningPosterPanel extends javax.swing.JPanel {
    private static final Logger LOGGER =
            Logger.getLogger(LearningPosterPanel.class.getName());

    private final LearningPosterCell cell;

    LearningPosterPanel(LearningPosterCell cell) {
        this.cell = cell;
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

        learningPosterPane = new LearningPosterEditorPane(cell);

        setFocusable(false);

        learningPosterPane.setBorder(null);
        learningPosterPane.setEditable(false);
        learningPosterPane.setFocusable(false);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, learningPosterPane)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, learningPosterPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 25, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JEditorPane learningPosterPane;
    // End of variables declaration//GEN-END:variables

    void updateLabel() {
        learningPosterPane.setText(cell.getLearningPosterText());
    }

    JEditorPane getLearningPosterPane() {
        return learningPosterPane;
}
 }
