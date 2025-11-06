package com.tc.common.xmlUtil;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Author jiangzhou
 * Date 2023/8/28
 * Description TODO
 **/
public class XMLUtil {

    /**
     * @Description: 判断是否为注册消息
     * @Param: [msg]
     * @return: boolean
     * @Author: jiangzhou
     * @Date: 2023/8/28
     */
    public static boolean isRegisterMsg(String msg) {
        return msg.contains("<Type>251</Type>") && msg.contains("<Command>1</Command>");
    }

//    public static <T> T fromXml(String xml, Class<T> clazz) {
//        try {
//            StringReader reader = new StringReader(xml);
//            JAXBContext context = JAXBContext.newInstance(clazz.getClass());
//            Unmarshaller unmarshaller = context.createUnmarshaller();
//            return (T) unmarshaller.unmarshal(reader);
//        } catch (JAXBException e) {
//            e.printStackTrace();
//            return null;
//        }
//    }

    /**
     * @Description: 获取xml消息类型
     * @Param: [msg]
     * @return: java.lang.String
     * @Author: jiangzhou
     * @Date: 2023/8/28
     */
    public static String getXMLType(String msg) {
        Pattern pattern = Pattern.compile("<Type>(.*?)</Type>");// 匹配的模式
        Matcher m = pattern.matcher(msg);
        while (m.find()) {
            return m.group(1);
        }
        return "";
    }

    /**
     * @Description: 获取指令类型
     * @Param: [msg]
     * @return: java.lang.String
     * @Author: jiangzhou
     * @Date: 2023/8/28
     */
    public static String getCommandType(String msg) {
        Pattern pattern = Pattern.compile("<Command>(.*?)</Command>");// 匹配的模式
        Matcher m = pattern.matcher(msg);
        while (m.find()) {
            return m.group(1);
        }
        return "";
    }

    public static <T> T xmlToClass(String s, Class... classesToBeBound) {
        StringReader reader = new StringReader(s);
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(classesToBeBound);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            return (T) jaxbUnmarshaller.unmarshal(reader);
        } catch (JAXBException e) {
            return null;
        }
    }

}
