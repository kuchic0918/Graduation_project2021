package com.example.graduation_project;

public class CommunityModel {
    String pId, pTitle, pContent, pLikes,pComments, pUri, pTime, uName, uEmail, uDp, uid;
    //커뮤니티 액티비티랑 같게 커뮤니티 창에 들어갈 정보

    public CommunityModel(String pId, String pTitle, String pContent, String pLikes, String pComments, String pUri, String pTime, String uName, String uEmail, String uDp, String uid) {
        this.pId = pId;
        this.pTitle = pTitle;
        this.pContent = pContent;
        this.pLikes = pLikes;
        this.pComments = pComments;
        this.pUri = pUri;
        this.pTime = pTime;
        this.uName = uName;
        this.uEmail = uEmail;
        this.uDp = uDp;
        this.uid = uid;
    }

    public String getpId() {
        return pId;
    }

    public void setpId(String pId) {
        this.pId = pId;
    }

    public String getpTitle() {
        return pTitle;
    }

    public void setpTitle(String pTitle) {
        this.pTitle = pTitle;
    }

    public String getpContent() {
        return pContent;
    }

    public void setpContent(String pContent) {
        this.pContent = pContent;
    }

    public String getpLikes() {
        return pLikes;
    }

    public void setpLikes(String pLikes) {
        this.pLikes = pLikes;
    }

    public String getpComments() {
        return pComments;
    }

    public void setpComments(String pComments) {
        this.pComments = pComments;
    }

    public String getpUri() {
        return pUri;
    }

    public void setpUri(String pUri) {
        this.pUri = pUri;
    }

    public String getpTime() {
        return pTime;
    }

    public void setpTime(String pTime) {
        this.pTime = pTime;
    }

    public String getuName() {
        return uName;
    }

    public void setuName(String uName) {
        this.uName = uName;
    }

    public String getuEmail() {
        return uEmail;
    }

    public void setuEmail(String uEmail) {
        this.uEmail = uEmail;
    }

    public String getuDp() {
        return uDp;
    }

    public void setuDp(String uDp) {
        this.uDp = uDp;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public CommunityModel() {

    }
}