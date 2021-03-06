package org.mule.tooling.lang.dw.refactor.ui;

//import com.intellij.openapi.editor.event.DocumentEvent;

import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.psi.PsiType;
import com.intellij.refactoring.ui.MethodSignatureComponent;
import com.intellij.refactoring.util.AbstractParameterTablePanel;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.TitledSeparator;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.util.ui.ColumnInfo;
import org.jetbrains.annotations.Nullable;
import org.mule.tooling.lang.dw.WeaveFileType;
import org.mule.tooling.lang.dw.refactor.utils.WeaveArgumentInfo;
import org.mule.tooling.lang.dw.refactor.utils.WeaveRefactorFunctionData;
import org.mule.weave.v2.sdk.NameIdentifierHelper;
import org.mule.weave.v2.ts.WeaveType;
import scala.Option;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

import static org.mule.tooling.lang.dw.refactor.utils.RefactorUtils.calculateSignature;


public class WeaveExtractFunctionDialog extends DialogWrapper {


    private String REFACTORING_NAME = "Extract Function";
    private JPanel contentPane;
    private JPanel methodNamePanel;
    private EditorTextField functionNameTextField;
    private JPanel inputParametersPanel;
    private JPanel previewPanel;
    private JCheckBox returnTypeEnabledCb;
    private JLabel nameLabel;
    private JCheckBox parameterTypeEnabledCb;

    private final MethodSignatureComponent mySignaturePreview;
    private Option<WeaveType> myHasReturn;
    private WeaveParameterTablePanel parameterTablePanel;
    private Boolean initialized = false;

    public WeaveExtractFunctionDialog(final Project project, Option<WeaveType> hasReturn, WeaveArgumentInfo[] input, String initialMethodName) {
        super(project, true);
        myHasReturn = hasReturn;
        mySignaturePreview = new MethodSignatureComponent("", project, WeaveFileType.getInstance());
        mySignaturePreview.setMinimumSize(new Dimension(500, 70));
        setModal(true);
        setTitle(REFACTORING_NAME);
        init();


        parameterTablePanel = new WeaveParameterTablePanel(input);
        inputParametersPanel.add(parameterTablePanel);
        functionNameTextField.setText(initialMethodName);

        previewPanel.setLayout(new BorderLayout());
        previewPanel.add(mySignaturePreview, BorderLayout.CENTER);

        nameLabel.setLabelFor(functionNameTextField);
        functionNameTextField.addDocumentListener(new DocumentListener() {
            @Override
            public void documentChanged(DocumentEvent event) {
                updateMethodPreview();
            }
        });

        functionNameTextField.setInputVerifier(new InputVerifier() {
            @Override
            public boolean verify(JComponent input) {
                return NameIdentifierHelper.isValidIdentifier(((EditorTextField) input).getText());
            }
        });

        parameterTypeEnabledCb.setSelected(true);

        returnTypeEnabledCb.addChangeListener(e -> updateMethodPreview());
        parameterTypeEnabledCb.addChangeListener(e -> updateMethodPreview());

        initialized = true;
        updateMethodPreview();

    }

    public boolean isInitialized() {
        return initialized;
    }

    private void updateMethodPreview() {
        mySignaturePreview.setSignature(calculateSignature(calculateFunctionData()));
    }

    public WeaveRefactorFunctionData calculateFunctionData() {
        final String functionName = functionNameTextField.getText();
        return new WeaveRefactorFunctionData(functionName, Arrays.asList(parameterTablePanel.getVariableData()), myHasReturn, parameterTypeEnabledCb.isSelected(), returnTypeEnabledCb.isSelected());
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        return functionNameTextField;
    }

    @Override
    protected JComponent createCenterPanel() {
        return contentPane;
    }

    @Override
    protected JComponent createContentPane() {
        return contentPane;
    }


    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        contentPane = new JPanel();
        contentPane.setLayout(new GridLayoutManager(5, 1, new Insets(0, 0, 0, 0), -1, -1));
        methodNamePanel = new JPanel();
        methodNamePanel.setLayout(new GridLayoutManager(3, 3, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(methodNamePanel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        nameLabel = new JLabel();
        nameLabel.setText("Name");
        nameLabel.setDisplayedMnemonic('N');
        nameLabel.setDisplayedMnemonicIndex(0);
        methodNamePanel.add(nameLabel, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        functionNameTextField = new EditorTextField();
        functionNameTextField.setText("");
        methodNamePanel.add(functionNameTextField, new GridConstraints(2, 1, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(5, 1, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        inputParametersPanel = new JPanel();
        inputParametersPanel.setLayout(new BorderLayout(0, 0));
        panel1.add(inputParametersPanel, new GridConstraints(0, 0, 3, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final TitledSeparator titledSeparator1 = new TitledSeparator();
        titledSeparator1.setText("Parameters");
        inputParametersPanel.add(titledSeparator1, BorderLayout.NORTH);
        previewPanel = new JPanel();
        previewPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(previewPanel, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final TitledSeparator titledSeparator2 = new TitledSeparator();
        titledSeparator2.setFocusable(true);
        titledSeparator2.setText("Signature preview");
        contentPane.add(titledSeparator2, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panel2.setAlignmentX(0.0f);
        panel2.setAlignmentY(0.0f);
        contentPane.add(panel2, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        returnTypeEnabledCb = new JCheckBox();
        returnTypeEnabledCb.setAlignmentY(0.0f);
        returnTypeEnabledCb.setBorderPainted(false);
        returnTypeEnabledCb.setBorderPaintedFlat(false);
        returnTypeEnabledCb.setContentAreaFilled(false);
        returnTypeEnabledCb.setHideActionText(false);
        returnTypeEnabledCb.setHorizontalAlignment(0);
        returnTypeEnabledCb.setHorizontalTextPosition(11);
        returnTypeEnabledCb.setInheritsPopupMenu(false);
        returnTypeEnabledCb.setMargin(new Insets(0, 0, 0, 0));
        returnTypeEnabledCb.setText("Specify result type");
        returnTypeEnabledCb.setMnemonic('T');
        returnTypeEnabledCb.setDisplayedMnemonicIndex(15);
        panel2.add(returnTypeEnabledCb);

    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }

    private static class TypeColumnInfo extends ColumnInfo<WeaveArgumentInfo, String> {

        public TypeColumnInfo() {
            super("Type");
        }

        @Nullable
        @Override
        public String valueOf(WeaveArgumentInfo weaveArgumentInfo) {
            return weaveArgumentInfo.getWtype().isDefined() ? weaveArgumentInfo.getWtype().get().toString(false, true) : "<Not Defined>";
        }

        @Override
        public Class<?> getColumnClass() {
            return PsiType.class;
        }

        @Override
        public boolean isCellEditable(WeaveArgumentInfo data) {
            return false;
        }
    }

    private class WeaveParameterTablePanel extends AbstractParameterTablePanel<WeaveArgumentInfo> {
        public WeaveParameterTablePanel(WeaveArgumentInfo[] variableData) {
            super(variableData, new NameColumnInfo(NameIdentifierHelper::isValidIdentifier), new TypeColumnInfo());
        }

        protected void updateSignature() {
            if (isInitialized()) {
                updateMethodPreview();
            }
        }

        protected void doEnterAction() {
        }

        protected void doCancelAction() {
        }
    }
}