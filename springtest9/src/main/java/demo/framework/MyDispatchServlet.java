package demo.framework;

import demo.annotation.*;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

public class MyDispatchServlet extends HttpServlet {

    public MyDispatchServlet() {
        super();
    }

    //定义一个全局的文件 用来存储xml文件读取出来的东西
    private Properties contextConfig = new Properties();

    //获得所有类名的列表
    private List<String> classNames= new ArrayList();

    //IOC容器
    private Map<String ,Object> ioc = new HashMap<String, Object>();

    private Map<String,Method> handlerMapping = new HashMap<String, Method>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req,resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //运行时的逻辑
        doDispathch(req,resp);
    }

    private void doDispathch(HttpServletRequest req, HttpServletResponse resp) {
        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        url=url.replace(contextPath,"").replaceAll("/+","/");
        if (!this.handlerMapping.containsKey(url)){
            try {
                resp.getWriter().write("404 Not Found!!");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        System.out.println(this.handlerMapping.get(url));

    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        //初始化阶段的入口

        //1.加载配置文件
        doLoadConfig(config.getInitParameter("contextConfigLocation"));
        //和xml里面的文件相同获得配置文件（获得文件路径）

        //2.扫描所有相关的类
        doScanner(contextConfig.getProperty("scanPackage"));

        //3.初始化所有扫描到的相关的类，并且将其保存到IOC容器之中
        doInstance();

        //4.完成DI操作，实际上就是自动赋值（依赖注入）
        doAutowired();

        //5.初始化HandlerMapping 把Url和Method一对一关联上
        initHandlerMapping();
        System.out.println("My Spring is init");
    }

    private void initHandlerMapping() {
        if (ioc.isEmpty()){
            return;
        }

        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            Class<?> clazz = entry.getValue().getClass();
            if (clazz.isAnnotationPresent(MyController.class)){
                continue;
            }
            String  baseUrl="";
            if (clazz.isAnnotationPresent(MyRequestMapping.class)){
                MyRequestMapping requestMapping = clazz.getAnnotation(MyRequestMapping.class);
                 baseUrl = requestMapping.value();
            }
            Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                if (!method.isAnnotationPresent(MyRequestParam.class)) {
                    continue;
                }
                MyRequestMapping requestMapping = method.getAnnotation(MyRequestMapping.class);
                String url = requestMapping.value();
                 url = baseUrl + "/" + url.replaceAll("/+","/");

                handlerMapping.put(url,method);
                System.out.println("Mapped : "+url+","+method);

            }
        }
    }

    private void doAutowired() {
        if (ioc.isEmpty()){
            return;
        }
        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            //只认你又没有加注解Autowired这个注解
            //private public default
            // Returns an array of {@code Field} objects reflecting all the fields
            //返回反映所有字段的{@code Field}对象数组
            Field[] fields = entry.getValue().getClass().getDeclaredFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(MyAutowried.class)){
                    continue;
                }
                MyAutowried autowried = field.getAnnotation(MyAutowried.class);

                String beanName = autowried.value().trim();
                //如果没有自己输入名字
                if ("".equals(beanName)){
                    //那就获得首字母小写的值
                    beanName= field.getType().getName();
                }
                //授权，强制访问
                field.setAccessible(true);

                try {
                    field.set(entry.getValue(),ioc.get(beanName));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void doInstance() {
        if (classNames.isEmpty()){
            return;
        }
        for (String className:classNames){

            try {
                Class<?> clazz = Class.forName(className);
                //用反射机制实现实例化
                //将实例化的对象放入IOC容器
                //神秘的IOC容器实际上就是一个Map

//                ioc.put(key,instance);

                if(clazz.isAnnotationPresent(MyController.class)){
                    Object instance = clazz.newInstance();
                    //1.采用默认的类名首字母小写作为key
                    String beanName = lowerFirstCase(clazz.getSimpleName());
                    ioc.put(beanName,instance);

                }else if (clazz.isAnnotationPresent(MyService.class)){
                    Object instance = clazz.newInstance();

                    //2.可以自定义beanName作为key
                    MyService service = clazz.getAnnotation(MyService.class);
                    String beanName = service.value();

                    if ("".equals(beanName.trim())){
                        beanName= lowerFirstCase(clazz.getSimpleName());
                    }

                    //3.如果是接口怎么办，不能实例化，就是把接口的实现类放入IOC容器之中
                    Class<?>[] interfaces = clazz.getInterfaces();
                    for (Class<?> i :  interfaces) {
                        ioc.put(i.getName(),instance);
                    }

                }else {
                    //没有加注解可以直接忽略
                    continue;
                }


            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private String lowerFirstCase(String simpleName) {
        char[] chars = simpleName.toCharArray();
        chars[0] +=32;
        return String.valueOf(chars);
    }

    private void doScanner(String scanPackage) {
        URL url = this.getClass().getClassLoader().getResource("/" + scanPackage.replaceAll("\\.", "/"));
        File classDir = new File(url.getFile());
        for (File file : classDir.listFiles()) {
            if (file.isDirectory()){
                doScanner(scanPackage+"."+file.getName());
            }else {
                String className=scanPackage+"."+file.getName().replace(".class","");
                classNames.add(className);
            }
        }

    }

    private void doLoadConfig(String contextConfigLocation) {
        //获得文件路径后直接读取出流
        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation);
        try {
            contextConfig.load(resourceAsStream);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (resourceAsStream != null){
                try {
                    resourceAsStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
