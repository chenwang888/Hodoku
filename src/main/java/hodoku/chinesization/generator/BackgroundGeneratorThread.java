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

import java.util.logging.Level;
import java.util.logging.Logger;
import hodoku.chinesization.sudoku.entity.DifficultyLevel;
import hodoku.chinesization.sudoku.entity.DifficultyType;
import hodoku.chinesization.sudoku.GameMode;
import hodoku.chinesization.sudoku.Options;
import hodoku.chinesization.sudoku.StepConfig;

/**
 * 该类的一个实例用于控制后台创建数独。
 * 下面的谜题是自动生成的存储在{@link Options}中:
 * <ul>
 * <li>每个难度等级对应10个数独</li>
 * <li>10 sudoku for {@link GameMode#LEARNING}</li>
 * <li>10 sudoku for {@link GameMode#}</li>
 * </ul>
 *
 * 拼图的创建由以下事件触发:
 * <ul>
 * <li>在程序启动时:缺失的拼图被创建(从当前{@link DifficultyLevel}开始)</li>
 * <li>当步骤配置被更改时:所有类型的谜题都会再次创建
 * <li>当训练步骤配置改变时:<code>LEARNING</code> and <code> </code> puzzle be redone</li>
 * <li>当GUI中的{@link DifficultyLevel}发生改变时:<code> </code>拼图重做</li>
 * <li>当从文件加载配置时，所有的谜题都被重做</li>

 * </ul>

 *这个类是单例。
 * 
 * @author hobiwan
 */
public class BackgroundGeneratorThread implements Runnable {
    /** 调试标记 */
    private static final boolean DEBUG = false;
    /** 单例实例 */
    private static BackgroundGeneratorThread instance = null;
    /** 真正的创造者 */
    private BackgroundGenerator generator;
    /** 创建线程 */
    private final Thread thread;
    /** 一个标志，表示传入了一个新的数独 */
    private boolean newRequest = false;
    /** 一个标志，表示线程是否已经启动 */
    private boolean threadStarted = false;
    
    /**
     * Creates an instance.
     */
    private BackgroundGeneratorThread() {
        thread = new Thread(this);
        generator = new BackgroundGenerator();
    }
    
    /**
     * 检索单例实例，必要时生成它。
     * 
     * @return 
     */
    public static BackgroundGeneratorThread getInstance() {
        if (instance == null) {
            instance = new BackgroundGeneratorThread();
        }
        return instance;
    }

    /**
     * 检查是否有匹配需求的拼图可用。
     * 
     * @param level
     * @param mode
     * @return 
     */
    public synchronized String getSudoku(DifficultyLevel level, GameMode mode) {
        // 从选项中找到正确的谜题 get the correct puzzles from Options
        String[] puzzles = getPuzzleArray(level, mode);
        // 检查是否有拼图 check if a puzzle is available
        String newPuzzle = null;
        if (puzzles[0] != null) {
            newPuzzle = puzzles[0];
            for (int i = 1; i < puzzles.length; i++) {
                puzzles[i - 1] = puzzles[i];
            }
            puzzles[puzzles.length - 1] = null;
        }
        if (DEBUG) {
            System.out.println("Got puzzle from cache: " + level.getName() + "/" + mode.name() + "/" + newPuzzle);
        }
        // start a new run 开始新的运行
        startCreation();
        // and give it back 把它返回
        return newPuzzle;
    }
    
    /**
     * Writes a new sudoku into the cache.
     * 将一个新的数独写入缓存。
     * @param level
     * @param mode
     * @param sudoku 
     */
    private synchronized void setSudoku(DifficultyLevel level, GameMode mode, String sudoku) {
        // get the correct puzzles from Options
        // 从选项中找到正确的谜题
        String[] puzzles = getPuzzleArray(level, mode);
        for (int i = 0; i < puzzles.length; i++) {
            if (puzzles[i] == null) {
                puzzles[i] = sudoku;
                break;
            }
        }
    }
    
    /**
     * The step configuration has been changed:
     * reset everything and start over.
     * 步骤配置已更改:
     * 重置所有内容并重新开始。
     */
    public synchronized void resetAll() {
        String[][] puzzles = Options.getInstance().getNormalPuzzles();
        for (int i = 0; i < puzzles.length; i++) {
            for (int j = 0; j < puzzles[i].length; j++) {
                puzzles[i][j] = null;
            }
        }
        resetTrainingPractising();
    }
    
