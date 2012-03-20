package jp.gr.java_conf.fragile.commons.container.config;

import java.util.Map;

import org.junit.Test;


/**
 * @author syuu256\gmail.com
 */
public class XMLPropertiesTest {

    /**
     *
     */
    @Test
    public void testGetInstance() {

        try {
            XMLProperties.getInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Map<String, BeanMetaData> map = XMLProperties.getInstance().getBeanMetaDatas();
        StringBuilder sb = new StringBuilder();
        for (final BeanMetaData beanMetaData : map.values())
            sb.append(beanMetaData.toString() + "\r\n");

        System.out.println(sb.toString());
    }
}
