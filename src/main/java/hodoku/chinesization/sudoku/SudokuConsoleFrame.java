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

package hodoku.chinesization.sudoku;

import hodoku.chinesization.upgrade.PropertyUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

/**
 *
 * @author  hobiwan
 */
public class SudokuConsoleFrame extends javax.swing.JFrame {
    private static final long serialVersionUID = 1L;
    private StreamHandler consoleHandler = null;

    /** Creates new form SudokuConsoleFrame */
    public SudokuConsoleFrame() {
        initComponents();
        
        PrintStream newOut =
                new PrintStream(new ConsoleOutputStream());
        System.setOut(newOut);
        System.setErr(newOut);

        consoleHandler = new StreamHandler(newOut, new SimpleFormatter());
        Logger rootLogger = Logger.getLogger("");
        rootLogger.addHandler(consoleHandler);
        rootLogger.setLevel(Level.CONFIG);
        
        getRootPane().setDefaultButton(closeButton);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        consoleTextArea = new javax.swing.JTextArea();
        closeButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        java.util.ResourceBundle bundle = PropertyUtil.loadResourceFile("intl/SudokuConsoleFrame"); // NOI18N
        setTitle(bundle.getString("SudokuConsoleFrame.title")); // NOI18N

        consoleTextArea.setColumns(20);
        consoleTextArea.setRows(5);
        jScrollPane1.setViewportView(consoleTextArea);

        closeButton.setText(bundle.getString("SudokuConsoleForm.closeButton.text")); // NOI18N
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 803, Short.MAX_VALUE)
                    .addComponent(closeButton, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 346, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(closeButton)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
    setVisible(false);
    System.exit(0);
}//GEN-LAST:event_closeButtonActionPerformed

    public void setIn() {
        System.setIn(new ConsoleInputStream());
    }
    
    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new SudokuConsoleFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton closeButton;
    private javax.swing.JTextArea consoleTextArea;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables

    class ConsoleOutputStream extends OutputStream {
        // we keep a buffer around for creating 1-char strings, to
        // avoid the potential horror of thousands of array allocations
        // per second
        private byte littlebuf[] = new byte[1];

        // Redirect output to the console
        @Override
        public void write(int b) throws IOException {
            littlebuf[0] = (byte) b;
            String s = new String(littlebuf, 0, 1);
            consoleTextArea.append(s);
            consoleTextArea.setCaretPosition(consoleTextArea.getText().length());
        }

        // Redirect output to the console
        @Override
        public void write(byte b[]) throws IOException {
            String s = new String(b, 0, b.length);
            consoleTextArea.append(s);
            consoleTextArea.setCaretPosition(consoleTextArea.getText().length());
        }

        // Redirect output to the console
        @Override
        public void write(byte b[], int off, int len) throws IOException {
            String s = new String(b, off, len);
            consoleTextArea.append(s);
            consoleTextArea.setCaretPosition(consoleTextArea.getText().length());
        }

        // nothing need be done here
        @Override
        public void flush() throws IOException {
        }

        // nothing need be done here
        @Override
        public void close() throws IOException {
        }
    }
    
    class ConsoleInputStream extends InputStream {

        @Override
        @SuppressWarnings("SleepWhileInLoop")
        public int read() throws IOException {
            while (true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(SudokuConsoleFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
    }
}
