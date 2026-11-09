# Changelog

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
