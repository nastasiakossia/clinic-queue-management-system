# Clinic Queue Management System with Custom 2–3 Trees

A clinic queue management system implemented in **Java** using **custom balanced 2–3 trees**.

The system manages doctors and patients, maintains doctor queues, and supports efficient workload analytics.

All core operations are designed to run in **O(log n)** time.

---

# Overview

This project simulates a clinic where doctors receive patients and maintain queues. Patients may join a specific doctor’s queue, automatically join the least busy doctor, or leave early. The system also supports efficient analytics on doctor workloads.

To achieve efficient performance, the system is built on top of several custom data structures rather than relying on standard library collections.

The design ensures that insertions, deletions, and queries remain efficient even as the system grows.

---

# Key Features

### Doctor management
Add or remove doctors from the system.
- `doctorEnter(id)`
- `doctorLeave(id)`

A doctor can leave the system only when their queue is empty.

---

### Patient management
Patients can join queues, leave early, or be served in FIFO order.

- `patientEnter(doctorId, patientId)`
- `patientEnter(patientId)`
- `patientLeaveEarly(patientId)`
- `nextPatientLeave(doctorId)`

Patients may either choose a specific doctor or be automatically assigned to the **least busy doctor**.

---

### Queries
The system supports efficient queries about doctor queues and patient assignments.

- `numPatients(doctorId)`
- `nextPatient(doctorId)`
- `waitingForDoctor(patientId)`

These queries allow the system to determine the next patient, the number of waiting patients, and which doctor a patient is assigned to.

---

### Workload analytics
The system also provides analytical queries over doctor workloads.

- `numDoctorsWithLoadInRange(low, high)`
- `averageLoadWithinRange(low, high)`
- `getLeastBusyDoctor()`
- `getTopKBusyDoctors(k)`


These operations allow the system to:
- count doctors whose workload lies in a given range
- compute the average workload of doctors within a range
- retrieve the least busy doctor
- retrieve the **top-k busiest doctors**

---

# Data Structures
The project implements several custom data structures.

### Tree<T>
A **generic balanced 2–3 search tree** used to store doctors and patients.

Supported operations:
- search
- insertion
- deletion

All operations run in **O(log n)** time.

---

### QueueTree
A specialized index that tracks doctor workloads.

Each node represents a specific load level and maintains:
- the number of doctors with that load
- a linked list of doctors with that load

This structure enables efficient range queries over doctor workloads.

---

### Doctor
Represents a doctor in the system.

Each doctor maintains:
- a unique doctor ID
- the current queue size
- a doubly linked list of patients waiting in the queue

---

### Patient
Represents a patient waiting for a doctor.

Each patient stores:
- patient ID
- reference to the assigned doctor
- pointers used to maintain the doctor's queue

---

### ClinicManager
The main system API.

This class coordinates all data structures:
- doctor index
- patient index
- workload index

It ensures that all structures remain consistent after each operation.

---

# Complexity

| Operation | Time Complexity |
|-----------|----------------|
Doctor insertion | O(log n) |
Patient insertion | O(log n) |
Patient removal | O(log n) |
Range queries | O(log n) |
Top-k busiest doctors | O(k + log n) |

---

# Project Structure
ClinicQueueSystem
│
├── ClinicManager.java
├── Tree.java
├── QueueTree.java
├── Doctor.java
├── Patient.java
├── Node.java
├── QNode.java
├── HasKey.java
└── Main.java


`Main.java` contains a test runner that validates the system functionality.

---

Motivation

This project demonstrates how custom data structures such as balanced search trees can be used to design efficient systems for real-world tasks like queue management and workload analytics.

Instead of relying on standard collections, the system implements its own indexing structures to support fast operations and analytical queries.


Author

Anastasia Kondrus
Technion – Data Science & Cognitive Science
