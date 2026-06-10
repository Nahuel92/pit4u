# Changelog

## [Unreleased]

## [0.3.0] - 2026-06-10

### Added

- Code highlighting

### Changed

- Upgraded to Java 25
- Changelog reporting now uses Jetbrains' changelog plugin
- Removed `untilBuild` version
- Updated dependencies

### Fixed

- `Other Parameters` now persist changes correctly if input is manually changed
- The plugin now automatically detects and aligns with the project's JUnit dependency version

## [0.2.5] - 2026-04-29

### Changed

- Updated dependencies (Pitest, Gradle and IntelliJ Platform SDK)

### Fixed

- There are no known issues at this moment. Hooray!

## [0.2.4] - 2025-12-09

### Changed

- Updated dependencies (Pitest, Gradle and IntelliJ Platform SDK)

### Fixed

- There are no known issues at this moment. Hooray!

## [0.2.3] - 2025-08-16

### Changed

- Updated dependencies (Pitest, Gradle and IntelliJ Platform SDK)
- IDEA 2025.1 is now the minimum version required for the plugin to work

### Fixed

- There are no known issues at this moment. Hooray!

## [0.2.2] - 2025-04-17

### Fixed

- A jUnit dependency issue that prevented the plugin from behaving as expected

## [0.2.1] - 2025-04-16

### Changed

- Updated dependencies (Pitest, Gradle and IntelliJ Platform SDK)

### Fixed

- There are no known issues at this moment. Hooray!

## [0.2.0] - 2025-01-14

### Added

- PIT4U contextual action is now available for Maven and Gradle projects (multi-module projects might not work well)

### Changed

- Updated dependencies (Pitest, Gradle and IntelliJ Platform SDK)

### Fixed

- Link to report misbehavior. Now it won't show if it wasn't generated due to an execution error

## [0.1.6] - 2024-11-14

### Fixed

- An incompatibility issue with IDEA 2024.3

## [0.1.5] - 2024-11-10

### Changed

- Replaced *Other Params* icon with a more aesthetic one
- Prepared plugin for upcoming new IDE version

### Fixed

- There are no known issues at this moment. Hooray!

## [0.1.4] - 2024-11-05

### Changed

- Updated PIT dependencies

### Fixed

- An error that happened when the selected report output is XML/CSV

## [0.1.3] - 2024-10-24

### Fixed

- *Other Params* state is now persisted and restored as expected
- When a configuration is deleted, *Other Params* state is now effectively removed

## [0.1.2] - 2024-10-21

### Changed

- Plugin unload no longer requires restarting the IDE

## [0.1.1] - 2024-10-20

### Changed

- Packages can now be chosen pressing Enter/return

### Fixed

- Missing plugin action error (which will be released in an upcoming version)
- Out-of-bounds error on Windows
- Advanced parameters are now correctly parsed

## [0.1.0] - 2024-10-20

### Added

- First release that adds a new configuration to run PIT mutation tests

[Unreleased]: https://github.com/Nahuel92/pit4u/compare/0.3.0...HEAD
[0.3.0]: https://github.com/Nahuel92/pit4u/compare/0.2.5...0.3.0
[0.2.5]: https://github.com/Nahuel92/pit4u/compare/0.2.4...0.2.5
[0.2.4]: https://github.com/Nahuel92/pit4u/compare/0.2.3...0.2.4
[0.2.3]: https://github.com/Nahuel92/pit4u/compare/0.2.2...0.2.3
[0.2.2]: https://github.com/Nahuel92/pit4u/compare/0.2.1...0.2.2
[0.2.1]: https://github.com/Nahuel92/pit4u/compare/0.2.0...0.2.1
[0.2.0]: https://github.com/Nahuel92/pit4u/compare/0.1.6...0.2.0
[0.1.6]: https://github.com/Nahuel92/pit4u/compare/0.1.5...0.1.6
[0.1.5]: https://github.com/Nahuel92/pit4u/compare/0.1.4...0.1.5
[0.1.4]: https://github.com/Nahuel92/pit4u/compare/0.1.3...0.1.4
[0.1.3]: https://github.com/Nahuel92/pit4u/compare/0.1.2...0.1.3
[0.1.2]: https://github.com/Nahuel92/pit4u/compare/0.1.1...0.1.2
[0.1.1]: https://github.com/Nahuel92/pit4u/compare/0.1.0...0.1.1
[0.1.0]: https://github.com/Nahuel92/pit4u/commits/0.1.0
