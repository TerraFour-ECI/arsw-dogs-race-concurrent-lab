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

### 🛠️**Solution**

#### *Problem*
The application was displaying results **before all greyhound threads had finished execution**, causing incomplete or incorrect rankings to be shown prematurely.

#### *Root Cause*
The main orchestration logic in `MainCanodromo` was calling the results display immediately after initiating all greyhound threads, without establishing a synchronization mechanism to ensure all concurrent executions had completed.

#### *Solution Implemented*
We integrated the **`join()` method** from the Java threading API to enforce **thread-level synchronization**:

**Key Implementation Details:**

- **In `MainCanodromo.java`**: After starting all greyhound threads, we added an explicit **join synchronization phase** where the main thread waits for each greyhound thread to complete.
- **How it works**: The `join()` method blocks the main thread's execution until the associated greyhound thread finishes its run, ensuring a **happens-before relationship** between thread completion and result display.
- **Sequential guarantee**: Only after all greyhounds have completed their concurrent execution does the application proceed to calculate and display the final ranking.

#### *Why This Works*
The `join()` method provides an **implicit mutual exclusion** at the thread orchestration level by forcing the main thread to synchronize with worker threads, preventing any premature result computation while races are still ongoing.

#### *Result*

>**Consistent and reliable ranking display**: Results are now guaranteed to show the correct final positions of all greyhounds without race condition artifacts.


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

### ✅ Test Results


![All tests passed](images/test.png)

---

### 📊 JaCoCo Coverage Analysis


![JaCoCo Coverage Report](images/jacoco.png)

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
