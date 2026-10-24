# 🎬 CinéMatrix: Theatre Ticket Booking System

A cinema seat reservation and booking audit platform developed using **Core Java (OOP)** and an interactive **web interface** with dual-mode runtime (standalone client-side simulation or live Java HTTP backend).

🌐 **Live Demo:** [https://abishal-afk.github.io/Theatre-booking/](https://abishal-afk.github.io/Theatre-booking/)

---

## 📌 Project Overview

This project simulates a real-world cinema box-office booking workflow:
- Dynamic movie selection with screening times and pricing tiers.
- A 2D matrix seat grid representing auditorium rows and columns.
- Differentiation between **VIP Recliner** (Rows A–B) and **Executive Standard** (Rows C–F) seating.
- Custom Java exception handling to prevent double-booking and invalid seat references.
- Local audit persistence replicating `bookings.txt` via Java I/O and browser local storage.
- Interactive web frontend featuring real-time tax calculation (18% GST), movie metadata cards, and a **Bright / Night** mode toggle.

---

## 🛠️ Architecture & Tech Stack

```text
├── Backend: Core Java (JDK 17/21 LTS)
│   ├── Built-in HTTP Server (com.sun.net.httpserver.HttpServer)
│   ├── Custom Exceptions (Checked Exceptions)
│   └── Persistent File I/O (BufferedReader / BufferedWriter)
│
└── Frontend: Single-Page Architecture (HTML5, CSS3, JavaScript ES6)
    ├── Dynamic 2D Auditorium Seating Grid
    ├── Dark / Bright Mode Theme Engine
    └── Local Storage Simulation
