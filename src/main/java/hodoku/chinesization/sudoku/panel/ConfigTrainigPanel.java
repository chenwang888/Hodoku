/*
 * Copyright (C) 2008-12  Bernhard Hobiger
 *
 * This file is part of HoDoKu.
 *
 * HoDoKu is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * HoDoKu is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with HoDoKu. If not, see <http://www.gnu.org/licenses/>.
 */

package hodoku.chinesization.sudoku.panel;

import hodoku.chinesization.generator.BackgroundGeneratorThread;
import hodoku.chinesization.sudoku.Options;
import hodoku.chinesization.sudoku.StepConfig;
import hodoku.chinesization.sudoku.entity.CheckNode;
import hodoku.chinesization.sudoku.entity.CheckRenderer;
import hodoku.chinesization.upgrade.PropertyUtil;

import java.awt.Color;
import java.awt.Component;
import java.util.Enumeration;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
 *
 * @author  hobiwan
 */
public class ConfigTrainigPanel extends javax.swing.JPanel {
    private static final long serialVersionUID = 1L;
    private StepConfig[] steps;
    private DefaultListModel model;
    
    private boolean listView = true; // absichtlich verkehrt, damit stepTree initialisiert wird
    
    /** Creates new form ConfigSolverPanel */
    @SuppressWarnings("unchecked")
    public ConfigTrainigPanel() {
        initComponents();
        
        stepList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        stepList.setCellRenderer(new CheckBoxRenderer());
        model = new DefaultListModel();
        stepList.setModel(model);
        
        stepTree.setCellRenderer(new CheckRenderer());
        stepTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        stepTree.putClientProperty("JTree.lineStyle", "Angled");
        
        // Alle Werte aus den Default-Optionen setzen
        initAll(false);
        
        checkButtons(false);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        stepTree = new javax.swing.JTree();
        jPanel1 = new javax.swing.JPanel();
        resetButton = new javax.swing.JButton();
        chosenLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        chosenTextArea = new javax.swing.JTextArea();
        warningLabel = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        stepScrollPane = new javax.swing.JScrollPane();
        stepList = new JList();
        jToolBar1 = new javax.swing.JToolBar();
        listButton = new javax.swing.JToggleButton();
        treeButton = new javax.swing.JToggleButton();

        stepTree.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                stepTreeMousePressed(evt);
            }
        });

        resetButton.setMnemonic(PropertyUtil.getProperty("intl/ConfigTrainingPanel","ConfigTrainingPanel.resetButton.mnemonic").charAt(0));
        java.util.ResourceBundle bundle = PropertyUtil.loadResourceFile("intl/ConfigTrainingPanel"); // NOI18N
        resetButton.setText(bundle.getString("ConfigTrainingPanel.resetButton.text")); // NOI18N
        resetButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetButtonActionPerformed(evt);
            }
        });

        chosenLabel.setText(bundle.getString("ConfigTrainigPanel.chosenLabel.text")); // NOI18N

        chosenTextArea.setColumns(20);
        chosenTextArea.setEditable(false);
        chosenTextArea.setFont(new java.awt.Font("SansSerif", 0, 12));
        chosenTextArea.setLineWrap(true);
        chosenTextArea.setRows(5);
        chosenTextArea.setWrapStyleWord(true);
        jScrollPane1.setViewportView(chosenTextArea);

        warningLabel.setText(bundle.getString("ConfigTrainigPanel.warningLabel.text")); // NOI18N
        warningLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(warningLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 214, Short.MAX_VALUE)
                    .addComponent(resetButton)
                    .addComponent(chosenLabel, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 214, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(chosenLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 215, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(warningLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 146, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(resetButton))
        );

        jPanel4.setLayout(new java.awt.BorderLayout());

        stepList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                stepListMouseClicked(evt);
            }
        });
        stepScrollPane.setViewportView(stepList);

        jPanel4.add(stepScrollPane, java.awt.BorderLayout.CENTER);

        listButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/listview16b.png"))); // NOI18N
        listButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                listButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(listButton);

        treeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/treeview16b.png"))); // NOI18N
        treeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                treeButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(treeButton);

        jPanel4.add(jToolBar1, java.awt.BorderLayout.NORTH);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, 227, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 437, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    
    private void stepTreeMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_stepTreeMousePressed
        TreePath path = stepTree.getPathForLocation(evt.getX(), evt.getY());
        if (path == null) {
            return;
        }
        CheckNode act = (CheckNode)path.getLastPathComponent();
        if (act != null) {
            act.toggleSelectionState();
            chosenTextArea.setText(Options.getInstance().getTrainingStepsString(steps, false));
            stepTree.repaint();
        }
    }//GEN-LAST:event_stepTreeMousePressed
        
    private void treeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_treeButtonActionPerformed
        checkButtons(false);
    }//GEN-LAST:event_treeButtonActionPerformed
    
    private void listButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_listButtonActionPerformed
        checkButtons(true);
    }//GEN-LAST:event_listButtonActionPerformed
    
    private void resetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetButtonActionPerformed
        initAll(true);
    }//GEN-LAST:event_resetButtonActionPerformed
                    
    private void stepListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_stepListMouseClicked
        int index = stepList.locationToIndex(evt.getPoint());
        if (index == stepList.getSelectedIndex()) {
            StepConfig conf = (StepConfig) stepList.getSelectedValue();
            conf.setEnabledTraining(!conf.isEnabledTraining());
            chosenTextArea.setText(Options.getInstance().getTrainingStepsString(steps, false));
            stepList.repaint();
        }
    }//GEN-LAST:event_stepListMouseClicked
    
    public void okPressed() {
        // Alle Werte übernehmen
        // Alle Werte übernehmen
        // Caution: steps[] is shared by ConfigSolverPanel and ConfigFindAllStepsPanel
        // okPressed() in ConfigSolverPanel has to be called first, here only the values
        // for enabledTraining are set
        boolean somethingChanged = false;
        StepConfig[] orgSteps0 = Options.getInstance().solverSteps;
        for (int i = 0; i < steps.length; i++) {
            for (int j = 0; j < orgSteps0.length; j++) {
                if (steps[i].getType() == orgSteps0[j].getType() && orgSteps0[j].isEnabled()) {
                    if (orgSteps0[j].isEnabledTraining() != steps[i].isEnabledTraining()) {
                        somethingChanged = true;
                    }
                    orgSteps0[j].setEnabledTraining(steps[i].isEnabledTraining());
                    break;
                }
            }
        }
        StepConfig[] orgSteps1 = Options.getInstance().getOrgSolverSteps();
        for (int i = 0; i < steps.length; i++) {
            for (int j = 0; j < orgSteps1.length; j++) {
                if (steps[i].getType() == orgSteps1[j].getType() && orgSteps1[j].isEnabled()) {
                    orgSteps1[j].setEnabledTraining(steps[i].isEnabledTraining());
                    break;
                }
            }
        }
        if (somethingChanged) {
            BackgroundGeneratorThread.getInstance().resetTrainingPractising();
        }
    }
    
    @SuppressWarnings("unchecked")
    private void initAll(boolean setDefault) {
        // Zuerst die Daten zurücksetzen
        if (setDefault) {
            // CAUTION: Reset to default resets only enabledProgress and indexProgress
            steps = Options.getInstance().copyStepConfigs(Options.getInstance().solverSteps, true, false, false, false);
            StepConfig[] orgSteps = Options.DEFAULT_SOLVER_STEPS;
            for (int i = 0; i < steps.length; i++) {
                for (int j = 0; j < orgSteps.length; j++) {
                    if (steps[i].getType() == orgSteps[j].getType()) {
                        steps[i].setEnabledTraining(orgSteps[j].isEnabledTraining());
                        break;
                    }
                }
            }
//            steps = Options.getInstance().copyStepConfigs(Options.DEFAULT_SOLVER_STEPS, true, false, false, false);
        } else {
            steps = Options.getInstance().copyStepConfigs(Options.getInstance().solverSteps, true, false, false, false);
        }
        
        // Liste neu laden
        model.removeAllElements();
        for (int i = 0; i < steps.length; i++) {
            if (! steps[i].isEnabled()) {
                // only active steps!
                continue;
            }
            model.addElement(steps[i]);
        }
        stepList.setSelectedIndex(-1);
        stepList.ensureIndexIsVisible(0);
        stepList.repaint();
        
        // Baum neu laden
        buildTree();

        chosenTextArea.setText(Options.getInstance().getTrainingStepsString(steps, false));
    }
    
    public void buildTree() {
        CheckNode root = new CheckNode();
        for (int i = 0; i < steps.length; i++) {
            if (! steps[i].isEnabled()) {
                // allow only steps, that are enabled (or we would never
                // find a suitable puzzle)
                continue;
            }
            @SuppressWarnings("unchecked")
            Enumeration<CheckNode> en = (Enumeration<CheckNode>)root.children();
            CheckNode act = null;
            while (en.hasMoreElements()) {
                act = en.nextElement();
                if (act.getCategory() == steps[i].getCategory()) {
                    break;
                }
                act = null;
            }
            if (act == null) {
                // neue Kategorie
                act = new CheckNode(steps[i].getCategoryName(), true,
                        steps[i].isEnabledTraining() ? CheckNode.FULL : CheckNode.NONE,
                        null, false, false, true, steps[i].getCategory());
                root.add(act);
            }
            act.add(new CheckNode(steps[i].getType().getStepName(), false,
                    steps[i].isEnabledTraining() ? CheckNode.FULL : CheckNode.NONE,
                    steps[i], false, false, true, null));
            if (act.getSelectionState() == CheckNode.FULL && ! steps[i].isEnabledTraining()) {
                act.setSelectionState(CheckNode.HALF);
            }
            if (act.getSelectionState() == CheckNode.NONE && steps[i].isEnabledTraining()) {
                act.setSelectionState(CheckNode.HALF);
            }
        }
        DefaultTreeModel tmpModel = new DefaultTreeModel(root);
        stepTree.setModel(tmpModel);
        stepTree.setShowsRootHandles(true);
        stepTree.setRootVisible(false);
        stepTree.setRowHeight(-1);
    }
    
    private void checkButtons(boolean setList) {
        boolean changeView = false;
        if (listView != setList) {
            changeView = true;
        }
        listView = setList;
        if (listView) {
            listButton.setSelected(true);
            treeButton.setSelected(false);
            if (changeView) {
                stepScrollPane.setViewportView(stepList);
            }
            stepList.requestFocusInWindow();
        } else {
            listButton.setSelected(false);
            treeButton.setSelected(true);
            if (changeView) {
                buildTree();
                stepScrollPane.setViewportView(stepTree);
            }
            stepTree.requestFocusInWindow();
        }
    }
    
    class CheckBoxRenderer extends JCheckBox implements ListCellRenderer {
        private static final long serialVersionUID = 1L;
        
        CheckBoxRenderer() {
        }
        
        @Override
        public Component getListCellRendererComponent(JList listBox, Object obj, int index,
                boolean isSelected, boolean hasFocus) {
            if (isSelected) {
                Color bg = UIManager.getColor("List.selectionBackground");
                if (bg == null) {
                    bg = UIManager.getColor("List[Selected].textBackground");
                }
                Color fg = UIManager.getColor("List.selectionForeground");
                if (fg == null) {
                    fg = UIManager.getColor("List[Selected].textForeground");
                }
                setBackground(bg);
                setForeground(fg);
//                System.out.println("SBG: " + bg);
//                System.out.println("SFG: " + fg);
                setOpaque(true);
            } else {
                setBackground(UIManager.getColor("List.background"));
                setForeground(UIManager.getColor("List.foreground"));
//                System.out.println("BG: " + UIManager.getColor("List.background"));
//                System.out.println("FG: " + UIManager.getColor("List.foreground"));
                setOpaque(false);
            }
            setText(((StepConfig)obj).toString());
            setSelected(((StepConfig)obj).isEnabledTraining());
            return this;
        }
        
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel chosenLabel;
    private javax.swing.JTextArea chosenTextArea;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JToggleButton listButton;
    private javax.swing.JButton resetButton;
    private JList stepList;
    private javax.swing.JScrollPane stepScrollPane;
    private javax.swing.JTree stepTree;
    private javax.swing.JToggleButton treeButton;
    private javax.swing.JLabel warningLabel;
    // End of variables declaration//GEN-END:variables
    
}