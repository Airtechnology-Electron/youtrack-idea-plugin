package com.github.jk1.ytplugin.timeTracker;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.github.jk1.ytplugin.issues.model.Issue;
import com.github.jk1.ytplugin.rest.CustomAttributesClient;
import com.github.jk1.ytplugin.tasks.YouTrackServer;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.tasks.TaskManager;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.michaelbaranov.microba.calendar.DatePicker;
import com.github.jk1.ytplugin.ComponentAware;
import java.text.*;
import java.util.*;

import com.intellij.openapi.project.Project;

public class ManualTimeEntryDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JSpinner hourSpinner;
    private JSpinner minutesSpinner;
    private JComboBox issueComboBox;
    private JComboBox typeComboBox;
    private JTextField commentField;
    private JPanel rootPanel;
    private JPanel buttonsPanel;
    private JPanel mainPanel;
    private JPanel generalPanel;
    private JPanel commentPanel;
    private JPanel typePanel;
    private JPanel issuePanel;
    private JLabel hoursLabel;
    private JLabel minutesLabel;
    private JLabel timeLabel;
    private JLabel issueLabel;
    private JLabel typeLabel;
    private JLabel commentLabel;
    private JLabel dateLabel;
    private JBLabel notifier;
    private JPanel notifyPanel;
    private JPanel customWorkItemsPanel;
    ;
    private DatePicker datePicker;

    Logger logger = Logger.getInstance("com.github.jk1.ytplugin");

    Project project;
    private List<Issue> ids;
    private List<String> tasksIdRepresentation = new ArrayList<>();
    private List<String> tasksIds = new ArrayList<>();
    YouTrackServer repo;

    private final int mandatoryComponentsCount = 13; // required to maintain the default state of the dialog, without custom attributes
    private final int mandatoryRowsCount = 5;
    private int attributeRow = mandatoryRowsCount;

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */

    public ManualTimeEntryDialog(Project project, YouTrackServer repo) {

        this.project = project;
        this.repo = repo;
        $$$setupUI$$$();

        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    onOK();
                } catch (ExecutionException ex) {
                    ex.printStackTrace();
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() throws ExecutionException, InterruptedException {
        String hours = hourSpinner.getValue().toString();
        String minutes = minutesSpinner.getValue().toString();
        Long time = TimeUnit.HOURS.toMinutes(Long.parseLong(hours)) + Long.parseLong(minutes);

        if (datePicker.getDate() == null) {
            notifier.setForeground(JBColor.RED);
            notifier.setText("Date is not specified");
        } else {
            try {
                int selectedIssueIndex = issueComboBox.getSelectedIndex();
                Future<?> future = new TimeTrackingConfigurator().checkIfTrackingIsEnabledForIssue(repo, selectedIssueIndex, ids);

                Object result = future.get();
                if (!(result instanceof Boolean)) {
                    notifier.setForeground(JBColor.RED);
                    notifier.setText("Unable to save time, please check your connection");
                    logger.debug("Error when posting time tracker item");
                } else if (!((Boolean) future.get())) {
                    if (issueComboBox.getSelectedIndex() == -1) {
                        notifier.setForeground(JBColor.RED);
                        notifier.setText("Please select the issue");
                        logger.debug("Issue is not selected or there are no issues in the list");
                    }
                } else {
                    String selectedId = ids.get(issueComboBox.getSelectedIndex()).getIssueId();

                    ApplicationManager.getApplication().invokeAndWait(() -> {
                        Map<String, String> attributes = getAttributes();
                    });

                    Future<Integer> futureCode = new TimeTrackerConnector(repo, project)
                            .addWorkItemManually(format(datePicker.getDate()),
                            typeComboBox.getItemAt(typeComboBox.getSelectedIndex()).toString(), selectedId,
                                    commentField.getText(), time.toString(), notifier);

                    if (futureCode.get() == 200) {
                        dispose();
                    }
                }
            } catch (IndexOutOfBoundsException e) {
                notifier.setForeground(JBColor.RED);
                notifier.setText("Please select the issue");
                logger.debug("Issue is not selected or there are no issues in the list:" + e.getMessage());
            }
        }
    }

    private Map<String, String> getAttributes() {
        int componentsCount = generalPanel.getComponentCount();
        Map<String, String> attributes = new HashMap<>(Collections.emptyMap());
        int numOfCustomAttributes = attributeRow - mandatoryRowsCount;
        for (int i = 1; i <= numOfCustomAttributes * 2; i += 2) {
            // according to the form layout
            JPanel attributePanel = (JPanel)generalPanel.getComponent(componentsCount - i - 1);
            String attributeName = ((JLabel)generalPanel.getComponent(componentsCount - i)).getText();
            JComboBox valueComboBox = (JComboBox)attributePanel.getComponent(0);
            attributes.put(attributeName, valueComboBox.getSelectedItem().toString());
        }
        return attributes;
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }


    String format(Date date) {
        return new SimpleDateFormat("dd MMM yyyy HH:mm").format(date);
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
        contentPane.setMinimumSize(new Dimension(500, 290));
        contentPane.setPreferredSize(new Dimension(500, 290));
        contentPane.setRequestFocusEnabled(true);
        contentPane.putClientProperty("html.disable", Boolean.FALSE);
        rootPanel = new JPanel();
        rootPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(rootPanel, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        rootPanel.add(buttonsPanel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_SOUTH, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        buttonOK = new JButton();
        buttonOK.setText("OK");
        buttonsPanel.add(buttonOK, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buttonCancel = new JButton();
        buttonCancel.setText("Cancel");
        buttonsPanel.add(buttonCancel, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        buttonsPanel.add(spacer1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(mainPanel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        mainPanel.add(generalPanel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        commentPanel = new JPanel();
        commentPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        generalPanel.add(commentPanel, new GridConstraints(3, 1, 1, 11, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        commentField = new JTextField();
        commentPanel.add(commentField, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        typePanel = new JPanel();
        typePanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        generalPanel.add(typePanel, new GridConstraints(2, 1, 1, 11, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        typePanel.add(typeComboBox, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        issuePanel = new JPanel();
        issuePanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        generalPanel.add(issuePanel, new GridConstraints(1, 1, 1, 11, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        issuePanel.add(issueComboBox, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        generalPanel.add(hourSpinner, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        timeLabel = new JLabel();
        timeLabel.setText("Spent time: ");
        generalPanel.add(timeLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        issueLabel = new JLabel();
        issueLabel.setText("Issue: ");
        generalPanel.add(issueLabel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        typeLabel = new JLabel();
        typeLabel.setText("Work type: ");
        generalPanel.add(typeLabel, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        commentLabel = new JLabel();
        commentLabel.setText("Comment:");
        generalPanel.add(commentLabel, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        dateLabel = new JLabel();
        dateLabel.setText("Date: ");
        generalPanel.add(dateLabel, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        hoursLabel = new JLabel();
        hoursLabel.setText("hours");
        generalPanel.add(hoursLabel, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        generalPanel.add(minutesSpinner, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        minutesLabel = new JLabel();
        minutesLabel.setText("minutes");
        generalPanel.add(minutesLabel, new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        notifyPanel = new JPanel();
        notifyPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(notifyPanel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        notifier = new JBLabel();
        notifier.setText("");
        notifyPanel.add(notifier, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }

    private void createUIComponents() {
        this.setTitle("Add Spent Time");
        datePicker = new DatePicker(new Date());
        createGeneralPanel();
        createIssueIdPanel();
        createTypeComboBox();
        createSpinners();
    }

    private void createGeneralPanel() {
        generalPanel = new JPanel();
        // rowCount to 10 as an assumption that there won't be more than 4 custom attributes
        generalPanel.setLayout(new GridLayoutManager(10, 12, new Insets(0, 0, 0, 0), -1, -1));
        generalPanel.add(datePicker, new GridConstraints(4, 1, 1, 11, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));

    }

    private void createSpinners() {
        SpinnerModel hoursModel = new SpinnerNumberModel(2,  //initial value
                0,  //min
                100,  //max
                1);
        hourSpinner = new JSpinner(hoursModel);
        hourSpinner.setEditor(new JSpinner.NumberEditor(hourSpinner, "00"));

        SpinnerModel minutesModel = new SpinnerNumberModel(0,  //initial value
                0,  //min
                60,  //max
                1);
        minutesSpinner = new JSpinner(minutesModel);
        minutesSpinner.setEditor(new JSpinner.NumberEditor(minutesSpinner, "00"));


    }

    private void createIssueIdPanel() {

        this.ids = ComponentAware.Companion.of(project).getIssueStoreComponent().get(repo).getAllIssues();

        for (Issue id : ids) {
            tasksIdRepresentation.add(id.getIssueId() + ": " + id.getSummary());
            tasksIds.add(id.getIssueId());
        }

        issueComboBox = new ComboBox(tasksIdRepresentation.toArray());
        issueComboBox.setSelectedIndex(tasksIds.indexOf(TaskManager.getManager(project).getActiveTask().getId()));

        issueComboBox.addActionListener(e -> {
            // assume that project ID ALWAYS does not have '-'
            String projectId =  ids.get(issueComboBox.getSelectedIndex()).getIssueId().split("-")[0];
            Map<String, List<String>> attributes =
                    new CustomAttributesClient(repo).getCustomAttributesForProjectInCallable(projectId);
            handleAttributes(attributes);
            contentPane.updateUI();
        });
    }

    private void handleAttributes(Map<String, List<String>> attributes) {
        removeOldCustomAttributes();
        attributeRow = mandatoryRowsCount;

        for (Map.Entry<String, List<String>> entry : attributes.entrySet()) {
            createAttributePanel(attributeRow, entry.getKey(),entry.getValue() );
            attributeRow++;
        }
    }

    private void removeOldCustomAttributes(){
        ApplicationManager.getApplication().invokeAndWait(() -> {
            int componentsToDelete = (attributeRow - mandatoryRowsCount) * 2; // label + comboBox in a row
            int componentsCount =  generalPanel.getComponentCount();
            if (componentsCount > mandatoryComponentsCount){
                for(int i = 1; i <= componentsToDelete; i++){
                    generalPanel.remove(componentsCount - i);
                }
            }
        });

        generalPanel.validate();
        generalPanel.repaint();
    }

    private void createAttributePanel(int row, String attributeName, List<String> attributeValues) {
        customWorkItemsPanel = new JPanel();
        customWorkItemsPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        ComboBox customWorkItemComboBox = new ComboBox(attributeValues.toArray());
        // TODO how to access values
        JLabel customWorkItemLabel = new JLabel(attributeName);
        customWorkItemsPanel.add(customWorkItemComboBox, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        generalPanel.add(customWorkItemsPanel, new GridConstraints(row, 1, 1, 11, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        generalPanel.add(customWorkItemLabel, new GridConstraints(row, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
    }

    private void createTypeComboBox() {

        TimeTrackingConfigurator timerService = new TimeTrackingConfigurator();

        List<String> types = timerService.getTypesInCallable(repo);
        TimeTracker timer = ComponentAware.Companion.of(project).getTimeTrackerComponent();
        typeComboBox = new ComboBox(types.toArray());
        var idx = 0;

        try {
            typeComboBox.setSelectedIndex(0);
            if (!types.isEmpty()) {
                ListIterator<String> iter = types.listIterator();

                while (iter.hasNext()) {
                    if (Objects.equals(iter.next(), timer.getType())) {
                        idx = iter.nextIndex();
                    }
                }
                typeComboBox.setSelectedIndex(idx);
            }
        } catch (IllegalArgumentException exp) {
            typeComboBox.setSelectedIndex(-1);
            logger.warn("Failed to fetch work items types");
        }
        typeComboBox.setEditable(true);
    }

}
