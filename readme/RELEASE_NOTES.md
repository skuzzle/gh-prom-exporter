<!-- This file is auto generated during release from readme/RELEASE_NOTES.md -->

[![Coverage Status](https://coveralls.io/repos/github/${github.user}/${github.name}/badge.svg?branch=${github.main-branch})](https://coveralls.io/github/${github.user}/${github.name}?branch=${github.main-branch}) [![Twitter Follow](https://img.shields.io/twitter/follow/skuzzleOSS.svg?style=social)](https://twitter.com/skuzzleOSS)

* Upgrade to Spring-Boot `2.6.3` (coming from `2.6.2`)
* Refactoring and improve internal documentation
* Add new metric: main branch commit count
* Add more internal metrics: `registered_scrapers`, `scrape_failures`, `abuses`, `api_calls` and `rate_limit_hits`


```
docker pull ${docker.image.name}:${project.version}
```