package com.teraime.poppyfield.room;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.teraime.poppyfield.base.ValueProps;

import java.util.HashMap;
import java.util.Map;

@Entity(tableName = "variabler")
public class VariableTable {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private final int id;
    @NonNull
    @ColumnInfo(name = "UUID")
    private final String UUID;
    @ColumnInfo(name = "L1")
    private final String L1;
    @ColumnInfo(name = "L2")
    private final String L2;
    @ColumnInfo(name = "L3")
    private final String L3;
    @ColumnInfo(name = "L4")
    private final String L4;
    @ColumnInfo(name = "L5")
    private final String L5;
    @ColumnInfo(name = "L6")
    private final String L6;
    @ColumnInfo(name = "L7")
    private final String L7;
    @ColumnInfo(name = "L8")
    private final String L8;
    @ColumnInfo(name = "L9")
    private final String L9;
    @ColumnInfo(name = "L10")
    private final String L10;
    @ColumnInfo(name = "var")
    private final String var;
    @ColumnInfo(name = "value")
    private final String value;
    @ColumnInfo(name = "lag")
    private final String lag;
    @ColumnInfo(name = "author")
    private final String author;
    @ColumnInfo(name = "timestamp")
    private final long  timestamp;
    @ColumnInfo(name = "year")
    private final String  year;

    public int getId()    { return id; }

    public String getL1() {
        return L1;
    }

    public String getL2() {
        return L2;
    }

    public String getL3() {
        return L3;
    }

    public String getL4() {
        return L4;
    }

    public String getL5() {
        return L5;
    }

    public String getL6() {
        return L6;
    }

    public String getL7() {
        return L7;
    }

    public String getL8() {
        return L8;
    }

    public String getL9() {
        return L9;
    }

    public String getL10() {
        return L10;
    }

    public String getVar() {
        return var;
    }

    public String getValue() {
        return value;
    }

    public String getLag() {
        return lag;
    }

    public String getAuthor() {
        return author;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getUUID() {
        return UUID;
    }

    public String getYear() {
        return year;
    }

    public VariableTable(int id,@NonNull String UUID, String year, String var, String value, String lag, String author, long timestamp, String L1, String L2, String L3, String L4, String L5, String L6, String L7, String L8, String L9, String L10) {
        this.id=id;
        this.UUID=UUID;
        this.year=year;
        this.var = var;
        this.value = value;
        this.lag = lag;
        this.author = author;
        this.timestamp = timestamp;
        this.L1 = L1;
        this.L2 = L2;
        this.L3 = L3;
        this.L4 = L4;
        this.L5 = L5;
        this.L6 = L6;
        this.L7 = L7;
        this.L8 = L8;
        this.L9 = L9;
        this.L10 = L10;
    }

    public ValueProps toMap() {
        HashMap<String, String> ret = new HashMap<String, String>();
        ret.put("value",value);
        ret.put("author",author);
        ret.put("UUID",UUID);
        return new ValueProps(ret);
    }
}
