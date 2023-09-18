package hodoku.chinesization.upgrade.strategy;

import hodoku.chinesization.sudoku.panel.MainFrame;

public class EasyDifficultyLevelStrategy implements DifficultyLevelStrategy {
    private MainFrame mainFrame;

    public EasyDifficultyLevelStrategy(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
    }

    @Override
    public void setDifficultyLevel() {
        // 实现简单难度级别的逻辑
    }
}