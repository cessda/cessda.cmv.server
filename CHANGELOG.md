# Changelog

All notable changes to the CESSDA Metadata Validator Server component will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

*For each release, use the following sub-sections:*

- *Added (for new features)*  
- *Changed (for changes in existing functionality)*  
- *Deprecated (for soon-to-be removed features)*  
- *Removed (for now removed features)*  
- *Fixed (for any bug fixes)*  
- *Security (in case of vulnerabilities)*

## [4.0.1] - 2025-02-04

### Added

* Added versions 2.0.1 of the CDC DDI metadata profiles, fixing issue [CMP-215](https://github.com/cessda/cessda.metadata.profiles/issues/215) ([#239](https://github.com/cessda/cessda.cmv.server/239))

## [4.0.0] - 2025-01-14

[![DOI](https://zenodo.org/badge/DOI/10.5281/zenodo.14643711.svg)](https://doi.org/10.5281/zenodo.14643711)

### Migration Notes

Due to changes in XML namespace support previous DDI profiles will validate differently. For correct validation, use the following profiles:

* CDC DDI 2.5 profiles - use version 3.0.0 or newer
* CDC DDI 2.6 profiles - use version 2.0.0 or newer
* CDC DDI 3.2 profile - use version 2.0.0 or newer
* CDC DDI 3.3 profile - use version 2.0.0 or newer
* EQB DDI 2.5 profile - use version 1.0.0 or newer

The built-in profiles have been updated with the necessary changes.

### Added

* Added new versions of the CESSDA Metadata Profiles to the CMV server ([#234](https://github.com/cessda/cessda.cmv.server/issues/234)). See the [release notes](https://github.com/cessda/cessda.metadata.profiles/releases/tag/3.0.0) for the changes.

### Changed

* Updated `cmv-core` to version 4.0.0. This is a major release with implications for all CMV users. See the [release notes](https://github.com/cessda/cessda.cmv.core/releases/tag/4.0.0) for more information.

### Fixed

* Fixed the profile's name being null if the profile document was missing a name ([PR-233](https://github.com/cessda/cessda.cmv.server/pull/233))
* Fixed the feedback form not working ([ac367d9](https://github.com/cessda/cessda.cmv.server/commit/ac367d91a77b5599033b9eb488998d6e7d1407fd)). This was caused by the EOSC Helpdesk being shut down.

## [3.0.1] - 2024-08-22

[![DOI](https://zenodo.org/badge/DOI/10.5281/zenodo.13364434.svg)](https://doi.org/10.5281/zenodo.13364434)

### Changed

- Migrated usages of `org.gesis.commons.resource.Resource` to `org.springframework.core.io.Resource` ([PR-214](https://github.com/cessda/cessda.cmv.server/pull/214))

### Fixed

- Fixed profile uploads failing with `java.net.MalformedURLException: unknown protocol: urn` ([#215](https://github.com/cessda/cessda.cmv.server/issues/215))
- Fixed discovery of predefined documents failing on Windows ([PR-207](https://github.com/cessda/cessda.cmv.server/pull/207))

## [3.0.0] - 2024-07-02

[![DOI](https://zenodo.org/badge/DOI/10.5281/zenodo.12527525.svg)](https://doi.org/10.5281/zenodo.12527525)

### Added

- Added DDI 3.3 schemas to the server ([PR-190](https://github.com/cessda/cessda.cmv.server/pull/190))

### Changed

- Updated CMV core to version 3.0.0 ([PR-196](https://github.com/cessda/cessda.cmv.server/pull/196))
- Updated OpenJDK to version 21 ([PR-204](https://github.com/cessda/cessda.cmv.server/pull/204))

### Fixed

- Fixed an old version of `commons-io` breaking the CSV export ([f0a4244](https://github.com/cessda/cessda.cmv.server/commit/f0a4244942cecc2f3b59a392b264b09d367d8533))

## [2.0.1] - 2023-12-05

### Changed

- Update `eu.cessda.cmv:cmv-documentation` from 2.0.0 to 2.0.1

## [2.0.0] - 2023-10-31

[![DOI](https://zenodo.org/badge/DOI/10.5281/zenodo.10013426.svg)](https://doi.org/10.5281/zenodo.10013426)

### Added

- Added the ability to individually remove documents from the validation list ([#34](https://github.com/cessda/cessda.cmv.server/issues/34))
- Added the ability to download validation reports directly from the results page ([#35](https://github.com/cessda/cessda.cmv.server/issues/35))
- Added structured logging support using the logging.json.enabled property ([#49](https://github.com/cessda/cessda.cmv.server/issues/49))
- Update SQAaaS badge after latest assessment ([#146](https://github.com/cessda/cessda.cmv.server/issues/146))

### Changed

- Run UI validation asynchronously ([#23](https://github.com/cessda/cessda.cmv.server/issues/23))
- Only clear previous validation results when new results finish processing ([#33](https://github.com/cessda/cessda.cmv.server/issues/33))
- Load UI strings from resource bundles ([#129](https://github.com/cessda/cessda.cmv.server/issues/129))
- Added clarification that the top buttons on the results panel will download all reports ([PR-149](https://github.com/cessda/cessda.cmv.server/pull/149))
- Updated the CMV validation profiles ([#155](https://github.com/cessda/cessda.cmv.server/issues/155))
- Display dropdown selections with the HTML `<select>` element ([#161](https://github.com/cessda/cessda.cmv.server/issues/161))

## [1.1.0] - 2023-05-23

[![DOI](https://zenodo.org/badge/DOI/10.5281/zenodo.7961907.svg)](https://doi.org/10.5281/zenodo.7961907)

### Added

- Added the ability to define a custom validation gate by specifying a list of constraints ([PR-107](https://github.com/cessda/cessda.cmv.server/pull/107))
- Added constraint documentation to the `/Validation` and `/Constraints` endpoints ([PR-110](https://github.com/cessda/cessda.cmv.server/pull/110))

### Changed

- Rearrange XSD schema violations below constraint violations, add a note about CDC validator behavior ([PR-123](https://github.com/cessda/cessda.cmv.server/pull/123))
- Updated the copyright statement in the Java source files ([PR-92](https://github.com/cessda/cessda.cmv.server/pull/92))
- Updated the Matomo analytics code to use the CESSDA Matomo Cloud instance ([PR-115](https://github.com/cessda/cessda.cmv.server/pull/115))

## Removed

* Removed the stylesheet references from the profile XMLs ([PR-98](https://github.com/cessda/cessda.cmv.server/pull/98))

## [1.0.0] - 2023-01-24

[![DOI](https://zenodo.org/badge/DOI/10.5281/zenodo.7545331.svg)](https://doi.org/10.5281/zenodo.7545331)

### Added

- Added latest version redirects for monolingual CDC profiles ([#63](https://github.com/cessda/cessda.cmv.server/issues/63))
- Enabled CORS for the Swagger documentation and the public API ([#67](https://github.com/cessda/cessda.cmv.server/issues/67))
- Validate the documents against the XML schema when validating in the user interface ([#68](https://github.com/cessda/cessda.cmv.server/issues/68))
- Transform the metadata profiles as part of the build process ([#DOCS-21](https://github.com/cessda/cessda.cmv.documentation/issues/21))

### Changed

- Update OpenJDK to 17 ([#64](https://github.com/cessda/cessda.cmv.server/issues/64))
- Replace the JIRA feedback form with the EOSC helpdesk feedback form ([#79](https://github.com/cessda/cessda.cmv.server/issues/79))

### Fixed

- Fixed EQB v0.1.1 profile redirect ([#66](https://github.com/cessda/cessda.cmv.server/issues/66))

## [0.4.2] - 2021-09-13

### Added

- Metadata profiles are now hosted on the CMV server ([#59](https://github.com/cessda/cessda.cmv.server/issues/59))
- A stylesheet for the metadata profiles was added, allowing browsers to render a friendly
  version ([#59](https://github.com/cessda/cessda.cmv.server/issues/59))

### Changed

- The description of the predefined profiles are read from the profiles
  themselves ([#61](https://github.com/cessda/cessda.cmv.server/issues/61))

## [0.4.1] - 2021-04-15

### Added

- Embed JIRA issue collector code ([#48](https://github.com/cessda/cessda.cmv.server/issues/48))
- Embed Matomo tracking code ([#47](https://github.com/cessda/cessda.cmv.server/issues/47))

## [0.4.0] - 2021-04-12

[![DOI](https://zenodo.org/badge/DOI/10.5281/zenodo.4681201.svg)](https://doi.org/10.5281/zenodo.4681201)

### Added

- Provide links to pre-defined profiles in user interface ([#28](https://github.com/cessda/cessda.cmv.server/issues/28))
- Validate url input field in ResourceSelectionComponent ([#26](https://github.com/cessda/cessda.cmv.server/issues/26))
- Post documents by REST API for validation ([#25](https://github.com/cessda/cessda.cmv.server/issues/25))

### Changed

- Align user interface layout to CESSDA guidelines  ([#42](https://github.com/cessda/cessda.cmv.server/issues/42))
- Replace Maven site docs by CESSDA's Jekyll docs ([#41](https://github.com/cessda/cessda.cmv.server/issues/41))
- By CESSDA requested repairs ([#37](https://github.com/cessda/cessda.cmv.server/issues/37), [#38](https://github.com/cessda/cessda.cmv.server/issues/38))
- Reject non-wellformed documents in user interface ([#20](https://github.com/cessda/cessda.cmv.server/issues/20))

### Fixed

- Validation of large document via rest api leads to 504 status code ([#36](https://github.com/cessda/cessda.cmv.server/issues/36))

### Security

- Only make actuator health endpoint available by default ([#44](https://github.com/cessda/cessda.cmv.server/issues/44))
- Apply dockle suggested changes to the Dockerfile ([#27](https://github.com/cessda/cessda.cmv.server/issues/27))

## [0.3.1] - 2020-09-23

### Changed

- Display exception message in better readable dialog window ([#22](https://github.com/cessda/cessda.cmv.server/issues/22))

### Fixed

- `org.gesis.commons.xml.ddi.DdiInputStream` corrupts UTF-16LE encoded xml documents

## [0.3.0] - 2020-09-17

### Changed

- Included [CDC profiles v1.0](https://bitbucket.org/cessda/cessda.metadata.profiles/src/v1.0)
- Upgrade to [eu.cessda.cmv:cmv-core:0.3.0](https://github.com/cessda/cessda.cmv.core/releases/tag/v0.3.0) and [eu.cessda.cmv:cmv:0.3.0](https://github.com/cessda/cessda.cmv/releases/tag/v0.3.0)

### Fixed

- Show notification if DDI document recognition fails ([#19](https://github.com/cessda/cessda.cmv.server/issues/19))
- Change endpoints urls to be consistent with other CESSDA products ([#18](https://github.com/cessda/cessda.cmv.server/issues/18))

[4.0.1]: https://github.com/cessda/cessda.cmv.server/releases/tag/4.0.1
[4.0.0]: https://github.com/cessda/cessda.cmv.server/releases/tag/4.0.0
[3.0.1]: https://github.com/cessda/cessda.cmv.server/releases/tag/3.0.1
[3.0.0]: https://github.com/cessda/cessda.cmv.server/releases/tag/3.0.0
[2.0.1]: https://github.com/cessda/cessda.cmv.server/releases/tag/2.0.1
[2.0.0]: https://github.com/cessda/cessda.cmv.server/releases/tag/2.0.0
[1.1.0]: https://github.com/cessda/cessda.cmv.server/releases/tag/1.1.0
[1.0.0]: https://github.com/cessda/cessda.cmv.server/releases/tag/v1.0.0
[0.4.2]: https://github.com/cessda/cessda.cmv.server/releases/tag/v0.4.2
[0.4.1]: https://github.com/cessda/cessda.cmv.server/releases/tag/v0.4.1
[0.4.0]: https://github.com/cessda/cessda.cmv.server/releases/tag/v0.4.0
[0.3.1]: https://github.com/cessda/cessda.cmv.server/releases/tag/v0.3.1
[0.3.0]: https://github.com/cessda/cessda.cmv.server/releases/tag/v0.3.0
