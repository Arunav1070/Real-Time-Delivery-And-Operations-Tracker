package com.example.realtimedeliveryandoperationstracker;

public class Job {
    private String id;
    private String title;
    private String description;
    private String status;

    public Job(){}

    public Job(String id, String title, String status) {
        this.id = id;
        this.title = title;
        this.status = status;
    }

    public Job(String id, String title, String description, String status) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
    }

    public String getId() {return id;}
    public String getTitle() {return title;}
    public String getDescription() {return description;}
    public String getStatus() {return status;}

    public void setId(String id) {this.id = id;}
    public void setTitle(String title) {this.title = title;}
    public void setDescription(String description) {this.description = description;}
    public void setStatus(String status) {this.status = status;}
}
