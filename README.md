IRIDA UI
========

Install Development Environment
-------------------------------

### Install Dependencies

#### On Ubuntu

1. Run: `./scripts/ubuntu_install.sh`
2. You're done installing!

#### Other Operating Systems

1. Install [NodeJS](http://www.nodejs.org)
1. Install `grunt-cli`: `sudo npm -g install grunt-cli protractor`
1. Install `sass` and `compass`: `sudo gem update; sudo gem install sass; sudo gem install compass;`
1. Install node packages: `sudo npm install`
1. Install bower packages: `bower install`
1. Update webdriver: `sudo webdriver-manager update`

### Install Live Reload Browser Extensions

This will allow you to make changes to the front end and have them reloaded automagically in the browser when you save a file.

﻿[Livereload Browser Extenions](http://feedback.livereload.com/knowledgebase/articles/86242-how-do-i-install-and-use-the-browser-extensions-)

Development
-----------

1. Start jetty: `mvn jetty:run`
1. Start the `grunt` development environment: `grunt dev`
    1. Compiles the scss --> css
    1. Creates a watch (watches for changes to js, html, and scss files) and completes the appropriate actions.
    1. Starts a live reload server:
        1. Proxies requests from localhost:9000 --> localhost:8080
        1. Reloads localhost:9000 when changes occurs to the watched files

### Grunt

1. `grunt dev`: development environment including live reload.
1. `grunt build`: create production version (called during `mvn package -Pprod`.

#### Single Run Tests

1. `grunt test`: run all tests once end exit.  Note: needs to have Jetty running `mvn jetty:run`.
1. `grunt test:e2e`: run end to end testing.  Note: needs to have Jetty running `mvn jetty:run`.
    - Connect browser to test to `http://localhost:9876/`
1. `grunt test:unit`: run unit tests.

#### Unit Test Coverage
1. `grunt test:coverage`.
1. Creates `coverage/` + name of each browser auto tested.
1. Within each folder there open the `index.html` to see a graphical representation of the coverage.

Maven
-----

#### Verify

1. During verify: `mvn verify -Pprod`

#### Package

`mvn package -Pprod`


vm's
====

1. Install Virtual Box
1. `curl -s https://raw.githubusercontent.com/xdissent/ievms/master/ievms.sh | env IEVMS_VERSIONS="8 9 10 11" bash`