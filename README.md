# 🌟 Cosmic Clicker - Idle Space Adventure

[![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com/)
[![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Build Status](https://img.shields.io/badge/Build-Passing-brightgreen?style=for-the-badge)](https://github.com)

> **Một idle clicker game phiêu lưu không gian đầy mê hoặc với hệ thống progression sâu sắc, âm thanh phong phú và vô số thử thách hấp dẫn!**

---

## 🚀 **Tổng Quan Game**

**Cosmic Clicker** là một idle clicker game được phát triển cho Android, nơi bạn sẽ khám phá vũ trụ rộng lớn, thu thập stardust, xây dựng đế chế thiên thể và trở thành chúa tể của toàn bộ vũ trụ!

### ✨ **Điểm Nổi Bật**
- 🎮 **Gameplay đa dạng**: Từ active clicking đến idle progression
- 🌌 **Hệ thống constellation**: 8 chòm sao với khả năng đặc biệt
- 🏆 **Thử thách phong phú**: 8 challenge khác nhau với mức độ khó tăng dần
- 🎵 **Âm thanh sống động**: 11 sound effects chất lượng cao
- 📊 **Tracking chi tiết**: Thống kê đầy đủ về progress và thành tích
- ⚡ **Auto-clicker**: Hệ thống tự động cho idle gameplay

---

## 🎮 **Cách Chơi**

### 🌟 **Bước Đầu**
1. **Tap ngôi sao** để thu thập Stardust
2. **Mua Celestial Objects** để tăng Stardust per Second (SPS)
3. **Unlock Research** để mở khóa tính năng mới
4. **Đạt Milestones** để nhận bonus mạnh mẽ

### 🔄 **Prestige Systems**
- **🌟 Supernova**: Reset progress để nhận Singularities (bonus SPS vĩnh viễn)
- **🌌 Galaxy Collapse**: Reset để nhận Galaxy Points 
- **💥 Big Crunch**: Reset toàn bộ để nhận Cosmic Essence (currency mạnh nhất)

---

## 🎯 **Tính Năng Chính**

### 🌟 **Combo System**
- Xây dựng combo bằng cách click liên tục
- **Combo timeout**: 2 giây giữa các click
- **Max combo**: 999x với damage bonus khổng lồ
- Constellation có thể tăng thời gian combo

### 🌌 **Constellation System**
- **8 chòm sao độc đáo** với bonus riêng biệt:
  - ⭐ **Orion's Belt**: +50% click value
  - 🌸 **Andromeda's Grace**: +25% SPS
  - 🧠 **Cassiopeia's Wisdom**: -20% research costs
  - 🐲 **Draco's Fortune**: -15% upgrade costs
  - 🔥 **Phoenix Rising**: +30% singularity gains
  - 🐎 **Pegasus Swift**: 2x auto-clicker speed
  - 🐻 **Ursa Major**: 2x rift spawn rate
  - 🎵 **Lyra's Rhythm**: +50% combo timeout

### 🏆 **Challenge System**
8 loại thử thách đa dạng:
1. **💰 Stardust Challenge**: Kiếm X stardust trong 1 run
2. **👆 Click Challenge**: Click X lần
3. **⚡ SPS Challenge**: Đạt X SPS
4. **🔥 Combo Challenge**: Duy trì X combo
5. **😴 Idle Challenge**: Kiếm stardust không click
6. **⏱️ Speed Run**: Đạt mục tiêu trong thời gian giới hạn
7. **🌌 Rift Challenge**: Thu thập X cosmic rifts
8. **🚫 No-Upgrade Challenge**: Đạt mục tiêu không mua upgrade

### 🎪 **Special Events**
Các sự kiện ngẫu nhiên xuất hiện với bonus khủng:
- **💎 Double Stardust**: x2 tất cả stardust gains
- **👆 Triple Clicks**: x3 click value
- **🌌 Rift Rush**: Rifts xuất hiện gấp 5 lần
- **💸 Discount Day**: Giảm 50% giá upgrade
- **🌟 Singularity Surge**: +50% singularity gains
- **🔥 Combo Frenzy**: Double combo timeout

### ⚡ **Auto-Clicker System**
- **Unlock**: Cần 100 Singularities
- **Auto-click**: Mỗi 100ms
- **Upgradeable**: Cost = level × 50 Singularities
- **Constellation bonus**: Pegasus Swift tăng gấp đôi tốc độ

---

## 🛠️ **Tính Năng Kỹ Thuật**

### 🎵 **Hệ Thống Âm Thanh**
- **11 sound effects** chất lượng cao:
  - Click sounds, upgrade sounds, milestone sounds
  - Achievement notifications, button feedback
  - Research complete, critical hits
  - Daily rewards, cosmic rifts
- **SoundPool optimization** với 8 streams đồng thời
- **Volume control** riêng cho từng loại âm thanh

### 💾 **Save System**
- **Auto-save** mỗi 30 giây
- **Offline progress** calculation
- **Complete state persistence** cho tất cả features
- **SharedPreferences** optimized

### 📊 **Statistics Tracking**
Tracking đầy đủ:
- Total play time, clicks, stardust earned
- Highest stardust reached, best SPS
- Prestige counts (supernovas, galaxy collapses, big crunches)
- Challenge completion, constellation unlocks
- Achievement progress, skills learned

---

## 🎨 **Giao Diện & UX**

### 🎯 **Modern UI Design**
- **Material Design 3** với gradient backgrounds
- **Floating Action Buttons** cho các feature chính
- **Card layouts** với rounded corners
- **Responsive design** cho mọi screen size

### ✨ **Visual Effects**
- **Critical hit animations** với floating text
- **Milestone celebrations** với scale & rotation
- **Smooth transitions** cho tất cả UI elements
- **Particle effects** cho cosmic rifts

### 🎮 **User Experience**
- **Haptic feedback** cho star clicks
- **Toast notifications** với thông tin chi tiết
- **Dialog system** với scrollable content
- **Intuitive navigation** với tab system

---

## 🏗️ **Cấu Trúc Dự Án**

```
IdleClickerGame/
├── app/src/main/
│   ├── java/com/zaigame/IdleClickerGame/
│   │   ├── MainActivity.java         # Core game logic
│   │   ├── Achievement.java          # Achievement system
│   │   ├── Upgrade.java             # Upgrade mechanics
│   │   ├── ResearchUpgrade.java     # Research system
│   │   └── Skill.java               # Skill tree
│   ├── res/
│   │   ├── layout/                  # XML layouts
│   │   ├── drawable/                # Graphics & icons
│   │   ├── raw/                     # Audio files
│   │   └── values/                  # Colors, strings, themes
│   └── AndroidManifest.xml
└── gradle files
```

### 🎵 **Audio Assets**
```
res/raw/
├── click_sound.mp3              # Star click feedback
├── upgrade_sound.mp3            # Purchase confirmations
├── milestone_sound.mp3          # Achievement notifications
├── achievement_sound.mp3        # Unlock celebrations
├── rift_sound.mp3              # Cosmic rift interactions
├── button_sound.mp3            # UI button feedback
├── notification_sound.mp3       # Critical hits, rewards
├── research_complete_sound.mp3  # Research completion
├── skill_unlock_sound.mp3      # Skill progression
├── background_music.mp3        # Ambient gameplay music
└── menu_music.mp3              # Menu background music
```

---

## 🚀 **Cài Đặt & Build**

### 📋 **Yêu Cầu**
- **Android Studio** Arctic Fox or newer
- **Android SDK** API 21+ (Android 5.0+)
- **Java 8** or newer
- **Gradle 7.0+**

### 🔧 **Build Instructions**
```bash
# Clone repository
git clone https://github.com/yourusername/cosmic-clicker.git
cd cosmic-clicker

# Build debug APK
./gradlew assembleDebug

# Build release APK  
./gradlew assembleRelease

# Install on connected device
./gradlew installDebug
```

### 🎮 **Run & Test**
```bash
# Run on emulator/device
./gradlew installDebug

# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest
```

---

## 🎯 **Gameplay Progression**

### 🌱 **Early Game** (0-1M Stardust)
- Learn basic clicking mechanics
- Buy first Celestial Objects
- Unlock basic research
- Complete first achievements

### 🌿 **Mid Game** (1M-1B Stardust)
- Perform first Supernova
- Unlock Constellation system
- Complete basic challenges  
- Build SPS production

### 🌳 **Late Game** (1B+ Stardust)
- Galaxy Collapse prestige
- Advanced constellations
- Difficult challenges
- Auto-clicker optimization

### 🌌 **End Game** (Cosmic Essence)
- Big Crunch mechanics
- Ultimate challenges
- Perfect constellation strategies
- Statistics optimization

---

## 🏆 **Achievements System**

### 🎯 **Categories**
- **👆 Clicking**: From first tap to click mastery
- **💰 Stardust**: Earning milestones
- **🛒 Upgrades**: Purchase achievements  
- **⚡ SPS**: Production milestones
- **🌟 Prestige**: Supernova & beyond
- **🌌 Constellations**: Stargazing rewards
- **🏆 Challenges**: Completion badges
- **⚡ Special**: Hidden & rare achievements

### 🎖️ **Notable Achievements**
- 🥇 **First Tap**: Your cosmic journey begins
- 🔥 **Combo Master**: Reach 50x combo
- ⭐ **Stargazer**: Unlock first constellation
- 🏆 **Challenger**: Complete first challenge
- 🤖 **Automation**: Unlock auto-clicker
- 🌟 **Reality Shaper**: Perform 5 Big Crunches

---

## 🔮 **Future Enhancements**

### 🎯 **Planned Features**
- **📱 Cloud Save**: Cross-device synchronization
- **🌐 Leaderboards**: Global competition
- **🎪 Seasonal Events**: Holiday themes
- **🛍️ Prestige Shop**: Singularity marketplace
- **📚 Tutorial System**: New player guidance
- **🎨 Customization**: Themes & skins

### 🚀 **Long-term Vision**
- **🌌 Universe Expansion**: New galaxies to explore
- **👥 Multiplayer**: Cooperative gameplay
- **🎮 Mini-games**: Interactive bonus rounds
- **📈 Advanced Analytics**: Detailed progress tracking

---

## 🤝 **Contributing**

We welcome contributions! Here's how you can help:

### 🐛 **Bug Reports**
- Use GitHub Issues
- Include device info & Android version
- Provide reproduction steps
- Attach screenshots if applicable

### ✨ **Feature Requests**
- Describe the feature clearly
- Explain the use case
- Consider game balance implications
- Suggest implementation approach

### 💻 **Code Contributions**
- Fork the repository
- Create feature branch
- Follow existing code style
- Add comments for complex logic
- Test thoroughly before PR

---

## 📄 **License**

This project is licensed under the **MIT License** - see the [LICENSE](LICENSE) file for details.

### 🎵 **Audio Credits**
- Sound effects: Premium game audio collection
- Background music: Ambient space themes
- All audio assets are royalty-free

---

## 👨‍💻 **Developer**

**ZaiGame Studio**
- 🌐 Website: [zaigame.dev](https://zaigame.dev)
- 📧 Email: contact@zaigame.dev
- 🐦 Twitter: [@ZaiGameStudio](https://twitter.com/ZaiGameStudio)

---

## 🙏 **Acknowledgments**

- **Android Development Community** for excellent documentation
- **Material Design Team** for beautiful UI guidelines  
- **Indie Game Developers** for inspiration and creativity
- **Beta Testers** for valuable feedback and bug reports

---

## 📈 **Stats & Milestones**

- ⭐ **11 Audio Effects** for immersive experience
- 🏆 **30+ Achievements** to unlock
- 🌌 **8 Constellations** with unique powers
- 🎯 **8 Challenge Types** for varied gameplay
- 📊 **20+ Statistics** tracked
- 🚀 **3 Prestige Systems** for endless progression

---

<div align="center">

### 🌟 **Start Your Cosmic Journey Today!** 🌟

**[Download APK](releases/latest) | [View Screenshots](screenshots/) | [Join Community](community/)**

---

*Made with ❤️ and lots of ☕ by ZaiGame Studio*

</div>
