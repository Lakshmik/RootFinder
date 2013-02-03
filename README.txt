RootFinder

Lakshmi Krishnamurthy
v2.1, 1 Feb 2013


RootFinder provides the implmentation of all the standard bracketing and open root finding techniques, along with a customizable and configurable framework that separates the initilization/bracketing functionality from the eventual root search.

RootFinder achieves its design goal by implementing its functionality over several packages:
·	Framework: Framework to accommodate bracketing/open convergence initialization, execution customization/configuration, iterative variate evolution, and search termination detection
·	Bracket Initialization Techniques: Implementation of the different techniques for the initial bracket extraction.
·	Open Method Convergence Zone Initialization Techniques: Implementation of the different techniques for the convergence zone starting variate extraction.
·	Iterative Open Methods: Implementation of the iterative Open Methods - Newton-Raphson and Secant Methods
·	Iterative Bracketing Primitive Methods: Implementation of the iterative bracketing primitives – Bisection, False Position, Quadratic Interpolation, Inverse Quadratic Interpolation, and Ridder.
·	Iterative Bracketing Compound Methods: Implementation of the iterative bracketing compound methodologies – Brent’s and Zheng’s methods.
·	Search Initialization Heuristics: Implementation of a number of search heuristics to make the search targeted and customized.
·	Samples: Samples for the various bracketing and the open methods, their cusomization, and configuration.
·	Documentation: Literature review, framework description, mathematical and formulation details of the different components, root finder synthetic knowledge unit (SKU) composition, and module and API usage guide.
·	Regression Tests: Statistical regression analysis and dispersion metric evaluation for the initialization and the iteration components of the different bracketing and open root finder methodologies.

Download RootFinder binary along with the complete CreditSuite source from the link here.

RootFinder is installed by simply placing the jar file it into the class-path.

RootFinder is part of CreditSuite – open suite analytics and trading/valuation system for credit products. Detailed documentation and downloads may be found here.

