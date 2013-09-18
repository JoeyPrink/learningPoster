/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * Sun designates this particular file as subject to the "Classpath"
 * exception as provided by Sun in the License file that accompanied
 * this code.
 */
package org.jdesktop.wonderland.modules.learningPoster.client;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.bind.JAXBException;
import javax.xml.bind.PropertyException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import org.jdesktop.wonderland.client.cell.asset.AssetUtils;
import org.jdesktop.wonderland.client.cell.properties.annotation.PropertiesFactory;
import org.jdesktop.wonderland.client.cell.properties.spi.PropertiesFactorySPI;
import org.jdesktop.wonderland.client.cell.properties.CellPropertiesEditor;
import org.jdesktop.wonderland.client.content.ContentBrowserManager;
import org.jdesktop.wonderland.client.content.spi.ContentBrowserSPI;
import org.jdesktop.wonderland.client.content.spi.ContentBrowserSPI.ContentBrowserListener;
import org.jdesktop.wonderland.client.login.LoginManager;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.contentrepo.client.ContentRepositoryRegistry;
import org.jdesktop.wonderland.modules.contentrepo.client.utils.ContentRepositoryUtils;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentCollection;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentNode;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentRepositoryException;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentResource;
import org.jdesktop.wonderland.modules.learningPoster.common.LearningPosterCellServerState;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentNode.Type;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import org.jdesktop.wonderland.modules.placemarks.api.common.Placemark;


/**
 * The property sheet for the poster module to set the used xml file
 * change the shown question
 * and add or delete new questions
 *
 * Code based on sample cell by
 * @author Jordan Slott <jslott@dev.java.net>
 * @author Ronny Standtke <ronny.standtke@fhnw.ch>
 * 
 * adapted by
 * @author Johanna Pirker <jpirker@iicm.edu>
 *
 */
