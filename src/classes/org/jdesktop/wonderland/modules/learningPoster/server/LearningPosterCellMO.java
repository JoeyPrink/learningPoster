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
package org.jdesktop.wonderland.modules.learningPoster.server;

import com.jme.math.Vector2f;
import com.sun.sgs.app.ManagedReference;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.appbase.server.cell.App2DCellMO;
import org.jdesktop.wonderland.modules.learningPoster.common.LearningPosterCellClientState;
import org.jdesktop.wonderland.modules.sharedstate.server.SharedStateComponentMO;
import org.jdesktop.wonderland.modules.learningPoster.common.LearningPosterCellServerState;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedBoolean;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedString;
import org.jdesktop.wonderland.modules.sharedstate.server.SharedMapSrv;
import org.jdesktop.wonderland.server.cell.annotation.UsesCellComponentMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;

/**
 * A server side cell to represent a learningPoster.<br>
 * Adapted from GenericCellMO originally written by Jordan Slott <jslott@dev.java.net>
 * <p>
 * This class adds a "shared state" component which can be used by the client-
 * side Cell code to synchronize the state of the Cell across clients.
 *
 * @author Bernard Horan
 */
public class LearningPosterCellMO extends App2DCellMO {

    /** The preferred width (from the WFS file) */
    private int preferredWidth;
    /** The preferred height (from the WFS file) */
    private int preferredHeight;
    @UsesCellComponentMO(SharedStateComponentMO.class)
    private ManagedReference<SharedStateComponentMO> sharedStateCompRef;

    /** Default constructor */
    public LearningPosterCellMO() {
        super();
    }

    @Override
    protected String getClientCellClassName(WonderlandClientID clientID, ClientCapabilities capabilities) {
        return "org.jdesktop.wonderland.modules.learningPoster.client.LearningPosterCell";
    }

    @Override
    protected CellClientState getClientState(CellClientState cellClientState, WonderlandClientID clientID, ClientCapabilities capabilities) {
        if (cellClientState == null) {
            cellClientState = new LearningPosterCellClientState(pixelScale);
        }
        ((LearningPosterCellClientState) cellClientState).setPreferredWidth(preferredWidth);
        ((LearningPosterCellClientState) cellClientState).setPreferredHeight(preferredHeight);
        return super.getClientState(cellClientState, clientID, capabilities);
    }

    @Override
    public void setServerState(CellServerState state) {
        super.setServerState(state);

        LearningPosterCellServerState serverState = (LearningPosterCellServerState) state;
        preferredWidth = serverState.getPreferredWidth();
        preferredHeight = serverState.getPreferredHeight();
        pixelScale = new Vector2f(serverState.getPixelScaleX(), serverState.getPixelScaleY());
    }

    @Override
    public CellServerState getServerState(CellServerState stateToFill) {
        if (stateToFill == null) {
            stateToFill = new LearningPosterCellServerState();
        }

        super.getServerState(stateToFill);

        LearningPosterCellServerState state = (LearningPosterCellServerState) stateToFill;
        state.setPreferredWidth(preferredWidth);
        state.setPreferredHeight(preferredHeight);

        return stateToFill;
    }

    /**
     * Set the text in the learningPoster, causing clients to update
     * @param text a string containing the text for the learningPoster, may be HTML
     */
    public void setLearningPosterText(String text) {
        SharedMapSrv sharedMap = sharedStateCompRef.get().get(LearningPosterCellClientState.SHARED_MAP_KEY);
        SharedString labelTextString = SharedString.valueOf(text);
        sharedMap.put(LearningPosterCellClientState.TEXT_LABEL_KEY, labelTextString);
    }

    /**
     * Get the current text in the learningPoster
     * @return the string of the text in the learningPoster, may be HTML
     */
    public String getLearningPosterText() {
        SharedMapSrv sharedMap = sharedStateCompRef.get().get(LearningPosterCellClientState.SHARED_MAP_KEY);
        SharedString sharedString = (SharedString) sharedMap.get(LearningPosterCellClientState.TEXT_LABEL_KEY);
        return sharedString.getValue();
    }

        /**
     * Set the text in the learningPoster, causing clients to update
     * @param text a string containing the text for the learningPoster, may be HTML
     */
    public void setActualFileName(String text) {
        SharedMapSrv sharedMap = sharedStateCompRef.get().get(LearningPosterCellClientState.SHARED_MAP_KEY);
        SharedString labelTextString = SharedString.valueOf(text);
        sharedMap.put(LearningPosterCellClientState.FILE_LABEL_KEY, labelTextString);
    }

    /**
     * Get the current text in the learningPoster
     * @return the string of the text in the learningPoster, may be HTML
     */
    public String getActualFileName() {
        SharedMapSrv sharedMap = sharedStateCompRef.get().get(LearningPosterCellClientState.SHARED_MAP_KEY);
        SharedString sharedString = (SharedString) sharedMap.get(LearningPosterCellClientState.FILE_LABEL_KEY);
        return sharedString.getValue();
    }

    /**
     * Set the "billboard" mode for the learningPoster, causing clients to update
     * @param mode if true, learningPoster is in billboard mode, otherwise false
     */
    public void setBillboardMode(boolean mode) {
        SharedMapSrv sharedMap = sharedStateCompRef.get().get(LearningPosterCellClientState.SHARED_MAP_KEY);
        SharedBoolean billboardModeBoolean = SharedBoolean.valueOf(mode);
        sharedMap.put(LearningPosterCellClientState.MODE_LABEL_KEY, billboardModeBoolean);
    }

    /**
     * Get the "billboard" mode for the learningPoster
     * @return if true, the learningPoster is in "billboard" mode, false otherwise
     */
    public boolean getBillboardMode() {
        SharedMapSrv sharedMap = sharedStateCompRef.get().get(LearningPosterCellClientState.SHARED_MAP_KEY);
        SharedBoolean sharedBoolean = (SharedBoolean) sharedMap.get(LearningPosterCellClientState.MODE_LABEL_KEY);
        return sharedBoolean.getValue();
    }
}
