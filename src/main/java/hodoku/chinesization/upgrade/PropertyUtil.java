package hodoku.chinesization.upgrade;

import learn.framework.common.log.Logger;
import learn.framework.ioc.aware.ElementFactoryAware;
import learn.framework.ioc.core.properties.PropertiesLoader;
import learn.framework.ioc.factory.ElementFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 * 类的作用和功能。
 * <p>
 * 类的设计思路。
 *
 * @author: cw
 * @since: 2023/9/15 15:13
 * @version: v0.0.2 WhiteDew beta
 * <p>
 * 修改记录：
 * 时间      修改人员    修改内容
 * ------------------------------
 */
public class PropertyUtil implements ElementFactoryAware {

    public static volatile PropertiesLoader propertiesLoader;
    public static volatile ElementFactory elementFactory;
    static Map<String, ResourceBundle> cacheMap = new HashMap<>();


    public static String getProperty(String fileName, String key) {

        PropertiesConfig config = elementFactory.getElement(PropertiesConfig.class);

        String path = new File(fileName + config.getEvn()).getPath();

        String property = propertiesLoader.getProperty(PropertiesConfig.class, key);

        if (property == null) {
            System.out.println(key);
        }
        return property;
    }

    public static String getValue(String key) {

        return propertiesLoader.getProperty(PropertiesConfig.class, key);
    }

    public static ResourceBundle loadResourceFile(String filePath) {
        PropertiesConfig config = elementFactory.getElement(PropertiesConfig.class);

        String gainPath = filePath + config.getEvn();

        try {

            ResourceBundle resourceBundle = cacheMap.get(gainPath);
            if (resourceBundle == null) {

                Logger.debug("load cache " + filePath);
                InputStream inputStream= PropertyUtil.class.getResourceAsStream("/"+gainPath);
                Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                resourceBundle = new PropertyResourceBundle(reader);
                cacheMap.put(gainPath, resourceBundle);
                reader.close();
            }
            return resourceBundle;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public synchronized void setBeanFactory(ElementFactory elementFactory) {

        PropertyUtil.propertiesLoader = elementFactory.getElement(PropertiesLoader.class);
        PropertyUtil.elementFactory = elementFactory;
    }


}
