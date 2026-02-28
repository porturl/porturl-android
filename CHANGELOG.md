# Changelog

## [0.5.0](https://github.com/porturl/porturl-android/compare/v0.4.0...v0.5.0) (2026-02-28)


### Features

* add categories, images, edit mode, drag to reorder ([79c3f56](https://github.com/porturl/porturl-android/commit/79c3f5665e48ac0f3d6cdf8df0d7c31ba90501c5))
* add ctrl+f for searching ([66a655c](https://github.com/porturl/porturl-android/commit/66a655c2c61ff2145b08c941c50ac25370e313ec))
* Add language switching support ([0238942](https://github.com/porturl/porturl-android/commit/023894206ac20ad03464e1bfe2ff62fb38d4576d))
* add pull to refresh, allow opening urls in custom tab ([b48f771](https://github.com/porturl/porturl-android/commit/b48f771a263aae5b1dc8fd8dca89c98bc4bb71bd))
* add support for opentelemetry tracing (untested) ([9d9ced4](https://github.com/porturl/porturl-android/commit/9d9ced45b377535ad3e24277abbed22c76274dae))
* Add translucent background setting ([b59be60](https://github.com/porturl/porturl-android/commit/b59be60c5641273e409c76c362d53426b1904b58))
* add user profile image support ([0a2e800](https://github.com/porturl/porturl-android/commit/0a2e800c9f9393ab9fce237063186e7261e3e94b))
* added backend availability check ([a227d71](https://github.com/porturl/porturl-android/commit/a227d7185299add41c12a4da47551bf5af70d648))
* allow adding categories and per category ordering ([623c325](https://github.com/porturl/porturl-android/commit/623c325f0203b75f71781d449a769cd9b26de920))
* allow adding/removing roles from applications ([21a834f](https://github.com/porturl/porturl-android/commit/21a834f4237a71a07afce4e11be6868448014607))
* allow listing realms and clients, internal keycloak/porturl clients are filtered out ([1c9c494](https://github.com/porturl/porturl-android/commit/1c9c494cc42f8fb8e5bf80247699b5587ea8f9ff))
* consistent layout across small, middle and large screen, add searchbar by default, make add app/category modal windows ([f36af74](https://github.com/porturl/porturl-android/commit/f36af7499292adaf153060639c5db079d40b9f04))
* dark/light mode theming and custom color support ([26b5dea](https://github.com/porturl/porturl-android/commit/26b5deaad7da026baa494cd9780201ecaf52781b))
* display category description within the category header, expandable on click ([958627d](https://github.com/porturl/porturl-android/commit/958627daf78334bcaccece3dd7145813155d7d07))
* don't use isolated chrome session due to missing porturl login session ([a13a517](https://github.com/porturl/porturl-android/commit/a13a517c91ba74f4b71db4a7a2b5885214044fcc))
* fetch issuer-uri from backend instead of manually specifying it ([7b66c46](https://github.com/porturl/porturl-android/commit/7b66c4684c2b7623d392ea1d4e090b5799b116e5))
* fix category sorting and improve app sorting, alphabetically sorting still broken ([b838ee3](https://github.com/porturl/porturl-android/commit/b838ee32dae946172d38618549d35eac32efd58a))
* foldable/tablet support, work in progress (sorting broken) ([e0c8c83](https://github.com/porturl/porturl-android/commit/e0c8c8370c025dc0fc1d8ad566c9b7eb3c0d19ff))
* implement haptic feedback on drag start/move and context menu ([725bd6e](https://github.com/porturl/porturl-android/commit/725bd6e439725b83b97111bc42e74c4b03829b58))
* implement search ([1fe0dee](https://github.com/porturl/porturl-android/commit/1fe0dee5e5705279619dd28d1b037afb666f544b))
* implement system language ([8fa7b80](https://github.com/porturl/porturl-android/commit/8fa7b80d31a0ed5588ceade38c38be89a97cd950)), closes [#16](https://github.com/porturl/porturl-android/issues/16)
* import/export of apps and categories ([caae4bc](https://github.com/porturl/porturl-android/commit/caae4bcf0c400ceacc087636da95773de4e3341a))
* improve network problem handling ([75d69bb](https://github.com/porturl/porturl-android/commit/75d69bbc0d1a6e9dab0d08bba7f6eaddddb8f6e9))
* initial version of app which can login and display the applist ([0b6a25e](https://github.com/porturl/porturl-android/commit/0b6a25e1fa516bf3d1e85d73c2243341d24affbe))
* instead of hamburger menus on all app icons use long press ([d1abdae](https://github.com/porturl/porturl-android/commit/d1abdae7371eacd5fd142de23ad810c8d5e24ceb))
* listview and alphabetical sorting as setting ([59711b8](https://github.com/porturl/porturl-android/commit/59711b812914b6834c3c1d16ab0c83c3401c9d21))
* manage roles in clients, add cross realm/client management, use dedicated client in master realm for cross realm management, update deps ([4ab643c](https://github.com/porturl/porturl-android/commit/4ab643c69cada7e486d2318885ae52c9f2456a87))
* redesign settings screen to be responsive ([b2af3d2](https://github.com/porturl/porturl-android/commit/b2af3d2864e04ee7acf7b54d95fcf54b1d54887f))
* rework navigation (add sidebar), better fit different screensizes, add release-please instead of old release mechanism, update to agp 9.0 ([f169263](https://github.com/porturl/porturl-android/commit/f169263ac7389eb9467587cae20a0f6ea865f5eb))
* save token and add session expiration information ([7642d3c](https://github.com/porturl/porturl-android/commit/7642d3c1949c324311d58aa98524da1674920240))
* separate user profile screen ([f5665b4](https://github.com/porturl/porturl-android/commit/f5665b46ccae62a97694377d58cb68dbd317e3f8))
* support right mouse button to open context menu in applist ([4e00879](https://github.com/porturl/porturl-android/commit/4e008790bc6424f5bd170e1d0b1744180b45719b))
* update versions, add preliminary icon, fixed category sorting, app sorting still broken ([7801952](https://github.com/porturl/porturl-android/commit/7801952b1077922e9e0a9d058b31e75add2aeb21))
* upload changelogs to playstore on new releases ([483c9e3](https://github.com/porturl/porturl-android/commit/483c9e3eed8f039b85ecc74ace006dc1b29e82e2))
* use isolated chrome session with sso bridge ([882b1f4](https://github.com/porturl/porturl-android/commit/882b1f45f882b2d000d27f93d12ec24c0f16df14))
* use separate buttons for adding categories/applications ([6c90b48](https://github.com/porturl/porturl-android/commit/6c90b4895054387d638e6c269bd9c7ce77d002a2))
* User Management Feature ([#30](https://github.com/porturl/porturl-android/issues/30)) ([0621f3b](https://github.com/porturl/porturl-android/commit/0621f3ba101e600d704644fdab8c39c5bd871fb4))


### Bug Fixes

* actually reload permissions on user change ([40b5b6f](https://github.com/porturl/porturl-android/commit/40b5b6f2a86d9a8afebc23d0a7538294523b3866))
* actually seperate user sessions and not share state ([62fa4d1](https://github.com/porturl/porturl-android/commit/62fa4d12caad96c235764a25a04845b4461c6480))
* add missing translations ([60b2ec3](https://github.com/porturl/porturl-android/commit/60b2ec3da34633c745b9e50592a18604887c393d))
* add missing translations ([4c981b4](https://github.com/porturl/porturl-android/commit/4c981b47061b65de721a23812c2121f6d827e2d1))
* add missing translations in models ([f2a9af3](https://github.com/porturl/porturl-android/commit/f2a9af3b9f2836f1d540597247787f260c5433e1))
* add more error logging ([0835019](https://github.com/porturl/porturl-android/commit/083501992a9fac4f5299cb96cfd00f48277a4b66))
* align color of add application button with add category button ([2d82d01](https://github.com/porturl/porturl-android/commit/2d82d01a05fd6f8fa99e8a9f7d8dc288f579be01))
* allow longer appnames, show url below title in listview ([6e6be49](https://github.com/porturl/porturl-android/commit/6e6be494952e5e1d7e7ce0ad8f96789479a585a2))
* apply custom color palette to all app components ([c0be4d6](https://github.com/porturl/porturl-android/commit/c0be4d6c07a38260bdf29d091241678b0d9c19ac))
* bottom bar overlays navigationbar now to save space, updated icons for add category/app ([b0b77ca](https://github.com/porturl/porturl-android/commit/b0b77ca3134d8d8f9fe2be91e5ac19f142a7dae1))
* broken ci build, update kotlin to 2.3.0 ([b9a14c6](https://github.com/porturl/porturl-android/commit/b9a14c6f35e0a49d2109265c4f46898fd4cec844))
* broken formatting ([c0647db](https://github.com/porturl/porturl-android/commit/c0647db61c567b12e481de4cde0644abe55b3ad8))
* broken keycloak linking ([0beceda](https://github.com/porturl/porturl-android/commit/0beceda054a15a3185fa593c17dfe5569f1d4036))
* broken yaml formatting ([984e188](https://github.com/porturl/porturl-android/commit/984e188d804f8b98092307282488ef3b929816e8))
* close sidebar with back button ([2b4b93e](https://github.com/porturl/porturl-android/commit/2b4b93e9c612759d8891cb52c6d8460e99e40fed))
* color picker works now and on initial switch to custom colors it starts from current color scheme ([e825fbd](https://github.com/porturl/porturl-android/commit/e825fbd460cd50bbe3fb667358326d246a0319e3))
* compile using Android API 36 to fix install on older versions ([db97f5a](https://github.com/porturl/porturl-android/commit/db97f5af70379cbe89b63e5ce38936e239e117d7))
* debug self signed cert configuration ([783d969](https://github.com/porturl/porturl-android/commit/783d969790c144245202b583c96ad7d1483f1cd5))
* default avatar based on mail as profile picture as well as added a border to actually make the place for the profile image visible ([322a10a](https://github.com/porturl/porturl-android/commit/322a10a48ad04d2b169b938099ce234dd21402cf))
* default sortorder alphabetical ([bbff039](https://github.com/porturl/porturl-android/commit/bbff039fa0555319bb4ed549c8b81290c38254dc))
* don't exit edit mode after adding/editing ([e9a62d3](https://github.com/porturl/porturl-android/commit/e9a62d3313401b1863e2922ac44238bf847c867b))
* don't reuse dialogs making it look strange on opening them if they still have old content ([d7cc498](https://github.com/porturl/porturl-android/commit/d7cc4983cf12917252c4e423f88d91cb4ed9c19a))
* drag and drop in multicolumn view, visually glitches bust mostly working ([cab8795](https://github.com/porturl/porturl-android/commit/cab8795d154f4d324f2f1ad51dff8821f38d7788))
* first login not showing apps/categories due to race condition in user creation logic ([26fc330](https://github.com/porturl/porturl-android/commit/26fc3305f59e46ace22f4274d28083347d7a653b))
* google play publishing ([878bfee](https://github.com/porturl/porturl-android/commit/878bfee69208d561e178a7234257d37202894f43))
* google play publishing ([f28948e](https://github.com/porturl/porturl-android/commit/f28948e2cb82299fc3a34a75603489e368f1a7c7))
* google play publishing ([0f315fa](https://github.com/porturl/porturl-android/commit/0f315fad3bfd3e0d074f2ba993641831937a67c1))
* google play publishing ([a5598fb](https://github.com/porturl/porturl-android/commit/a5598fb2a1ec9dd44227c21438966c064d9f5ae2))
* google play publishing ([1635681](https://github.com/porturl/porturl-android/commit/16356818e53dadfa8aafbfdb2f9cc57237815c23))
* google play publishing ([6de713f](https://github.com/porturl/porturl-android/commit/6de713ff8a4a4209d84cfebbe492b83d5a5b3c18))
* google play publishing ([fe290c5](https://github.com/porturl/porturl-android/commit/fe290c5da3cbc2dc19cd978e739aff0e21af5c9f))
* hide categories with no matching search ([4a7a2be](https://github.com/porturl/porturl-android/commit/4a7a2beec2584b49ae08b487dfb1a7de14d31d45))
* image upload/retrieval works now ([f583134](https://github.com/porturl/porturl-android/commit/f583134b05f6f9fb2fb38b0d877b08503a6520d3))
* improve broken search bar appearance, now slides in and out gracefully ([b007c45](https://github.com/porturl/porturl-android/commit/b007c45b25d9b8b7322396f8bd05d1b3d434458e))
* improve drag detection to allow context menu ([c0c9003](https://github.com/porturl/porturl-android/commit/c0c9003afa0e21649402bfef04dc2c2fc6bd3734))
* improve login screen backend url not set/reachable status ([e576134](https://github.com/porturl/porturl-android/commit/e576134bd6178b9c19f8a5ab45f6047db99b18f1))
* improve predictive back gesture animations ([5a6f9ae](https://github.com/porturl/porturl-android/commit/5a6f9aeaad1b7963a847fb7e5ad548c26b5cbd7d))
* improve secure storage, use api 36 as target to allow installing on old android versions ([32fc038](https://github.com/porturl/porturl-android/commit/32fc038dcbef444ce4f36ed99467f702db79ed96))
* infinite loop on backend down from settings to login screen, added login screen retry button if backend is down ([78839c4](https://github.com/porturl/porturl-android/commit/78839c4b719e5acfbe73e4182fc6d578e19b14fe))
* initial usage of custom colors started with black colors ([482319c](https://github.com/porturl/porturl-android/commit/482319cf6af444450991408ab02286085794a687))
* keep profile image after updating app ([976039c](https://github.com/porturl/porturl-android/commit/976039ce425a36e0d2557942429c4d7969b2829c))
* lint error ([87981f3](https://github.com/porturl/porturl-android/commit/87981f3d25be3521dd4eca11afba2c538a5c1b71))
* lint error ([c3bcd2e](https://github.com/porturl/porturl-android/commit/c3bcd2e73679322439b327720f72a06ca23e5aed))
* lint error ([702842a](https://github.com/porturl/porturl-android/commit/702842acf473f91f61383cd62ae6212266d6e19c))
* lint errors and deprecated ([0aef60f](https://github.com/porturl/porturl-android/commit/0aef60fefdf85f988fd28dd506c62096b5cd6845))
* logout session on authorization provider as well; fix storing token to prevent early logout ([f0e35e0](https://github.com/porturl/porturl-android/commit/f0e35e02a659ab49fb0540840eb77474dbac8f65))
* longer timeouts, don't handle timeouts/unknownhost as session expired ([c3011a1](https://github.com/porturl/porturl-android/commit/c3011a133e424159f630e164f76a129fb7db4df9))
* loose focus on searchbar when scrolling in applist ([575f725](https://github.com/porturl/porturl-android/commit/575f72578feedc14d693d14cc1bf1343c9c32be3))
* make sidebar behave similar on foldable open, closed and tablet ([14af025](https://github.com/porturl/porturl-android/commit/14af02579e04a767cd8763c925b18cabb22974bc))
* make sure gradle wrapper is executable ([9f05052](https://github.com/porturl/porturl-android/commit/9f0505208dddee755c86ccfebd6cf4840cd9f14c))
* make sure startup is smooth by showing the splashscreen until the settings are loaded to prevent theme flickering on startup ([10468bc](https://github.com/porturl/porturl-android/commit/10468bc2ddebe051e9b44b964adfe5a7e057fd35))
* missing brightness slider ([6a25af9](https://github.com/porturl/porturl-android/commit/6a25af9fab10edc6a6419014a87c678019e9d834))
* move seach bar on big screen to bottom for consistency with small screen ([b8a9f62](https://github.com/porturl/porturl-android/commit/b8a9f629a104890a87f6918e2b6ca66ff79dec38))
* offline_access scope for longer access ([7af8c33](https://github.com/porturl/porturl-android/commit/7af8c33450275b701392559c14e09d55ccb2fd10))
* on expired session redirect to login ([ee5e37e](https://github.com/porturl/porturl-android/commit/ee5e37ea13da24efc26a52fa7b563bff0bab64d2))
* positioning of long press menu ([e050bdd](https://github.com/porturl/porturl-android/commit/e050bddb6761092b87040989b12dc194689e0ace))
* prevent broken pullrequest builds due to lack of access to secrets ([18ec350](https://github.com/porturl/porturl-android/commit/18ec350e44e5177e4ce87a7c302c32ecd98956de))
* primary and secondary color are actually used now ([cca8f53](https://github.com/porturl/porturl-android/commit/cca8f5301b57f3ac082d8cbcc63cf6815367ac0a))
* profile image display and placement of profile dropdown menu ([b34c82c](https://github.com/porturl/porturl-android/commit/b34c82cbf3b03d3a6e46e5c925c80b6992fb1e50))
* remove deprecated allowBackup, on by default ([ffe79e8](https://github.com/porturl/porturl-android/commit/ffe79e85b769cc88a9698adab23769f63a49ec63))
* remove unused category enable/icon fields ([2867215](https://github.com/porturl/porturl-android/commit/28672153acf740dc7ab495b0a233c7451a0fd17f))
* scrolling up in desktop mode shouldn't trigger pulltorefresh, add dedicated refresh button on wider layouts ([a1d364a](https://github.com/porturl/porturl-android/commit/a1d364a17b477b1cf0a8da23dc109349db59fc2b))
* search bar not visible on searching, remove useless padding ([6675e0d](https://github.com/porturl/porturl-android/commit/6675e0d1e7a2e519152067a43ed4ed50c43319ba))
* searching moves the search result out of screen boundaries ([7956deb](https://github.com/porturl/porturl-android/commit/7956deb3da9a61121641cce2969204e85eda84b1))
* show correct user/profile after logging in again ([51adf22](https://github.com/porturl/porturl-android/commit/51adf224a04170049c8f6e3182c064ed8aa76362))
* simplify icons (only one size) and fix setting on app creation ([746a250](https://github.com/porturl/porturl-android/commit/746a25091fbbbd2e2c1697688e01d531db00d2cb))
* smaller icons, better spacing ([57b479b](https://github.com/porturl/porturl-android/commit/57b479bf837f0895a16b0a496ab980b430648d51))
* styling of context menus ([4e13129](https://github.com/porturl/porturl-android/commit/4e131299f1c9c8b4cf6b109f4aacbac9d5e12a6b))
* use one topbar component ([86c27b1](https://github.com/porturl/porturl-android/commit/86c27b1f473aabd1c029cb4c043a23bbd124235c))
* use tertiary color for category headings ([9b9342c](https://github.com/porturl/porturl-android/commit/9b9342c7c3ba9fed7ef4f8d17924cda2644e34b0))
* vibration on app context menu ([f9cf86c](https://github.com/porturl/porturl-android/commit/f9cf86c27028973eb6373fd708d628067d17e846))


### Documentation

* add privacy statement for google play ([99b97f7](https://github.com/porturl/porturl-android/commit/99b97f727fb725dbbb8fa261f95ca67478599ced))

## [0.4.0](https://github.com/porturl/porturl-android/compare/v0.3.0...v0.4.0) (2026-02-28)


### Features

* add ctrl+f for searching ([66a655c](https://github.com/porturl/porturl-android/commit/66a655c2c61ff2145b08c941c50ac25370e313ec))
* allow listing realms and clients, internal keycloak/porturl clients are filtered out ([1c9c494](https://github.com/porturl/porturl-android/commit/1c9c494cc42f8fb8e5bf80247699b5587ea8f9ff))
* consistent layout across small, middle and large screen, add searchbar by default, make add app/category modal windows ([f36af74](https://github.com/porturl/porturl-android/commit/f36af7499292adaf153060639c5db079d40b9f04))
* display category description within the category header, expandable on click ([958627d](https://github.com/porturl/porturl-android/commit/958627daf78334bcaccece3dd7145813155d7d07))
* don't use isolated chrome session due to missing porturl login session ([a13a517](https://github.com/porturl/porturl-android/commit/a13a517c91ba74f4b71db4a7a2b5885214044fcc))
* implement haptic feedback on drag start/move and context menu ([725bd6e](https://github.com/porturl/porturl-android/commit/725bd6e439725b83b97111bc42e74c4b03829b58))
* import/export of apps and categories ([caae4bc](https://github.com/porturl/porturl-android/commit/caae4bcf0c400ceacc087636da95773de4e3341a))
* improve network problem handling ([75d69bb](https://github.com/porturl/porturl-android/commit/75d69bbc0d1a6e9dab0d08bba7f6eaddddb8f6e9))
* listview and alphabetical sorting as setting ([59711b8](https://github.com/porturl/porturl-android/commit/59711b812914b6834c3c1d16ab0c83c3401c9d21))
* manage roles in clients, add cross realm/client management, use dedicated client in master realm for cross realm management, update deps ([4ab643c](https://github.com/porturl/porturl-android/commit/4ab643c69cada7e486d2318885ae52c9f2456a87))
* redesign settings screen to be responsive ([b2af3d2](https://github.com/porturl/porturl-android/commit/b2af3d2864e04ee7acf7b54d95fcf54b1d54887f))
* support right mouse button to open context menu in applist ([4e00879](https://github.com/porturl/porturl-android/commit/4e008790bc6424f5bd170e1d0b1744180b45719b))
* upload changelogs to playstore on new releases ([483c9e3](https://github.com/porturl/porturl-android/commit/483c9e3eed8f039b85ecc74ace006dc1b29e82e2))
* use isolated chrome session with sso bridge ([882b1f4](https://github.com/porturl/porturl-android/commit/882b1f45f882b2d000d27f93d12ec24c0f16df14))


### Bug Fixes

* add more error logging ([0835019](https://github.com/porturl/porturl-android/commit/083501992a9fac4f5299cb96cfd00f48277a4b66))
* allow longer appnames, show url below title in listview ([6e6be49](https://github.com/porturl/porturl-android/commit/6e6be494952e5e1d7e7ce0ad8f96789479a585a2))
* bottom bar overlays navigationbar now to save space, updated icons for add category/app ([b0b77ca](https://github.com/porturl/porturl-android/commit/b0b77ca3134d8d8f9fe2be91e5ac19f142a7dae1))
* broken formatting ([c0647db](https://github.com/porturl/porturl-android/commit/c0647db61c567b12e481de4cde0644abe55b3ad8))
* broken keycloak linking ([0beceda](https://github.com/porturl/porturl-android/commit/0beceda054a15a3185fa593c17dfe5569f1d4036))
* close sidebar with back button ([2b4b93e](https://github.com/porturl/porturl-android/commit/2b4b93e9c612759d8891cb52c6d8460e99e40fed))
* compile using Android API 36 to fix install on older versions ([db97f5a](https://github.com/porturl/porturl-android/commit/db97f5af70379cbe89b63e5ce38936e239e117d7))
* debug self signed cert configuration ([783d969](https://github.com/porturl/porturl-android/commit/783d969790c144245202b583c96ad7d1483f1cd5))
* default sortorder alphabetical ([bbff039](https://github.com/porturl/porturl-android/commit/bbff039fa0555319bb4ed549c8b81290c38254dc))
* don't reuse dialogs making it look strange on opening them if they still have old content ([d7cc498](https://github.com/porturl/porturl-android/commit/d7cc4983cf12917252c4e423f88d91cb4ed9c19a))
* first login not showing apps/categories due to race condition in user creation logic ([26fc330](https://github.com/porturl/porturl-android/commit/26fc3305f59e46ace22f4274d28083347d7a653b))
* google play publishing ([878bfee](https://github.com/porturl/porturl-android/commit/878bfee69208d561e178a7234257d37202894f43))
* google play publishing ([f28948e](https://github.com/porturl/porturl-android/commit/f28948e2cb82299fc3a34a75603489e368f1a7c7))
* google play publishing ([0f315fa](https://github.com/porturl/porturl-android/commit/0f315fad3bfd3e0d074f2ba993641831937a67c1))
* google play publishing ([a5598fb](https://github.com/porturl/porturl-android/commit/a5598fb2a1ec9dd44227c21438966c064d9f5ae2))
* google play publishing ([1635681](https://github.com/porturl/porturl-android/commit/16356818e53dadfa8aafbfdb2f9cc57237815c23))
* google play publishing ([6de713f](https://github.com/porturl/porturl-android/commit/6de713ff8a4a4209d84cfebbe492b83d5a5b3c18))
* google play publishing ([fe290c5](https://github.com/porturl/porturl-android/commit/fe290c5da3cbc2dc19cd978e739aff0e21af5c9f))
* hide categories with no matching search ([4a7a2be](https://github.com/porturl/porturl-android/commit/4a7a2beec2584b49ae08b487dfb1a7de14d31d45))
* improve drag detection to allow context menu ([c0c9003](https://github.com/porturl/porturl-android/commit/c0c9003afa0e21649402bfef04dc2c2fc6bd3734))
* improve predictive back gesture animations ([5a6f9ae](https://github.com/porturl/porturl-android/commit/5a6f9aeaad1b7963a847fb7e5ad548c26b5cbd7d))
* infinite loop on backend down from settings to login screen, added login screen retry button if backend is down ([78839c4](https://github.com/porturl/porturl-android/commit/78839c4b719e5acfbe73e4182fc6d578e19b14fe))
* lint errors and deprecated ([0aef60f](https://github.com/porturl/porturl-android/commit/0aef60fefdf85f988fd28dd506c62096b5cd6845))
* longer timeouts, don't handle timeouts/unknownhost as session expired ([c3011a1](https://github.com/porturl/porturl-android/commit/c3011a133e424159f630e164f76a129fb7db4df9))
* loose focus on searchbar when scrolling in applist ([575f725](https://github.com/porturl/porturl-android/commit/575f72578feedc14d693d14cc1bf1343c9c32be3))
* make sure startup is smooth by showing the splashscreen until the settings are loaded to prevent theme flickering on startup ([10468bc](https://github.com/porturl/porturl-android/commit/10468bc2ddebe051e9b44b964adfe5a7e057fd35))
* remove unused category enable/icon fields ([2867215](https://github.com/porturl/porturl-android/commit/28672153acf740dc7ab495b0a233c7451a0fd17f))
* scrolling up in desktop mode shouldn't trigger pulltorefresh, add dedicated refresh button on wider layouts ([a1d364a](https://github.com/porturl/porturl-android/commit/a1d364a17b477b1cf0a8da23dc109349db59fc2b))
* search bar not visible on searching, remove useless padding ([6675e0d](https://github.com/porturl/porturl-android/commit/6675e0d1e7a2e519152067a43ed4ed50c43319ba))
* searching moves the search result out of screen boundaries ([7956deb](https://github.com/porturl/porturl-android/commit/7956deb3da9a61121641cce2969204e85eda84b1))
* simplify icons (only one size) and fix setting on app creation ([746a250](https://github.com/porturl/porturl-android/commit/746a25091fbbbd2e2c1697688e01d531db00d2cb))
* styling of context menus ([4e13129](https://github.com/porturl/porturl-android/commit/4e131299f1c9c8b4cf6b109f4aacbac9d5e12a6b))
* vibration on app context menu ([f9cf86c](https://github.com/porturl/porturl-android/commit/f9cf86c27028973eb6373fd708d628067d17e846))


### Documentation

* add privacy statement for google play ([99b97f7](https://github.com/porturl/porturl-android/commit/99b97f727fb725dbbb8fa261f95ca67478599ced))

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
