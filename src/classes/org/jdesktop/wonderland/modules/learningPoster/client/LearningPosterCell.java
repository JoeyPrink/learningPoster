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
/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2010, Sun Microsystems, Inc., All Rights Reserved
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

import java.awt.Rectangle;
import java.util.ResourceBundle;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.client.cell.annotation.UsesCellComponent;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuActionListener;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItem;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItemEvent;
import org.jdesktop.wonderland.client.contextmenu.SimpleContextMenuItem;
import org.jdesktop.wonderland.client.contextmenu.cell.ContextMenuComponent;
import org.jdesktop.wonderland.client.contextmenu.spi.ContextMenuFactorySPI;
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.wonderland.client.scenemanager.event.ContextEvent;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.modules.appbase.client.cell.App2DCell;
import org.jdesktop.wonderland.modules.learningPoster.common.LearningPosterCellClientState;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedMapCli;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedMapEventCli;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedMapListenerCli;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedStateComponent;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedBoolean;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedString;

/**
 * The cell that renders the learningPoster.<br>
 * Adapted from the "generic" cell facility originally written by Jordan Slott
 * 
 * @author Bernard Horan
 * @author Jon Kaplan
 */
public class LearningPosterCell extends App2DCell {

    @UsesCellComponent
    private ContextMenuComponent contextComp = null;
    private static final ResourceBundle bundle = ResourceBundle.getBundle("org/jdesktop/wonderland/modules/learningPoster/client/resources/Bundle");
    private ContextMenuFactorySPI menuFactory = null;
    // The "shared state" Cell component
    @UsesCellComponent
    protected SharedStateComponent sharedStateComp;
    // The listener for changes to the shared map
    private SharedMapListenerCli mapListener = null;
    private String learningPosterText;
    private String actualFileName;
    private boolean billboardMode = false;
    private LearningPosterForm learningPosterForm;
    /** The (singleton) window created by the learningPoster app */
    private LearningPosterWindow window;
    /** The cell client state message received from the server cell */
    private LearningPosterCellClientState clientState;

