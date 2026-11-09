# Changelog

## [0.1.8]

- Task 2: in-app language setting (Persian / English), no AppCompat.
  Settings has a "Language" row in the Appearance card that opens a
  dialog with "فارسی" and "English" (each always in its own language);
  the choice is persisted (AppLanguage FA/EN, default FA) and the
  activity recreates on change. MainActivity applies the saved language
  in attachBaseContext via a withAppLocale helper (Locale.setDefault +
  config locale + layout direction, minSdk 26), so digits, month and
  weekday names, number separators and RTL/LTR all follow the setting
  while the calendar stays Jalali with the English month names in
  English mode. Reminder notifications and the channel name read
  strings through the same wrapped context, so they match the app
  language too. Unit test for the pref-to-locale mapping
  (AppLanguageTest); every string exists in both languages.

## [0.1.7]

- Task 1: theme setting (System / Light / Dark). Settings has a new
  "Appearance" card above "Calendar" with three radio rows; the choice
  is persisted (ThemeMode SYSTEM/LIGHT/DARK, default SYSTEM) and the
  app theme follows it, including the status and navigation bar icon
  colors (enableEdgeToEdge + a SideEffect that re-applies the bar
  appearance when the theme changes). Light-mode audit: every screen
  already uses scheme colors for text, icons, surfaces, sheets,
  dialogs and the date picker, and the semantic colors (income green,
  expense red, installment orange, Friday red, event palette) read on
  both backgrounds, so no color needed changing.

## [0.1.6]

