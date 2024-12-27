package com.ultramega.timetracker.listeners;

import com.intellij.codeInsight.editorActions.TypedHandlerDelegate;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.EditorMouseEvent;
import com.intellij.openapi.editor.event.EditorMouseListener;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.ultramega.timetracker.TimeTrackerService;
import org.jetbrains.annotations.NotNull;

//TODO decide if mouse movement should cancel inactivity (EditorMouseMotionListener)
public class InputMouseListener extends TypedHandlerDelegate implements EditorMouseListener {
    @Override
    public void mousePressed(EditorMouseEvent e) {
        resetLastInputTime(e.getEditor().getProject());
    }

    @Override
    public void mouseReleased(EditorMouseEvent e) {
        resetLastInputTime(e.getEditor().getProject());
    }

    @Override
    public void mouseEntered(EditorMouseEvent e) {
        resetLastInputTime(e.getEditor().getProject());
    }

    @Override
    public void mouseExited(EditorMouseEvent e) {
        resetLastInputTime(e.getEditor().getProject());
    }

    @Override
    public @NotNull Result charTyped(char c, @NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file) {
        resetLastInputTime(editor.getProject());

        return super.charTyped(c, project, editor, file);
    }

    private void resetLastInputTime(Project project) {
        if (project != null) {
            TimeTrackerService service = project.getService(TimeTrackerService.class);
            service.resetLastInputTime();
        }
    }
}