    /** Constructor, takes Cell's ID and Cache
     * @param cellID The ID of the cell.
     * @param cellCache the cell cache which instantiated, and owns, this cell.
     */
    public LearningPosterCell(CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);
        mapListener = new MySharedMapListener();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setStatus(CellStatus status, boolean increasing) {
        super.setStatus(status, increasing);
        if (increasing && status == CellStatus.ACTIVE) {
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    learningPosterForm = new LearningPosterForm(LearningPosterCell.this);
                }
            });

            // Create the shared hash map and initialize the learningPoster text
            // if it does not already exist.
            SharedMapCli sharedMap = sharedStateComp.get(LearningPosterCellClientState.SHARED_MAP_KEY);
            SharedString learningPosterString = sharedMap.get(LearningPosterCellClientState.TEXT_LABEL_KEY, SharedString.class);
            if (learningPosterString == null) {
                learningPosterString = SharedString.valueOf(bundle.getString("HELLO_WORLD!"));
                sharedMap.put(LearningPosterCellClientState.TEXT_LABEL_KEY, learningPosterString);
            }
            learningPosterText = learningPosterString.toString();
            SharedString actualFileNameString = sharedMap.get(LearningPosterCellClientState.FILE_LABEL_KEY, SharedString.class);
            if (actualFileNameString == null) {
                actualFileNameString = SharedString.valueOf(bundle.getString("FILENAME"));
                sharedMap.put(LearningPosterCellClientState.FILE_LABEL_KEY, actualFileNameString);
            }
            actualFileName = actualFileNameString.toString();

            SharedBoolean billboardModeBoolean = sharedMap.get(LearningPosterCellClientState.MODE_LABEL_KEY, SharedBoolean.class);
            if (billboardModeBoolean == null) {
                billboardModeBoolean = SharedBoolean.FALSE;
                sharedMap.put(LearningPosterCellClientState.MODE_LABEL_KEY, billboardModeBoolean);
            }
            billboardMode = billboardModeBoolean.getValue();

            //Add menu item to edit the text from the context menu
            if (menuFactory == null) {
                final ContextMenuActionListener l = new ContextMenuActionListener() {

                    public void actionPerformed(ContextMenuItemEvent event) {
                        openLearningPosterForm();
                    }
                };
                menuFactory = new ContextMenuFactorySPI() {

                    public ContextMenuItem[] getContextMenuItems(ContextEvent event) {
                        return new ContextMenuItem[]{
                                    new SimpleContextMenuItem(bundle.getString("SET_TEXT"), l)
                                };
                    }
                };
                contextComp.addContextMenuFactory(menuFactory);
            }
            //Create the learningPoster app
            LearningPosterApp stApp = new LearningPosterApp("learningPoster", clientState.getPixelScale());
            setApp(stApp);

            // Tell the app to be displayed in this cell.
            stApp.addDisplayer(this);

            // This app has only one window, so it is always top-level
            try {
                window = new LearningPosterWindow(this, stApp, clientState.getPreferredWidth(),
                        clientState.getPreferredHeight(), true, pixelScale);
                window.setTitle("learningPoster");
                window.setDecorated(false);
            } catch (InstantiationException ex) {
                throw new RuntimeException(ex);
            }

            // Both the app and the user want this window to be visible
            window.setVisibleApp(true);
            window.setVisibleUser(this, true);
        }
        if (status == CellStatus.RENDERING && increasing == true) {
            // Initialize the render with the current learningPoster text
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    learningPosterForm.updateForm();
                    window.updateLabel();
                }
            });

            // Listen for changes in the learningPoster text from other clients
            SharedMapCli sharedMap = sharedStateComp.get(LearningPosterCellClientState.SHARED_MAP_KEY);
            sharedMap.addSharedMapListener(LearningPosterCellClientState.TEXT_LABEL_KEY, mapListener);
            sharedMap.addSharedMapListener(LearningPosterCellClientState.FILE_LABEL_KEY, mapListener);
            sharedMap.addSharedMapListener(LearningPosterCellClientState.MODE_LABEL_KEY, mapListener);

        }
        if (!increasing && status == CellStatus.DISK) {
            // Remove the listener for changes to the shared map
            SharedMapCli sharedMap = sharedStateComp.get(LearningPosterCellClientState.SHARED_MAP_KEY);
            sharedMap.removeSharedMapListener(LearningPosterCellClientState.TEXT_LABEL_KEY, mapListener);
            sharedMap.removeSharedMapListener(LearningPosterCellClientState.FILE_LABEL_KEY, mapListener);
            sharedMap.removeSharedMapListener(LearningPosterCellClientState.MODE_LABEL_KEY, mapListener);
            //Cleanup menu
            if (menuFactory != null) {
                contextComp.removeContextMenuFactory(menuFactory);
                menuFactory = null;
            }
            window.setVisibleApp(false);
            window = null;

            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    if (learningPosterForm != null) {
                        learningPosterForm.dispose();
                    }
                }
            });
        }
    }

    String getLearningPosterText() {
        return learningPosterText;
    }
    String getActualFileName() {
        return actualFileName;
    }

    boolean getBillboardMode() {
        return billboardMode;
    }

    void openLearningPosterForm() {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                Rectangle parentBounds = getParentFrame().getBounds();
                Rectangle formBounds = learningPosterForm.getBounds();
                learningPosterForm.setLocation(parentBounds.width / 2 - formBounds.width / 2 + parentBounds.x,
                        parentBounds.height - formBounds.height - parentBounds.y);

                learningPosterForm.setVisible(true);
            }
        });
    }

    private JFrame getParentFrame() {
        return JmeClientMain.getFrame().getFrame();
    }

    void setLearningPosterText(String text) {
        if (!text.equals(learningPosterText)) {
            learningPosterText = text;
            SharedMapCli sharedMap = sharedStateComp.get(LearningPosterCellClientState.SHARED_MAP_KEY);
            SharedString labelTextString = SharedString.valueOf(learningPosterText);
            sharedMap.put(LearningPosterCellClientState.TEXT_LABEL_KEY, labelTextString);
        }
    }

        void setActualFileName(String text) {
        if (!text.equals(actualFileName)) {
            actualFileName = text;
            SharedMapCli sharedMap = sharedStateComp.get(LearningPosterCellClientState.SHARED_MAP_KEY);
            SharedString labelTextString = SharedString.valueOf(actualFileName);
            sharedMap.put(LearningPosterCellClientState.FILE_LABEL_KEY, labelTextString);
        }
    }

    void setBillboardMode(boolean mode) {
        if (billboardMode != mode) {
            billboardMode = mode;
            SharedMapCli sharedMap = sharedStateComp.get(LearningPosterCellClientState.SHARED_MAP_KEY);
            SharedBoolean billboardModeBoolean = SharedBoolean.valueOf(billboardMode);
            sharedMap.put(LearningPosterCellClientState.MODE_LABEL_KEY, billboardModeBoolean);
        }
    }

    /**
     * Initialize the cell with parameters from the server.
     *
     * @param state the client state with which initialize the cell.
     */
    @Override
    public void setClientState(CellClientState state) {
        super.setClientState(state);
        clientState = (LearningPosterCellClientState) state;
    }

    /**
     * Listens to changes in the shared map and updates the learningPoster text or billboard mode
     */
    class MySharedMapListener implements SharedMapListenerCli {

        public void propertyChanged(SharedMapEventCli event) {
            if (event.getPropertyName().equals(LearningPosterCellClientState.TEXT_LABEL_KEY)) {
                SharedString learningPosterTextString = (SharedString) event.getNewValue();
                learningPosterText = learningPosterTextString.getValue();
            }
            if (event.getPropertyName().equals(LearningPosterCellClientState.FILE_LABEL_KEY)) {
                SharedString actualFileNameString = (SharedString) event.getNewValue();
                actualFileName = actualFileNameString.getValue();
            }
            if (event.getPropertyName().equals(LearningPosterCellClientState.MODE_LABEL_KEY)) {
                SharedBoolean billboardModeBoolean = (SharedBoolean) event.getNewValue();
                billboardMode = billboardModeBoolean.getValue();
            }

            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    learningPosterForm.updateForm();
                    window.updateLabel();
                }
            });
        }
    }
}
