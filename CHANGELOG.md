# Changelog
All notable changes to the CESSDA Metadata Validator Server component will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

*For each release, use the following sub-sections:*  
*- Added (for new features)*  
*- Changed (for changes in existing functionality)*  
*- Deprecated (for soon-to-be removed features)*  
*- Removed (for now removed features)*  
*- Fixed (for any bug fixes)*  
*- Security (in case of vulnerabilities)*

## [0.4.0] - 2021-04-12

### Added
- Provide links to pre-defined profiles in user interface (#28)
- Validate url input field in ResourceSelectionComponent (#26)
- Post documents by REST API for validation (#25)

### Changed
- Align user interface layout to CESSDA guidelines  (#42)
- Replace Maven site docs by CESSDA's Jekyll docs (#41)
- By CESSDA requested repairs (#37, #38)
- Reject non-wellformed documents in user interface (#20)

### Fixed
- Validation of large document via rest api leads to 504 status code (#36)

### Security
- Only make actuator health endpoint available by default (#44)
- Apply dockle suggested changes to the Dockerfile (#27)

## [0.3.1] - 2020-09-23

### Changed
- Display exception message in better readable dialog window (#22)

### Fixed
- org.gesis.commons.xml.ddi.DdiInputStream corrupts UTF-16LE encoded xml documents

## [0.3.0] - 2020-09-17

### Changed
- Included [CDC profiles v1.0](https://bitbucket.org/cessda/cessda.metadata.profiles/src/v1.0)
- Upgrade to [eu.cessda.cmv:cmv-core:0.3.0](https://bitbucket.org/cessda/cessda.cmv.core/src/v0.3.0) and [eu.cessda.cmv:cmv:0.3.0](https://bitbucket.org/cessda/cessda.cmv/src/v0.3.0)

### Fixed
- Show notification if DDI document recognition fails (#19)
- Change endpoints urls to be consistent with other CESSDA products (#18)

[0.4.0]: https://bitbucket.org/cessda/cessda.cmv.server/src/v0.4.0
[0.3.1]: https://bitbucket.org/cessda/cessda.cmv.server/src/v0.3.1
[0.3.0]: https://bitbucket.org/cessda/cessda.cmv.server/src/v0.3.0
