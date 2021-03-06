package org.mule.tooling.lang.dw.launcher.configuration.ui;

import com.intellij.application.options.ModulesComboBox;
import org.mule.tooling.lang.dw.ui.WeaveNameIdentifierSelector;

import javax.swing.*;

public class WeaveTestRunnerConfPanel {
  private JPanel mainPanel;
  private ModulesComboBox modules;
  private WeaveNameIdentifierSelector testField;

  public WeaveTestRunnerConfPanel() {
    modules.addItemListener(e -> testField.setModule(modules.getSelectedModule()));
  }

  public WeaveNameIdentifierSelector getTestField() {
    return testField;
  }

  public JPanel getMainPanel() {
    return mainPanel;
  }

  public ModulesComboBox getModuleCombo() {
    return modules;
  }
}
