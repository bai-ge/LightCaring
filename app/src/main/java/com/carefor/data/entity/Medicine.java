package com.carefor.data.entity;

import android.os.Parcel;
import android.os.Parcelable;

import com.carefor.util.Tools;

/**
 * Created by baige on 2018/4/19.
 */

public class Medicine implements Parcelable {
    String name;
    String dosage;

    public Medicine() {
    }
    public Medicine(String name, String dosage) {
        this.name = name;
        this.dosage = dosage;
    }
    public Medicine(Parcel source) {
        this.name = source.readString();
        this.dosage = source.readString();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(dosage);
    }

    public static final Parcelable.Creator<Medicine> CREATOR = new Creator<Medicine>() {
        @Override
        public Medicine createFromParcel(Parcel source) {
            return new Medicine(source);
        }

        @Override
        public Medicine[] newArray(int size) {
            return new Medicine[size];
        }
    };

    @Override
    public boolean equals(Object obj) {
        if(obj == null){
            return false;
        }
        if(obj instanceof Medicine){
            Medicine medicine = (Medicine) obj;
            if(Tools.isEquals(getName(), medicine.getName()) && Tools.isEquals(getDosage(), medicine.getDosage())){
                return true;
            }
        }
        return super.equals(obj);
    }

    @Override
    public String toString() {
        return "Medicine{" +
                "name='" + name + '\'' +
                ", dosage='" + dosage + '\'' +
                '}';
    }

}
