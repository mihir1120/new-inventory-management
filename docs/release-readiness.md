# Ashokvatika Release Readiness

This app is prepared for production hardening now, with future A/B testing hooks intentionally left disabled until a real feature experiment is ready.

## Current production guardrails

- Admin access currently uses a temporary controlled login (`admin` / `123`) for the present release phase and should be replaced before broad public rollout.
- Firebase authentication is required before the admin inventory and approval flows start syncing.
- Release builds block screenshots and screen recording at the window level.
- Release builds keep code shrinking and resource shrinking enabled.
- CI now runs `lint`, `test`, and `assembleRelease` on every pull request and release-oriented branch update.
- Runtime experiment hooks are present in code. To run live A/B tests, enable build-time experiment support and configure Firebase Remote Config parameters in the Firebase console.

## Pre-Play-Store checklist

1. Create and verify production Firebase Authentication admin accounts.
2. Confirm Firestore security rules allow only authenticated admin users to read and write inventory and access-request collections.
3. Build and sign an Android App Bundle with the production keystore.
4. Run `./gradlew lint test assembleRelease` in CI and locally before upload.
5. Complete a smoke test on at least one small-screen phone and one large-screen phone.
6. Verify login failure handling, inventory CRUD, OTC cart flow, media upload, and share/export actions on a release build.
7. Upload the release to Play Console internal testing before moving to closed or open testing tracks.

## QA focus areas

- Authentication: valid admin login, invalid password, repeated invalid attempts, relaunch with active session.
- Inventory: add, edit, delete, image upload, video upload, sync recovery after temporary offline conditions.
- OTC: add multiple items, update quantities, save deals, clear cart, reopen deals.
- Platform: rotation, app resume, background/foreground, denied media permissions, low-connectivity behavior.

## Future A/B testing path

The app now includes Firebase Remote Config and Analytics hooks for experimentation. To run a live A/B test, turn on experiment support for the target build, publish Remote Config defaults, and create the experiment in Firebase A/B Testing. Do not run production experiments through a client-only toggle.
