package com.intellij.uiDesigner.projectView;

import com.intellij.ide.IdeBundle;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ProjectViewNode;
import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.projectView.impl.nodes.ClassTreeNode;
import com.intellij.ide.projectView.impl.nodes.PsiFileNode;
import com.intellij.ide.projectView.impl.nodes.BasePsiNode;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vcs.FileStatus;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;

public class FormNode extends ProjectViewNode<Form>{
  private final Collection<BasePsiNode<? extends PsiElement>> myChildren;

  public FormNode(Project project, Object value, ViewSettings viewSettings) {
    super(project, (Form) value,  viewSettings);
    myChildren = getChildren(project, (Form) value, viewSettings);
  }

  public FormNode(Project project, Form value, ViewSettings viewSettings,
                  Collection<BasePsiNode<? extends PsiElement>> children) {
    super(project, value, viewSettings);
    myChildren = children;
  }

  @NotNull
  public Collection<BasePsiNode<? extends PsiElement>> getChildren() {
    return myChildren;
  }

  public String getTestPresentation() {
    return "Form:" + getValue().getName();
  }

  public boolean contains(@NotNull VirtualFile file) {
    for (final AbstractTreeNode aMyChildren : myChildren) {
      ProjectViewNode treeNode = (ProjectViewNode)aMyChildren;
      if (treeNode.contains(file)) return true;
    }
    return false;
  }

  public void update(PresentationData presentation) {
    if (getValue() == null || !getValue().isValid()) {
      setValue(null);
    } else {
      presentation.setPresentableText(getValue().getName());
      presentation.setIcons(StdFileTypes.GUI_DESIGNER_FORM.getIcon());
    }
  }

  public void navigate(final boolean requestFocus) {
    getValue().navigate(requestFocus);
  }

  public boolean canNavigate() {
    return getValue().canNavigate();
  }

  public boolean canNavigateToSource() {
    return getValue().canNavigateToSource();
  }

  public String getToolTip() {
    return IdeBundle.message("tooltip.ui.designer.form");
  }

  @Override
  public FileStatus getFileStatus() {
    for(BasePsiNode<? extends PsiElement> child: myChildren) {
      if (!child.getValue().isValid()) continue;
      final FileStatus fileStatus = child.getFileStatus();
      if (fileStatus != FileStatus.NOT_CHANGED) {
        return fileStatus;
      }
    }
    return FileStatus.NOT_CHANGED;
  }

  public static AbstractTreeNode constructFormNode(final PsiClass classToBind, final Project project, final ViewSettings settings) {
    final Form form = new Form(classToBind);
    final Collection<BasePsiNode<? extends PsiElement>> children = getChildren(project, form, settings);
    return new FormNode(project, form, settings, children);
  }

  private static Collection<BasePsiNode<? extends PsiElement>> getChildren(final Project project, final Form form, final ViewSettings settings) {
    final HashSet<BasePsiNode<? extends PsiElement>> children = new HashSet<BasePsiNode<? extends PsiElement>>();
    for (PsiFile formBoundToClass : form.getFormFiles()) {
      children.add(new PsiFileNode(project, formBoundToClass, settings));
    }
    children.add(new ClassTreeNode(project, form.getClassToBind(), settings));
    return children;
  }
}
