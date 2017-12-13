package com.getmeashop.realestate.partner.database;

/**
 * Created by nikka on 9/8/15.
 */
public class User {
    private String fname = "";
    private String lname = "";
    private String id = "";
    private String pid = "";
    private String username = "";
    private String email = "";
    private String password = "";
    private String r_uri = "";
    private String synced = "";
    private String address = "";
    private String contact = "";
    private String isActv = "";
    private boolean isArchv = false;
    private String created = "";
    private String modified = "";
    private String city = "";
    private String storeinfoid = "";

    public User(String fname, String lname, String pid, String username, String email, String password,
                String r_uri, String contact, String address, String isActv, String created,
                String modified, String city, String storeInfoId) {
        this.fname = fname;
        this.lname = lname;
        this.pid = pid;
        this.email = email;
        this.password = password;
        this.r_uri = r_uri;
        this.contact = contact;
        this.address = address;
        this.isActv = isActv;
        this.username = username;
        this.created = created;
        this.modified = modified;
        this.city = city;
        this.storeinfoid = storeInfoId;
    }

    public User(String fname, String lname, String pid, String username, String email, String password,
                String r_uri, String contact, String address, String isActv, String created,
                String modified, String city) {
        this.fname = fname;
        this.lname = lname;
        this.pid = pid;
        this.email = email;
        this.password = password;
        this.r_uri = r_uri;
        this.contact = contact;
        this.address = address;
        this.isActv = isActv;
        this.username = username;
        this.created = created;
        this.modified = modified;
        this.city = city;
    }

    public User() {

    }

    public String getModified() {
        return modified;
    }

    public void setModified(String modified) {
        this.modified = modified;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public boolean getIsArchv() {
        return isArchv;
    }

    public void setIsArchv(boolean isArchv) {
        this.isArchv = isArchv;
    }

    public String getFname() {
        return fname;
    }

    public void setFname(String fname) {
        this.fname = fname;
    }

    public String getLname() {
        return lname;
    }

    public void setLname(String lname) {
        this.lname = lname;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getR_uri() {
        return r_uri;
    }

    public void setR_uri(String r_uri) {
        this.r_uri = r_uri;
    }

    public String getSynced() {
        return synced;
    }

    public void setSynced(String synced) {
        this.synced = synced;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getIsActv() {
        return isActv;
    }

    public void setIsActv(String isActv) {
        this.isActv = isActv;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getStoreinfo() {
        return storeinfoid;
    }

    public void setStoreinfo(String storeinfo) {
        this.storeinfoid = storeinfo;
    }
}
