package io.oreto.gungnir.test.data;


import java.util.List;

public class DataRepo implements IRepo {

    @Override
    public List<String> list() {
       return List.of("a", "b", "c");
    }
}
