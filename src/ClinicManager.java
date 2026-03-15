/**
 * Clinic Queue Management System.
 *
 * This class manages doctors and patients using custom 2–3 tree indexes.
 * It supports:
 * - dynamic doctor and patient insertion/removal
 * - automatic assignment to the least busy doctor
 * - fast load statistics queries
 * - retrieving the busiest doctors
 *
 * All core operations run in O(log n) time.
 */
public class ClinicManager {
    private final Tree<Doctor> doctorsTree;
    private final Tree<Patient> patientsTree;
    private final QueueTree queueTree;

    public ClinicManager() {
        this.doctorsTree = new Tree<>();
        this.patientsTree = new Tree<>();
        this.queueTree = new QueueTree();
    }



    // ====================
    // Queue helper methods
    // ====================

    /**
     * Appends a patient to the end of a doctor's queue.
     */
    private static void enqueuePatient(Doctor d, Patient p) {
        p.prev = d.tail;
        p.next = null;

        if (d.tail != null) {
            d.tail.next = p;
        } else {
            d.head = p;
        }
        d.tail = p;
        d.queueSize++;
    }

    /**
     * Removes a patient from the doctor's queue.
     */
    private static void removeFromQueue(Patient p) {
        Doctor d = p.doctor;

        if (p.prev != null) {
            p.prev.next = p.next;
        } else {
            d.head = p.next;
        }

        if (p.next != null) {
            p.next.prev = p.prev;
        } else {
            d.tail = p.prev;
        }

        p.prev = null;
        p.next = null;

        d.queueSize--;
    }

    /**
     * Removes a doctor from the load bucket corresponding to the current queue size.
     */
    private void queueMinus(int queueSize, Doctor d){
        QNode q = queueTree.search(queueSize);
        if (q == null) {
            throw new IllegalStateException("queueMinus: key " + queueSize + " not found");
        }
        queueTree.removeDoctorFromBucket(q, d);
        if (q.docCount == 1) {
            queueTree.delete(q);
        } else {
            q.docCount--;
            queueTree.fixUp(q);
        }
    }

    /**
     * Inserts a doctor into the load bucket corresponding to the current queue size.
     */
    private void queuePlus(int queueSize, Doctor d){
        QNode q_new = queueTree.search(queueSize);
        if (q_new == null) {
            queueTree.insert(queueSize, d);
        } else {
            q_new.docCount++;
            queueTree.addDoctorToBucket(q_new, d);
            queueTree.fixUp(q_new);
        }
    }



    // ====================
    // Doctor management
    // ====================

    private void doctorRemove(String key) {
        doctorsTree.delete(key);
    }

    public void doctorEnter(String doctorId) {
        if (doctorId == null) {
            throw new IllegalArgumentException("doctorId cannot be null");
        }

        if (doctorsTree.search(doctorId) != null) {
            throw new IllegalArgumentException("Doctor already exists in the system");
        }

        Doctor d = new Doctor(doctorId);
        doctorsTree.insert(d);
        queuePlus(d.queueSize, d);
    }

    public void doctorLeave(String doctorId) {
        if (doctorId == null) {
            throw new IllegalArgumentException("doctorId cannot be null");
        }
        Doctor d = doctorsTree.search(doctorId);
        if (d == null) {
            throw new IllegalArgumentException("There is no doctor with such ID");
        }
        if (d.queueSize != 0) {
            throw new IllegalArgumentException("The doctor still has patients");
        }
        queueMinus(d.queueSize, d);
        doctorRemove(doctorId);
    }

    public int numPatients(String doctorId) {
        if (doctorId == null) {
            throw new IllegalArgumentException("doctorID cannot be null");
        }
        Doctor d = doctorsTree.search(doctorId);
        if (d == null) {
            throw new IllegalArgumentException("There is no doctor with such ID");
        }
        return d.queueSize;
    }

    public String nextPatient(String doctorId) {
        if (doctorId == null) {
            throw new IllegalArgumentException("doctorID cannot be null");
        }
        Doctor d = doctorsTree.search(doctorId);
        if (d == null) {
            throw new IllegalArgumentException("There is no doctor with such ID");
        }
        if (d.queueSize == 0) {
            throw new IllegalArgumentException("The doctor does not have any patients");
        }
        return d.head.patientId;
    }



    // ====================
    // Patient management
    // ====================

    private void patientRemove(String key) {
        patientsTree.delete(key);
    }

