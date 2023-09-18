/*
 * 版权所有 (C) 2008-12 Bernhard Hobiger
 *
 * 这个文件是HoDoKu的一部分。
 *
 * HoDoKu是自由软件：您可以根据GNU通用公共许可证（GPL）的条款重新发布和更改它，
 * 不论是版本3还是（如果您愿意）任何后续版本。
 *
 * HoDoKu以希望它有用的方式进行发布，但没有任何保证，即使没有暗示的保证，
 * 包括针对特定目的的适销性或适用性的保证。请参阅GNU通用公共许可证获取更多详细信息。
 *
 * 您应该随附于HoDoKu的副本。如果没有，参见<http://www.gnu.org/licenses/>。
 */
package hodoku.chinesization.sudoku.entity;

import java.io.Serializable;

/**
 * 候选数类
 */
public class Candidate implements Cloneable, Comparable<Candidate>, Serializable {

    private static final long serialVersionUID = 1L;
    private int value;
    private int index;

    public Candidate() {
    }

    /**
     * 构造函数
     * @param index 索引
     * @param value 值
     */
    public Candidate(int index, int value) {
        this.index = index;
        this.value = value;
    }

    @Override
    public int compareTo(Candidate o) {
        int ret = value - o.value;
        if (ret == 0) {
            ret = index - o.index;
        }
        return ret;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (!(o instanceof Candidate)) {
            return false;
        }
        Candidate c = (Candidate) o;
        if (index == c.index && value == c.value) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + this.value;
        hash = 29 * hash + this.index;
        return hash;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public String toString() {
        return index + "/" + value;
    }
}