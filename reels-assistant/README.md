# 릴답 (ReelReply)
Android 10+ screenshot-to-comment assistant. Does not automate Instagram taps or posting.

## Features
- Select or share a screenshot from Instagram
- Optionally add caption/video context
- HTTPS multimodal AI proxy, expected response: {"skip":false,"reason":"","comments":["...","...","..."]}
- Conservative skip for ads, ambiguous material, or poor-quality captions (enforced by proxy)
- 3 candidate comments, copy to clipboard, manual review
- No backend required for the UI. Actual image understanding requires an optional AI proxy (not bundled).

## AI proxy
POST JSON: `imageBase64` (JPEG), `caption`, `language`, `mode`.
Never ship provider API keys in an APK. Implement the proxy server-side. Only send screenshots to a trusted endpoint with user permission.
Suggested system instruction: Analyze the actual screenshot and caption. If advertising, ambiguous, sensitive, or insufficient context return skip=true. Otherwise generate exactly 3 short, distinct Korean comments referencing visibly grounded details. Never invent motion, audio, identities or events. Do not automatically post.

## Build
`gradle --no-daemon -p reels-assistant assembleDebug`

GitHub Actions builds APK artifact from this module.