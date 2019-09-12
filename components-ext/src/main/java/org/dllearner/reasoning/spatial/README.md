# Supported Spatial Relations

The spatial reasoner component can infer for individuals _a_, _b_, _c_, ... for which spatial information is contained in the knowledge base

- whether _a_ and _b_ are [_connected_](https://en.wikipedia.org/wiki/Region_connection_calculus)
- whether _a_ and _b_ are [_disconnected_](https://en.wikipedia.org/wiki/Region_connection_calculus)
- whether _a_ [_is part of_](https://en.wikipedia.org/wiki/Region_connection_calculus) _b_
- whether _a_ [_is proper part of_](https://en.wikipedia.org/wiki/Region_connection_calculus) _b_
- whether _a_ and _b_ are [_spatially equal_](https://en.wikipedia.org/wiki/Region_connection_calculus)
- whether _a_ and _b_ are [_overlapping_](https://en.wikipedia.org/wiki/Region_connection_calculus)
- whether _a_ is [_discrete from_](https://en.wikipedia.org/wiki/Region_connection_calculus) _b_
- whether _a_ and _b_ are [_partially overlapping_](https://en.wikipedia.org/wiki/Region_connection_calculus)
- whether _a_ and _b_ are [_externally connected_](https://en.wikipedia.org/wiki/Region_connection_calculus)
- whether _a_ is a [_tangential proper part of_](https://en.wikipedia.org/wiki/Region_connection_calculus) _b_
- whether _a_ is a [_non-tangential proper part of_](https://en.wikipedia.org/wiki/Region_connection_calculus) _b_
- whether _a_ is the _spatial sum_ of a set of individuals
- whether _a_ is _equivalent to the universal spatial region_
- whether _a_ is the _spatial complement_ of _b_
- whether _a_ is the _spatial intersection_ of _b_ and _c_
- whether _a_ is the _spatial difference_ of _b_ and _c_
- whether _a_ is _near_ _b_
- whether _a_ is _inside_ _b_
- whether _a_ _runs along_ _b_ (as e.g. a trajectory running along a certain street)
- whether _a_ _passes_ _b_ (as e.g. a trajectory passing a certain point of interest)
- whether _a_ _starts near_ _b_
- whether _a_ _ends near_ _b_

Moreover it allows querying all individuals that are in such a spatial relation with another individual.
