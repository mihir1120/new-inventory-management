# A/B Testing Setup

Ashokvatika is now instrumented for Firebase Remote Config based A/B testing.

## Implemented experiment

- **Experiment surface:** gallery share-sheet behavior
- **Parameter key:** `gallery_share_sheet_variant`
- **Supported variants:**
  - `auto_open`
  - `manual_open`

## Current behavior

- If experiments are disabled, the app defaults to `auto_open`.
- Experiments are only honored when both:
  - `BuildConfig.ENABLE_EXPERIMENTS` is `true`
  - Remote Config parameter `experiments_enabled` is `true`

## Firebase setup steps

1. Enable Google Analytics in the Firebase project.
2. Open Firebase Remote Config.
3. Add parameter `experiments_enabled` with value `true`.
4. Add parameter `gallery_share_sheet_variant` with default value `auto_open`.
5. Open Firebase A/B Testing and create a Remote Config experiment using `gallery_share_sheet_variant`.
6. Set one variant to `auto_open` and the other to `manual_open`.
7. Choose success metrics such as engagement, share actions, retention, or custom analytics event trends.

## Logged analytics events

- `ab_gallery_variant_exposure`
- `ab_gallery_sheet_opened`
- `ab_gallery_share_action`

## Important note

Code instrumentation is complete, but the live experiment itself must still be created and launched in the Firebase console.
