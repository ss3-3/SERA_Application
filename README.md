# SERA Application

**Student Event Reservation Application** - A comprehensive Android application for managing campus events, reservations, and payments built with Jetpack Compose and Firebase.

## 📱 Overview

SERA is a modern event management platform designed for educational institutions, enabling students to discover and register for campus events while providing organizers with powerful tools to manage events, track attendance, and process payments.

## ✨ Key Features

### For Participants
- **Event Discovery**: Browse and search upcoming campus events by category (Academic, Career, Art, Wellness, Music, Festival)
- **Easy Registration**: Reserve seats for events with zone selection (Rock Zone / Normal Zone)
- **Secure Payments**: Integrated PayPal payment processing
- **Digital Receipts**: Generate and download PDF receipts with QR codes
- **Reservation Management**: View reservation history and request refunds
- **Real-time Notifications**: Stay updated on event changes and approvals

### For Organizers
- **Event Creation**: Create and manage events with detailed information and images
- **Seat Management**: Configure venue capacity with multiple seating zones
- **Payment Tracking**: Monitor revenue and payment statuses
- **Participant Management**: View attendee lists and reservation details
- **Analytics Dashboard**: Track event performance and revenue trends

### For Administrators
- **User Management**: Approve organizer accounts and manage user roles
- **Event Approval**: Review and approve events before publication
- **Comprehensive Reports**: 
  - Revenue trends and analytics
  - Event participation statistics
  - User growth metrics
  - Top-performing events
- **System Oversight**: Monitor all platform activities

## 🏗️ Architecture

The application follows **Clean Architecture** principles with clear separation of concerns:

```
app/
├── data/              # Data layer
│   ├── local/         # Room database (offline caching)
│   ├── remote/        # Firebase & API integrations
│   ├── repository/    # Repository implementations
│   └── mapper/        # Data transformations
├── domain/            # Business logic layer
│   ├── model/         # Domain models
│   ├── repository/    # Repository interfaces
│   └── usecase/       # Business use cases
├── presentation/      # UI layer
│   ├── ui/            # Compose screens
│   └── viewmodel/     # ViewModels
└── di/                # Dependency injection
```

### Design Patterns
- **MVVM (Model-View-ViewModel)**: UI architecture pattern
- **Repository Pattern**: Data access abstraction
- **Use Case Pattern**: Encapsulated business logic
- **Dependency Injection**: Hilt for DI management

## 🛠️ Tech Stack

### Core Technologies
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Minimum SDK**: 24 (Android 7.0)
- **Target SDK**: 36

### Key Libraries

#### Architecture & DI
- **Hilt**: Dependency injection
- **Navigation Compose**: Type-safe navigation
- **ViewModel & LiveData**: State management

#### Database & Storage
- **Room**: Local database with offline support
- **Firebase Firestore**: Cloud database
- **Firebase Storage**: Image and file storage
- **Firebase Authentication**: User authentication

#### Networking & APIs
- **Retrofit**: REST API client
- **OkHttp**: HTTP client with logging
- **Gson**: JSON serialization

#### UI & Media
- **Material 3**: Modern Material Design components
- **Coil**: Image loading and caching
- **Vico**: Charts and data visualization

#### Payments & Documents
- **PayPal SDK**: Payment processing
- **iText7**: PDF generation
- **ZXing**: QR code generation

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog | 2023.1.1 or later
- JDK 11 or higher
- Android SDK with API level 36
- Firebase project with Firestore, Authentication, and Storage enabled

### Firebase Setup

1. **Create a Firebase Project**
   - Go to [Firebase Console](https://console.firebase.google.com/)
   - Create a new project or use an existing one

2. **Add Android App**
   - Register your app with package name: `com.example.sera_application`
   - Download `google-services.json`
   - Place it in the `app/` directory

3. **Enable Firebase Services**
   - **Authentication**: Enable Email/Password sign-in
   - **Firestore Database**: Create database in production mode
   - **Storage**: Enable Firebase Storage

4. **Configure Security Rules**
   - Copy the contents from `firestore.rules` to your Firestore Rules
   - Deploy the rules in Firebase Console

### Installation

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd SERA_Application
   ```

2. **Open in Android Studio**
   - Open Android Studio
   - Select "Open an Existing Project"
   - Navigate to the cloned directory

3. **Sync Gradle**
   - Wait for Gradle sync to complete
   - Resolve any dependency issues

4. **Add Firebase Configuration**
   - Ensure `google-services.json` is in the `app/` directory
   - Verify Firebase dependencies in `build.gradle.kts`

5. **Build and Run**
   - Connect an Android device or start an emulator
   - Click "Run" or press `Shift + F10`

## 🔐 Firebase Security Rules

The application uses comprehensive Firestore security rules to protect user data:

- **Role-based Access Control**: ADMIN, ORGANIZER, PARTICIPANT roles
- **Document-level Permissions**: Users can only access their own data
- **Approval Workflow**: Organizers require admin approval before creating events
- **Payment Security**: Strict validation for payment status changes

See `firestore.rules` for the complete ruleset.

## 📊 Database Schema

### Collections

#### `users`
- User profiles with role-based access
- Fields: userId, fullName, email, role, isApproved, accountStatus

#### `events`
- Event information and metadata
- Fields: eventId, name, organizerId, category, status, date, location, seats, pricing

#### `reservations`
- User event registrations
- Fields: reservationId, userId, eventId, seats, status, zoneSeats

#### `payments`
- Payment transactions
- Fields: paymentId, userId, eventId, amount, status, paymentMethod

#### `notifications`
- User notifications
- Fields: notificationId, userId, title, message, type, isRead

## 🎨 UI/UX Features

- **Material 3 Design**: Modern, accessible interface
- **Dark Mode Support**: Adaptive theming
- **Responsive Layouts**: Optimized for various screen sizes
- **Image Caching**: Fast loading with Coil
- **Smooth Animations**: Polished user experience
- **Form Validation**: Real-time input validation

## 🔄 Offline Support

The app uses Room database for local caching:
- Events are cached for offline viewing
- Reservations sync when connection is restored
- Optimistic UI updates for better UX

## 📱 User Roles & Permissions

### Participant (Default)
- Browse and search events
- Create reservations
- Make payments
- View reservation history
- Request refunds

### Organizer (Requires Approval)
- All participant features
- Create and manage events
- View participant lists
- Track payments and revenue
- Generate reports

### Administrator
- All organizer features
- Approve organizer accounts
- Approve events before publication
- Access system-wide reports
- Manage all users and events

## 🧪 Testing

### Running Tests
```bash
# Unit tests
./gradlew test

# Instrumented tests
./gradlew connectedAndroidTest
```

## 📦 Build Variants

### Debug
- Development build with logging enabled
- Firebase emulator support

### Release
- Optimized production build
- ProGuard/R8 minification
- Signed APK for distribution

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 👥 Authors

- Development Team - SERA Application

## 🙏 Acknowledgments

- Firebase for backend infrastructure
- Jetpack Compose for modern Android UI
- Material Design for design guidelines
- PayPal for payment processing
- All open-source contributors

## 📞 Support

For issues, questions, or contributions, please open an issue in the repository.

---

**Version**: 1.0  
**Last Updated**: December 2025  
**Platform**: Android 7.0+
