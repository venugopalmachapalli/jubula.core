/*******************************************************************************
 * Copyright (c) 2014 BREDEX GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BREDEX GmbH - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.jubula.client.wiki.ui.dialogs;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jubula.client.core.model.INodePO;
import org.eclipse.jubula.client.ui.utils.DelayableTimer;
import org.eclipse.jubula.client.wiki.ui.i18n.Messages;
import org.eclipse.jubula.client.wiki.ui.Activator;
import org.eclipse.mylyn.wikitext.tracwiki.core.TracWikiLanguage;
import org.eclipse.mylyn.wikitext.ui.editor.MarkupSourceViewer;
import org.eclipse.mylyn.wikitext.ui.editor.MarkupSourceViewerConfiguration;
import org.eclipse.mylyn.wikitext.ui.viewer.MarkupViewer;
import org.eclipse.mylyn.wikitext.ui.viewer.MarkupViewerConfiguration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;

/**
 * 
 * @author BREDEX GmbH
 */
public class DescriptionEditDialog extends TitleAreaDialog {

    /** the wait time(ms) before the preview is updated */
    private static final int WAIT_BEFORE_UPDATE = 400;

    /** the working node */
    private INodePO m_workNode;

    /** the description text as document */
    private IDocument m_description;
    
    /**
     * 
     * @param parentShell the parent shell
     * @param workNode the node which should be worked with
     */
    public DescriptionEditDialog(Shell parentShell, INodePO workNode) {
        super(parentShell);
        setShellStyle(getShellStyle() | SWT.RESIZE);
        m_workNode = workNode;
        m_description = new Document(workNode.getDescription());
    }
    
    /**
     * {@inheritDoc}
     */
    protected Control createDialogArea(Composite parent) {
        final String dialogTitle = Messages.EditDescriptionDialogTitle;
        setTitle(dialogTitle);
        setMessage(Messages.EditDescriptionDialogDescription);
        getShell().setText(dialogTitle);
        
        SashForm sashForm = new SashForm(parent, SWT.VERTICAL);
        final GridData sashGridData = createGridData();
        sashForm.setLayoutData(sashGridData);

        createDialogComponents(sashForm);
        return sashForm;
    }
    
    /**
     * @param sashForm the parent sash
     */
    private void createDialogComponents(SashForm sashForm) {
        // styles for the viewer and sourceViewer
        int styles = SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION | SWT.WRAP
                | SWT.V_SCROLL;
        
        // creating group and source viewer
        Group sourceGroup = new Group(sashForm, SWT.SHADOW_ETCHED_IN);
        sourceGroup.setText(Messages.EditDescriptionDialogSourceViewer);
        sourceGroup.setLayout(new GridLayout());
        MarkupSourceViewer sourceViewer = createsMarkupSourceViewer(
                sourceGroup, styles);
        
        // creating group and preview
        Group previewGroup = new Group(sashForm, SWT.SHADOW_ETCHED_IN);
        previewGroup.setText(Messages.EditDescriptionDialogPreview);
        previewGroup.setLayout(new GridLayout());
        MarkupViewer viewer = createMarkupViewer(previewGroup, styles);

        addDocumentListenerViewer(sourceViewer, viewer);
    }
    
    /**
     * @param sourceViewer the {@link MarkupSourceViewer} which is the source 
     *                     of the description
     * @param viewer the {@link MarkupViewer} which should get the new data
     */
    private void addDocumentListenerViewer(
            final MarkupSourceViewer sourceViewer, final MarkupViewer viewer) {
        final DelayableTimer delayedPreviewTimer = new DelayableTimer(
                WAIT_BEFORE_UPDATE, new Runnable() {
                    public void run() {
                        Display.getDefault().syncExec(new Runnable() {
                            public void run() {
                                viewer.setDocument(new Document(m_description
                                        .get()));
                            }
                        });
                    }
                });
        m_description.addDocumentListener(new IDocumentListener() {
            public void documentChanged(DocumentEvent event) {
                delayedPreviewTimer.cancel();
                delayedPreviewTimer.schedule();                
            }
            
            public void documentAboutToBeChanged(DocumentEvent event) {
                // Nothing before changed
            }
        });
    }

    /**
     * creates the {@link MarkupSourceViewera} for the description.
     * @param parent the parent
     * @param styles styles that should be used for the {@link MarkupSourceViewera}
     * @return the created {@link MarkupSourceViewera}
     */
    private MarkupSourceViewer createsMarkupSourceViewer(Composite parent,
            int styles) {
        final MarkupSourceViewer sourceViewer = new MarkupSourceViewer(parent,
                null, styles, new TracWikiLanguage());
        sourceViewer.setDocument(m_description);
        MarkupSourceViewerConfiguration sourceViewerConfig = 
                new MarkupSourceViewerConfiguration(null);
        sourceViewer.configure(sourceViewerConfig);
        sourceViewer.getControl().setLayoutData(createGridData());
        return sourceViewer;
    }

    /**
     * creates the {@link MarkupViewer} for the rendered description
     * @param parent the parent
     * @param styles styles thaht should be used for the {@link MarkupViewer}
     * @return the created {@link MarkupViewer}
     */
    private MarkupViewer createMarkupViewer(Composite parent, int styles) {
        final MarkupViewer viewer = new MarkupViewer(parent, null, null, true,
                styles);
        MarkupViewerConfiguration viewerConfiguration = 
                new MarkupViewerConfiguration(viewer);
        viewerConfiguration.setEnableSelfContainedIncrementalFind(true);
        viewerConfiguration.setDisableHyperlinkModifiers(false);
        viewer.configure(viewerConfiguration);
        viewer.setMarkupLanguage(new TracWikiLanguage());
        viewer.getControl().setLayoutData(createGridData());
        viewer.setDocument(new Document(m_workNode.getDescription()));
        return viewer;
    }

    /**
     * 
     * @return a {@link GridData} with full grabbing of space
     */
    private GridData createGridData() {
        final GridData areaGridData = new GridData();
        areaGridData.grabExcessVerticalSpace = true;
        areaGridData.grabExcessHorizontalSpace = true;
        areaGridData.horizontalAlignment = GridData.FILL;
        areaGridData.verticalAlignment = GridData.FILL;
        return areaGridData;
    }

    /**
     * get the description
     * @return the description
     */
    public String getDescription() {
        return m_description.get();
    }
    
    /** {@inheritDoc} */
    protected IDialogSettings getDialogBoundsSettings() {
        return Activator.getActivator().getDialogSettings();
    }
    
}