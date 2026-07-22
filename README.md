# CubixSMP

**CubixSMP** — плагин для Paper 1.21.4, добавляющий продвинутую систему уровней (Cubix Level) на ваш SMP-сервер. Игроки зарабатывают опыт (XP) за различные действия в игре и повышают свой уровень.

🇷🇺 Разработан специально для русскоязычных SMP-серверов. Все сообщения полностью переводимы через `messages.yml`.

---

## 📋 Содержание

- [Возможности](#-возможности)
- [Источники XP](#-источники-xp)
- [Команды и права](#-команды-и-права)
- [Плейсхолдеры (PlaceholderAPI)](#-плейсхолдеры-placeholderapi)
- [Установка](#-установка)
- [Конфигурация](#-конфигурация)
- [Сборка из исходников](#-сборка-из-исходников)
- [Требования](#-требования)
- [Структура проекта](#-структура-проекта)
- [Часто задаваемые вопросы](#-часто-задаваемые-вопросы)
- [Лицензия](#-лицензия)

---

## ✨ Возможности

- **8 видов деятельности** для получения XP (шахтёрство, фермерство, рубка, рыбалка, охота, исследование, время онлайн, ежедневный бонус)
- **Гибкая система уровней** — формула XP настраивается в config.yml
- **Защита от читеров** — XP начисляется только за натуральные блоки и мобов
- **PlaceholderAPI** — интеграция с TAB, Scoreboard, Chat и другими плагинами
- **Полная переводимость** — все сообщения в отдельном файле messages.yml

---

## ⛏ Источники XP

| Активность | Описание | XP |
|:----------:|----------|:--:|
| ⛏ Шахтёрство | Добыча руд (уголь → древние обломки) | 1–10 XP |
| 🌾 Фермерство | Сбор урожая, тыкв, арбузов, ягод, мёда | 0.5–5 XP |
| 🌲 Рубка деревьев | Любое бревно (дуб, ель, берёза, тропическое, акация, вишня, мангр, адские стебли) | 0.2 XP |
| 🎣 Рыбалка | Любая пойманная рыба | 5 XP |
| ⚔ Охота | Убийство мобов (зеомби 3 XP, хранитель 50 XP, иссушитель и т.д.) | 1–50 XP |
| 🚶 Дистанция | Каждые 1000 блоков пути | 5 XP |
| ⏱ Время онлайн | Каждые 30 минут игры | 10 XP |
| ☀ Ежедневный бонус | Раз в сутки через `/cubixsmp daily` | 50 XP |

> **Важно:** XP начисляется ТОЛЬКО за натуральные ресурсы. Поставленные игроком блоки, мобы из спавнеров и яиц призыва — XP не дают.

---

## 🎮 Команды и права

### Команды

| Команда | Алиасы | Описание | Право |
|---------|--------|----------|-------|
| `/cubixsmp` | `/cs`, `/cubix`, `/csmp` | Показать статистику (уровень, XP, прогресс) | `cubixsmp.user` |
| `/cubixsmp stats` | | Показать статистику | `cubixsmp.user` |
| `/cubixsmp daily` | | Получить ежедневный бонус | `cubixsmp.user` |
| `/cubixsmp sound` | | Вкл/выкл звук получения XP | `cubixsmp.user` |
| `/cubixsmp leaders` | | Топ игроков по уровню | `cubixsmp.user` |
| `/cubixsmp reload` | | Перезагрузить конфигурацию | `cubixsmp.reload` |
| `/cubixsmp admin` | | Админ-команды | `cubixsmp.admin` |

### Админ-команды

`/cubixsmp admin <subcommand> [args]`

| Подкоманда | Описание |
|------------|----------|
| `info <player>` | Информация об игроке (уровень, XP, время игры) |
| `setlevel <player> <level>` | Установить уровень |
| `addxp <player> <amount>` | Добавить XP |
| `removexp <player> <amount>` | Забрать XP |
| `reset <player>` | Сбросить прогресс игрока |

### Права (Permissions)

| Право | Описание | По умолчанию |
|-------|----------|:------------:|
| `cubixsmp.user` | Базовые команды | ✅ true |
| `cubixsmp.reload` | Перезагрузка конфига | ❌ op |
| `cubixsmp.admin` | Админ-доступ | ❌ op |
| `cubixsmp.admin.setlevel` | Установка уровня | ❌ op |
| `cubixsmp.admin.addxp` | Добавление XP | ❌ op |
| `cubixsmp.admin.removexp` | Забирание XP | ❌ op |
| `cubixsmp.admin.reset` | Сброс прогресса | ❌ op |
| `cubixsmp.admin.info` | Информация об игроке | ❌ op |

---

## 🔌 Плейсхолдеры (PlaceholderAPI)

Если на сервере установлен PlaceholderAPI, доступны следующие плейсхолдеры:

| Плейсхолдер | Описание | Пример |
|-------------|----------|--------|
| `%cubixsmp_level%` | Текущий уровень игрока | `42` |
| `%cubixsmp_xp%` | Текущий опыт | `150` |
| `%cubixsmp_level_xp_needed%` | Опыта до следующего уровня | `200` |
| `%cubixsmp_level_progress%` | Процент прогресса | `42%` |
| `%cubixsmp_level_playtime%` | Общее время игры | `3ч 15мин` |
| `%cubixsmp_action%` | Последнее действие | `Mining` |

### Примеры использования

**В TAB (Nametag):**
```
%cubixsmp_level% §7Уровень
```
→ `§e42 §7Уровень`

**В Scoreboard:**
```
§7XP: %cubixsmp_xp%§7/§a%cubixsmp_level_xp_needed%
```
→ `§7XP: §e150§7/§a200`

**В чате:**
```
§7[§6⚡%cubixsmp_level%§7] §f%player_name%
```
→ `§7[§6⚡42§7] §frizer001`

---

## 📦 Установка

1. Скачайте `CubixSMP-1.2.jar` со [страницы релизов](https://github.com/rizer001/CubixSMP/releases)
2. Поместите JAR в папку `plugins/` вашего сервера
3. (Опционально) Установите **PlaceholderAPI** для поддержки плейсхолдеров
4. Перезапустите сервер или выполните `/reload`
5. Настройте XP значения в `plugins/CubixSMP/config.yml`
6. Выполните `/cubixsmp reload` для применения изменений

---

## ⚙ Конфигурация

Все настройки XP находятся в `config.yml`. Структура файла:

```yaml
settings:
  xp-base: 100                # XP для уровня 0→1
  xp-multiplier: 1.5          # Каждый уровень требует (base + level × multiplier) XP
  max-level: 100              # Максимальный уровень
  distance-interval: 1000     # Блоков на один тик XP
  xp-per-distance-interval: 5 # XP за интервал
  playtime-interval: 1800     # Секунд на один тик XP
  xp-per-playtime-interval: 10
  daily-bonus-xp: 50          # XP за ежедневный бонус
  use-actionbar: true         # true = actionbar, false = чат
  leaders-limit: 10           # Игроков в топе

mining:
  enabled: true
  blocks:
    COAL_ORE: 1.0
    IRON_ORE: 3.0
    DIAMOND_ORE: 5.0
    ANCIENT_DEBRIS: 10.0

farming:
  enabled: true
  crops:
    WHEAT: 0.5
    PUMPKIN: 1.0
    MELON: 1.0

woodcutting:
  enabled: true
  logs:
    OAK_LOG: 0.2
    SPRUCE_LOG: 0.2

fishing:
  enabled: true
  xp-per-catch: 5.0

hunting:
  enabled: true
  mobs:
    ZOMBIE: 3.0
    CREEPER: 4.0
    WARDEN: 50.0
```

**Формула XP на уровень:**
```
XP_нужно = xp-base + (текущий_уровень × xp-multiplier)

Уровень 0→1:  100 + (0 × 1.5)  = 100  XP
Уровень 1→2:  100 + (1 × 1.5)  = 101.5 XP
Уровень 99→100: 100 + (99 × 1.5) = 248.5 XP
```

---

## 🔨 Сборка из исходников

```bash
git clone https://github.com/rizer001/CubixSMP.git
cd CubixSMP
./gradlew shadowJar
```

Результат: `build/libs/CubixSMP-1.2.jar` (также копируется в `Jar/CubixSMP-1.2.jar`)

---

## 📋 Требования

- **Сервер:** Paper 1.21.4 (или его форки: Purpur, Pufferfish и т.д.)
- **Java:** 21+
- **Опционально:** PlaceholderAPI 2.11+

---

## 📁 Структура проекта

```
CubixSMP/
├── build.gradle              — Система сборки (Gradle + Shadow)
├── settings.gradle
├── gradlew / gradlew.bat     — Gradle Wrapper
├── src/main/java/com/cubixsmp/
│   ├── CubixSMP.java              — Main класс
│   ├── CubixSMPCommand.java       — Обработчик команд
│   ├── CubixSMPTabCompleter.java  — Таб-комплитер
│   ├── CubixSMPPlaceholderExpansion.java — Плейсхолдеры PAPI
│   ├── LevelManager.java          — Менеджер уровней/XP
│   ├── PlayerDataManager.java     — Сохранение данных игроков (YAML)
│   ├── NaturalCheck.java          — Проверка натуральности блоков/мобов
│   ├── MessagesManager.java       — Управление сообщениями
│   ├── ConfigGuideManager.java    — Управление plugin-guide.txt
│   ├── PlacedBlockTracker.java    — Трекер поставленных блоков (PDC чанков)
│   └── listeners/
│       ├── MiningListener.java    — Шахтёрство
│       ├── FarmingListener.java   — Фермерство
│       ├── WoodcuttingListener.java — Рубка деревьев
│       ├── FishingListener.java   — Рыбалка
│       ├── HuntingListener.java   — Охота
│       ├── DistanceListener.java  — Дистанция
│       ├── PlaytimeListener.java  — Время онлайн
│       └── DailyBonusListener.java — Ежедневный бонус
├── src/main/resources/
│   ├── plugin.yml              — Описание плагина
│   ├── config.yml              — Конфигурация XP
│   ├── messages.yml            — Сообщения (переводимые)
│   └── plugin-guide.txt        — Ссылка на README
└── Jar/                        — Готовые сборки
```

### Зависимости

- `io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT` (compileOnly)
- `me.clip:placeholderapi:2.11.6` (compileOnly, опционально)

---

## ❓ Часто задаваемые вопросы

**Q: Почему XP не начисляется за руду?**
A: Проверьте включена ли секция (`enabled: true`). Убедитесь что руда натуральная (вокруг неё камень, а не пустота/воздух). Deepslate варианты руд должны быть в config.yml отдельно.

**Q: Можно ли добавить XP за новые блоки/мобов?**
A: Да! Просто добавьте `MATERIAL_NAME: XP` в соответствующую секцию config.yml и выполните `/cubixsmp reload`.

**Q: Работает ли плагин на Spigot/CraftBukkit?**
A: Нет, требуется Paper 1.21.4 или его форк. На Spigot проверка натуральности мобов будет работать с ограничениями.

**Q: Как сбросить прогресс игрока?**
A: Удалите файл `playerdata/<UUID игрока>.yml` и перезагрузите плагин.

**Q: Почему плейсхолдеры не работают?**
A: Убедитесь что PlaceholderAPI установлен. Выполните `/papi info CubixSMP`. Если расширение не зарегистрировано — перезагрузите сервер.

**Q: Как изменить XP за определённое действие?**
A: Отредактируйте `config.yml`. Для руд — `mining.blocks.MATERIAL: XP`. Для мобов — `hunting.mobs.ENTITY_TYPE: XP`. Выполните `/cubixsmp reload`.

**Q: Безопасен ли плагин от читеров?**
A: Плагин использует несколько методов проверки: трекер поставленных блоков (PDC чанков), статический анализ окружения (натуральный камень/листва), Paper API для причины спавна мобов + fallback на поиск спавнеров в соседних чанках.

---

## 📄 Лицензия

Этот проект распространяется под лицензией **GNU Affero General Public License v3.0**.  
Полный текст лицензии находится в файле [LICENSE](./LICENSE).

Copyright © 2026 rizer001

---

<p align="center">
  <b>⚡ Спасибо за использование CubixSMP! Удачи на сервере! ⚡</b>
</p>
