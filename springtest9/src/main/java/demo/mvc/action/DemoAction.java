package demo.mvc.action;

import demo.annotation.MyAutowried;
import demo.annotation.MyController;
import demo.annotation.MyRequestMapping;
import demo.annotation.MyRequestParam;
import demo.service.IDemoService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@MyController
@MyRequestMapping("/demo")
public class DemoAction {

    @MyAutowried
    private IDemoService demoService;

    @MyRequestMapping("/query.json")
    public void query(HttpServletRequest req, HttpServletResponse resp,@MyRequestParam("name") String name){
        String result = demoService.get(name);
        try {
            resp.getWriter().write(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @MyRequestMapping("/add.json")
    public void add (HttpServletRequest req, HttpServletResponse resp,@MyRequestParam("a") Integer a,@MyRequestParam("b")  Integer b){
        try {
            resp.getWriter().write(a+" + "+b+ " = "+(a+b));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @MyRequestMapping("/remove.json")
    public void remove (HttpServletRequest req, HttpServletResponse resp,@MyRequestParam("id")  Integer id){

    }
}
