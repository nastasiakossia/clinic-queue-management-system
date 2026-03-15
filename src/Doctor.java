public class Doctor implements HasKey {
    public final String doctorId;
    Patient head, tail;
    Doctor loadPrev, loadNext;
    int queueSize;

    public Doctor(String doctorId) {
        this.doctorId = doctorId;
        this.queueSize = 0;
        this.head = null;
        this.tail = null;
    }

    @Override
    public String id() {
        return doctorId;
    }
}