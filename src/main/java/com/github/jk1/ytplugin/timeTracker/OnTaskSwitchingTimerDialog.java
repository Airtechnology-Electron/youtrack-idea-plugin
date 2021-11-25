package com.github.jk1.ytplugin.timeTracker;

import com.github.jk1.ytplugin.ComponentAware;
import com.github.jk1.ytplugin.tasks.YouTrackServer;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.concurrent.ConcurrentHashMap;

public class OnTaskSwitchingTimerDialog extends DialogWrapper {
    private JPanel contentPane;
    private JLabel note;

    private Project project;
    private YouTrackServer repository;

    TimeTracker timer;
    SpentTimePerTaskStorage timePerTaskStorage;

    Logger logger = Logger.getInstance("com.github.jk1.ytplugin");

    PostAction postAction = new PostAction();
    ContinueAction continueAction = new ContinueAction();


    public OnTaskSwitchingTimerDialog(@NotNull Project project, YouTrackServer repository) {
        super(project, true);
        this.project = project;
        this.repository = repository;
        timer = ComponentAware.Companion.of(project).getTimeTrackerComponent();
        timePerTaskStorage = ComponentAware.Companion.of(project).getSpentTimePerTaskStorage();

        setTitle("Saved Time Tracking Items");
        $$$setupUI$$$();
        // todo: display not only in minutes
        Long savedTime = timePerTaskStorage.getSavedTimeForLocalTask(timer.getIssueId());
        String message = "<html>Time " + timer.Companion.formatTimePeriod(savedTime) +
                " min is recorded for the task " +  timer.getIssueId() + ".<br/>Do you wish to continue tracking or post " +
                "time to YouTrack and start a new entry?</html>";
        note.setText(message);

        init();
    }

    @Override
    protected Action @NotNull [] createActions() {
        return new Action[]{postAction, continueAction};
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
        contentPane.setLayout(new GridLayoutManager(1, 1, new Insets(10, 10, 10, 10), -1, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        note = new JLabel();
        note.setText("<html>Time ... is recorded for the task ... .<br/>Do you want to continue tracking or post time recorded to YouTrack and start new entry?</html>");
        panel1.add(note, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return contentPane;
    }

    protected class ContinueAction extends DialogWrapperAction {
        protected ContinueAction() {
            super("Continue");
        }

        @Override
        protected void doAction(ActionEvent e) {
            close(0);
        }
    }

    protected class PostAction extends DialogWrapperAction {
        protected PostAction() {
            super("Post to YouTrack");
        }

        @Override
        protected void doAction(ActionEvent e) {
            ConcurrentHashMap<String, Long> item = new ConcurrentHashMap<String, Long>() {
                {
                    this.put(timer.getIssueId(), timePerTaskStorage.getSavedTimeForLocalTask(timer.getIssueId()));
                }
            };

            logger.debug("Time to post on task switching: " + timer.getIssueId() + " for " +
                    timePerTaskStorage.getSavedTimeForLocalTask(timer.getIssueId()));

            new TimeTrackerConnector().postSavedTimeToServer(repository, project, item);
            close(0);
        }

    }

}