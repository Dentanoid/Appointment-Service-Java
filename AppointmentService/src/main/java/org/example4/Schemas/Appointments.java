package org.example4.Schemas;
import org.bson.Document;

public class Appointments implements CollectionSchema {
    String clinic_id;
    String dentist_id;
    String patient_id;
    String start_time;
    String end_time;
    String appointment_id;

    public Appointments() {
        this.clinic_id = " ";
        this.dentist_id = " ";
        this.patient_id = " ";
        this.start_time = " ";
        this.end_time = " ";
        this.appointment_id = " ";
    }

    public Document getDocument() {
        return new Document("clinic_id", this.clinic_id)
              .append("dentist_id", this.dentist_id)
              .append("patient_id", this.patient_id)
              .append("start_time", this.start_time)
              .append("end_time", this.end_time)
              .append("appointment_id", this.appointment_id);
    }
}
