package com.chainedminds.models;

import java.util.ArrayList;
import java.util.List;

public class _DbData {

    public String statement;

    public int generatedID;

    public List<String> columns = new ArrayList<>();
    public List<String> types = new ArrayList<>();
    public List<List<Object>> rows = new ArrayList<>();
}