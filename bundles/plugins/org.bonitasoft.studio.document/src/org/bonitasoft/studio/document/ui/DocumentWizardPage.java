/**
 * Copyright (C) 2014 Bonitasoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.studio.document.ui;

import org.bonitasoft.studio.common.ExpressionConstants;
import org.bonitasoft.studio.common.jface.databinding.validator.GroovyReferenceValidator;
import org.bonitasoft.studio.common.jface.databinding.validator.InputLengthValidator;
import org.bonitasoft.studio.document.DocumentInitialContentValidator;
import org.bonitasoft.studio.document.DocumentNameValidator;
import org.bonitasoft.studio.document.SelectDocumentInBonitaStudioRepository;
import org.bonitasoft.studio.document.i18n.Messages;
import org.bonitasoft.studio.expression.editor.filter.AvailableExpressionTypeFilter;
import org.bonitasoft.studio.expression.editor.provider.IExpressionValidator;
import org.bonitasoft.studio.expression.editor.viewer.ExpressionViewer;
import org.bonitasoft.studio.expression.editor.viewer.ObservableExpressionContentProvider;
import org.bonitasoft.studio.model.expression.Expression;
import org.bonitasoft.studio.model.process.Document;
import org.bonitasoft.studio.model.process.DocumentType;
import org.bonitasoft.studio.model.process.Pool;
import org.bonitasoft.studio.model.process.ProcessPackage;
import org.bonitasoft.studio.pics.Pics;
import org.bonitasoft.studio.pics.PicsConstants;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.PojoObservables;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.SelectObservableValue;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.databinding.validation.MultiValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.emf.databinding.EMFDataBindingContext;
import org.eclipse.emf.databinding.EMFObservables;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.viewers.ViewerProperties;
import org.eclipse.jface.databinding.wizard.WizardPageSupport;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

public class DocumentWizardPage extends WizardPage {

    private final EObject context;
    private final Document document;
    private Text documentNameText;
    private Text documentDescriptionText;
    private ExpressionViewer documentUrlViewer;
    private ExpressionViewer documentMimeTypeViewer;
    private Text documentTextId;
    private Button browseButton;
    //    private Button externalCheckbox;
    private Composite detailsComposite;
    private EMFDataBindingContext emfDataBindingContext;
    private WizardPageSupport pageSupport;

    private Button radioButtonExternal;
    private Button radioButtonInternal;
    private Button radioButtonNone;
    private StackLayout stack;

    protected static final String NONE = "none";
    protected static final String EXTERNAL = "external";
    protected static final String INTERNAL = "internal";

    protected static final String LINK = "link";
    protected static final String FIELD = "field";

    private Composite externalCompo;
    private Composite internalCompo;
    private Composite noneCompo;
    private Composite propertiesComposite;
    private Composite mimeTypeComposition;
    private Link hideLink;
    private Composite manageLinkComposition;
    private StackLayout mimeStack;
    private Composite mimeCompo;
    private Label mimeTypeLabel;
    private ControlDecoration cd;
    private final DocumentInitialContentValidator externalValidator;
    private IObservableValue nameObserved;
    private IObservableValue documentInternalIDObserved;
    private IObservableValue externalUrlObserved;
    private IObservableValue btnDocumentTypeNone;
    private IObservableValue btnDocumentTypeExternal;
    private IObservableValue btnDocumentTypeInternal;
    private IObservableValue externalInitialContentObserveWidget;


    public DocumentWizardPage(final EObject context,final Document document){
        super(DocumentWizardPage.class.getName());
        this.context = context;
        this.document = document;
        setTitle(Messages.bind(Messages.documentWizardPageTitle, getCurrentContextName()));
        setDescription(Messages.newDocumentWizardDescription);
        emfDataBindingContext = new EMFDataBindingContext();
        setPageComplete(false);
        externalValidator = new DocumentInitialContentValidator();
    }

    @Override
    public void createControl(final Composite parent) {
        final Composite mainComposite = new Composite(parent, SWT.NONE);
        mainComposite.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
        mainComposite.setLayout(GridLayoutFactory.fillDefaults().numColumns(2).margins(7, 7).create());
        createDetailsPanel(mainComposite);
        pageSupport =  WizardPageSupport.create(this, emfDataBindingContext) ;
        bindDetails();
        setControl(mainComposite);
    }

    private String getCurrentContextName(){
        String name = "---";
        EObject container = context;
        while (!(container instanceof Pool) && container.eContainer()!=null) {
            container = container.eContainer();
        }
        if(container!=null && container instanceof Pool){
            name = ((Pool) container).getName();
        }
        return name;
    }

    private void createDetailsPanel(final Composite mainComposite) {
        detailsComposite = new Composite(mainComposite,SWT.NONE);
        detailsComposite.setLayout(GridLayoutFactory.fillDefaults().numColumns(2).spacing(10, 5).create());
        detailsComposite.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
        createDocumentNameField(detailsComposite);
        createDocumentDescriptionField(detailsComposite);
        createDocumentInitialValuefields(detailsComposite);

        createMimeType(detailsComposite);

        updateMimeTypeStack(LINK);
    }

    private void createMimeType(final Composite detailsComposite) {

        mimeTypeLabel = new Label(detailsComposite, SWT.NONE);
        mimeTypeLabel.setLayoutData(GridDataFactory.fillDefaults().align(SWT.END, SWT.CENTER).create());
        mimeTypeLabel.setText(Messages.mimeType);
        mimeTypeLabel.setAlignment(SWT.CENTER);
        cd = new ControlDecoration(mimeTypeLabel, SWT.RIGHT);
        cd.setImage(Pics.getImage(PicsConstants.hint));
        cd.setDescriptionText(Messages.explanationMimeTypeDocument);

        mimeCompo = new Composite(detailsComposite, SWT.NONE);
        mimeCompo.setLayout(GridLayoutFactory.fillDefaults().numColumns(1).create());
        mimeCompo.setLayoutData(GridDataFactory.fillDefaults().create());
        mimeStack = new StackLayout();
        mimeCompo.setLayout(mimeStack);
        createDocumentMimeTypeField(mimeCompo);
        createDocumentManageMimeTypeLink(mimeCompo);
    }

    private void createDocumentNameField(final Composite detailsComposite) {
        final Label nameLabel = new Label(detailsComposite, SWT.NONE);
        nameLabel.setText(Messages.name + " *");
        nameLabel.setLayoutData(GridDataFactory.fillDefaults().align(SWT.END, SWT.CENTER).create());
        documentNameText = new Text(detailsComposite, SWT.BORDER);
        documentNameText.setText("");
        documentNameText.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
    }

    private void createDocumentDescriptionField(final Composite detailsComposite) {
        final Label description = new Label(detailsComposite, SWT.NONE);
        description.setLayoutData(GridDataFactory.fillDefaults().align(SWT.END, SWT.TOP).create());
        description.setText(Messages.description);

        documentDescriptionText = new Text(detailsComposite, SWT.BORDER | SWT.V_SCROLL);
        documentDescriptionText.setText("");
        documentDescriptionText.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).hint(SWT.DEFAULT, 60).create());
    }

    private void createDocumentMimeTypeField(final Composite detailsComposite) {


        mimeTypeComposition = new Composite(detailsComposite, SWT.NONE);
        mimeTypeComposition.setLayout(GridLayoutFactory.fillDefaults().numColumns(2).create());
        mimeTypeComposition.setLayoutData(GridDataFactory.fillDefaults().create());

        documentMimeTypeViewer = new ExpressionViewer(mimeTypeComposition,
                SWT.BORDER, ProcessPackage.Literals.DOCUMENT__MIME_TYPE);
        documentMimeTypeViewer.addFilter(new AvailableExpressionTypeFilter(new String[] { ExpressionConstants.CONSTANT_TYPE }));
        documentMimeTypeViewer.getControl().setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
        documentMimeTypeViewer.getTextControl().setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
        documentMimeTypeViewer.setExample(Messages.hintMimeTypeDocument);


        hideLink = new Link(mimeTypeComposition, SWT.NONE);
        hideLink.setText("<A>" + Messages.hideMimeType + "</A>");
        hideLink.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                updateMimeTypeStack(LINK);
            }
        });

    }

    private void createDocumentManageMimeTypeLink(final Composite detailsComposite) {

        manageLinkComposition = new Composite(detailsComposite, SWT.NONE);
        manageLinkComposition.setLayout(GridLayoutFactory.fillDefaults().numColumns(1).create());
        manageLinkComposition.setLayoutData(GridDataFactory.fillDefaults().create());

        final Link manageLink = new Link(manageLinkComposition, SWT.NONE);
        manageLink.setText("<A>" + Messages.manageMimeType + "</A>");
        manageLink.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                updateMimeTypeStack(FIELD);
            }
        });
    }

    private void createDocumentURL(final Composite slaveComposite) {
        final Label documentURLLabel = new Label(slaveComposite, SWT.NONE);
        documentURLLabel.setText(Messages.documentExternalLabel + " *");

        documentUrlViewer = new ExpressionViewer(slaveComposite, SWT.BORDER,
                ProcessPackage.Literals.DOCUMENT__URL);
        documentUrlViewer.addFilter(new AvailableExpressionTypeFilter(
                new String[] { ExpressionConstants.CONSTANT_TYPE }));
        documentUrlViewer.getControl().setLayoutData(
                GridDataFactory.fillDefaults().grab(true, false).create());
        documentUrlViewer.getTextControl().setLayoutData(
                GridDataFactory.fillDefaults().grab(true, false).create());
        documentUrlViewer.setExample(Messages.hintExternalUrl);
        documentUrlViewer
        .setContentProvider(new ObservableExpressionContentProvider());

    }

    private void createDocumentBrowse(final Composite slaveComposite) {
        final Label documentBrowserLabel = new Label(slaveComposite, SWT.NONE);
        documentBrowserLabel.setText(Messages.documentInternalLabel + " *");

        final Composite browseWithTextComposite =new Composite(
                slaveComposite,SWT.NONE);
        browseWithTextComposite.setLayout(GridLayoutFactory.fillDefaults()
                .numColumns(2).create());
        browseWithTextComposite.setLayoutData(GridDataFactory.fillDefaults()
                .grab(true, false).create());
        documentTextId = new Text(browseWithTextComposite,SWT.BORDER);
        documentTextId.setText("");
        documentTextId.setLayoutData(GridDataFactory.fillDefaults()
                .grab(true, false).indent(10, 0).create());

        browseButton = new Button(browseWithTextComposite, SWT.FLAT);
        browseButton.setText(Messages.Browse);
        browseButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                super.widgetSelected(e);
                final SelectDocumentInBonitaStudioRepository selectDocumentInBonitaStudioRepository = new SelectDocumentInBonitaStudioRepository(
                        PlatformUI.getWorkbench().getActiveWorkbenchWindow());
                if (IDialogConstants.OK_ID == selectDocumentInBonitaStudioRepository
                        .open()) {
                    documentTextId
                    .setText(selectDocumentInBonitaStudioRepository
                            .getSelectedDocument().getDisplayName());
                }
            }
        });
    }

    private void createDocumentInitialValuefields(final Composite parent) {

        final Label radioButtonLabel = new Label(parent, SWT.NONE);
        radioButtonLabel.setText(Messages.initialValueLabel);
        radioButtonLabel.setLayoutData(GridDataFactory.fillDefaults().align(SWT.END, SWT.CENTER).indent(0, 25).create());
        createRadioButtonComposition(parent);

        new Composite(parent, SWT.NONE);

        propertiesComposite = new Composite(parent, SWT.NONE);
        propertiesComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        stack = new StackLayout();
        propertiesComposite.setLayout(stack);

        noneCompo = new Composite(propertiesComposite, SWT.NONE);

        createInternalComposition(propertiesComposite);

        createExternalComposition(propertiesComposite);

        if (document.getDocumentType().equals(DocumentType.NONE)) {
            updateStack(NONE);
        } else if (document.getDocumentType().equals(DocumentType.INTERNAL)) {
            updateStack(INTERNAL);
        } else {
            updateStack(EXTERNAL);
        }

    }

    private void createExternalComposition(final Composite propertiesComposite) {
        externalCompo = new Composite(propertiesComposite, SWT.NONE);
        externalCompo.setLayout(new GridLayout(2, false));
        externalCompo.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
        createDocumentURL(externalCompo);
    }

    private void createInternalComposition(final Composite propertiesComposite) {
        internalCompo = new Composite(propertiesComposite, SWT.NONE);
        internalCompo.setLayout(new GridLayout(2, false));
        internalCompo.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
        createDocumentBrowse(internalCompo);
    }

    private void createRadioButtonComposition(final Composite parent) {
        final Composite compo = new Composite(parent, SWT.NONE);
        compo.setLayout(GridLayoutFactory.fillDefaults().numColumns(4).spacing(20, 0).create());
        compo.setLayoutData(GridDataFactory.fillDefaults().indent(0, 25).create());

        radioButtonNone = new Button(compo, SWT.RADIO);
        radioButtonNone.setText(Messages.initialValueButtonNone);
        radioButtonNone.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                super.widgetSelected(e);
                if(radioButtonNone.getSelection()){
                    radioButtonExternal.setSelection(false);
                    radioButtonInternal.setSelection(false);
                    updateStack(NONE);
                }
            }

        });

        radioButtonInternal = new Button(compo, SWT.RADIO);
        radioButtonInternal.setText(Messages.initialValueButtonInternal);
        final ControlDecoration infoBonita = new ControlDecoration(radioButtonInternal, SWT.RIGHT);
        infoBonita.show();
        infoBonita.setImage(Pics.getImage(PicsConstants.hint));
        infoBonita.setDescriptionText(Messages.initialValueButtonInternalToolTip);
        radioButtonInternal.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                super.widgetSelected(e);
                if (radioButtonInternal.getSelection()) {
                    radioButtonNone.setSelection(false);
                    radioButtonExternal.setSelection(false);
                    updateStack(INTERNAL);
                }
            }

        });


        radioButtonExternal = new Button(compo, SWT.RADIO);
        radioButtonExternal.setText(Messages.initialValueButtonExternal);
        final ControlDecoration infoExternal = new ControlDecoration(radioButtonExternal, SWT.RIGHT);
        infoExternal.show();
        infoExternal.setImage(Pics.getImage(PicsConstants.hint));
        infoExternal.setDescriptionText(Messages.initialValueButtonExternalToolTip);
        radioButtonExternal.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                super.widgetSelected(e);
                if (radioButtonExternal.getSelection()) {
                    radioButtonNone.setSelection(false);
                    radioButtonInternal.setSelection(false);
                    updateStack(EXTERNAL);
                }
            }

        });
    }

    protected void bindDetails() {

        bindDocumentURL();

        bindDocumentMIMEType();

        bindDocumentName();

        bindDocumentDescription();

        bindDocumentType();

        bindDocumentDefaultValueID();

        addMultiValidator();

    }

    private void addMultiValidator() {
        final MultiValidator multiValidator = new MultiValidator() {

            @Override
            public IStatus validate() {

                String defaultID = null;
                if (documentInternalIDObserved.getValue() != null) {
                    defaultID = documentInternalIDObserved.getValue().toString();
                }
                final String url = ((Expression) (externalInitialContentObserveWidget.getValue())).getContent();

                final Boolean externalBtn = (Boolean) btnDocumentTypeExternal.getValue();
                final Boolean internalBtn = (Boolean) btnDocumentTypeInternal.getValue();

                if (externalBtn && url.isEmpty()) {
                    return ValidationStatus.error(Messages.error_documentURLEmpty);
                }

                if (internalBtn && (defaultID == null || defaultID.isEmpty())) {
                    return ValidationStatus.error(Messages.error_documentDefaultIDEmpty);
                }

                return ValidationStatus.ok();
            }
        };

        emfDataBindingContext.addValidationStatusProvider(multiValidator);
    }

    private void bindDocumentDefaultValueID() {

        documentInternalIDObserved = EMFObservables.observeValue(document,
                ProcessPackage.Literals.DOCUMENT__DEFAULT_VALUE_ID_OF_DOCUMENT_STORE);

        emfDataBindingContext.bindValue(
                SWTObservables.observeDelayedValue(500, SWTObservables.observeText(documentTextId, SWT.Modify)),
                documentInternalIDObserved);

    }

    private void bindDocumentURL() {

        documentUrlViewer.addExpressionValidator(ExpressionConstants.ALL_TYPES, new IExpressionValidator() {

            @Override
            public IStatus validate(final Object arg0) {


                final IStatus out = externalValidator.validate(document);

                final String actualErrorMessage = getErrorMessage();
                if (!out.equals(ValidationStatus.ok())) {
                    if (actualErrorMessage == null) {
                        setErrorMessage(out.getMessage());
                    }
                    setPageComplete(false);
                    return out;
                } else {
                    if (actualErrorMessage != null && !actualErrorMessage.equals(Messages.error_documentURLEmpty)) {
                        setPageComplete(false);
                    } else {
                        setErrorMessage(null);
                        setPageComplete(true);
                    }
                    return ValidationStatus.ok();
                }
            }

            @Override
            public void setInputExpression(final Expression inputExpression) {
            }

            @Override
            public void setDomain(final EditingDomain domain) {
            }

            @Override
            public void setContext(final EObject context) {
            }
        });

        externalUrlObserved = EMFObservables.observeValue(document, ProcessPackage.Literals.DOCUMENT__URL);

        externalInitialContentObserveWidget = ViewerProperties.singleSelection().observeDelayed(500, documentUrlViewer);
        emfDataBindingContext.bindValue(
                externalInitialContentObserveWidget,
                externalUrlObserved);

        documentUrlViewer.setInput(document);
    }

    private void bindDocumentMIMEType() {
        final IObservableValue mimeTypeObserved = EMFObservables.observeValue(document,
                ProcessPackage.Literals.DOCUMENT__MIME_TYPE);

        emfDataBindingContext.bindValue(
                ViewerProperties.singleSelection().observe(documentMimeTypeViewer),
                mimeTypeObserved);
        documentMimeTypeViewer.setInput(document);
    }

    private void bindDocumentDescription() {
        final IObservableValue descriptionObserved = EMFObservables.observeValue(
                document,
                ProcessPackage.Literals.ELEMENT__DOCUMENTATION);
        emfDataBindingContext.bindValue(SWTObservables
                .observeDelayedValue(500, SWTObservables.observeText(
                        documentDescriptionText, SWT.Modify)),
                        descriptionObserved);
    }

    private void bindDocumentName() {

        final UpdateValueStrategy targetToModel = new UpdateValueStrategy();

        targetToModel.setAfterGetValidator(new InputLengthValidator(
                Messages.name, 50));
        targetToModel.setBeforeSetValidator(new GroovyReferenceValidator(
                Messages.name, false));
        targetToModel.setAfterConvertValidator(new DocumentNameValidator(context, document != null ? document.getName() : null));
        nameObserved = EMFObservables.observeValue(document, ProcessPackage.Literals.ELEMENT__NAME);
        emfDataBindingContext.bindValue(
                SWTObservables.observeDelayedValue(500, SWTObservables.observeText(documentNameText, SWT.Modify)),
                nameObserved,
                targetToModel,
                null);

    }

    private void bindDocumentType() {
        final SelectObservableValue documentTypeObservableValue = new SelectObservableValue(ProcessPackage.DOCUMENT_TYPE);

        btnDocumentTypeNone = SWTObservables.observeSelection(radioButtonNone);
        documentTypeObservableValue.addOption(DocumentType.NONE, btnDocumentTypeNone);

        btnDocumentTypeExternal = SWTObservables.observeSelection(radioButtonExternal);
        documentTypeObservableValue.addOption(DocumentType.EXTERNAL, btnDocumentTypeExternal);
        documentTypeObservableValue.addValueChangeListener(new IValueChangeListener() {

            @Override
            public void handleValueChange(final ValueChangeEvent arg0) {
                if (arg0.diff.getNewValue().equals(DocumentType.EXTERNAL)) {
                    document.setDocumentType(DocumentType.EXTERNAL);
                }
                documentUrlViewer.validate();
            }
        });

        btnDocumentTypeInternal = SWTObservables.observeSelection(radioButtonInternal);
        documentTypeObservableValue.addOption(DocumentType.INTERNAL, btnDocumentTypeInternal);

        emfDataBindingContext.bindValue(
                documentTypeObservableValue,
                PojoObservables.observeValue(document, "documentType"));

    }

    protected void resetDatabindingContext() {
        if (emfDataBindingContext != null) {
            emfDataBindingContext.dispose();
        }
        emfDataBindingContext = new EMFDataBindingContext();
    }

    public  EObject getContext(){
        return context;
    }

    public Document getDocument(){
        return document;
    }

    @Override
    public void dispose() {
        if(pageSupport != null){
            pageSupport.dispose();
        }
        if (emfDataBindingContext != null) {
            emfDataBindingContext.dispose();
            emfDataBindingContext = null;
        }
        super.dispose();
    }

    protected void updateStack(final String type) {
        if (type.equals(NONE)) {
            stack.topControl = noneCompo;
        } else if (type.equals(EXTERNAL)) {
            stack.topControl = externalCompo;
        } else if (type.equals(INTERNAL)) {
            stack.topControl = internalCompo;
        }
        propertiesComposite.layout();
    }

    protected void updateMimeTypeStack(final String type) {
        if (type.equals(LINK)) {
            mimeStack.topControl = manageLinkComposition;
            mimeTypeLabel.setVisible(false);
            cd.hide();
        } else if (type.equals(FIELD)) {
            mimeStack.topControl = mimeTypeComposition;
            mimeTypeLabel.setVisible(true);
            cd.show();
        }
        mimeCompo.layout();
    }



}
