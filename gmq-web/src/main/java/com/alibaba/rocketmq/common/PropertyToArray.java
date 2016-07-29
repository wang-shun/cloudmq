package com.alibaba.rocketmq.common;

import com.alibaba.rocketmq.domain.gmq.User;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * 对象通过注解反射生成单元格
 *
 * @author gaoyanlei
 * @since 2016/7/12
 */
public class PropertyToArray {

    public static String[] EntityToPropertyArray(Class<?> t) {
        Field[] fields = t.getDeclaredFields();
        List fieldList = new ArrayList();
        for (Field f : fields) {
            FieldMeta meta = f.getAnnotation(FieldMeta.class);
            if (meta != null) {
                fieldList.add(f.getName());
            }
        }
        return (String[]) fieldList.toArray(new String[fieldList.size()]);
    }


    public static void main(String[] args) {
        System.out.println(EntityToPropertyArray(User.class));
    }
}
