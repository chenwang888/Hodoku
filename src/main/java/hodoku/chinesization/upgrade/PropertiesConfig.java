package hodoku.chinesization.upgrade;

import hodoku.chinesization.sudoku.panel.MainFrame;
import learn.framework.ioc.annotaion.*;
import learn.framework.ioc.core.properties.PropertiesLoader;
import learn.framework.ioc.factory.DefaultElementFactoryImpl;
import learn.framework.ioc.factory.ElementFactory;

/**
 * 类的作用和功能。
 * <p>
 * 类的设计思路。
 *
 * @author: cw
 * @since: 2023/9/15 15:35
 * @version: v0.0.2 WhiteDew beta
 * <p>
 * 修改记录：
 * 时间      修改人员    修改内容
 * ------------------------------
 */
@Configuration
@PropertySource("classpath:/intl/*_cn.properties")
public class PropertiesConfig {

    @Value("_cn.properties")
    String evn;

    public String getEvn() {
        return evn;
    }


//    @Element("mainFrame")
    public MainFrame mainFrame() {
        return new MainFrame(null);
    }
}
