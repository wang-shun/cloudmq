package com.alibaba.rocketmq.util.restful.handle;

import com.alibaba.rocketmq.util.MyBeanUtils;
import com.alibaba.rocketmq.util.restful.domian.AbstractEntity;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * @author gaoyanlei
 * @since 2016/7/25
 */

public class ObjectHandle {
    public static <T> List<T> getForList(AbstractEntity abstractEntity, Class<T> responseType)
            throws IllegalAccessException, InstantiationException, InvocationTargetException {
        if (abstractEntity.getStatus().getCode() == 200) {
            List list = (List) abstractEntity.getData();
            List<T> ts = new ArrayList<T>();
            for (int i = 0; i < list.size(); i++) {
                T o = responseType.newInstance();
                MyBeanUtils.copyMap2Bean(o, (Map) list.get(i));
                ts.add(o);
            }
            return ts;
        }
        return null;
    }


    public static <T> T getForObject(AbstractEntity abstractEntity, Class<T> responseType)
            throws IllegalAccessException, InstantiationException, InvocationTargetException {
        if (abstractEntity.getStatus().getCode() == 200) {
            T o = responseType.newInstance();
            MyBeanUtils.copyMap2Bean(o, (Map) abstractEntity.getData());
            return o;
        }
        return null;
    }
}
