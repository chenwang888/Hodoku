/*
 * 版权所有 (C) 2008-12 Bernhard Hobiger
 *
 * 此文件是 HoDoKu 的一部分。
 *
 * HoDoKu 是自由软件：您可以根据自由软件基金会发布的GNU通用公共许可证的版本3或者更高版本的条款进行修改和再发布。
 *
 * HoDoKu 被提供的目标是希望它对您有用，但不提供任何明示或暗示的保证，包括对特定用途的适用性以及其它方面的保证。详细了解请参阅GNU通用公共许可证。
 *
 * 如果您没有收到 GNU通用公共许可证的副本，请查看 <http://www.gnu.org/licenses/>。
 */
package hodoku.chinesization.sudoku.dialog;

import hodoku.chinesization.sudoku.panel.MainFrame;
import hodoku.chinesization.upgrade.PropertyUtil;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

/**
 *
 * HoDoKu关于对话框类
 * 作者：hobiwan
 */
public class AboutDialog extends javax.swing.JDialog {
    private static final long serialVersionUID = 1L;

    /**
     * 创建一个新的AboutDialog实例
     * @param parent 父窗口
     * @param modal 是否模态对话框
     */
    public AboutDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();

        versionLabel.setText(MainFrame.VERSION + " (" + MainFrame.BUILD + ")");
        getRootPane().setDefaultButton(closeButton);

        // 设置按下ESC键关闭对话框
        KeyStroke escapeKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
        Action escapeAction = new AbstractAction() {
            private static final long serialVersionUID = 1L;
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible( false );
            }
        };
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escapeKeyStroke, "ESCAPE");
        getRootPane().getActionMap().put("ESCAPE", escapeAction);
    }

    /**
     * 初始化对话框的组件
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        versionLabel = new javax.swing.JLabel();
        copyrightLabel = new javax.swing.JLabel();
        copyleftLabel = new javax.swing.JLabel();
        logoLabel = new javax.swing.JLabel();
        closeButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        java.util.ResourceBundle bundle = PropertyUtil.loadResourceFile("intl/AboutDialog"); // NOI18N
        setTitle(bundle.getString("AboutDialog.title")); // NOI18N

        versionLabel.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        versionLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        versionLabel.setText("HoDoKu v1.0"); // NOI18N

        copyrightLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        copyrightLabel.setText(bundle.getString("AboutDialog.copyrightLabel.text")); // NOI18N

        copyleftLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        copyleftLabel.setText(bundle.getString("AboutDialog.copyleftLabel.text")); // NOI18N

        logoLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        logoLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/gplv3-127x51.png"))); // NOI18N

        closeButton.setMnemonic(PropertyUtil.getProperty("intl/AboutDialog","AboutDialog.closeButton.mnemonic").charAt(0));
        closeButton.setText(bundle.getString("AboutDialog.closeButton.text")); // NOI18N
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                                .addContainerGap()
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(logoLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 375, Short.MAX_VALUE)
                                                        .addComponent(copyrightLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 375, Short.MAX_VALUE)
                                                        .addComponent(versionLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 375, Short.MAX_VALUE)
                                                        .addComponent(copyleftLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 375, Short.MAX_VALUE)))
                                        .addGroup(layout.createSequentialGroup()
                                                .addGap(147, 147, 147)
                                                .addComponent(closeButton, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addContainerGap())
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(versionLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(copyrightLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(copyleftLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(logoLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 49, Short.MAX_VALUE)
                                .addComponent(closeButton)
                                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void closeButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
        // 关闭对话框
        setVisible(false);
    }//GEN-LAST:event_closeButtonActionPerformed

    /**
     * 主方法，用于启动对话框的显示
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                AboutDialog dialog = new AboutDialog(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton closeButton;
    private javax.swing.JLabel copyleftLabel;
    private javax.swing.JLabel copyrightLabel;
    private javax.swing.JLabel logoLabel;
    private javax.swing.JLabel versionLabel;
    // End of variables declaration//GEN-END:variables

}