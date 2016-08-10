package com.example.zack.triggersafe;

/**
 * Created by Zack on 8/10/2016.
 */
public class Officer{
    private String officerName;
    private String tagID;

    public Officer(String name, String id){
        officerName = name;
        tagID = id;
    }
    public String getTagID(){
        return tagID;
    }
    public String getName(){
        return officerName;
    }
}