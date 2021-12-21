<!-- This file is auto generated during release from readme/README.md -->

[![Coverage Status](https://coveralls.io/repos/github/${github.user}/${github.name}/badge.svg?branch=${github.main-branch})](https://coveralls.io/github/${github.user}/${github.name}?branch=${github.main-branch})
[![Twitter Follow](https://img.shields.io/twitter/follow/skuzzleOSS.svg?style=social)](https://twitter.com/skuzzleOSS)


# gh-prom-exporter

Export your favorite GitHub repositories to Prometheus

Use it _as a service_: See https://gh.skuzzle.de for instructions
or
Use it _on-premise_: `docker pull ${docker.image.name}:${project.version}`

## On-Premise deployment with docker
This application can easily be run as a docker container in whatever environment you like:

```
docker run -p 8080:8080 \
    -e WEB_ALLOWANONYMOUSSCRAPE=true \
    ${docker.image.name}:${project.version}
```

With _anonymous scraping_ allowed, you can now easily view the scrape results directly in the browser by navigating to
`https://your.docker.host:8080/YOUR-GITHUB-USERNAME/YOUR-REPOSITORY`.

The scraped repository can just as easy be added as static scrape target to your prometheus' scrape configs: 

```
scrape_configs:
- job_name: CHANGE_ME
  scrape_interval: 2m
  metrics_path: /YOUR-GITHUB-USERNAME/YOUR-REPOSITORY
  static_configs:
    - targets: ['your.docker.host:8080']
```

In case you want to enforce authenticated scrapes only, use this configuration instead:
```
docker run -p 8080:8080 \
    ${docker.image.name}:${project.version}
```

And

```
scrape_configs:
- job_name: CHANGE_ME
  scrape_interval: 2m
  basic_auth: 
    username: YOUR-GITHUB-USERNAME
    password: YOUR-GITHUB-ACCESS-TOKEN
  metrics_path: /YOUR-GITHUB-USERNAME/YOUR-REPOSITORY
  static_configs:
    - targets: ['your.docker.host:8080']
```