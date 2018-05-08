package org.mule.tooling.lang.dw.refactor;


import com.intellij.lang.refactoring.RefactoringSupportProvider;
import com.intellij.psi.PsiElement;
import com.intellij.refactoring.RefactoringActionHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mule.tooling.lang.dw.parser.psi.WeaveNamedElement;

public class WeaveRefactoringSupportProvider extends RefactoringSupportProvider {

  @Nullable
  @Override
  public RefactoringActionHandler getIntroduceConstantHandler() {
    return new IntroduceConstantHandler();
  }

  @Nullable
  @Override
  public RefactoringActionHandler getIntroduceVariableHandler() {
    return super.getIntroduceVariableHandler();
  }

  @Nullable
  @Override
  public RefactoringActionHandler getIntroduceVariableHandler(PsiElement element) {
    return super.getIntroduceVariableHandler(element);
  }

  @Override
  public boolean isSafeDeleteAvailable(@NotNull PsiElement element) {
    return element instanceof WeaveNamedElement;
  }

  @Override
  public boolean isMemberInplaceRenameAvailable(PsiElement element, PsiElement context) {
    return element instanceof WeaveNamedElement;
  }
}
