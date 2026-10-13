# Ashokvatika Design And App Improvement Report

Date: 2026-05-14

This report combines:

- the current Android app implementation in the workspace
- the earlier release audit in `ashokvatika_release_audit.pdf`
- the earlier prompt review in `ashokvatika_prompt_review.pdf`

The goal is to turn those observations into a practical improvement list for design, UX, stability, and release readiness.

## 1. Executive Summary

Ashokvatika already has a recognizable visual identity: the orange/green brand palette, logo-led screens, and category-first dashboard make the app feel distinct. The main opportunity now is not adding more visual variety, but making the product feel more trustworthy, more consistent, and easier to operate during real sales work.

The biggest improvement areas are:

- stronger trust signals in login, cart, and payment screens
- a more consistent navigation and layout system across screens
- less clutter in action surfaces so the important tasks stand out faster
- safer business logic for admin access, payment closure, and data persistence
- better performance and maintainability as the app grows

## 2. Design Improvements

### A. Strengthen first impression and trust

The current login screen is visually clean, but the product should feel more professional and more secure the moment it opens.

Suggested improvements:

- Replace the current default-filled credentials with empty fields and a clear admin-only message.
- Add a small subtitle under the logo such as "Admin inventory and OTC operations" so users understand the app purpose immediately.
- Use a real primary action button for sign-in instead of relying mostly on the password field confirmation icon.
- Keep the visibility toggle, but reduce the emphasis of secondary controls so the main action is clearer.
- Add subtle success, loading, and error states with stronger messaging hierarchy.

Why this matters:

- It improves trust.
- It reduces accidental misuse.
- It makes the app feel like a real business tool rather than a prototype.

### B. Build one consistent header system

The app currently repeats custom header behavior in many places. Some screens align well, while others require one-off spacing fixes.

Suggested improvements:

- Standardize one header pattern for back button, page title, optional search, and optional secondary action.
- Reuse the same spacing, icon size, top padding, and alignment across OTC, Open Cart, Closed Deals, inventory, docs, and art sections.
- Use a fixed content width system on tablets so screens feel intentionally designed instead of stretched.
- Keep one shared "screen shell" composable for page spacing, safe areas, and scroll behavior.

Why this matters:

- The app will feel more polished immediately.
- Future design tweaks become much faster.
- Users learn navigation once and carry it everywhere.

### C. Improve hierarchy on dashboard and inventory screens

The home dashboard is brand-heavy and memorable, which is good. The next step is making the most-used actions easier to scan.

Suggested improvements:

- Prioritize the most-used tiles such as OTC, Plants, and Open Cart with stronger emphasis than lower-frequency destinations.
- Add short helper labels or microcopy where needed so new staff understand what each section does.
- Introduce clearer section separation between inventory browsing, document tools, and art/social outputs.
- Use consistent empty states for categories with no data, rather than leaving wide blank areas.

Inventory screen suggestions:

- Keep latest items first, but also show sort/filter affordances clearly.
- Make search always visible and useful, not just icon-led.
- Show compact metadata chips like stock, last updated, and status so users can scan the list faster.
- Add a stronger distinction between complete and incomplete items.

### D. Make forms easier to complete under pressure

The product-entry and price-entry flows are powerful, but they can feel dense because many fields appear together and some calculations are hidden inside popups.

Suggested improvements:

- Group fields into clear sections: product info, stock info, vendor info, media, pricing, and checkout.
- Add step labels or collapsible cards so the screen feels shorter.
- Surface calculated values like landing price, margin, and checkout total in one persistent summary area.
- Use inline validation before save instead of allowing ambiguous partial data.
- Add helper text for vendor, landing price, and markup fields so staff know the intended input.

Why this matters:

- The flow becomes easier to learn.
- Data quality improves.
- Staff can work faster with fewer corrections.

### E. Upgrade the cart and payment experience

The OTC and cart experience is one of the most important parts of the app, so it should feel highly reliable.

Suggested improvements:

- Promote client details, payment status, and final confirmation into a more structured checkout sequence.
- Separate "draft cart", "ready to bill", "payment pending", and "payment verified" states visually.
- Use clear warning banners if required information is missing.
- Add a summary card showing item count, subtotal, discount, landing total, and profit/loss before final save.
- Make the final close-deal action visually distinct and only available when the deal is actually complete.

Why this matters:

- It reduces revenue mistakes.
- It increases confidence during real customer interactions.
- It makes the app feel safer and more premium.

### F. Improve accessibility and readability

The brand palette is strong, but some screens rely on light weights, image labels, and icon-only controls that may become hard to read quickly.

Suggested improvements:

- Increase contrast on value text, especially in pricing and summary areas.
- Use regular or medium text weight for important operational values instead of thin weights.
- Avoid removing too many labels just to make screens cleaner; keep icons paired with accessible meaning where needed.
- Ensure touch targets remain large enough on compact phones.
- Add content descriptions and semantic labels consistently for accessibility tools.

