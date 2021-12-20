package com.github.jk1.ytplugin.timeTracker;

import com.github.jk1.ytplugin.ComponentAware;
import com.github.jk1.ytplugin.tasks.YouTrackServer;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AllSavedTimerItemsDialog extends DialogWrapper {
    private JPanel contentPane;
    private JCheckBox selectAllCheckBox;
    private JPanel panel1;
    private JScrollPane scrollPane1;

    private JBTable timeTrackerItemsTable;

    PostAction postAction = new PostAction();
    RemoveAction removeAction = new RemoveAction();

    @NotNull
    private final Project project;
    private final YouTrackServer repo;

    public AllSavedTimerItemsDialog(@NotNull Project project, @NotNull YouTrackServer repo) {
        super(project, true);
        this.project = project;
        this.repo = repo;
        setTitle("Tracked Time");
        $$$setupUI$$$();

        addSelectAllFeatureToCheckBox();
        addSelectAllFeatureToTheTable();
        setActionsEnabled();
        init();
    }

    private void setActionsEnabled() {
        postAction.setEnabled(timeTrackerItemsTable != null &&
                pickSelectedTimeTrackerItemsOnly(timeTrackerItemsTable).size() != 0);
        removeAction.setEnabled(timeTrackerItemsTable != null &&
                pickSelectedTimeTrackerItemsOnly(timeTrackerItemsTable).size() != 0);
    }

    private void addSelectAllFeatureToCheckBox() {
        selectAllCheckBox.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                if (selectAllCheckBox.isSelected()) {
                    for (int i = 0; i < timeTrackerItemsTable.getRowCount(); i++)
                        timeTrackerItemsTable.getModel().setValueAt(true, i, 0);
                } else {
                    for (int i = 0; i < timeTrackerItemsTable.getRowCount(); i++)
                        timeTrackerItemsTable.getModel().setValueAt(false, i, 0);
                }
                setActionsEnabled();
            }

            @Override
            public void mousePressed(MouseEvent mouseEvent) {
            }

            @Override
            public void mouseReleased(MouseEvent mouseEvent) {
            }

            @Override
            public void mouseEntered(MouseEvent mouseEvent) {
            }

            @Override
            public void mouseExited(MouseEvent mouseEvent) {
            }
        });
    }

    private void addSelectAllFeatureToTheTable() {

        for (int i = 0; i < timeTrackerItemsTable.getRowCount(); i++) {
            int rowNum = i;
            timeTrackerItemsTable.getCellEditor(i, 0).addCellEditorListener(new CellEditorListener() {
                @Override
                public void editingStopped(ChangeEvent e) {
                    if (!(Boolean) timeTrackerItemsTable.getCellEditor(rowNum, 0).getCellEditorValue()) {
                        selectAllCheckBox.setSelected(false);
                    }
                    postAction.setEnabled(pickSelectedTimeTrackerItemsOnly(timeTrackerItemsTable).size() != 0);
                    removeAction.setEnabled(pickSelectedTimeTrackerItemsOnly(timeTrackerItemsTable).size() != 0);
                }

                @Override
                public void editingCanceled(ChangeEvent e) {
                }
            });
        }
    }


    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        createUIComponents();
        contentPane = new JPanel();
        contentPane.setLayout(new GridLayoutManager(3, 1, new Insets(10, 10, 10, 10), -1, -1));
        contentPane.setMinimumSize(new Dimension(426, 483));
        contentPane.setPreferredSize(new Dimension(426, 483));
        contentPane.setBorder(BorderFactory.createTitledBorder(null, "", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel2.putClientProperty("html.disable", Boolean.TRUE);
        contentPane.add(panel2, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, new Dimension(300, 300), null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel2.add(spacer1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel1.add(scrollPane1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        selectAllCheckBox = new JCheckBox();
        selectAllCheckBox.setSelected(true);
        selectAllCheckBox.setText("Select all");
        contentPane.add(selectAllCheckBox, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
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

    @Override
    protected Action @NotNull [] createActions() {
        return new Action[]{postAction, removeAction, getCancelAction()};
    }

    private void createUIComponents() {
        SpentTimePerTaskStorage storage = ComponentAware.Companion.of(project).getSpentTimePerTaskStorage();
        scrollPane1 = createTable(storage);
    }

    private JScrollPane createTable(SpentTimePerTaskStorage storage) {

        ConcurrentHashMap<String, Long> timerItems = storage.getAllStoredItems();
        int columns = 3;
        int i = 0;

        // form data structure
        Object[][] timerData = new Object[timerItems.size()][columns];
        for (Map.Entry<String, Long> entry : timerItems.entrySet()) {
            String id = entry.getKey();
            String time = TimeTracker.Companion.formatTimePeriod(entry.getValue());

            timerData[i][0] = true;
            timerData[i][1] = id;
            timerData[i][2] = time + " min";

            i++;
        }

        Object[] columnsHeaders = new String[]{"Selected", "Issue", "Time"};

        DefaultTableModel model = new DefaultTableModel(timerData, columnsHeaders) {
            @Override
            public Class getColumnClass(int column) {
                return getValueAt(0, column).getClass();
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return column != 1 && column != 2;
            }
        };
        timeTrackerItemsTable = new JBTable(model);

        return new JBScrollPane(timeTrackerItemsTable);
    }

    protected ConcurrentHashMap<String, Long> pickSelectedTimeTrackerItemsOnly(JBTable table) {
        ConcurrentHashMap<String, Long> selectedItems = new ConcurrentHashMap<>();
        SpentTimePerTaskStorage storage = ComponentAware.Companion.of(project).getSpentTimePerTaskStorage();

        for (int i = 0; i < table.getRowCount(); i++) {
            // If time tracking item is selected, put issue id and time to map
            if ((Boolean) table.getValueAt(i, 0)) {
                String id = (String) table.getValueAt(i, 1);
                // not to do time representation parsing from table we use SpentTimePerTaskStorage
                selectedItems.put(id, storage.getSavedTimeForLocalTask(id));
            }
        }
        return selectedItems;
    }

    protected class RemoveAction extends DialogWrapperAction {
        protected RemoveAction() {
            super("Discard");
        }

        @Override
        protected void doAction(ActionEvent e) {
            ConcurrentHashMap<String, Long> selectedItems = pickSelectedTimeTrackerItemsOnly(timeTrackerItemsTable);
            SpentTimePerTaskStorage storage = ComponentAware.Companion.of(project).getSpentTimePerTaskStorage();
            selectedItems.forEach((task, time) -> {
                storage.resetSavedTimeForLocalTask(task);
                TrackerNotification trackerNote = new TrackerNotification();
                trackerNote.notify("Discarded " + TimeTracker.Companion.formatTimePeriod(time) +
                        " min of tracked time for " + task, NotificationType.INFORMATION);
            });

            close(0);
        }

    }

    protected class PostAction extends DialogWrapperAction {
        protected PostAction() {
            super("Post to YouTrack");
        }

        @Override
        protected void doAction(ActionEvent e) {
            ConcurrentHashMap<String, Long> selectedItems = pickSelectedTimeTrackerItemsOnly(timeTrackerItemsTable);

            new TimeTrackerConnector(repo, project).postSavedWorkItemsToServer(selectedItems);
            close(0);
        }
    }
}

