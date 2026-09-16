# Delivery & Operations Tracking Mobile App

A real-time Android application designed for delivery drivers and field operators to manage job assignments, track statuses, and synchronize operations seamlessly across platforms.

---

## Key Features

Real-Time Job Sync: New tasks and job status updates sync instantly across devices without requiring manual page refreshes.
Streamlined Job Creation: Quick-add interface allows dispatchers and operators to create new jobs on the fly and make them instantly selectable.
Interactive Dashboard & Job Selector: Operators can seamlessly toggle between multiple active jobs via a dynamic selector and view immediate card updates.
Guided Workflow Safeguards: Prevents status errors with an enforced lifecycle step sequence (`Assigned` $\to$ `Accepted` $\to$ `In Progress` $\to$ `Completed` or `Failed`).
Offline Resilience: Automatically saves local progress during connectivity losses and syncs updates to the cloud as soon as the device reconnects.
Secure Operator Access: Protected sign-in system ensures only authorized personnel can view and update operational task details.

---

## Application Workflow

1. Sign In: Operators log into their account to access the active operational dashboard.
2. Create or Select Job:
 Tap the **+** button to enter a new job title and description.
 Select any active task from the dashboard dropdown selector.


3. Update Status: Advance the job through its delivery lifecycle using single-tap action buttons:
Accept Job $\to$ Acknowledges assignment.
Start Delivery $\to$ Marks the job as actively in progress.
Complete / Fail $\to$ Concludes the task with a final operational result.


4. Cloud Synchronization: All status updates reflect in real time across connected dashboards and storage systems.

---

## System Requirements

Operating System: Android 8.0 (API Level 26) or higher.
Network: Active internet connection (Cellular or Wi-Fi) for live synchronization. Offline mode supported for temporary connectivity drops.

## Installation

Clone this repository in Android Studio using the GitHub url : https://github.com/Arunav1070/Real-Time-Delivery-And-Operations-Tracker.git
