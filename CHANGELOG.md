# Changelog

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
