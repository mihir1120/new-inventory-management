# Google Play Policy Notes

These notes summarize what still needs to happen before publishing the policy documents created for this app.

## Privacy policy hosting

Google Play requires the privacy policy to be available at an active, publicly accessible, non-geofenced, non-editable URL. A local markdown file is not enough by itself for Play Console submission.

## Replace placeholders before launch

Update the following in both policy files before publishing:

- publisher / legal entity name;
- support or privacy contact email;
- governing law clause in the Terms and Conditions;
- any deletion-request workflow details if you later add user accounts.

## Keep Play Console consistent

The Google Play Data safety form must match the app's actual behavior and the privacy policy.

The Google Play target audience selection must also match the policy text. The current intended audience is:

- **Ages 13-15**
- **Ages 16-17**
- **Ages 18 and above**

The app should **not** be represented as intended for children under 13.

Based on the current codebase, review disclosures for:

- Firebase Authentication and Firestore usage;
- uploaded camera/gallery/document media;
- customer and transaction data entered by authorized users;
- user-initiated sharing to third-party apps.

## Relevant Google Play references

- User Data policy: [support.google.com/googleplay/android-developer/answer/10144311](https://support.google.com/googleplay/android-developer/answer/10144311)
- Data safety form guidance: [support.google.com/googleplay/android-developer/answer/10787469](https://support.google.com/googleplay/android-developer/answer/10787469)
- Sensitive permissions and file/media access guidance: [support.google.com/googleplay/android-developer/answer/9888170](https://support.google.com/googleplay/android-developer/answer/9888170)
- Families policy: [support.google.com/googleplay/android-developer/answer/9893335](https://support.google.com/googleplay/android-developer/answer/9893335)
- Target audience settings guidance: [support.google.com/googleplay/android-developer/answer/9867159](https://support.google.com/googleplay/android-developer/answer/9867159)
