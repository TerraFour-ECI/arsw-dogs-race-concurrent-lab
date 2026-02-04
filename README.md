# 🐕 Lab 2 – Concurrent Programming: Greyhound Race

## Software Architecture (ARSW)

### Objective
The objective of this lab is for the student to **analyze, fix, and design a concurrent solution**, identifying **synchronization problems**, **critical regions**, and applying **appropriate concurrency control mechanisms** in Java.

The exercise is based on a simulation of a **greyhound race**, where each greyhound runs as an independent thread and advances through a lane until completing the track.

---

## Problem Context
In the simulation:

- Each **greyhound** runs concurrently (one thread per greyhound).
- All greyhounds share an **arrival registry**.
- The system allows **starting**, **stopping**, and **resuming** the race.
- At the end of the race, the **arrival order (ranking)** must be displayed consistently.

The application initially presents **synchronization problems** that must be analyzed and fixed.

---

## General Project Structure

The project follows a **layer separation**, consistent with the previous lab:

```
src
 ├── main
 │   └── java
 │       └── edu.eci.arsw.dogsrace
 │           ├── app        -> Entry point and orchestration
 │           ├── threads    -> Execution threads (greyhounds)
 │           ├── control    -> Concurrent execution control
 │           ├── domain     -> Model and shared state
 │           └── ui         -> Graphical interface
 └── test
     └── java
         └── edu.eci.arsw.dogsrace
```

---

## Activities to Develop

### 1️⃣ Thread Completion Synchronization
Fix the application so that the results notification is displayed **only when all greyhound threads have finished their execution**.

**Hints:**
- The action to start the race and display results is performed from `MainCanodromo`.
- You can use the `join()` method of the `Thread` class.

---

### 2️⃣ Identification of Inconsistencies and Critical Regions
Run the application multiple times and identify **inconsistencies in the ranking**.

**Tasks:**
- Identify the critical regions.
- Explain why they generate inconsistencies.
- Synchronize only those regions.

---

### 3️⃣ Pause and Continue Functionalities
Implement the **Stop** and **Continue** functionalities.

**Expected behavior:**
- **Stop**: all greyhounds suspend their execution.
- **Continue**: all greyhounds resume the race.

**Restrictions:**
- Use language synchronization mechanisms.
- Use a **common monitor**.
- Use `wait()` and `notifyAll()`.

---

## Evaluation Criteria

### Functionality
- Execution stopped and resumed consistently.
- Ranking without inconsistencies.

### Design
- Synchronization only of critical regions.
- Reactivation with a single call using a common monitor.

---

## Deliverables
- Functional source code.
- Brief explanation of the critical regions and synchronization used.
- Evidence of correct execution.

---

## Final Remarks
This lab reinforces key concepts of **concurrent programming**, **correct synchronization design**, and **layered architecture**, which will be reused in subsequent labs.
