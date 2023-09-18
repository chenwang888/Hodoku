package hodoku.chinesization.upgrade.builder;

import hodoku.chinesization.sudoku.SudokuPanel;
import hodoku.chinesization.sudoku.panel.MainFrame;

import javax.swing.*;

public class MainFrameBuilder {

    private MainFrame mainFrame;

    public MainFrameBuilder() {
        mainFrame = new MainFrame(null);
    }

    public MainFrameBuilder setSudokuPanel(SudokuPanel sudokuPanel) {
        // mainFrame.sudokuPanel = sudokuPanel;
        return this;
    }

    public MainFrameBuilder setToggleButtons(JToggleButton[] toggleButtons) {
        // mainFrame.toggleButtons = toggleButtons;
        return this;
    }

    // 其他属性的设置方法

    public MainFrame build() {
        return mainFrame;
    }
}