    /**
     * The training configuration has changed: recreate the
     * LEARNING and PRACTISING puzzles and start over.
     * 训练配置已更改:重新创建
     * 学习和练习拼图，然后重新开始。
     */
    public synchronized void resetTrainingPractising() {
        String[] puzzles1 = Options.getInstance().getLearningPuzzles();
        for (int i = 0; i < puzzles1.length; i++) {
            puzzles1[i] = null;
        }
        puzzles1 = Options.getInstance().getPractisingPuzzles();
        for (int i = 0; i < puzzles1.length; i++) {
            puzzles1[i] = null;
        }
        startCreation();
    }
    
    /**
     * The level has been changed, check if the PRACTISING puzzles
     * have to be recreated.
     * 水平已改变，检查是否练习拼图
     * 必要时重新创建。
     * 
     * @param newLevel 
     */
    public synchronized void setNewLevel(int newLevel) {
        int maxTrainingLevel = getTrainingLevel();
        if (maxTrainingLevel == -1 || newLevel < maxTrainingLevel) {
            // we cant create suitable puzzles -> ignore
            // 我们不能创造合适的谜题->忽略
            return;
        }
        if (newLevel == Options.getInstance().getPractisingPuzzlesLevel()) {
            // nothing to do!
            return;
        }
        String[] puzzles = Options.getInstance().getPractisingPuzzles();
        for (int i = 0; i < puzzles.length; i++) {
            puzzles[i] = null;
        }
        Options.getInstance().setPractisingPuzzlesLevel(newLevel);
        startCreation();
    }
    
    /**
     * Schedules a new creation run. If the thread is not yet running,
     * it is started. The thread is signalled.
     * Since it is possible, that the thread is still busy with another
     * run, a flag is set as well.
     *
     * 安排一个新的创建运行。如果线程还没有运行，
     * 已启动。这个线索是有信号的。
     * 因为有可能一个线程仍然忙于处理另一个线程
     * 运行时，也设置了一个标志。
     */
    public void startCreation() {
        if (thread == null) {
            return;
        }
        if (! threadStarted) {
            thread.start();
            threadStarted = true;
            if (DEBUG) {
                System.out.println("BackgroundCreationThread started!");
            }
        }
        synchronized(thread) {
            // set a flag indicating a newly scheduled check
            // 设置一个标志位，表示新计划的检查
            newRequest = true;
            if (DEBUG) {
                System.out.println("new creation request scheduled!");
            }
            // wake up the thread, if it is sleeping
            // 唤醒线程，如果它正在睡眠
            thread.notify();
        }
    }
    
