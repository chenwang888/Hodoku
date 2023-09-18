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

package hodoku.chinesization.generator;

import java.awt.EventQueue;
import java.util.List;
import hodoku.chinesization.solver.SudokuSolver;
import hodoku.chinesization.solver.SudokuSolverFactory;
import hodoku.chinesization.sudoku.emuns.ClipboardMode;
import hodoku.chinesization.sudoku.entity.DifficultyLevel;
import hodoku.chinesization.sudoku.GameMode;
import hodoku.chinesization.sudoku.progress.GenerateSudokuProgressDialog;
import hodoku.chinesization.sudoku.Options;
import hodoku.chinesization.sudoku.entity.SolutionStep;
import hodoku.chinesization.sudoku.Sudoku2;

/**
 * BackgroundGenerator使用给定的{@link DifficultyLevel}生成数独游戏
 * 和给定的{@link GameMode}。可以包含这个类的实例
 * 在{@link BackgroundGeneratorThread}中或在{@link GenerateSudokuProgressDialog}中
 * 如果从{@link GenerateSudokuProgressDialog}调用，它会使用
 * 默认的解决方案，并向对话框报告进度。如果一个谜题已经
 * 找到，对话框关闭。<br> .<br> .<br> .<br> .<br> .
 * 如果它是从{@link BackgroundGeneratorThread}调用的，它只是简单地传递
 * 如果没有找到拼图，则生成拼图或<code>null</code>。
 * 
 * @author hobiwan
 */
public class BackgroundGenerator {
    /** 调用时的最大尝试次数 {@link BackgroundGeneratorThread}. */
    private static final int MAX_TRIES = 20000;
    /** 调用时的当前尝试次数 {@link GenerateSudokuProgressDialog}. */
    private int anz = 0;
    /** 从GUI调用时的进度对话框。 */
    private GenerateSudokuProgressDialog progressDialog = null;

    /**
     * Generates a new instance.
     */
    public BackgroundGenerator() {
        // nothing to do!
    }
    
    /**
     * Creates a sudoku without responses to the GUI. Delegates to 
     * {@link #generate(DifficultyLevel, GameMode, GenerateSudokuProgressDialog) }.
     * 
     * @param level
     * @param mode
     * @return 
     */
    public String generate(DifficultyLevel level, GameMode mode) {
        Sudoku2 sudoku = generate(level, mode, null);
        if (sudoku != null) {
            return sudoku.getSudoku(ClipboardMode.CLUES_ONLY);
        }
        return null;
    }
    
    /**
     * 生成一个新的数独:如果<code>dlg</code> is <code>null</code>，则进度为
     * 当puzzle被发现时，报告并关闭对话框。的
     * 检查当前线程是否中断
     * 如果<code>dlg</code>不<code>null</code>，则继续创建
     * 直到找到谜题或{@link #MAX_TRIES}已经尝试
     * 运行。
     *
     * @param level
     * @param mode
     * @param dlg
     * @return 
     */
    public Sudoku2 generate(DifficultyLevel level, GameMode mode, GenerateSudokuProgressDialog dlg) {
        long actMillis = System.currentTimeMillis();
        progressDialog = dlg;
        Sudoku2 sudoku = null;
        SudokuGenerator creator = null; 
        SudokuSolver solver = null;
        setAnz(0);
        if (dlg == null) {
            // 获取任何实例
            solver = SudokuSolverFactory.getInstance();
            creator = SudokuGeneratorFactory.getInstance();
        } else {
            // 使用默认的求解器
            solver = SudokuSolverFactory.getDefaultSolverInstance();
            creator = SudokuGeneratorFactory.getDefaultGeneratorInstance();
        }
        while (dlg == null || ! Thread.currentThread().isInterrupted()) {
            sudoku = creator.generateSudoku(true);
            if (sudoku == null) {
                // 由于模式无效，无法创建数独
                return null;
            }
            Sudoku2 solvedSudoku = sudoku.clone();
            boolean ok = solver.solve(level, solvedSudoku, true, null, false, 
                    Options.getInstance().solverSteps, mode);
            boolean containsTrainingStep = true;
            if (mode != GameMode.PLAYING) {
                containsTrainingStep = false;
                List<SolutionStep> steps = solver.getSteps();
                for (SolutionStep step : steps) {
                    if (step.getType().getStepConfig().isEnabledTraining()) {
                        containsTrainingStep = true;
                        break;
                    }
                }
            }
            if (ok && containsTrainingStep && 
                    (solvedSudoku.getLevel().getOrdinal() == level.getOrdinal()
                    || mode == GameMode.LEARNING)) {
                sudoku.setLevel(solvedSudoku.getLevel());
                sudoku.setScore(solvedSudoku.getScore());
                break;
            }
            setAnz(getAnz() + 1);
            if (dlg != null) {
                if ((System.currentTimeMillis() - actMillis) > 500) {
                    actMillis = System.currentTimeMillis();
                    EventQueue.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            progressDialog.updateProgressLabel();
                            //progressLabel.setText(Integer.toString(getAnz()));
                        }
                    });
                }
            } else {
                if (getAnz() > MAX_TRIES) {
                    // 放弃……
                    sudoku = null;
                    break;
                }
            }
        }
        if (dlg == null) {
            // 归还一切
            SudokuGeneratorFactory.giveBack(creator);
            SudokuSolverFactory.giveBack(solver);
        }
        return sudoku;
    }

    /**
     * @return the anz
     */
    public synchronized int getAnz() {
        return anz;
    }

    /**
     * @param anz the anz to set
     */
    public synchronized void setAnz(int anz) {
        this.anz = anz;
    }
}
