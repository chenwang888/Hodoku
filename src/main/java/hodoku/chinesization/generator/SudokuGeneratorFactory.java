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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * HoDoKu uses one instance of class {@link SudokuGenerator} from within the
 * GUI. This instance is called the <b>defaultGenerator</b>. For use in background
 * threads an arbitrary number of additional generator instances can be gotten
 * from this factory.<br>
 * Generators have to be released after they are used.
 *
 * HoDoKu使用类{@link SudokuGenerator}的一个实例
 *  GUI。该实例称为<b>defaultGenerator</b>。在后台使用
 * 线程可以获得任意数量的额外生成器实例
 * 从这个工厂
 * generator在使用后必须被释放。
 *
 * @author hobiwan
 */
public class SudokuGeneratorFactory {
    /** The <b>defaultGenerator</b> for use by the GUI. */
    // <b>defaultGenerator</b> 供GUI使用。
    private static final SudokuGenerator defaultGenerator = new SudokuGenerator();
    /** All SudokuGenerator instances created by this factory. */
    //这个工厂创建的所有SudokuGenerator实例。
    private static List<generatorInstance> instances = new ArrayList<generatorInstance>();
    /** A background thread that cleans up unused SudokuGenerator instances. */
    // 一个后台线程，清理未使用的SudokuGenerator实例。
    private static final Thread thread = new Thread( new Runnable() {
        @Override
        @SuppressWarnings("SleepWhileInLoop")
        public void run() {
            while (true) {
                synchronized (thread) {
                    Iterator<generatorInstance> iterator = instances.iterator();
                    while (iterator.hasNext()) {
                        generatorInstance act = iterator.next();
                        if (act.inUse == false && (System.currentTimeMillis() - act.lastUsedAt) > GENERATOR_TIMEOUT) {
                            iterator.remove();
                        }
                    }
                }
                try {
                    Thread.sleep(GENERATOR_TIMEOUT);
                } catch (InterruptedException ex) {
                    // do nothing
                }
            }
        }
    });
    /** The default cleanup time for SudokuGenerator instances. */
    // SudokuGenerator实例的默认清理时间。
    private static final long GENERATOR_TIMEOUT = 5 * 60 * 1000;

    /**
     * One entry in {@link #instances}.
     * {@link #instances}中的一个条目。
     */
    private static class generatorInstance {
        /** The generator held in this entry. */
        // 发电机保持在这个条目中。
        SudokuGenerator instance = null;
        /** <code>true</code>, if the generator has been handed out by the factory. */
        // 如果 generator 是由工厂创建出的。
        boolean inUse = true;
        /** Last time the generator was returned to the factory. */
        // 上次  generator 时间
        long lastUsedAt = -1;

        /**
         * Create a new entry for {@link #instances}.
         * @param instance
         */
        private generatorInstance(SudokuGenerator instance) {
            this.instance = instance;
        }
    }

    /** Start the thread */
    static {
        thread.start();
    }

    /**
     * This class is a utility class that cannot be instantiated.
     */
    private SudokuGeneratorFactory() { /* class cannot be instantiated! */ }

    /** Get the {@link #defaultGenerator}.
     * @return 
     */
    public static SudokuGenerator getDefaultGeneratorInstance() {
        return defaultGenerator;
    }

    /**
     * Hand out an ununsed generator or create a new one if necessary.
     * 如果有必要，可以使用一个未使用的 generator 或创建一个新的。
     * @return
     */
    public static SudokuGenerator getInstance() {
        SudokuGenerator ret = null;
        synchronized (thread) {
            for (generatorInstance act : instances) {
                if (act.inUse == false) {
                    act.inUse = true;
                    ret = act.instance;
                    break;
                }
            }
            if (ret == null) {
                ret = new SudokuGenerator();
                instances.add(new generatorInstance(ret));
            }
        }
        return ret;
    }

    /**
     * Gives a generator back to the factory.
     * 给工厂一个发电机。
     * @param generator
     */
    public static void giveBack(SudokuGenerator generator) {
        synchronized (thread) {
            for (generatorInstance act : instances) {
                if (act.instance == generator) {
                    act.inUse = false;
                    act.lastUsedAt = System.currentTimeMillis();
                    break;
                }
            }
        }
    }
}
