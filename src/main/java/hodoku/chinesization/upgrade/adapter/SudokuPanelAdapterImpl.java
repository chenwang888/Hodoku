package hodoku.chinesization.upgrade.adapter;

import hodoku.chinesization.sudoku.panel.MainFrame;

public class SudokuPanelAdapterImpl implements SudokuPanelAdapter {
    private MainFrame mainFrame;

    public SudokuPanelAdapterImpl(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
    }

    @Override
    public void updateSudokuPanel() {
        // 实现适配逻辑
    }
}