@PropertiesFactory(LearningPosterCellServerState.class)
public class LearningPosterCellProperties
        extends JPanel implements PropertiesFactorySPI {

    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(
            "org/jdesktop/wonderland/modules/learningPoster/client/resources/Bundle");
    private static final Logger LOGGER =
            Logger.getLogger(LearningPosterCellProperties.class.getName());
    CellPropertiesEditor editor = null;
    //   private String originalLearningPosterText = null;
    private File actualFile = null;

    /** Creates new form LearningPosterCellProperties */
    public LearningPosterCellProperties() {
        initComponents();
        
        // ComboBoxModel shapeTypeComboBoxModel = new DefaultComboBoxModel(
        //         new String[]{"Questions"});
        // questionComboBox.setModel(shapeTypeComboBoxModel);

        // Listen for when the Browse... button is selected and display a
        // GUI to browser the content repository. Wait until OK has been
        // selected and fill in the text field with the URI
        browseButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                // Fetch the browser for the webdav protocol and display it.
                // Add a listener for the result and update the value of the
                // text field for the URI
                ContentBrowserManager manager =
                        ContentBrowserManager.getContentBrowserManager();
                final ContentBrowserSPI browser =
                        manager.getDefaultContentBrowser();
                browser.addContentBrowserListener(new ContentBrowserListener() {

                    public void okAction(String uri) {
                        uriTextField.setText(uri);
                        browser.removeContentBrowserListener(this);
                    }

                    public void cancelAction() {
                        browser.removeContentBrowserListener(this);
                    }
                });
                browser.setVisible(true);
            }
        });



        // Listen for when the  New File... button is selected and display a
        // FileChooser to the local file system.
        newFileButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                JFileChooser fileChooser = new JFileChooser();
                FileNameExtensionFilter xmlfilter = new FileNameExtensionFilter("xml files (*.xml)", "xml");
                fileChooser.setFileFilter(xmlfilter);
                int outputPath = fileChooser.showOpenDialog(LearningPosterCellProperties.this);
                if (outputPath == JFileChooser.APPROVE_OPTION) {
                    actualFile = fileChooser.getSelectedFile();
                    try {
                        URL url = uploadFile(actualFile);
                        ((LearningPosterCell) editor.getCell()).setActualFileName(url.toString());
                        uriTextField.setText(url.toString());
                        LOGGER.log(Level.SEVERE, "1: uploading File{0}", actualFile);
                    } catch (ContentRepositoryException ex) {
                        Logger.getLogger(LearningPosterCellProperties.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ex) {
                        Logger.getLogger(LearningPosterCellProperties.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        });



        // Listen for when the Edit button is selected and display a simple
        // text editor with the URI in the text field
        setButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                String uri = uriTextField.getText();
                //    actualFile = new File(uri);
                setQuestionComboBox(actualFile);
            }
        });

        // Listen for when the Edit button is selected and display a simple
        // text editor with the URI in the text field
        addNewQuestionButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                try {
                    Document doc = LearningPosterXMLProcessor.getDocument("learningData.xml");
                    String newQuestion = newQuestionTextField.getText();
                    String questionType = (String) questionTypeComboBox.getSelectedItem();
                    Element rootElement = doc.getDocumentElement();

                    Element question = LearningPosterXMLProcessor.addQuestion(rootElement, newQuestion, questionType);
                    LearningPosterXMLProcessor.addAnswer(question, answerTextField1.getText(), answerCheckBox1.isSelected());
                    LearningPosterXMLProcessor.addAnswer(question, answerTextField2.getText(), answerCheckBox2.isSelected());
                    LearningPosterXMLProcessor.addAnswer(question, answerTextField3.getText(), answerCheckBox3.isSelected());
                    LearningPosterXMLProcessor.addAnswer(question, answerTextField4.getText(), answerCheckBox4.isSelected());
                    actualFile = LearningPosterXMLProcessor.writeFileFromDoc(doc, "learningData.xml");
                    createQuestionMap(actualFile);
                    URL url = uploadFile(actualFile);
                    ((LearningPosterCell) editor.getCell()).setActualFileName(url.toString());
                    LOGGER.severe(url.toString());

                } catch (ContentRepositoryException ex) {
                    Logger.getLogger(LearningPosterCellProperties.class.getName()).log(Level.SEVERE, null, ex);
                } catch (TransformerConfigurationException ex) {
                    Logger.getLogger(LearningPosterCellProperties.class.getName()).log(Level.SEVERE, null, ex);
                } catch (TransformerException ex) {
                    Logger.getLogger(LearningPosterCellProperties.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ParserConfigurationException ex) {
                    Logger.getLogger(LearningPosterCellProperties.class.getName()).log(Level.SEVERE, null, ex);
                } catch (SAXException ex) {
                    Logger.getLogger(LearningPosterCellProperties.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(LearningPosterCellProperties.class.getName()).log(Level.SEVERE, null, ex);
                }


            }

            private void createQuestionMap(File file) {
                try {
                    HashMap questionMap = LearningPosterXMLProcessor.createQuestionArray(LearningPosterXMLProcessor.getRootElementFromFile(file));
                    String[] questionArray = new String[questionMap.size()];
                    Iterator it = questionMap.keySet().iterator();
                    int j = 0;
                    while (it.hasNext()) {
                        Object nObject = it.next();
                        questionArray[j] = nObject.toString() + "::" + questionMap.get(nObject);
                        j++;
                    }
                    ComboBoxModel questionComboBoxModel = new DefaultComboBoxModel(questionArray);
                    questionComboBox.setModel(questionComboBoxModel);
                } catch (ParserConfigurationException ex) {
                    Logger.getLogger(LearningPosterCellProperties.class.getName()).log(Level.SEVERE, null, ex);
                } catch (SAXException ex) {
                    Logger.getLogger(LearningPosterCellProperties.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(LearningPosterCellProperties.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

        // Listen for when the Edit button is selected and display a simple
        // text editor with the URI in the text field
        createDemoFileButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                try {
                    actualFile = LearningPosterXMLProcessor.makeDemo("learningData.xml");
                    URL url = uploadFile(actualFile);
                    uriTextField.setText(url.toString());

                        ((LearningPosterCell) editor.getCell()).setActualFileName(url.toString());
                } catch (ContentRepositoryException ex) {
                    Logger.getLogger(LearningPosterCellProperties.class.getName()).log(Level.SEVERE, null, ex);
                } catch (TransformerConfigurationException ex) {
                    Logger.getLogger(LearningPosterCellProperties.class.getName()).log(Level.SEVERE, null, ex);
                } catch (TransformerException ex) {
                    Logger.getLogger(LearningPosterCellProperties.class.getName()).log(Level.SEVERE, null, ex);
                } catch (PropertyException ex) {
                    Logger.getLogger(LearningPosterCellProperties.class.getName()).log(Level.SEVERE, null, ex);
                } catch (JAXBException ex) {
                    Logger.getLogger(LearningPosterCellProperties.class.getName()).log(Level.SEVERE, null, ex);
                } catch (SAXException ex) {
                    Logger.getLogger(LearningPosterCellProperties.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(LearningPosterCellProperties.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ParserConfigurationException ex) {
                    Logger.getLogger(LearningPosterCellProperties.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

        // Listen for when the Edit button is selected and display a simple
        // text editor with the URI in the text field
        setQuestionButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                String newQuestion = (String) questionComboBox.getSelectedItem();
                System.out.println(newQuestion);
                String htmlString = null;
                try {
                    htmlString = LearningPosterXMLProcessor.createHTMLString(LearningPosterXMLProcessor.getRootElementFromFile(actualFile), newQuestion.split("::")[0]);
                } catch (ParserConfigurationException ex) {
                    Logger.getLogger(LearningPosterCellProperties.class.getName()).log(Level.SEVERE, null, ex);
                } catch (SAXException ex) {
                    Logger.getLogger(LearningPosterCellProperties.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(LearningPosterCellProperties.class.getName()).log(Level.SEVERE, null, ex);
                }
                ((LearningPosterCell) editor.getCell()).setLearningPosterText(htmlString);

                LOGGER.log(Level.SEVERE, "daaaaaaaaaaaaaa1111111{0}", newQuestion);
                //CellServerState state = editor.getCellServerState();
                // ((LearningPosterCellServerState) state).setLearningPosterText(newQuestion);
                // editor.addToUpdateList(state);
            }
        });

    }

    /**
     * @inheritDoc()
     */
    public String getDisplayName() {
        return BUNDLE.getString("learningPoster");
    }

    /**
     * @inheritDoc()
     */
    public JPanel getPropertiesJPanel() {
        return this;
    }

    /**
     * @inheritDoc()
     */
    public void setCellPropertiesEditor(CellPropertiesEditor editor) {
        this.editor = editor;
    }

    /**
     * @inheritDoc()
     */
    public void open() {
        // Fetch the current state from the cell's server state and update
        // the GUI.
        CellServerState state = editor.getCellServerState();
        if (state != null) {
            //   originalLearningPosterText = ((LearningPosterCellServerState) state).getLearningPosterText();
            //   questionComboBox.setSelectedItem(originalLearningPosterText);
        }
    }

    /**
     * @inheritDoc()
     */
    public void close() {
        // Do nothing for now.
    }

    /**
     * @inheritDoc()
     */
    public void apply() {
        // Take the value from the shape type and populate the server state
        // with it.
        String newQuestion = (String) questionComboBox.getSelectedItem();
        System.out.println(newQuestion);
        LOGGER.severe(newQuestion);
        CellServerState state = editor.getCellServerState();
        ((LearningPosterCellServerState) state).setLearningPosterText(newQuestion);
        editor.addToUpdateList(state);
    }

    /**
     * @inheritDoc()
     */
    public void restore() {
        //    questionComboBox.setSelectedItem(originalLearningPosterText);
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
        jDialog1 = new javax.swing.JDialog();
        jLabel1 = new javax.swing.JLabel();
        questionComboBox = new javax.swing.JComboBox();
        browseContentLabel = new javax.swing.JLabel();
        uriTextField = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();
        newFileButton = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        jSeparator2 = new javax.swing.JSeparator();
        setQuestionButton = new javax.swing.JButton();
        changeQuestionFileLabel = new javax.swing.JLabel();
        changeQuestionLabel = new javax.swing.JLabel();
        addNewQuestionLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        newQuestionTextField = new javax.swing.JTextArea();
        answerTextField1 = new javax.swing.JTextField();
        answerTextField2 = new javax.swing.JTextField();
        answerTextField3 = new javax.swing.JTextField();
        answerTextField4 = new javax.swing.JTextField();
        answerCheckBox1 = new javax.swing.JCheckBox();
        answerCheckBox2 = new javax.swing.JCheckBox();
        answerCheckBox3 = new javax.swing.JCheckBox();
        answerCheckBox4 = new javax.swing.JCheckBox();
        questionTypeComboBox = new javax.swing.JComboBox();
        setButton = new javax.swing.JButton();
        createDemoFileButton = new javax.swing.JButton();
        addNewQuestionButton = new javax.swing.JButton();
        jSeparator3 = new javax.swing.JSeparator();
        addNewQuestionLabel1 = new javax.swing.JLabel();
        trueReactionTextField = new javax.swing.JTextField();
        falseReactionTextField = new javax.swing.JTextField();
        addNewQuestionLabel2 = new javax.swing.JLabel();
        addNewQuestionLabel3 = new javax.swing.JLabel();
        truePlacemarksButton = new javax.swing.JButton();
        falsePlaceMarksButton = new javax.swing.JButton();
        setReactionPlacemarksButton = new javax.swing.JButton();

        org.jdesktop.layout.GroupLayout jDialog1Layout = new org.jdesktop.layout.GroupLayout(jDialog1.getContentPane());
        jDialog1.getContentPane().setLayout(jDialog1Layout);
        jDialog1Layout.setHorizontalGroup(
            jDialog1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 400, Short.MAX_VALUE)
        );
        jDialog1Layout.setVerticalGroup(
            jDialog1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 300, Short.MAX_VALUE)
        );

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/jdesktop/wonderland/modules/learningPoster/client/resources/Bundle"); // NOI18N
        jLabel1.setText(bundle.getString("CHANGE_QUESTION")); // NOI18N

        questionComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                shapeTypeActionPerformed(evt);
            }
        });
        questionComboBox.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                shapeTypePropertyChange(evt);
            }
        });

        browseContentLabel.setText(bundle.getString("BROWSER_CONTENT")); // NOI18N

        uriTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                uriTextFieldActionPerformed(evt);
            }
        });

        browseButton.setText(bundle.getString("BROWSER_CONTENT")); // NOI18N
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });

        newFileButton.setText(bundle.getString("ADD_NEW_FILE")); // NOI18N
        newFileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newFileButtonActionPerformed(evt);
            }
        });

        setQuestionButton.setText(bundle.getString("SET_QUESTION")); // NOI18N

        changeQuestionFileLabel.setText(bundle.getString("CHANGE_QUESTION_FILE")); // NOI18N

        changeQuestionLabel.setText(bundle.getString("CHANGE_QUESTION")); // NOI18N

        addNewQuestionLabel.setText(bundle.getString("ADD_NEW_QUESTION")); // NOI18N

        newQuestionTextField.setColumns(20);
        newQuestionTextField.setRows(5);
        jScrollPane1.setViewportView(newQuestionTextField);

        answerTextField1.setText(bundle.getString("ANSWER")); // NOI18N

        answerTextField2.setText(bundle.getString("ANSWER")); // NOI18N

        answerTextField3.setText(bundle.getString("ANSWER")); // NOI18N

        answerTextField4.setText(bundle.getString("ANSWER")); // NOI18N

        answerCheckBox1.setText(bundle.getString("TRUE")); // NOI18N
        answerCheckBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                answerCheckBox1ActionPerformed(evt);
            }
        });

        answerCheckBox2.setText(bundle.getString("TRUE")); // NOI18N

        answerCheckBox3.setText(bundle.getString("TRUE")); // NOI18N

        answerCheckBox4.setText(bundle.getString("TRUE")); // NOI18N

        questionTypeComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Discussion Question", "Concept Question" }));
        questionTypeComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                questionTypeComboBoxActionPerformed(evt);
            }
        });

        setButton.setText(bundle.getString("SET")); // NOI18N
        setButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setButtonActionPerformed(evt);
            }
        });

        createDemoFileButton.setText(bundle.getString("NEW_FILE")); // NOI18N

        addNewQuestionButton.setText(bundle.getString("ADD_NEW_QUESTION")); // NOI18N

        addNewQuestionLabel1.setText(bundle.getString("ANSWER_REACTION")); // NOI18N

        trueReactionTextField.setText(bundle.getString("PLACEMARKS")); // NOI18N

        falseReactionTextField.setText(bundle.getString("PLACEMARKS")); // NOI18N

        addNewQuestionLabel2.setText(bundle.getString("TRUE")); // NOI18N

        addNewQuestionLabel3.setText(bundle.getString("FALSE")); // NOI18N

        truePlacemarksButton.setText(bundle.getString("PLACEMARKS")); // NOI18N

        falsePlaceMarksButton.setText(bundle.getString("PLACEMARKS")); // NOI18N

        setReactionPlacemarksButton.setText(bundle.getString("SET")); // NOI18N
        setReactionPlacemarksButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setReactionPlacemarksButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, jSeparator1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 507, Short.MAX_VALUE)
                            .add(layout.createSequentialGroup()
                                .add(browseContentLabel)
                                .add(18, 18, 18)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(layout.createSequentialGroup()
                                        .add(uriTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 289, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .add(18, 18, 18)
                                        .add(setButton))
                                    .add(layout.createSequentialGroup()
                                        .add(browseButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 120, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(newFileButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 111, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(createDemoFileButton)))
                                .add(43, 43, 43))
                            .add(changeQuestionFileLabel)
                            .add(jSeparator2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 507, Short.MAX_VALUE)
                            .add(changeQuestionLabel)
                            .add(layout.createSequentialGroup()
                                .add(jLabel1)
                                .add(10, 10, 10)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(setQuestionButton)
                                    .add(questionComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 340, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                .add(97, 97, 97))
                            .add(addNewQuestionLabel)))
                    .add(layout.createSequentialGroup()
                        .add(68, 68, 68)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 351, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(layout.createSequentialGroup()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                                    .add(org.jdesktop.layout.GroupLayout.LEADING, answerTextField2)
                                    .add(org.jdesktop.layout.GroupLayout.LEADING, answerTextField1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 305, Short.MAX_VALUE)
                                    .add(org.jdesktop.layout.GroupLayout.LEADING, answerTextField3)
                                    .add(org.jdesktop.layout.GroupLayout.LEADING, answerTextField4)
                                    .add(org.jdesktop.layout.GroupLayout.LEADING, questionTypeComboBox, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(answerCheckBox4)
                                    .add(answerCheckBox3)
                                    .add(answerCheckBox2)
                                    .add(answerCheckBox1)))
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, addNewQuestionButton)))
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(jSeparator3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 507, Short.MAX_VALUE))
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(addNewQuestionLabel1)))
                .addContainerGap())
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(addNewQuestionLabel3)
                    .add(addNewQuestionLabel2))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, falseReactionTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 324, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, trueReactionTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 324, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(truePlacemarksButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(falsePlaceMarksButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(setReactionPlacemarksButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .add(75, 75, 75))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(changeQuestionFileLabel)
                .add(17, 17, 17)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(browseContentLabel)
                    .add(uriTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(setButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 23, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(browseButton)
                    .add(newFileButton)
                    .add(createDemoFileButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 15, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(1, 1, 1)
                .add(changeQuestionLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(questionComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel1))
                .add(18, 18, 18)
                .add(setQuestionButton)
                .add(28, 28, 28)
                .add(jSeparator2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 15, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(addNewQuestionLabel)
                .add(18, 18, 18)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 70, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(answerTextField1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(answerCheckBox1))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(answerTextField2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(answerCheckBox2))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(answerTextField3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(answerCheckBox3))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(answerTextField4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(answerCheckBox4))
                .add(18, 18, 18)
                .add(questionTypeComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(18, 18, 18)
                .add(addNewQuestionButton)
                .add(18, 18, 18)
                .add(jSeparator3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 15, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(addNewQuestionLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(trueReactionTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(truePlacemarksButton))
                    .add(addNewQuestionLabel2))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(falseReactionTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(addNewQuestionLabel3)
                    .add(falsePlaceMarksButton))
                .add(18, 18, 18)
                .add(setReactionPlacemarksButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 23, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void shapeTypePropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_shapeTypePropertyChange
        // early return needed because of late model setting
        /*if (editor == null) {
        return;
        }

        // If the shape type has changed since the initial value, then
        // set the dirty bit to try
        String newLearningPosterText = (String) questionComboBox.getSelectedItem();
        if ((originalLearningPosterText != null)
        && (originalLearningPosterText.equals(newLearningPosterText) == false)) {
        editor.setPanelDirty(LearningPosterCellProperties.class, true);
        } else {
        editor.setPanelDirty(LearningPosterCellProperties.class, false);
        }*/
    }//GEN-LAST:event_shapeTypePropertyChange

    private void shapeTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_shapeTypeActionPerformed
        // TODO add your handling code here:
        // If the shape type has changed since the initial value, then
        // set the dirty bit to try
     /*   String newLearningPosterText = (String) questionComboBox.getSelectedItem();
        if ((originalLearningPosterText != null)
        && (originalLearningPosterText.equals(newLearningPosterText) == false)) {
        editor.setPanelDirty(LearningPosterCellProperties.class, true);
        } else {
        editor.setPanelDirty(LearningPosterCellProperties.class, false);
        }*/
    }//GEN-LAST:event_shapeTypeActionPerformed

    private void uriTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_uriTextFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_uriTextFieldActionPerformed

    private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_browseButtonActionPerformed

    private void newFileButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newFileButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_newFileButtonActionPerformed

    private void setButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_setButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_setButtonActionPerformed

    private void answerCheckBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_answerCheckBox1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_answerCheckBox1ActionPerformed

    private void questionTypeComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_questionTypeComboBoxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_questionTypeComboBoxActionPerformed

    private void setReactionPlacemarksButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_setReactionPlacemarksButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_setReactionPlacemarksButtonActionPerformed

    /**
     * A simple text frame with a "Save" button
     */
    private class SimpleTextEditor extends JFrame {

        private String uri = null;
        private JTextArea textArea = null;

        public SimpleTextEditor(final String uri) {
            this.uri = uri;

            // Set up the simple editor GUI components
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            getContentPane().setLayout(new BorderLayout());
            textArea = new JTextArea();
            JScrollPane scrollPane = new JScrollPane(textArea);
            getContentPane().add(scrollPane, BorderLayout.CENTER);
            JButton saveButton = new JButton(BUNDLE.getString("Save"));
            getContentPane().add(saveButton, BorderLayout.SOUTH);

            // Download the URI and text in the text area
            try {
                // Download the URI and text in the text area
                URL url = AssetUtils.getAssetURL(uri);
                textArea.setText(getURLAsString(url));
            } catch (MalformedURLException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }

            // Listen for the "Save" button
            saveButton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    ContentNode contentNode =
                            ContentRepositoryUtils.findContentNode(null, uri);
                    ContentResource res = (ContentResource) contentNode;
                    String text = textArea.getText();
                    try {
                        res.put(text.getBytes());
                    } catch (ContentRepositoryException ex) {
                        LOGGER.log(Level.SEVERE, null, ex);
                    }
                }
            });

            pack();
            setSize(300, 300);
        }

        private String getURLAsString(URL url) throws IOException {
            StringBuilder sb = new StringBuilder();
            BufferedReader r = new BufferedReader(
                    new InputStreamReader(url.openStream()));
            String line = null;
            while ((line = r.readLine()) != null) {
                sb.append(line + "\n");
            }
            return sb.toString();
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addNewQuestionButton;
    private javax.swing.JLabel addNewQuestionLabel;
    private javax.swing.JLabel addNewQuestionLabel1;
    private javax.swing.JLabel addNewQuestionLabel2;
    private javax.swing.JLabel addNewQuestionLabel3;
    private javax.swing.JCheckBox answerCheckBox1;
    private javax.swing.JCheckBox answerCheckBox2;
    private javax.swing.JCheckBox answerCheckBox3;
    private javax.swing.JCheckBox answerCheckBox4;
    private javax.swing.JTextField answerTextField1;
    private javax.swing.JTextField answerTextField2;
    private javax.swing.JTextField answerTextField3;
    private javax.swing.JTextField answerTextField4;
    private javax.swing.JButton browseButton;
    private javax.swing.JLabel browseContentLabel;
    private javax.swing.JLabel changeQuestionFileLabel;
    private javax.swing.JLabel changeQuestionLabel;
    private javax.swing.JButton createDemoFileButton;
    private javax.swing.JButton falsePlaceMarksButton;
    private javax.swing.JTextField falseReactionTextField;
    private javax.swing.JDialog jDialog1;
    private javax.swing.JFileChooser jFileChooser1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JButton newFileButton;
    private javax.swing.JTextArea newQuestionTextField;
    private javax.swing.JComboBox questionComboBox;
    private javax.swing.JComboBox questionTypeComboBox;
    private javax.swing.JButton setButton;
    private javax.swing.JButton setQuestionButton;
    private javax.swing.JButton setReactionPlacemarksButton;
    private javax.swing.JButton truePlacemarksButton;
    private javax.swing.JTextField trueReactionTextField;
    private javax.swing.JTextField uriTextField;
    // End of variables declaration//GEN-END:variables

    public <T extends CellServerState> void updateGUI(T cellServerState) {
        LearningPosterCellServerState state = (LearningPosterCellServerState) cellServerState;
        //      originalLearningPosterText = state.getLearningPosterText();
        //    questionComboBox.setSelectedItem(originalLearningPosterText);
    }

    public <T extends CellServerState> void getCellServerState(T state) {
        ((LearningPosterCellServerState) state).setLearningPosterText(
                (String) questionComboBox.getSelectedItem());
    }

    /**
     * Uploads the given PDF file to the user's WebDav area. Write the file into
     * the pdf/ directory, and put it under at <File Name>.pdf/<File Name>.pdf,
     * creating the directories that are necessary.
     *
     * @param file The PDF File to upload to WebDav
     * @throw ContentRepositoryException Upon error uploading to WebDav
     * @throw IOException Upon error writing to WebDav
     */
    public URL uploadFile(File file)
            throws ContentRepositoryException, IOException {
        LOGGER.log(Level.SEVERE, "2: uploading File{0}", file);

        // Fetch the pdf/ directory in the user's WebDav area. Check to see if
        // a <File Name>.pdf/ directory exists, otherwise create it.
        String xmlFileName = file.getName();
        ContentCollection fileRoot = getFileRoot();
        ContentNode node = fileRoot.getChild(xmlFileName);
        if (node == null) {
            // Create the directory if it does not exist.
            node = fileRoot.createChild(xmlFileName, Type.COLLECTION);
        } else if (!(node instanceof ContentCollection)) {
            // If it does exist, but is not a directory, then delete it and
            // recreate the directory.
            node.getParent().removeChild(xmlFileName);
            node = fileRoot.createChild(xmlFileName, Type.COLLECTION);
        }
        ContentCollection pdfDir = (ContentCollection) node;

        // Beneath the <File Name>.pdf/ directory, create a new resource for
        // the PDF File, removing it if it already exists.
        ContentNode resource = pdfDir.getChild(xmlFileName);
        if (resource == null) {
            // Create the resource if it does not exist.
            resource = pdfDir.createChild(xmlFileName, Type.RESOURCE);
        } else if (!(resource instanceof ContentResource)) {
            // If it does exist, but is not a resource, then delete it and
            // recreate the resource
            resource.getParent().removeChild(xmlFileName);
            resource = pdfDir.createChild(xmlFileName, Type.RESOURCE);
        }

        // Upload the file to WebDav
        ((ContentResource) resource).put(file);
        return ((ContentResource) resource).getURL();
    }

    /**
     * Returns the root directory for all PDF files, pdf/ under the user's
     * WebDav directory.
     */
    public ContentCollection getFileRoot() throws ContentRepositoryException {

        // Fetch the user's root using the current primary server. It should
        // be ok to use the primary server at this point
        ContentRepositoryRegistry r = ContentRepositoryRegistry.getInstance();
        ServerSessionManager session = LoginManager.getPrimary();

        // Try to find the pdf/ directory if it exists, otherwise, create it
        ContentCollection userRoot = r.getRepository(session).getUserRoot();
        ContentNode node = (ContentNode) userRoot.getChild("xml");
        if (node == null) {
            node = (ContentNode) userRoot.createChild("xml", Type.COLLECTION);
        } else if (!(node instanceof ContentCollection)) {
            node.getParent().removeChild("xml");
            node = (ContentNode) userRoot.createChild("xml", Type.COLLECTION);
        }
        return (ContentCollection) node;
    }

    private void setQuestionComboBox(File file) {
        Element rootElement = null;
        try {
            LOGGER.severe(file.toString());
            LOGGER.severe(file.getAbsolutePath());
            LOGGER.severe(String.valueOf(file.length()));
            LOGGER.severe(file.toURI().toString());

            rootElement = LearningPosterXMLProcessor.getRootElementFromFile(file);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(LearningPosterCellProperties.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(LearningPosterCellProperties.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(LearningPosterCellProperties.class.getName()).log(Level.SEVERE, null, ex);
        }
        HashMap questionMap = LearningPosterXMLProcessor.createQuestionArray(rootElement);
        String[] questionArray = new String[questionMap.size()];
        Iterator it = questionMap.keySet().iterator();
        int j = 0;
        while (it.hasNext()) {
            Object nObject = it.next();
            questionArray[j] = nObject.toString() + "::" + questionMap.get(nObject);
            j++;
        }
        ComboBoxModel questionComboBoxModel = new DefaultComboBoxModel(questionArray);
        questionComboBox.setModel(questionComboBoxModel);
    }
}
