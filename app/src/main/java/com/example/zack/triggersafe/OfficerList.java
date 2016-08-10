package com.example.zack.triggersafe;

/**
 * Created by Zack on 8/10/2016.
 */
public class OfficerList {
    private int versionNumber;
    private int officerCount;
    private Officer[] officers;

    public OfficerList(int version, Officer[] newOfficers){
        versionNumber = version;
        officers = newOfficers;
        officerCount = officers.length;
    }
    public int getVersionNumber(){
        return versionNumber;
    }
    public int getCount(){
        return officerCount;
    }
    public Officer[] getOfficers(){
        return officers;
    }
    public String getOfficerName(String id){
        for(Officer o : officers){
            if(o.getTagID().equals(id)){
                return o.getName();
            }
        }
        return null;
    }
    public String getTagString(){
        String tagString = "";
        for(Officer officer: officers){
            tagString += officer.getTagID();
            tagString += ",";
        }
        return tagString;
    }
}
