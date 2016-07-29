package com.alibaba.rocketmq.util.restful.handle;

import com.alibaba.rocketmq.util.MyBeanUtils;
import com.alibaba.rocketmq.util.restful.domian.AbstractEntity;
import org.apache.commons.beanutils.BeanUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * @author gaoyanlei
 * @since 2016/7/25
 */

public class ObjectHandle {

    private static final int successCode = 200;

    @SuppressWarnings("unchecked")
    public static <T> List<T> getForList(AbstractEntity abstractEntity, Class<T> clazz) throws RuntimeException {
        try {
            if (abstractEntity.getStatus().getCode() == successCode) {
                List list = (List) abstractEntity.getData();
                List<T> ts = new ArrayList<T>();
                for (int i = 0; i < list.size(); i++) {
                    T o = clazz.newInstance();
                    MyBeanUtils.copyMap2Bean(o, (Map) list.get(i));
                    ts.add(o);
                }
                return ts;
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException("transform to list bean error. msg=" + e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T getForObject(AbstractEntity abstractEntity, Class<T> clazz) throws RuntimeException {
        try {
            if (abstractEntity.getStatus().getCode() == successCode) {
                T object = clazz.newInstance();
                BeanUtils.populate(object, (Map<String, Object>) abstractEntity.getData());
                return object;
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException("transform to Object bean error. msg=" + e.getMessage(), e);
        }
    }
}