- Visual bug pass. Every icon now has an explicit tint (never
  LocalContentColor), and the app root is a Surface with contentColor =
  onBackground, so the hamburger, search, Stats and Year arrows and the
  add/copy/delete icons are visible in dark and light. No Arabic-script
  character remains in the English strings (the app name is now
  "Calendar"); the Week range line is built from two string resources
  ("16 – 22 Aban" / "28 Aban – 4 Azar", Persian «۱۶ تا ۲۲ آبان» /
  «۲۸ آبان تا ۴ آذر») and no English remains in the Persian strings.
  Day title is weekday + day + month only ("Monday, 18 Aban"), with the
  year on the muted line ("1405 · November 9, 2026"). Empty states ("No
  transactions yet", "No installments yet", Schedule, "No results") are
  centered horizontally and vertically. The sheet's 9 color circles are
  28dp, spread across the full width, scrolling with 24dp end padding
  when they do not fit. The drawer gear is a proper gear and the Week
  icon is three tall rounded rectangles. Transactions and Installments
  use the same bottom add pill as the calendar ("Add transaction +" /
  "Add installment +") with 96dp list bottom padding; the Year view
  shows no add pill. Sheets and the search field use filled fields
  (surfaceContainerHigh, 16dp corners, no indicator, 56dp, accent
  cursor/label on focus), type chips in 20% type color with a 1.5dp
  border, and a fully rounded Save button (accent when enabled,
  surfaceContainerHigh with muted text when disabled).

## [0.1.5]

- Stage 7: calendar views, part 2 (Week and Year). The drawer now lists
  Year, Month, Week, Day, Schedule. Week view: a pager of weeks (first day
  from settings) with a 7-day strip (same today/selected styles and marker
  dots as the day strip) and, below it, the 7 days as sections (day header
  with number + weekday, the usual cards, a compact "No events" row when
  empty); swiping moves the selected day to the same weekday, tapping a
  strip day selects it and scrolls the list to its section; the header
  shows the selected day's month + year and the week's Shamsi range. Year
  view: the year number with prev/next arrows in the header and a slide
  animation on change; a vertically scrolling 3-column grid of 12 mini
  months (month name, weekday letters, 6x7 numbers, Friday red, today a
  filled accent square, a 3dp dot under days with events in the first
  marker's color, no Gregorian numbers); tapping a mini month switches to
  Month view on that month, selecting today if it is inside that month,
  otherwise the 1st. Markers are computed per mini month only when it is
  composed (LazyVerticalGrid), so scrolling stays smooth.

## [0.1.4]

- Stage 6: calendar views, part 1 (Day and Schedule). The drawer has two
  groups: Views (Month, Day, Schedule) and the tabs (Transactions,
  Installments, Stats); the view mode is saved in AppSettings and the
  Calendar screen opens in it, with the selected date shared by all modes
  and the same header actions (menu, title, search, today badge). Day view:
  a 7-day strip (weekday letters + numbers, first day from settings,
  selected day filled, tiny marker dots) over a day pager (±3650 days);
  swiping moves the selected day and the strip, tapping a strip day selects
  it; each page shows the same day content (summary, grouped cards,
  installment check, empty state). Schedule view: a lazy list from the
  selected month with month headers, day headers (large number + weekday,
  muted Gregorian) and the usual cards; only days with events appear and
  empty months are skipped; 3-month windows load near the end and the top
  (with scroll restoration when prepending). The selected-day section was
  extracted into DayEvents so all three modes share the same cards.

## [0.1.3]

- Stage 5: calendar settings. AppSettings (SharedPreferences + StateFlow)
  with first day (Saturday/Sunday/Monday), two Gregorian switches, marker
  style and default reminder time; a settings screen (gear in the drawer
  header, back arrow to close) with rounded 24dp cards for Calendar, Event
  colors (four radio rows with three-bar previews) and Reminders (up/down
  time dialog). The first day rotates the weekday letters and monthCells
  (Friday stays red wherever it falls; the date picker follows because it
  reuses monthCells) with new monthCells tests (default Saturday-first
  unchanged, plus Monday-first). The Gregorian switches hide the tile
  number (Shamsi number and markers centered) and the header line. Marker
  colors scale saturation via HSV (very muted 0.45, muted 0.7, bright 1.0,
  very bright 1.0 with 10% higher value) everywhere. The reminder time
  feeds the scheduler and reschedules on change.

## [0.1.2]

- Stage 4: selected-day section and search. The day section shows a muted
  income/expense summary line (non-zero parts only) and groups its cards as
  installments, expenses, incomes; installment cards have a round check
  button that toggles paid immediately (paid ones at 50% alpha with a
  strikethrough title; paid installments already had no grid marker).
  Long-press on a day tile opens the add sheet for that date. Search: a
  search button in the calendar header opens a full-screen page (back
  arrow, auto-focused filled rounded field); matching is case-insensitive
  with Arabic ي/ك and Persian/Arabic digits normalized; repeating events
  show their next occurrence, each unpaid installment due date is its own
  row (max 12); results sort by date from today with past ones after;
  tapping one jumps the pager to that month and selects the date.

## [0.1.1]

- Stage 3: reminders. Transaction and Installment gain remind (None / On
  the day / 1 day before / 2 days before / 1 week before); pure
  nextReminders in the new notify package (09:00 fire time, 90-day window,
  repeating transactions via occurrencesBetween, unpaid installments via
  dueDate) with unit tests; one inexact alarm (setAndAllowWhileIdle, no
  exact-alarm permission) for the earliest reminder, a receiver that posts
  due reminders and re-arms, a boot receiver, and reschedule after every
  add/update/delete and on app start. Notifications open the calendar on
  the due date; repeats share one notification id so they don't stack.
  POST_NOTIFICATIONS is asked only when a reminder is picked (never at
  start); if denied the choice is saved and a hint shows under the row.
  Reminder row in TxSheet and InstallmentSheet.

## [0.1.0]

- Stage 2: repeat. Transaction gains repeat (None/Daily/Weekly/Monthly/
  Yearly) and an optional end date; pure occurrence engine (data/
  Occurrences.kt) with unit tests for monthly/yearly clamping, inclusive end
  date and weekly across a year boundary; repeating events show on every
  matching day, stats and the summary count every occurrence, the list shows
  each series once with a repeat icon and label; TxSheet has a Repeat row
  (dialog list) and an Ends row (Never or a date, never before the start);
  deleting a repeating event deletes the whole series.
- Versions now run 0.0.1–0.0.10, then 0.1.0–0.1.10, and so on.

## [0.0.13]

- Stage 1 re-verified under the language rules: note + color, event sheet,
  month-grid date picker and event-color display were already in from
  0.0.10; the picker's confirm button now reads OK (was Confirm)

## [0.0.12]

- Language follows the device: removed the forced Persian locale and the
  forced RTL from MainActivity; per-app language supported via
  locales_config.xml (en + fa); supportsRtl stays true
- Month names, Gregorian month names, weekday names and letters moved from
  code into string-arrays (English default, Persian in values-fa); the
  helpers keep their names and read them from resources
- Digits follow the language: Persian digits and «٬» only in fa, Latin
  digits and "," otherwise; the currency word still comes from strings
- Date formats come from resources in each language's word order
  (date_full, date_gregorian)
- Prev/next arrows are autoMirrored vectors and point the right way in
  both directions

## [0.0.11]

- Language setup: values/strings.xml is now English (default), values-fa
  holds the Persian translations; app_name stays «تقویم» in both. No UI
  text is hardcoded in Kotlin anymore (the «تومان» after amounts moved to
  amount_with_currency; formatAmount now returns only the grouped digits)
- MainActivity forces the fa locale in attachBaseContext (Locale.setDefault
  + configuration context) so the app stays Persian + RTL on any device
- README gains a Working rules section

## [0.0.10]

- Event sheet, stage 1: transactions store a note and a color (8-color
  palette, default = type color); old data files load unchanged
- Sheet: title auto-focuses when adding, amount has a تومان suffix, 9
  color circles with a ring and check on the selected one, note field
  (max 4 lines), copy and delete side by side in edit mode with a
  «حذف شود؟» confirmation, sheet scrolls with the keyboard
- Date picker is now a month-grid dialog (prev/next across years, today
  outlined, امروز / انصراف / تأیید)
- Day cards and the list show the event color on the side bar and the
  note under the title

## [0.0.9]

- Color fix: full Material3 schemes for dark and light (no baseline
  purple), window background follows day/night, every screen root paints
  scheme.background, no hardcoded text/icon colors
- Drawer is now a floating rounded panel (78% width, 12dp insets, 28dp
  corners, surface color, no shadow) with a menu button + divider header
  and explicit selected/unselected item colors
- Month header: 24sp month name with year + Gregorian line, 36dp today
  badge that jumps to today
- Grid: rounded surfaceContainerLow tiles with 3dp gaps, boxed
  today/selected numbers, full-width stacked marker bars
- Add pill is now a fixed 56dp bottom overlay; day section has 96dp
  bottom padding

## [0.0.8]

- Side drawer navigation replaces the bottom pill (4 tabs only); back
  closes the drawer
- Month view restyled: slim header with menu + month name, smaller weekday
  letters, today in a rounded box, card-less day section with a gray add
  pill at the bottom

## [0.0.7]

- Stats tab: month selector, income/expense/balance cards, installments
  due this month, 6-month bar chart drawn with Canvas (no chart library)
- Month aggregation in a pure function with a unit test

## [0.0.6]

- Installments tab: JSON store + repository (same pattern as transactions),
  cards with progress and next due date, expandable paid toggles, add/edit
  sheet with first-month picker
- Installment markers (orange) on the calendar via a composite provider;
  tapping one in the day card opens its edit sheet

## [0.0.5]

- Calendar day card: "افزودن" button opens the add sheet for the selected
  day; tapping a transaction row opens it in edit mode
- Grid markers and the card update immediately after save/delete

## [0.0.4]

- Transactions tab: summary card (income / expense / balance), list grouped
  by Jalali day, add/edit bottom sheet with own Jalali date picker
- Calendar markers now come from real transactions

## [0.0.3]

- Transactions data layer: JSON store (atomic temp-file writes), repository
  with live StateFlow, real markers from stored transactions
- No new UI in this step

## [0.0.2]

- Smaller APK: R8 minify + resource shrinking, no icon library (6 own vector
  drawables instead), unused dependencies removed, only the `fa` locale,
  dependency metadata excluded from the APK
- No look or behavior change

## [0.0.1]

- Jalali engine (33-year-cycle algorithm, no library) with unit tests
- Month calendar: Shamsi + Gregorian dates, swipe between months, day markers
- Floating pill navigation: تقویم، تراکنش‌ها، اقساط، آمار (calendar is real; others are placeholders)
- Persian RTL UI, light/dark following the system
