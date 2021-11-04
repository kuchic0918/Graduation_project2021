package com.example.graduation_project;

public class MemberInfo {
    private  String photoUri;
    private String name;
    private String phoneNumber;
    private String birthDay;
    private String address;

    public MemberInfo(String name, String phoneNumber, String birthDay, String address,String photoUri){
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.birthDay = birthDay;
        this.address = address;
        this.photoUri = photoUri;
    }
    public MemberInfo(String name, String phoneNumber, String birthDay, String address){
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.birthDay = birthDay;
        this.address = address;
    }
    public String getName(){
        return this.name;
    }
    public void setName(String name){
        this.name = name;
    }
    public String getPhoneNumber(){
        return this.phoneNumber;
    }
    public void setPhoneNumber(String phoneNumber){
        this.phoneNumber = phoneNumber;
    }
    public String getBirthDay(){
        return this.birthDay;
    }
    public void setBirthDay(String birthDay){
        this.birthDay = birthDay;
    }
    public String getAddress(){
        return this.address;
    }
    public void setAddress(String address){
        this.address = address;
    }
    public String getPhotoUrl(){return this.photoUri;}
    public void setPhotoUrl(String photoUrl){this.photoUri = photoUrl;}
}

