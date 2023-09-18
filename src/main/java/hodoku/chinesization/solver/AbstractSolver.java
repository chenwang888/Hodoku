/*
 * 版权所有 (C) 2008-12 Bernhard Hobiger
 *
 * 本文件是 HoDoKu 的一部分。
 *
 * HoDoKu 是自由软件：您可以在自由软件基金会发布的 GNU 通用公共许可证的版本 3 或（根据您的选择）任何之后的版本下重新分发和修改它。
 *
 * HoDoKu 希望它有用，但没有任何担保；甚至没有对适销性或特定用途适用性的隐含担保。
 * 有关更多详细信息，请参阅 GNU 通用公共许可证。
 *
 * 您应该同时收到 GNU 通用公共许可证的副本。
 * 如果没有，请参阅 <http://www.gnu.org/licenses/>。
 */

/*
 * Sudoku2-Grid with Indices for Debugging:
 *
 *      1  2  3    4  5  6    7  8  9
 *   +----------+----------+----------+
 * 1 | 00 01 02 | 03 04 05 | 06 07 08 | 1
 * 2 | 09 10 11 | 12 13 14 | 15 16 17 | 2
 * 3 | 18 19 20 | 21 22 23 | 24 25 26 | 3
 *   +----------+----------+----------+
 * 4 | 27 28 29 | 30 31 32 | 33 34 35 | 4
 * 5 | 36 37 38 | 39 40 41 | 42 43 44 | 5
 * 6 | 45 46 47 | 48 49 50 | 51 52 53 | 6
 *   +----------+----------+----------+
 * 7 | 54 55 56 | 57 58 59 | 60 61 62 | 7
 * 8 | 63 64 65 | 66 67 68 | 69 70 71 | 8
 * 9 | 72 73 74 | 75 76 77 | 78 79 80 | 9
 *   +----------+----------+----------+
 *      1  2  3    4  5  6    7  8  9
 */

package hodoku.chinesization.solver;

import hodoku.chinesization.sudoku.entity.SolutionStep;
import hodoku.chinesization.sudoku.entity.SolutionType;
import hodoku.chinesization.sudoku.Sudoku2;

public abstract class AbstractSolver {
    protected SudokuStepFinder finder;
    protected Sudoku2 sudoku;

    public AbstractSolver(SudokuStepFinder finder) {
        this.finder = finder;
    }

    protected abstract SolutionStep getStep(SolutionType type);

    protected abstract boolean doStep(SolutionStep step);

    protected void cleanUp() {
        // 什么也不做
    }
}