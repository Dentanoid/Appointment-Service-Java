package org.example4.Schemas;
import org.bson.Document;

public class AvailableTimes implements CollectionSchema {
    String clinic_id;
    String dentist_id;
    String start_time;
    String end_time;

    public AvailableTimes() {
        this.clinic_id = " ";
        this.dentist_id = " ";
        this.start_time = " ";
        this.end_time = " ";
    }

    public Document getDocument() {
        return new Document("clinic_id", this.clinic_id)
              .append("dentist_id", this.dentist_id)
              .append("start_time", this.start_time)
              .append("end_time", this.end_time);
    }
}
