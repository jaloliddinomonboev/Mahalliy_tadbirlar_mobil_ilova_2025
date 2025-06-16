package com.example.myeventmanagement;

public class User {
    private String uid; // Firebase foydalanuvchi UID'si
    private String name;
    private String email;
    private String phone;
    private String profileImage;
    private boolean isApproved;
    private String role;

    // 🔹 Bo'sh konstruktor (Firebase uchun kerak)
    public User() {}

    // 🔹 To'liq konstruktor
    public User(String uid, String name, String email, String phone, String profileImage, boolean isApproved, String role) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.profileImage = profileImage;
        this.isApproved = isApproved;
        this.role = role;
    }

    // 🔹 Getter va Setter metodlari
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public boolean isApproved() {
        return isApproved;
    }

    public void setApproved(boolean approved) {
        isApproved = approved;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