## 3. App And Product Improvements

### A. Continue hardening admin login

Current observation:

- The app now provisions an administrator PIN on first launch instead of shipping default credentials, but it still relies on a local device credential rather than centrally managed admin accounts.

Recommendation:

- Keep the local provisioning flow for controlled installs only if needed.
- Prefer moving production admin access fully to Firebase Auth or another centrally managed admin identity system.
- Preserve rate limiting and add auditable admin account management before broad rollout.

Priority: Critical

### B. Do not allow deal closure without payment verification

Current observation:

- The payment dialog still uses a placeholder QR image and allows deal completion without a real verified payment state.

Recommendation:

- Introduce explicit payment states: pending, verifying, verified, failed.
- Require transaction reference plus customer identity before close.
- Hide or disable close-deal actions in production until payment confirmation is real.

Priority: Critical

### C. Persist open cart and draft work safely

Current observation:

- Open cart rows, customer fields, and current transaction state are still mostly kept in in-memory UI state in `AppRoot.kt`.

Recommendation:

- Move cart and draft-deal state into a `ViewModel`.
- Persist drafts with Room, DataStore, or Firestore draft documents.
- Restore work after app backgrounding, rotation, or process death.

Priority: High

### D. Store pricing data correctly

Current observation:

- Vendor details and landing-price inputs are collected in the editor flow, but the saved `InventoryItem` shape still does not preserve them cleanly.
- Firestore writes still map `landingPrice` from `item.checkout`.

Recommendation:

- Add explicit fields for vendor name, vendor phone, vendor email, and landing price.
- Stop overloading checkout as a substitute for landing price.
- Reopen/edit flows should restore all pricing inputs exactly.

Priority: High

### E. Stop storing image payloads directly in Firestore documents

Current observation:

- `AdminFirestoreRepository.kt` still writes `imageBase64` into product documents.

Recommendation:

- Upload images and videos to cloud storage.
- Keep only URLs, thumbnails, metadata, and hashes in Firestore.
- Use separate media objects for multi-image inventory items.

Priority: High

### F. Split the giant admin screen file

Current observation:

- `AdminScreens.kt` remains extremely large and mixes UI, state, calculations, media handling, and checkout logic.

Recommendation:

- Split by feature area: dashboard, inventory editor, list views, cart, deals, payment, media, and art tools.
- Extract state classes and pure calculation helpers into separate files.
- Add previews and isolated tests for smaller composables.

Priority: High

### G. Improve image loading performance

Current observation:

- Local image decoding is handled manually in multiple places.

Recommendation:

- Move to Coil for cached Compose image loading.
- Generate smaller thumbnails for list screens.
- Keep full-size media only where it is genuinely needed.

Priority: Medium

### H. Use stronger transaction references

Current observation:

- Transaction code generation still uses the last digits of `System.currentTimeMillis()`.

Recommendation:

- Generate collision-resistant transaction IDs using full timestamp plus random suffix, or server-generated IDs.

Priority: Medium

### I. Add more real release tests

Current observation:

- There are some targeted unit tests, but the risky business flows still need broader coverage.

Recommendation:

- Add tests for login rejection, payment gating, inventory save/edit/restore, cart persistence, and saved deal integrity.
- Add one release-smoke checklist that mirrors how staff actually use the app.

Priority: Medium

## 4. Suggested Product Roadmap

### Phase 1: Immediate fixes

- Replace local-only admin access with a production identity plan.
- Block unverified payment closure.
- Persist cart and draft deal state.
- Fix landing-price and vendor persistence.

### Phase 2: UX polish

- Unify headers and spacing.
- Improve cart summary and payment confirmation UX.
- Refine inventory cards, search, and status chips.
- Improve form grouping and validation.

### Phase 3: Scale and maintainability

- Break up `AdminScreens.kt`.
- Move image handling to cached loaders and cloud storage.
- Add stronger analytics and event logging for important actions.
- Expand automated test coverage for release confidence.

## 5. Highest-Impact Suggestions For The Team

If the team can only do a few things next, these will give the biggest return:

1. Make the app feel trustworthy: secure login, real payment verification, and clearer confirmation states.
2. Make operations safer: persist cart/deal drafts and save pricing data correctly.
3. Make the UI more consistent: one header system, one spacing system, one form structure.
4. Make lists more scannable: stronger status indicators, visible search, and better metadata.
5. Make the codebase easier to grow: split the admin screen and reduce one-file complexity.

## 6. Closing Recommendation

Ashokvatika does not need a full visual redesign. It needs a focused "trust, clarity, and stability" pass.

The brand identity is already present. The next version should concentrate on:

- trust in admin access
- confidence in checkout and deal closure
- clarity in forms and lists
- consistency across screens
- stronger technical foundations behind the UI

That combination will improve both how the app looks and how safe it feels to use in real business operations.
