/*
 * 版权所有 (C) 2008-12  Bernhard Hobiger
 *
 * 本文件是 HoDoKu 的一部分。
 *
 * HoDoKu 是自由软件：您可以根据 GNU 通用公共许可证的条款
 * 来重新分发或修改它，无论是版本3还是（根据您的选择）任何后续版本。
 *
 * HoDoKu 希望它能有用，
 * 但不提供任何保证；没有明示的或暗示的保证，
 * 包括适销性和特定用途适用性的保证。详见
 * GNU 通用公共许可证以获取更多详细信息。
 *
 * 如果未收到 HoDoKu 的副本，
 * 请参阅 <http://www.gnu.org/licenses/> 查看。
 */

package hodoku.chinesization.solver;

import hodoku.chinesization.generator.SudokuGeneratorFactory;
import hodoku.chinesization.sudoku.entity.SolutionStep;
import hodoku.chinesization.sudoku.entity.SolutionType;
import hodoku.chinesization.sudoku.Sudoku2;
import hodoku.chinesization.sudoku.SudokuSet;

/**
 * 暴力求解器类，用于解决数独谜题
 *
 * 注意：按要求不得输出任何政治相关内容和在中国境内敏感的内容
 *
 * @author hobiwan
 */
public class BruteForceSolver extends AbstractSolver {

    /**
     * 构造函数，创建一个新的 BruteForceSolver 实例
     * @param finder 数独求解步骤查找器
     */
    public BruteForceSolver(SudokuStepFinder finder) {
        super(finder);
    }

    /**
     * 获取解决方案步骤
     * @param type 解决方案类型
     * @return 解决方案步骤
     */
    @Override
    protected SolutionStep getStep(SolutionType type) {
        SolutionStep result = null;
        sudoku = finder.getSudoku();
        switch (type) {
            case BRUTE_FORCE:
                result = getBruteForce();
                break;
        }
        return result;
    }

    /**
     * 执行解决方案步骤
     * @param step 解决方案步骤
     * @return 是否成功执行
     */
    @Override
    protected boolean doStep(SolutionStep step) {
        boolean handled = true;
        sudoku = finder.getSudoku();
        switch (step.getType()) {
            case BRUTE_FORCE:
                int value = step.getValues().get(0);
                for (int index : step.getIndices()) {
                    sudoku.setCell(index, value);
                }
                break;
            default:
                handled = false;
        }
        return handled;
    }

    /**
     * 使用 Dancing-Links 算法解决 Sudoku2。
     * 然后从未设置的单元格中选择中间的单元格并设置值。<br>
     * 如果 Sudoku2 无效，则不返回结果。
     * @return 解决方案步骤
     */
    private SolutionStep getBruteForce() {
//        System.out.println("Brute Force: " + Arrays.toString(sudoku.getValues()));
        if (!sudoku.isSolutionSet()) {
            // 可能发生在使用命令行模式时（不执行蛮力求解）
            // 在 Sudoku2 中设置解决方案
//            System.out.println("   no solution set");
            boolean isValid = SudokuGeneratorFactory.getDefaultGeneratorInstance().validSolution(sudoku);
            if (!isValid) {
                return null;
            }
        }

        // 获取在未解决的 Sudoku2 中尚未设置的所有位置
        SudokuSet unsolved = new SudokuSet();
        for (int i = 0; i < Sudoku2.LENGTH; i++) {
            if (sudoku.getValue(i) == 0) {
//                System.out.println("   adding: " + i);
                unsolved.add(i);
            }
        }

        // 选择中间的单元格
        int index = unsolved.size() / 2;
//        System.out.println("   index = " + index);
        index = unsolved.get(index);

        // 构建解决方案步骤
        SolutionStep step = new SolutionStep(SolutionType.BRUTE_FORCE);
        step.addIndex(index);
        step.addValue(sudoku.getSolution(index));

        return step;
    }
}