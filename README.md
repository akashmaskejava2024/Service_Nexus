# Nexus | Hyper-Local Service Marketplace 

**Nexus** is a real-time, location-aware marketplace designed to connect local service providers (Workers) with users in need of assistance (Customers). By leveraging geospatial coordinates, Nexus allows for precise job matching, competitive bidding, and secure job completion verification.

---

##  Key Features

###  Dual User Workflows
* **For Customers:** * Drop a pin on an interactive map to request services.
    * Upload images via **Pillow** to provide visual context for the task.
    * Review and accept competitive bids from local workers.
* **For Workers:**
    * Access a dynamic dashboard showing jobs within a **5km radius**.
    * Submit custom bids with pricing and messages.
    * Track active, pending, and completed job history.

###  Geospatial Intelligence
* Integrated **Leaflet.js** and **OpenStreetMap** for an interactive, pin-drop location system.
* Backend logic calculates proximity based on precise latitude and longitude coordinates.

###  Secure Verification (QR Code)
* **Verification Engine:** Uses the `qrcode` library to generate unique, in-memory tokens upon job completion.
* **Fraud Prevention:** Customers scan the worker's QR code to officially mark a job as `COMPLETED`, triggering the feedback and rating system.

---

##  Tech Stack

* **Backend:** Python 3.x, Django (MVT Architecture)
* **Frontend:** Jinja, Tailwind CSS, Leaflet.js
* **Database:** SQLite (Relational schema with Geospatial fallbacks)
* **Key Libraries:** `qrcode`, `Pillow`, `asgiref`

---

##  Installation & Setup

1. **Clone the repository:**
   ```bash
   git clone [https://github.com/akashmaskejava2024/Nexus.git](https://github.com/akashmaskejava2024/Nexus.git)
   cd Nexus