    public void patientEnter(String doctorId, String patientId) {
        if (doctorId == null) {
            throw new IllegalArgumentException("doctorId cannot be null");
        }
        if (patientId == null) {
            throw new IllegalArgumentException("patientID cannot be null");
        }
        Doctor d = doctorsTree.search(doctorId);
        if (d == null) {
            throw new IllegalArgumentException("There is no doctor with such ID");
        }
        if (patientsTree.search(patientId) != null) {
            throw new IllegalArgumentException("There is already a patient with such ID");
        }
        Patient p = new Patient(patientId, d);
        patientsTree.insert(p);
        queueMinus(d.queueSize, d);
        enqueuePatient(d, p);
        queuePlus(d.queueSize, d);
    }

    /**
     * Assigns a new patient to the least busy doctor.
     */
    public void patientEnter(String patientId) {
        String doctorId = getLeastBusyDoctor();
        patientEnter(doctorId, patientId);
    }

    public String nextPatientLeave(String doctorId) {
        if (doctorId == null) {
            throw new IllegalArgumentException("doctorID cannot be null");
        }
        Doctor d = doctorsTree.search(doctorId);
        if (d == null) {
            throw new IllegalArgumentException("There is no doctor with such ID");
        }
        if (d.queueSize == 0) {
            throw new IllegalArgumentException("The doctor does not have any patients");
        }
        Patient p = d.head;
        queueMinus(d.queueSize, d);
        removeFromQueue(p);
        queuePlus(d.queueSize, d);
        patientRemove(p.patientId);
        return p.patientId;
    }

    public void patientLeaveEarly(String patientId) {
        if (patientId == null) {
            throw new IllegalArgumentException("patientID cannot be null");
        }
        Patient p = patientsTree.search(patientId);
        if (p == null) {
            throw new IllegalArgumentException("There is no patient with such ID");
        }
        Doctor d = p.doctor;
        queueMinus(d.queueSize, d);
        removeFromQueue(p);
        queuePlus(d.queueSize, d);
        patientRemove(p.patientId);
    }

    public String waitingForDoctor(String patientId) {
        if (patientId == null) {
            throw new IllegalArgumentException("patientID cannot be null");
        }
        Patient p = patientsTree.search(patientId);
        if (p == null) {
            throw new IllegalArgumentException("There is no patient with such ID");
        }
        return p.doctor.doctorId;
    }



    // ====================
    // Range queries
    // ====================

    /**
     * Returns the number of doctors whose load is within the inclusive range [low, high].
     */
    public int numDoctorsWithLoadInRange(int low, int high) {

        if (low > high || queueTree.root == null) return 0;

        int hiKey = queueTree.upperBound(high);
        if (hiKey == -1) return 0;

        int loMinus1Key = queueTree.upperBound(low - 1);
        int hi = queueTree.sumOfDoctors(hiKey);
        int lo = (loMinus1Key == -1) ? 0 : queueTree.sumOfDoctors(loMinus1Key);

        return hi - lo;
    }

    /**
     * Returns the average doctor load within the inclusive range [low, high].
     * Returns 0 if the range contains no doctors.
     */
    public int averageLoadWithinRange(int low, int high) {
        if (low > high || queueTree.root == null) return 0;

        int hiKey = queueTree.upperBound(high);
        if (hiKey == -1) return 0;

        int loMinus1Key = queueTree.upperBound(low - 1);

        int doctors = numDoctorsWithLoadInRange(low, high);
        if (doctors == 0) return 0;

        int sumHi = queueTree.sumOfPatients(hiKey);
        int sumLo = (loMinus1Key == -1) ? 0 : queueTree.sumOfPatients(loMinus1Key);

        return (sumHi - sumLo) / doctors;
    }



    // ====================
    // Advanced analytics
    // ====================

    /**
     * Returns the ID of a doctor with the minimum current load.
     */
    public String getLeastBusyDoctor() {
        if (queueTree.root == null) {
            throw new IllegalArgumentException("There is no doctor");
        }
        QNode x = queueTree.root;
        while (!x.isLeaf()) {
            x = x.left;
        }
        return x.headDoctor.id();
    }

    /**
     * Returns the IDs of the k busiest doctors in descending load order.
     * If fewer than k doctors exist, returns all doctors.
     */
    public String[] getTopKBusyDoctors(int k) {
        if (k <= 0) {
            throw new IllegalArgumentException("k must be positive");
        }
        if (queueTree.root == null) {
            return new String[0];
        }
        String[] doctors = new String[k];
        QNode node = queueTree.maxLeaf();
        int load = node.key;
        int count = 0;

        while (count < k && load >= 0) {
            QNode current = queueTree.search(load);

            if (current != null) {
                Doctor d = current.headDoctor;
                while (d != null && count < k) {
                    doctors[count] = d.doctorId;
                    count++;
                    d = d.loadNext;
                }
            }

            load--;
        }
        if (count == k) {
            return doctors;
        }

        String[] trimmed = new String[count];
        for (int i = 0; i < count; i++) {
            trimmed[i] = doctors[i];
        }
        return trimmed;
    }
}