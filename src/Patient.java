public class Patient implements HasKey {
    public final String patientId;
    Doctor doctor;
    Patient prev, next;

    public Patient(String patientId, Doctor doctor) {
        this.patientId = patientId;
        this.doctor = doctor;
    }

    @Override
    public String id() {
        return patientId;
    }
}