    /**
     * The main thread: If it is signalled it checks, which type of puzzle
     * is missing. As long as a missing puzzle type is found, the creation is
     * continued.
     *
     * 主线程:如果它被发出信号，它会检查哪种类型的谜题
     * 缺失。只要找到了缺失的谜题类型，就算是创造了
     * 持续。
     */
    @Override
    public void run() {
        while (!thread.isInterrupted()) {
            try {
                synchronized (thread) {
                    if (newRequest == false) {
                        thread.wait();
                    }
                    if (newRequest) {
                        newRequest = false;
                    } else {
                        continue;
                    }
                }
                if (DEBUG) {
                    System.out.println("Creation starting...");
                }
                DifficultyLevel level = null;
                GameMode mode = null;
                while (level == null && !thread.isInterrupted()) {
                    // get the next puzzle
                    // 得到下一个谜题
                    synchronized (this) {
                        // find out, what to do
                        // 找出要做什么
                        String[][] puzzles = Options.getInstance().getNormalPuzzles();
                        for (int i = 0; i < puzzles.length; i++) {
                            for (int j = 0; j < puzzles[i].length; j++) {
                                if (puzzles[i][j] == null) {
                                    if (DEBUG) {
                                        System.out.println("found level: "+ (i + 1));
                                    }
                                    level = Options.getInstance().getDifficultyLevel(i + 1);
                                    mode = GameMode.PLAYING;
                                    if (DEBUG) {
                                        System.out.println("   " + level.getName()+ "/" + mode.name());
                                    }
                                    break;
                                }
                            }
                            if (level != null) {
                                break;
                            }
                        }
                        int trLevel = getTrainingLevel();
                        String[] puzzles1 = Options.getInstance().getLearningPuzzles();
                        if (level == null && trLevel != -1) {
                            for (int i = 0; i < puzzles1.length; i++) {
                                if (puzzles1[i] == null) {
                                    if (DEBUG) {
                                        System.out.println("found level: "+ (i + 1));
                                    }
                                    level = Options.getInstance().getDifficultyLevel(DifficultyType.EXTREME.ordinal());
                                    mode = GameMode.LEARNING;
                                    if (DEBUG) {
                                        System.out.println("   " + level.getName()+ "/" + mode.name());
                                    }
                                    break;
                                }
                            }
                        }
                        if (trLevel != -1 && Options.getInstance().getPractisingPuzzlesLevel() == -1) {
                            setNewLevel(Options.getInstance().getActLevel());
                        }
                        puzzles1 = Options.getInstance().getPractisingPuzzles();
                        if (DEBUG) {
                            System.out.println("looking for pract: " + level + "/" + trLevel + "/" + Options.getInstance().getActLevel() + "/" + Options.getInstance().getPractisingPuzzlesLevel());
                        }
                        if (level == null && trLevel != -1 && Options.getInstance().getActLevel() >= trLevel) {
                            for (int i = 0; i < puzzles1.length; i++) {
                                if (puzzles1[i] == null) {
                                    if (DEBUG) {
                                        System.out.println("found level: " + (i + 1));
                                    }
                                    level = Options.getInstance().getDifficultyLevel(Options.getInstance().getPractisingPuzzlesLevel());
                                    mode = GameMode.PRACTISING;
                                    if (DEBUG) {
                                        System.out.println("   " + level.getName()+ "/" + mode.name());
                                    }
                                    break;
                                }
                            }
                        }
                    }
                    //new puzzle type found?
                    // 发现新的谜题类型?
                    if (level == null) {
                        // we are done for now
                        // 我们现在就讲到这里
                        if (DEBUG) {
                            System.out.println("creation: nothing to do!");
                        }
                        break;
                    }
                    if (DEBUG) {
                        System.out.println("  creating " + level.getName() + "/" + mode.name());
                    }
                    // ok, create the puzzle
                    // 好吧，创造一个谜题
                    String puzzle = generator.generate(level, mode);
                    if (puzzle == null) {
                        //couldnt create one -> stop for now
                        // BUG: dont give up just now!
                        //不能创建一个->停止现在
                        // BUG:现在不要放弃!
                        if (DEBUG) {
                            System.out.println("couldnt find suitable puzzles, retrying!");
                        }
                        break;
                    }
                    // store it 存储它
                    setSudoku(level, mode, puzzle);
                    if (DEBUG) {
                        System.out.println("  created in background: " + level.getName() + "/" + mode.name() + "/" + puzzle);
                    }
                    // and try again 再试一次
                    level = null;
                    mode = null;
                }
                if (DEBUG) {
                    System.out.println("Done (level = " + level + ", isInterrupted() = " + thread.isInterrupted() + ")!");
                }
            } catch (InterruptedException ex) {
                thread.interrupt();
            } catch (Exception ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Error checking progress", ex);
            }
        }
    }
    
    /**
     * Gets the correct cache array from Options.
     * 从选项中获取正确的缓存数组。
     * 
     * @param level
     * @param mode
     * @return 
     */
    private String[] getPuzzleArray(DifficultyLevel level, GameMode mode) {
        String[] puzzles = null;
        switch (mode) {
            case PLAYING:
                puzzles = Options.getInstance().getNormalPuzzles()[level.getOrdinal() - 1];
                break;
            case LEARNING:
                puzzles = Options.getInstance().getLearningPuzzles();
                break;
            case PRACTISING:
                puzzles = Options.getInstance().getPractisingPuzzles();
                break;
        }
        return puzzles;
    }
    
    /**
     * Utility method: gets the {@link DifficultyLevel} of the most difficult
     * training step. If no training step is set, -1 is returned.<br>
     * This method is used in two ways: To decide, if LEARNING/PRACTISING steps
     * should be created at all (if no trainig step is enabled, creation will
     * be impossible), and to decide, if PRACTISING steps have to be
     * redone after a change of the games current DifficultyLevel (if the
     * current level is lower than the level of the hardest training step,
     * no new PRACTISING puzzles have to be created).
     *
     * 实用方法:获得最困难的{ @link难题
     * 培训步骤。如果没有训练步骤,-1返回。< br >
     * 这种方法使用两种方法决定,如果学习/练习步骤
     * 应该创建(如果没有启用trainig步骤,创建将会
     * 不可能),并决定,如果必须采取步骤
     * 在游戏的改变之后重新进行(如果
     * 当前水平低于最困难的训练步骤水平,
     * 不需要创建新的练习谜题。
     * 
     * @return 
     */
    private int getTrainingLevel() {
        StepConfig[] conf = Options.getInstance().getOrgSolverSteps();
        int level = -1;
        for (StepConfig act : conf) {
            if (act.isEnabledTraining()) {
                int actLevel = act.getLevel();
                if (actLevel > level) {
                    level = actLevel;
                }
            }
        }
        return level;
    }
}
