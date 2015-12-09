package com.example.pb.voicememos;

import java.util.Date;
import java.util.UUID;

public class AudioRecord {
    private Date date;
    private String name;

    public AudioRecord(Date date, String name) {
        this.date = date;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Date getDate() {
        return date;
    }


}
