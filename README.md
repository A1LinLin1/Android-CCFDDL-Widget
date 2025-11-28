# Android-CCFDDL-Widget

An Android App + Home Screen Widget for tracking and displaying upcoming CCF conference deadlines.
Users can filter conferences by **CCF ranking** and **research direction**, and the widget will show the **next three deadlines** with countdown (D-xx).

## ✨ Features

* 📝 Filter by **CCF Rank** (A / B / C)
* 📚 Filter by **research direction** (AI, SC, SYS, DB, NW, SE ...)
* 📃 Display conference info:

  * Conference Name
  * CCF Rank
  * Direction
  * Deadline Time
  * D-Countdown
  * Short Description
* 📌 Widget shows the **next 3 nearest deadlines**
* 🔄 Auto refresh via WorkManager (every 6–12 hours)
* 💾 Local cache to reduce network load
* 🎨 Material Design + modern card-style widget UI

## 🧱 Tech Stack

* Kotlin + Android Jetpack
* AppWidgetProvider (RemoteViews)
* WorkManager (background updates)
* OkHttp (network)
* SnakeYAML / JSON (data parsing)
* Material Components (UI)
* SharedPreferences (user settings)
* GitHub Raw (ccfddl dataset)

## 📦 Project Structure

```
app/
 ├── data/        # Repository, remote fetch, cache
 ├── model/       # Conference data models
 ├── ui/          # Settings Activity + preview
 ├── widget/      # Home screen widget
 └── res/         # Layouts, colors, xml configs
```

## 🗂 Data Source

All conference deadline data comes from the official ccfddl dataset.

## 📌 Roadmap

* [ ] Basic widget UI with mock data
* [ ] Settings page for rank + direction
* [ ] Real ccfddl fetching & YAML parsing
* [ ] Cache + refresh logic
* [ ] Final widget polish (shadows, chips, labels)

## 🛠 Development

Clone repo:

```bash
git clone https://github.com/A1LinLin1/Android-CCFDDL-Widget.git
```

Open in Android Studio → run the app → place the widget on home screen.

## 📄 License

MIT License
