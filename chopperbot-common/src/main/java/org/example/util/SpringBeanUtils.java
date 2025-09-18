package org.example.util;

import lombok.Getter;
import lombok.NonNull;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;


/**
 * @author wangmin
 * @version 1.0
 * @description spring 上下文获取
 * @date 2024/4/3 14:03
 */
@Component
public class SpringBeanUtils implements ApplicationContextAware, BeanPostProcessor, DisposableBean {
    /**
     * 获取spring上下文
     */
    @Getter
    private static ApplicationContext applicationContext;

    /**
     * 通过name,以及Clazz返回指定的Bean
     */
    public static <T> T getBean(String name, Class<T> clazz) {
        return getApplicationContext().getBean(name, clazz);
    }

    /**
     * 通过class获取Bean
     */
    public static <T> T getBean(Class<T> clazz) {
        return getApplicationContext().getBean(clazz);
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        if (SpringBeanUtils.applicationContext == null) {
            SpringBeanUtils.applicationContext = applicationContext;
        }
    }

    @Override
    public void destroy() {
        applicationContext = null;
    }
}
