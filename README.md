<!-- This file is auto generated during release from readme/README.md -->

[![Coverage Status](https://coveralls.io/repos/github/skuzzle/gh-prom-exporter/badge.svg?branch=master)](https://coveralls.io/github/skuzzle/gh-prom-exporter?branch=master)
[![Twitter Follow](https://img.shields.io/twitter/follow/skuzzleOSS.svg?style=social)](https://twitter.com/skuzzleOSS)


# gh-prom-exporter

Export your favorite GitHub repositories to Prometheus

* Use it _as a service_: See https://gh.skuzzle.de for instructions
* Deploy it _on-premise_: `docker pull ghcr.io/skuzzle/gh-prom-exporter/gh-prom-exporter:0.0.6`

## On-Premise deployment with docker
This application can easily be run as a docker container in whatever environment you like:

```
docker run -p 8080:8080 \
    -e WEB_ALLOWANONYMOUSSCRAPE=true \
    ghcr.io/skuzzle/gh-prom-exporter/gh-prom-exporter:0.0.6
```

With _anonymous scraping_ allowed, you can now easily view the scrape results directly in the browser by navigating to
`https://your.docker.host:8080/YOUR-GITHUB-USERNAME/YOUR-REPOSITORY`.

The scraped repository can just as easy be added as static scrape target to your prometheus' scrape configs: 

```
scrape_configs:
- job_name: CHANGE_ME
  scrape_interval: 2m
  metrics_path: /YOUR-GITHUB-USERNAME/YOUR-REPOSITORY1,YOUR-REPOSITORY2
  static_configs:
    - targets: ['your.docker.host:8080']
```

In case you want to enforce authenticated scrapes only, use this configuration instead:
```
docker run -p 8080:8080 \
    ghcr.io/skuzzle/gh-prom-exporter/gh-prom-exporter:0.0.6
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