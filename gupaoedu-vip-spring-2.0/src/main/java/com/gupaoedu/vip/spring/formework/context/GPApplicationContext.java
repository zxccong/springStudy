package com.gupaoedu.vip.spring.formework.context;

import com.gupaoedu.vip.spring.formework.core.GPBeanFactory;
import com.gupaoedu.vip.spring.formework.beans.config.GPBeanDefinition;
import com.gupaoedu.vip.spring.formework.beans.support.GPBeanDefinitionReader;
import com.gupaoedu.vip.spring.formework.beans.support.GPDefaultListableBeanFactory;

import java.util.List;
import java.util.Map;

/**
 * 按之前源码分析的套路，IOC、DI、MVC、AOP
 *
 * Created by Tom.
 */
public class GPApplicationContext extends GPDefaultListableBeanFactory implements GPBeanFactory {

    private String [] configLoactions;
    private GPBeanDefinitionReader reader;

    public GPApplicationContext(String... configLoactions){
        this.configLoactions = configLoactions;
        try {
            refresh();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void refresh() throws Exception{
        //1、定位，定位配置文件
        reader = new GPBeanDefinitionReader(this.configLoactions);

        //2、加载配置文件，扫描相关的类，把它们封装成BeanDefinition
        List<GPBeanDefinition> beanDefinitions = reader.loadBeanDefinitions();

        //3、注册，把配置信息放到容器里面(伪IOC容器)
        doRegisterBeanDefinition(beanDefinitions);

        //4、把不是延时加载的类，有提前初始化
        doAutowrited();
    }

    //只处理非延时加载的情况
    private void doAutowrited() {
        for (Map.Entry<String, GPBeanDefinition> beanDefinitionEntry : super.beanDefinitionMap.entrySet()) {
           String beanName = beanDefinitionEntry.getKey();
           if(!beanDefinitionEntry.getValue().isLazyInit()) {
               getBean(beanName);
           }
        }
    }

    private void doRegisterBeanDefinition(List<GPBeanDefinition> beanDefinitions) throws Exception {

        for (GPBeanDefinition beanDefinition: beanDefinitions) {
            if(super.beanDefinitionMap.containsKey(beanDefinition.getFactoryBeanName())){
                throw new Exception("The “" + beanDefinition.getFactoryBeanName() + "” is exists!!");
            }
            super.beanDefinitionMap.put(beanDefinition.getFactoryBeanName(),beanDefinition);
        }
        //到这里为止，容器初始化完毕
    }

    //依赖注入，从这里开始，通过读取BeanDefinition中的信息
    //然后，通过反射机制创建一个实例并返回
    //Spring做法是，不会把最原始的对象放出去，会用一个BeanWrapper来进行一次包装
    //装饰器模式：
    //1、保留原来的OOP关系
    //2、我需要对它进行扩展，增强（为了以后AOP打基础）
    public Object getBean(String beanName) {

//        //1、初始化
//        instantiateBean(beanName,new GPBeanDefinition());
//        //class A{ B b;}
//        //class B{ A a;}
//        //先有鸡还是先有蛋的问题，一个方法是搞不定的，要分两次
//
//        //2、注入
//        populateBean(beanName,new GPBeanDefinition(),new GPBeanWrapper());


        return null;
    }

//    private void populateBean(String beanName, GPBeanDefinition gpBeanDefinition, GPBeanWrapper gpBeanWrapper) {
//    }
//
//    private void instantiateBean(String beanName, GPBeanDefinition gpBeanDefinition) {
//    }
}
