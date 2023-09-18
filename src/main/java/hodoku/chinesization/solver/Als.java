/*
 * 版权所有 (C) 2008-12  Bernhard Hobiger
 *
 * 本文件是 HoDoKu 的一部分。
 *
 * HoDoKu 是自由软件：您可以根据自由软件基金会发布的 GNU 通用公共许可证 (GNU General Public License) 的版本 3 或（根据您的选择）任何更高版本来重新分发和修改它。
 *
 * HoDoKu 希望它是有用的，但是没有任何担保；甚至没有默示的担保适销性或适用于特定用途的担保。详细了解 GNU 通用公共许可证的更多信息。
 *
 * 您应该收到 GNU 通用公共许可证的副本，如果没有，请参阅 <http://www.gnu.org/licenses/>。
 */
package hodoku.chinesization.solver;

import hodoku.chinesization.sudoku.entity.SolutionStep;
import hodoku.chinesization.sudoku.Sudoku2;
import hodoku.chinesization.sudoku.SudokuSet;

/**
 * ALS（几乎锁定集）是一组数字单元格，这些单元格一起包含一个比单元格数量多一个的候选数。根据此定义，仅剩两个候选数的单个单元格也是一个有效的ALS。<br><br>
 *
 * ALS的搜索由{@link SudokuStepFinder#getAlses()}完成，可以限制搜索以排除单个单元格。
 *
 * 作者：hobiwan
 */
public class Als {

    /**
     * 所有属于ALS的索引
     */
    public SudokuSet indices;

    /**
     * 所有包含在ALS中的数字（仅数字，不包括候选数）
     */
    public short candidates;

    /**
     * 对于ALS中包含的每个数字，所有包含该数字作为候选数的单元格
     */
    public SudokuSet[] indicesPerCandidat = new SudokuSet[10];

    /**
     * 对于ALS中包含的每个数字，所有与包含该候选数单元格相关联的ALS之外的单元格
     */
    public SudokuSet[] buddiesPerCandidat = new SudokuSet[10];

    /**
     * 类似于{@link #buddiesPerCandidat}，但包括包含该候选数单元格的ALS单元格（用于行/列搜索）
     */
    public SudokuSet[] buddiesAlsPerCandidat = new SudokuSet[10];

    /**
     * ALS之外的所有包含至少一个与ALS相关联的候选数的单元格
     */
    public SudokuSet buddies;

    /**
     * ALS的惩罚值（用于计算链长度）
     */
    public int chainPenalty = -1;

    /**
     * 创建一个新的ALS。<br><br>
     * <b>注意：</b>使用此构造函数创建的ALS除非调用了{@link #computeFields(solver.SudokuStepFinder)}，否则无法使用。
     * @param indices
     * @param candidates
     */
    public Als(SudokuSet indices, short candidates) {
        this.indices = new SudokuSet(indices);
        this.candidates = candidates;
    }

    /**
     * 计算所有附加字段；在初始搜索之后执行以优化查找双重ALS。
     *
     * @param finder
     */
    public void computeFields(SudokuStepFinder finder) {
        this.buddies = new SudokuSet();
        for (int i = 1; i <= 9; i++) {
            if ((candidates & Sudoku2.MASKS[i]) != 0) {
                SudokuSet sudokuCandidates = finder.getCandidates()[i];
                indicesPerCandidat[i] = new SudokuSet(indices);
                indicesPerCandidat[i].and(sudokuCandidates);
                buddiesPerCandidat[i] = new SudokuSet();
                Sudoku2.getBuddies(indicesPerCandidat[i], buddiesPerCandidat[i]);
                buddiesPerCandidat[i].andNot(indices);
                buddiesPerCandidat[i].and(finder.getCandidates()[i]);
                buddiesAlsPerCandidat[i] = new SudokuSet(buddiesPerCandidat[i]);
                buddiesAlsPerCandidat[i].or(indicesPerCandidat[i]);
                buddies.or(buddiesPerCandidat[i]);
            }
        }
    }

    /**
     * ALS在链中视为一个链接。这优先选择包含大ALS的链，而不是稍微更长但包含较小（或根本没有）ALS的链。将惩罚值添加到链长度，以抑制该行为。
     * @param candSize ALS中的候选数数量
     * @return 要添加到链长度的链接数
     */
    public static int getChainPenalty(int candSize) {
        //return 0;
        if (candSize == 0 || candSize == 1) {
            return 0;
        } else if (candSize == 2) {
            return candSize - 1;
        } else {
            return (candSize - 1) * 2;
        }
    }

    /**
     * 返回ALS的链惩罚值（参见{@link #getChainPenalty(int)}）。
     * @return 要添加到链长度的链接数
     */
    public int getChainPenalty() {
        if (chainPenalty == -1) {
            chainPenalty = getChainPenalty(Sudoku2.ANZ_VALUES[candidates]);
        }
        return chainPenalty;
    }

    /**
     * 如果它们包含相同的索引，则两个ALS相等。
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (!(o instanceof Als)) {
            return false;
        }
        Als a = (Als) o;
        return indices.equals(a.indices);
    }

    /**
     * 适用于{@link #equals(Object)}的哈希码方法。
     * @return
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + (this.indices != null ? this.indices.hashCode() : 0);
        return hash;
    }

    /**
     * ALS的可读格式。
     * @return
     */
    @Override
    public String toString() {
        //return "ALS: " + candidates.toString() + " - " + indices.toString();
        return "ALS: " + SolutionStep.getAls(this);
    }
}