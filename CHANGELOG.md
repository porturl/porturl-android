# Changelog

## [0.3.0](https://github.com/porturl/porturl-android/compare/v0.2.1...v0.3.0) (2026-02-15)


### Features

* add support for opentelemetry tracing (untested) ([9d9ced4](https://github.com/porturl/porturl-android/commit/9d9ced45b377535ad3e24277abbed22c76274dae))
* separate user profile screen ([f5665b4](https://github.com/porturl/porturl-android/commit/f5665b46ccae62a97694377d58cb68dbd317e3f8))


### Bug Fixes

* actually reload permissions on user change ([40b5b6f](https://github.com/porturl/porturl-android/commit/40b5b6f2a86d9a8afebc23d0a7538294523b3866))
* actually seperate user sessions and not share state ([62fa4d1](https://github.com/porturl/porturl-android/commit/62fa4d12caad96c235764a25a04845b4461c6480))
* add missing translations ([60b2ec3](https://github.com/porturl/porturl-android/commit/60b2ec3da34633c745b9e50592a18604887c393d))
* default avatar based on mail as profile picture as well as added a border to actually make the place for the profile image visible ([322a10a](https://github.com/porturl/porturl-android/commit/322a10a48ad04d2b169b938099ce234dd21402cf))
* improve secure storage, use api 36 as target to allow installing on old android versions ([32fc038](https://github.com/porturl/porturl-android/commit/32fc038dcbef444ce4f36ed99467f702db79ed96))
* make sure gradle wrapper is executable ([9f05052](https://github.com/porturl/porturl-android/commit/9f0505208dddee755c86ccfebd6cf4840cd9f14c))
* move seach bar on big screen to bottom for consistency with small screen ([b8a9f62](https://github.com/porturl/porturl-android/commit/b8a9f629a104890a87f6918e2b6ca66ff79dec38))
* prevent broken pullrequest builds due to lack of access to secrets ([18ec350](https://github.com/porturl/porturl-android/commit/18ec350e44e5177e4ce87a7c302c32ecd98956de))

## [0.2.1](https://github.com/porturl/porturl-android/compare/v0.2.0...v0.2.1) (2026-01-21)


### Bug Fixes

* make sidebar behave similar on foldable open, closed and tablet ([14af025](https://github.com/porturl/porturl-android/commit/14af02579e04a767cd8763c925b18cabb22974bc))

## [0.2.0](https://github.com/porturl/porturl-android/compare/v0.1.1...v0.2.0) (2026-01-21)


### Features

* Add translucent background setting ([b59be60](https://github.com/porturl/porturl-android/commit/b59be60c5641273e409c76c362d53426b1904b58))
* add user profile image support ([0a2e800](https://github.com/porturl/porturl-android/commit/0a2e800c9f9393ab9fce237063186e7261e3e94b))
* allow adding/removing roles from applications ([21a834f](https://github.com/porturl/porturl-android/commit/21a834f4237a71a07afce4e11be6868448014607))
* implement system language ([8fa7b80](https://github.com/porturl/porturl-android/commit/8fa7b80d31a0ed5588ceade38c38be89a97cd950)), closes [#16](https://github.com/porturl/porturl-android/issues/16)
* instead of hamburger menus on all app icons use long press ([d1abdae](https://github.com/porturl/porturl-android/commit/d1abdae7371eacd5fd142de23ad810c8d5e24ceb))
* rework navigation (add sidebar), better fit different screensizes, add release-please instead of old release mechanism, update to agp 9.0 ([f169263](https://github.com/porturl/porturl-android/commit/f169263ac7389eb9467587cae20a0f6ea865f5eb))
* User Management Feature ([#30](https://github.com/porturl/porturl-android/issues/30)) ([0621f3b](https://github.com/porturl/porturl-android/commit/0621f3ba101e600d704644fdab8c39c5bd871fb4))


### Bug Fixes

* add missing translations ([4c981b4](https://github.com/porturl/porturl-android/commit/4c981b47061b65de721a23812c2121f6d827e2d1))
* add missing translations in models ([f2a9af3](https://github.com/porturl/porturl-android/commit/f2a9af3b9f2836f1d540597247787f260c5433e1))
* align color of add application button with add category button ([2d82d01](https://github.com/porturl/porturl-android/commit/2d82d01a05fd6f8fa99e8a9f7d8dc288f579be01))
* apply custom color palette to all app components ([c0be4d6](https://github.com/porturl/porturl-android/commit/c0be4d6c07a38260bdf29d091241678b0d9c19ac))
* broken ci build, update kotlin to 2.3.0 ([b9a14c6](https://github.com/porturl/porturl-android/commit/b9a14c6f35e0a49d2109265c4f46898fd4cec844))
* broken yaml formatting ([984e188](https://github.com/porturl/porturl-android/commit/984e188d804f8b98092307282488ef3b929816e8))
* color picker works now and on initial switch to custom colors it starts from current color scheme ([e825fbd](https://github.com/porturl/porturl-android/commit/e825fbd460cd50bbe3fb667358326d246a0319e3))
* improve broken search bar appearance, now slides in and out gracefully ([b007c45](https://github.com/porturl/porturl-android/commit/b007c45b25d9b8b7322396f8bd05d1b3d434458e))
* improve login screen backend url not set/reachable status ([e576134](https://github.com/porturl/porturl-android/commit/e576134bd6178b9c19f8a5ab45f6047db99b18f1))
* initial usage of custom colors started with black colors ([482319c](https://github.com/porturl/porturl-android/commit/482319cf6af444450991408ab02286085794a687))
* keep profile image after updating app ([976039c](https://github.com/porturl/porturl-android/commit/976039ce425a36e0d2557942429c4d7969b2829c))
* lint error ([87981f3](https://github.com/porturl/porturl-android/commit/87981f3d25be3521dd4eca11afba2c538a5c1b71))
* lint error ([c3bcd2e](https://github.com/porturl/porturl-android/commit/c3bcd2e73679322439b327720f72a06ca23e5aed))
* lint error ([702842a](https://github.com/porturl/porturl-android/commit/702842acf473f91f61383cd62ae6212266d6e19c))
* missing brightness slider ([6a25af9](https://github.com/porturl/porturl-android/commit/6a25af9fab10edc6a6419014a87c678019e9d834))
* positioning of long press menu ([e050bdd](https://github.com/porturl/porturl-android/commit/e050bddb6761092b87040989b12dc194689e0ace))
* primary and secondary color are actually used now ([cca8f53](https://github.com/porturl/porturl-android/commit/cca8f5301b57f3ac082d8cbcc63cf6815367ac0a))
* profile image display and placement of profile dropdown menu ([b34c82c](https://github.com/porturl/porturl-android/commit/b34c82cbf3b03d3a6e46e5c925c80b6992fb1e50))
* remove deprecated allowBackup, on by default ([ffe79e8](https://github.com/porturl/porturl-android/commit/ffe79e85b769cc88a9698adab23769f63a49ec63))
* show correct user/profile after logging in again ([51adf22](https://github.com/porturl/porturl-android/commit/51adf224a04170049c8f6e3182c064ed8aa76362))
* smaller icons, better spacing ([57b479b](https://github.com/porturl/porturl-android/commit/57b479bf837f0895a16b0a496ab980b430648d51))
* use one topbar component ([86c27b1](https://github.com/porturl/porturl-android/commit/86c27b1f473aabd1c029cb4c043a23bbd124235c))
* use tertiary color for category headings ([9b9342c](https://github.com/porturl/porturl-android/commit/9b9342c7c3ba9fed7ef4f8d17924cda2644e34b0))
