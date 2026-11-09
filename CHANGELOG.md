# Changelog

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
