# PickPack

An Android app for auto-parts order management and fulfillment. Store staff receive order notifications in real time, view order details, advance orders through the fulfillment workflow, and locate items in the warehouse using an interactive 360° panorama view.

## Features

- **Order Management** — view incoming orders, order details, and order history
- **Order Workflow** — advance orders through fulfillment stages (pick → pack → complete)
- **Push Notifications** — real-time order alerts via Firebase Cloud Messaging with custom notification sound and action buttons
- **360° Panorama View** — locate parts in the warehouse using an interactive panorama with item location markers
- **Store Registration** — register the store device and subscribe/unsubscribe to FCM topics

## Tech Stack

| Category | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose (Material 3) |
| Architecture | Clean Architecture (data / domain / presentation) + MVVM |
| Dependency Injection | Hilt |
| Networking | Ktor Client (Android engine, content negotiation, kotlinx.serialization) |
| Push Notifications | Firebase Cloud Messaging |
| Local Storage | DataStore Preferences |
| Image Loading | Coil |
| 3D / Panorama | SceneView |
| Build | Gradle (Kotlin DSL) with version catalog (`libs.versions.toml`) |

## Project Structure

```
app/src/main/java/com/autopart/
├── networkclient/               # Reusable network client module
│   ├── data/remote/network/     # Ktor client, auth, endpoints, response handling
│   ├── data/repository/         # HTTP API client implementation
│   └── domain/                  # Network service contracts, use cases, error models
└── ordermgmt/                   # Order management app
    ├── data/                    # FCM service, notification repository, local data source
    ├── di/                      # Hilt modules
    ├── domain/usecase/          # FCM token, store registration, topic subscription
    ├── orderdetail/             # Order detail feature (data / domain / presentation)
    ├── orderhistory/            # Order history feature
    ├── panorama/                # 360° panorama warehouse view
    ├── presentation/            # Shared viewmodels
    └── ui/theme/                # Compose theme
```

## Requirements

- Android Studio (latest stable)
- JDK 11+
- Android SDK: min SDK 24, target/compile SDK 37

## Setup

1. **Clone the repository**

   ```bash
   git clone https://github.com/sid288791/PickPack.git
   cd PickPack
   ```

2. **Firebase configuration**

   The project uses Firebase Cloud Messaging. A `google-services.json` is included under `app/`. To use your own Firebase project, replace it with the config file from the [Firebase Console](https://console.firebase.google.com/).

3. **Build and run**

   ```bash
   ./gradlew assembleDebug
   ```

   Or open the project in Android Studio and run the `app` configuration.

## Testing

```bash
# Unit tests
./gradlew test

# Instrumented tests (requires a connected device/emulator)
./gradlew connectedAndroidTest
```

## Documentation

Step-by-step implementation docs are available in the [`docs/`](docs/) folder:

1. [Project Setup](docs/01-project-setup.md)
2. [Application Theme](docs/02-application-theme.md)
3. [Network Client](docs/03-network-client.md)
4. [Notification System](docs/04-notification-system.md)
5. [Home & Order History](docs/05-home-order-history.md)
6. [Order Detail API](docs/06-order-detail-api.md)
7. [Panorama 360 View](docs/07-panorama-360-view.md)
8. [Navigation & Main Activity](docs/08-navigation-main-activity.md)
