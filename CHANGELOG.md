# Changelog

## [0.3.0](https://github.com/porturl/porturl-android/compare/v0.2.1...v0.3.0) (2026-01-21)


### Features

* add categories, images, edit mode, drag to reorder ([79c3f56](https://github.com/porturl/porturl-android/commit/79c3f5665e48ac0f3d6cdf8df0d7c31ba90501c5))
* Add language switching support ([0238942](https://github.com/porturl/porturl-android/commit/023894206ac20ad03464e1bfe2ff62fb38d4576d))
* add pull to refresh, allow opening urls in custom tab ([b48f771](https://github.com/porturl/porturl-android/commit/b48f771a263aae5b1dc8fd8dca89c98bc4bb71bd))
* Add translucent background setting ([b59be60](https://github.com/porturl/porturl-android/commit/b59be60c5641273e409c76c362d53426b1904b58))
* add user profile image support ([0a2e800](https://github.com/porturl/porturl-android/commit/0a2e800c9f9393ab9fce237063186e7261e3e94b))
* added backend availability check ([a227d71](https://github.com/porturl/porturl-android/commit/a227d7185299add41c12a4da47551bf5af70d648))
* allow adding categories and per category ordering ([623c325](https://github.com/porturl/porturl-android/commit/623c325f0203b75f71781d449a769cd9b26de920))
* allow adding/removing roles from applications ([21a834f](https://github.com/porturl/porturl-android/commit/21a834f4237a71a07afce4e11be6868448014607))
* dark/light mode theming and custom color support ([26b5dea](https://github.com/porturl/porturl-android/commit/26b5deaad7da026baa494cd9780201ecaf52781b))
* fetch issuer-uri from backend instead of manually specifying it ([7b66c46](https://github.com/porturl/porturl-android/commit/7b66c4684c2b7623d392ea1d4e090b5799b116e5))
* fix category sorting and improve app sorting, alphabetically sorting still broken ([b838ee3](https://github.com/porturl/porturl-android/commit/b838ee32dae946172d38618549d35eac32efd58a))
* foldable/tablet support, work in progress (sorting broken) ([e0c8c83](https://github.com/porturl/porturl-android/commit/e0c8c8370c025dc0fc1d8ad566c9b7eb3c0d19ff))
* implement search ([1fe0dee](https://github.com/porturl/porturl-android/commit/1fe0dee5e5705279619dd28d1b037afb666f544b))
* implement system language ([8fa7b80](https://github.com/porturl/porturl-android/commit/8fa7b80d31a0ed5588ceade38c38be89a97cd950)), closes [#16](https://github.com/porturl/porturl-android/issues/16)
* initial version of app which can login and display the applist ([0b6a25e](https://github.com/porturl/porturl-android/commit/0b6a25e1fa516bf3d1e85d73c2243341d24affbe))
* instead of hamburger menus on all app icons use long press ([d1abdae](https://github.com/porturl/porturl-android/commit/d1abdae7371eacd5fd142de23ad810c8d5e24ceb))
* rework navigation (add sidebar), better fit different screensizes, add release-please instead of old release mechanism, update to agp 9.0 ([f169263](https://github.com/porturl/porturl-android/commit/f169263ac7389eb9467587cae20a0f6ea865f5eb))
* save token and add session expiration information ([7642d3c](https://github.com/porturl/porturl-android/commit/7642d3c1949c324311d58aa98524da1674920240))
* update versions, add preliminary icon, fixed category sorting, app sorting still broken ([7801952](https://github.com/porturl/porturl-android/commit/7801952b1077922e9e0a9d058b31e75add2aeb21))
* use separate buttons for adding categories/applications ([6c90b48](https://github.com/porturl/porturl-android/commit/6c90b4895054387d638e6c269bd9c7ce77d002a2))
* User Management Feature ([#30](https://github.com/porturl/porturl-android/issues/30)) ([0621f3b](https://github.com/porturl/porturl-android/commit/0621f3ba101e600d704644fdab8c39c5bd871fb4))


### Bug Fixes

* add missing translations ([4c981b4](https://github.com/porturl/porturl-android/commit/4c981b47061b65de721a23812c2121f6d827e2d1))
* add missing translations in models ([f2a9af3](https://github.com/porturl/porturl-android/commit/f2a9af3b9f2836f1d540597247787f260c5433e1))
* align color of add application button with add category button ([2d82d01](https://github.com/porturl/porturl-android/commit/2d82d01a05fd6f8fa99e8a9f7d8dc288f579be01))
* apply custom color palette to all app components ([c0be4d6](https://github.com/porturl/porturl-android/commit/c0be4d6c07a38260bdf29d091241678b0d9c19ac))
* broken ci build, update kotlin to 2.3.0 ([b9a14c6](https://github.com/porturl/porturl-android/commit/b9a14c6f35e0a49d2109265c4f46898fd4cec844))
* broken yaml formatting ([984e188](https://github.com/porturl/porturl-android/commit/984e188d804f8b98092307282488ef3b929816e8))
* color picker works now and on initial switch to custom colors it starts from current color scheme ([e825fbd](https://github.com/porturl/porturl-android/commit/e825fbd460cd50bbe3fb667358326d246a0319e3))
* don't exit edit mode after adding/editing ([e9a62d3](https://github.com/porturl/porturl-android/commit/e9a62d3313401b1863e2922ac44238bf847c867b))
* drag and drop in multicolumn view, visually glitches bust mostly working ([cab8795](https://github.com/porturl/porturl-android/commit/cab8795d154f4d324f2f1ad51dff8821f38d7788))
* image upload/retrieval works now ([f583134](https://github.com/porturl/porturl-android/commit/f583134b05f6f9fb2fb38b0d877b08503a6520d3))
* improve broken search bar appearance, now slides in and out gracefully ([b007c45](https://github.com/porturl/porturl-android/commit/b007c45b25d9b8b7322396f8bd05d1b3d434458e))
* improve login screen backend url not set/reachable status ([e576134](https://github.com/porturl/porturl-android/commit/e576134bd6178b9c19f8a5ab45f6047db99b18f1))
* initial usage of custom colors started with black colors ([482319c](https://github.com/porturl/porturl-android/commit/482319cf6af444450991408ab02286085794a687))
* keep profile image after updating app ([976039c](https://github.com/porturl/porturl-android/commit/976039ce425a36e0d2557942429c4d7969b2829c))
* lint error ([87981f3](https://github.com/porturl/porturl-android/commit/87981f3d25be3521dd4eca11afba2c538a5c1b71))
* lint error ([c3bcd2e](https://github.com/porturl/porturl-android/commit/c3bcd2e73679322439b327720f72a06ca23e5aed))
* lint error ([702842a](https://github.com/porturl/porturl-android/commit/702842acf473f91f61383cd62ae6212266d6e19c))
* logout session on authorization provider as well; fix storing token to prevent early logout ([f0e35e0](https://github.com/porturl/porturl-android/commit/f0e35e02a659ab49fb0540840eb77474dbac8f65))
* make sidebar behave similar on foldable open, closed and tablet ([14af025](https://github.com/porturl/porturl-android/commit/14af02579e04a767cd8763c925b18cabb22974bc))
* missing brightness slider ([6a25af9](https://github.com/porturl/porturl-android/commit/6a25af9fab10edc6a6419014a87c678019e9d834))
* offline_access scope for longer access ([7af8c33](https://github.com/porturl/porturl-android/commit/7af8c33450275b701392559c14e09d55ccb2fd10))
* on expired session redirect to login ([ee5e37e](https://github.com/porturl/porturl-android/commit/ee5e37ea13da24efc26a52fa7b563bff0bab64d2))
* positioning of long press menu ([e050bdd](https://github.com/porturl/porturl-android/commit/e050bddb6761092b87040989b12dc194689e0ace))
* primary and secondary color are actually used now ([cca8f53](https://github.com/porturl/porturl-android/commit/cca8f5301b57f3ac082d8cbcc63cf6815367ac0a))
* profile image display and placement of profile dropdown menu ([b34c82c](https://github.com/porturl/porturl-android/commit/b34c82cbf3b03d3a6e46e5c925c80b6992fb1e50))
* remove deprecated allowBackup, on by default ([ffe79e8](https://github.com/porturl/porturl-android/commit/ffe79e85b769cc88a9698adab23769f63a49ec63))
* show correct user/profile after logging in again ([51adf22](https://github.com/porturl/porturl-android/commit/51adf224a04170049c8f6e3182c064ed8aa76362))
* smaller icons, better spacing ([57b479b](https://github.com/porturl/porturl-android/commit/57b479bf837f0895a16b0a496ab980b430648d51))
* use one topbar component ([86c27b1](https://github.com/porturl/porturl-android/commit/86c27b1f473aabd1c029cb4c043a23bbd124235c))
* use tertiary color for category headings ([9b9342c](https://github.com/porturl/porturl-android/commit/9b9342c7c3ba9fed7ef4f8d17924cda2644e34b0))

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
