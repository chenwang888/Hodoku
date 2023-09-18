package hodoku.chinesization.upgrade;

import hodoku.chinesization.sudoku.Main;
import hodoku.chinesization.sudoku.panel.MainFrame;
import learn.framework.common.log.LogLevel;
import learn.framework.common.log.Logger;
import learn.framework.ioc.DefaultApplication;
import learn.framework.ioc.annotaion.Component;
import learn.framework.ioc.annotaion.ComponentScan;
import learn.framework.ioc.core.ApplicationContext;
import learn.framework.ioc.core.analysis.ClassAnalysis;
import learn.framework.ioc.factory.DefaultElementFactoryImpl;
import learn.framework.ioc.factory.ElementFactory;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * 类的作用和功能。
 * <p>
 * 类的设计思路。
 *
 * @author: cw
 * @since: 2023/9/15 15:30
 * @version: v0.0.2 WhiteDew beta
 * <p>
 * 修改记录：
 * 时间      修改人员    修改内容
 * ------------------------------
 */
@ComponentScan({

        "learn.framework",
        "hodoku.chinesization.upgrade",
        //"hodoku.chinesization.sudoku",
})
public class HodokuStart {
    public static void main(String[] args) throws IOException, InterruptedException {

        // 设置日志级别
        //Logger.setLogLevel(LogLevel.INFO.toString());

        // 初始化容器
        DefaultApplication.run(HodokuStart.class);
        Main.main(args);
    }

}
