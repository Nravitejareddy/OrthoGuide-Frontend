# OrthoGuide - Mobile App (Android)

The OrthoGuide Mobile Application is a native Android solution built with Kotlin to provide patients with real-time monitoring and direct access to orthodontic care from their smartphones. It emphasizes performance and reliability.

---

## 📱 Mobile Architecture
- **Language:** Kotlin 2.x
- **Pattern:** MVVM (Model-View-ViewModel)
- **Binding:** ViewBinding for type-safe resource access.
- **Networking:** Retrofit 2 with OkHttp3.

---

## 🏗 Key Features & Modules
- **Retrofit Client:** Centralized networking service for secure API communication.
- **OTP Verification:** Dedicated activities for 6-digit verification codes.
- **Chat Interface:** Interactive UI for AI diagnostic requests.
- **Treatment Status:** Real-time visibility into current trays and compliance metrics.
- **Offline Support:** Basic caching for persistence of critical patient data.

---

## 🛠 Tech Stack
- **IDE:** Android Studio (Ladybug or newer recommended)
- **Frameworks:** Retrofit 2, Gson, Materia Design 3.
- **Build System:** Gradle (Kotlin Script .kts)
- **API Version:** 24+ (Android 7.0 "Nougat" and above)

---

## 🚀 Getting Started

1. **Prerequisites:**
   - Android Studio installed.
   - A virtual or physical device running Android 7.0+.

2. **Configuration:**
   - Open `app/src/main/java/com/yourname/orthoguide/network/RetrofitClient.kt`.
   - Update `BASE_URL`:
     ```kotlin
     private const val BASE_URL = "http://YOUR_LOCAL_IP:5001/"
     ```
     *Tip: Use your machine's local IP (e.g., 10.110.83.71) if testing on a physical device.*

3. **Build & Sync:**
   - Sync Gradle files and ensure all dependencies are resolved.

4. **Installation:**
   - Run the app from Android Studio and select your target device.

---

## 🏗 Project Layout
- `com.yourname.orthoguide.network`: API service and client logic.
- `com.yourname.orthoguide.ui`: Modular activities and fragments.
- `res/layout`: Material Design XML layouts.

---

**OrthoGuide Mobile** - *Ortho-care in the palm of your hand.*
