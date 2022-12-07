# MapMatcher (trivial name, I know)

![Java-17+](https://img.shields.io/badge/java-17%2B-orange.svg) [![License: GPL v3](https://img.shields.io/badge/License-MIT-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)

<a href="https://codeclimate.com/github/mtrevisan/MapMatcher/maintainability"><img src="https://api.codeclimate.com/v1/badges/bff8577200d792e1e197/maintainability" /></a>

[![Project Status: Active – The project has reached a stable, usable state and is being actively developed.](https://www.repostatus.org/badges/latest/active.svg)](https://www.repostatus.org/#active)
==========

<br />

## Forewords
This is, as the name suggests, yet another map matcher (see [this page](https://en.wikipedia.org/wiki/Map_matching)) between a series of observations (mainly noisy GPS points) and the most probable path on a given graph.<br/>
It uses a first–order [Hidden Markov Model](https://en.wikipedia.org/wiki/Hidden_Markov_model), [Viterbi algorithm](https://en.wikipedia.org/wiki/Viterbi_algorithm), [A* search algorithm](https://en.wikipedia.org/wiki/A*_search_algorithm), [Ramer–Douglas–Peuker algorithm](https://en.wikipedia.org/wiki/Ramer%E2%80%93Douglas%E2%80%93Peucker_algorithm) for point simplification, [Bentley–Ottmann algorithm](https://en.wikipedia.org/wiki/Bentley%E2%80%93Ottmann_algorithm) for intersection between points and polylines, [Graham scan](https://en.wikipedia.org/wiki/Graham_scan) for convex hull calculation, [Kalman filter](https://en.wikipedia.org/wiki/Kalman_filter) for smoothing the observations, [HPR tree](https://en.wikipedia.org/wiki/Hilbert_R-tree) to get the list of roads, various geodetics algorithms, and a graph merger builder that connects also near edges.

<br />

| This project adheres to the **[Zero Bugs Commitment](https://github.com/classgraph/classgraph/blob/master/Zero-Bugs-Commitment.md)**. |
|---------------------------------------------------------------------------------------------------------------------------------------|

<br/>
<br/>

## Table of Contents
1. [Contributing](#contributing)
2. [Changelog](#changelog)
    1. [version 0.0.0](#changelog-0.0.0)
3. [License](#license)

<br/>

<a name="contributing"></a>
## Contributing
Please report issues to the [issue tracker](https://github.com/mtrevisan/MapMatcher/issues) if you have any difficulties using this module, found a bug, or request a new feature.

Pull requests are welcomed.

<br/>

<a name="changelog"></a>
## Changelog

<a name="changelog-0.0.0"></a>
### version 0.0.0 - 20221202
- First version.


<br/>

<a name="license"></a>
## License
This project is licensed under [MIT license](http://opensource.org/licenses/MIT).
For the full text of the license, see the [LICENSE](LICENSE) file.
