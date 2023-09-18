/*
 * 版权所有 (C) 2008-12  Bernhard Hobiger
 *
 * 本文件是 HoDoKu 的一部分。
 *
 * HoDoKu 是自由软件：您可以重新分发和/或修改
 * 根据GNU通用公共许可证的条款，由
 * 自由软件基金会发布的版本3或
 * （根据您的选择）任何以后的版本。
 *
 * 希望HoDoKu有用，
 * 但没有任何保证；也没有暗示的保证
 * 适销性或特定用途适用性。详见
 * GNU通用公共许可证以获取更多详情。
 *
 * 如果没有收到GNU通用公共许可证的副本
 * 随 HoDoKu 提供。如果没有，请参阅<http://www.gnu.org/licenses/>。
 */
package hodoku.chinesization.sudoku.solver;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import hodoku.chinesization.solver.Als;

/**
 * AlsInSolutionStep类是克隆和序列化的可变类，用于表示解数步骤中的ALS。
 */
public class AlsInSolutionStep implements Cloneable, Serializable {

    private static final long serialVersionUID = 1L;
    private List<Integer> indices = new ArrayList<Integer>();
    private List<Integer> candidates = new ArrayList<Integer>();
    private int chainPenalty = -1;

    public AlsInSolutionStep() {
    }

    /**
     * 添加索引到ALS中。
     * @param index 索引值
     */
    public void addIndex(int index) {
        indices.add(index);
    }

    /**
     * 添加候选数到ALS中。
     * @param cand 候选数
     */
    public void addCandidate(int cand) {
        candidates.add(cand);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object clone()
            throws CloneNotSupportedException {
        AlsInSolutionStep newAls = (AlsInSolutionStep) super.clone();
        newAls.indices = (List<Integer>) ((ArrayList<Integer>) indices).clone();
        newAls.candidates = (List<Integer>) ((ArrayList<Integer>) candidates).clone();
        return newAls;
    }

    /**
     * 获取ALS的索引列表。
     * @return 索引列表
     */
    public List<Integer> getIndices() {
        return indices;
    }

    /**
     * 设置ALS的索引列表。
     * @param indices 索引列表
     */
    public void setIndices(List<Integer> indices) {
        this.indices = indices;
    }

    /**
     * 获取ALS的候选数列表。
     * @return 候选数列表
     */
    public List<Integer> getCandidates() {
        return candidates;
    }

    /**
     * 设置ALS的候选数列表。
     * @param candidates 候选数列表
     */
    public void setCandidates(List<Integer> candidates) {
        this.candidates = candidates;
    }

    /**
     * 获取链式技巧的惩罚值。
     * @return 链式技巧的惩罚值
     */
    public int getChainPenalty() {
        if (chainPenalty == -1) {
            chainPenalty = Als.getChainPenalty(indices.size());
        }
        return chainPenalty;
    }

    /**
     * 设置链式技巧的惩罚值。
     * @param chainPenalty 链式技巧的惩罚值
     */
    public void setChainPenalty(int chainPenalty) {
        this.chainPenalty = chainPenalty;
    }
}