package demo.service.impl;

import demo.annotation.MyService;
import demo.service.IDemoService;

@MyService
public class DemoService implements IDemoService {
    public String get(String name) {
        return "My name is" +name;
    }
}
