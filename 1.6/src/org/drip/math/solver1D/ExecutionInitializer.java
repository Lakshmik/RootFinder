
package org.drip.math.solver1D;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2012 Lakshmi Krishnamurthy
 * 
 * This file is part of CreditAnalytics, a free-software/open-source library for fixed income analysts and
 * 		developers - http://www.credit-trader.org
 * 
 * CreditAnalytics is a free, full featured, fixed income credit analytics library, developed with a special
 * 		focus towards the needs of the bonds and credit products community.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *   	you may not use this file except in compliance with the License.
 *   
 *  You may obtain a copy of the License at
 *  	http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  	distributed under the License is distributed on an "AS IS" BASIS,
 *  	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  
 *  See the License for the specific language governing permissions and
 *  	limitations under the License.
 */

/**
 * ExecutionInitializer implements the initialization execution and customization functionality.
 * 
 * ExecutionInitializer performs two types of variate initialization:
 * 	- Bracketing initialization: This brackets the fixed point using the bracketing algorithm described in
 * 		http://www.credit-trader.org. If successful, a pair of variate/OF coordinate nodes that bracket the
 * 		fixed point are generated. These brackets are eventually used by routines that iteratively determine
 * 		the fixed point. Bracketing initialization is controlled by the parameters in
 * 		BracketingControlParams.
 * 	- Convergence Zone initialization: This generates a variate that lies within the convergence zone for the
 * 		iterative determination of the fixed point using the Newton's method. Convergence Zone Determination
 * 		is controlled by the parameters in ConvergenceControlParams.
 *
 * @author Lakshmi Krishnamurthy
 */

public class ExecutionInitializer {
	class StartingVariateOF {
		public double _dblOF = java.lang.Double.NaN;
		public double _dblVariate = java.lang.Double.NaN;

		public StartingVariateOF (
			final double dblVariate,
			final double dblOF)
			throws java.lang.Exception
		{
			if (!org.drip.math.common.NumberUtil.IsValid (_dblOF = dblOF) ||
				!org.drip.math.common.NumberUtil.IsValid (_dblVariate = dblVariate))
				throw new java.lang.Exception ("StartingVariateOF constructor: Invalid inputs!");
		}
	}

	private org.drip.math.solver1D.ObjectiveFunction _of = null;
	private org.drip.math.solver1D.BracketingControlParams _bcp = null;
	private org.drip.math.solver1D.ConvergenceControlParams _ccp = null;

	private java.util.SortedMap<java.lang.Double, java.lang.Double> _mapOFMap = new
		java.util.TreeMap<java.lang.Double, java.lang.Double>();

	private double evaluateOF (
		final double dblVariate)
		throws java.lang.Exception
	{
		if (_mapOFMap.containsKey (dblVariate)) return _mapOFMap.get (dblVariate);

		double dblOF = _of.evaluate (dblVariate);

		_mapOFMap.put (dblVariate, dblOF);

		return dblOF;
	}

	private StartingVariateOF validateVariate (
		final double dblVariate,
		final org.drip.math.solver1D.BracketingOutput bop)
	{
		double dblOF = java.lang.Double.NaN;

		try {
			dblOF = evaluateOF (dblVariate);
		} catch (java.lang.Exception e) {
			dblOF = java.lang.Double.NaN;
		}

		if (!bop.incrOFCalcs() || !org.drip.math.common.NumberUtil.IsValid (dblOF)) return null;

		_mapOFMap.put (dblVariate, dblOF);

		try {
			return new StartingVariateOF (dblVariate, dblOF);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private StartingVariateOF initializeBracketingVariate (
		final org.drip.math.solver1D.BracketingOutput bop)
	{
		if (null == bop) return null;

		int iNumExpansions = _bcp.getNumExpansions();

		double dblVariate = _bcp.getVariateStart();

		StartingVariateOF sv = validateVariate (dblVariate, bop);

		if (null != sv) return sv;

		double dblBracketWidth = _bcp.getStartingBracketWidth();

		while (0 <= iNumExpansions--) {
			if (null != (sv = validateVariate (dblVariate - dblBracketWidth, bop))) return sv;

			if (null != (sv = validateVariate (dblVariate + dblBracketWidth, bop))) return sv;

			dblBracketWidth *= _bcp.getBracketWidthExpansionFactor();
		}

		return null;
	}

	private boolean bracketingDone (
		final double dblVariateLeft,
		final double dblVariateRight,
		final double dblOFLeft,
		final double dblOFRight,
		final double dblOFGoal,
		final org.drip.math.solver1D.BracketingOutput bop)
	{
		if (((dblOFLeft - dblOFGoal) * (dblOFRight - dblOFGoal)) > 0.) return false;

		double dblOF = java.lang.Double.NaN;
		double dblOFPrev = java.lang.Double.NaN;
		double dblVariate = java.lang.Double.NaN;
		double dblVariatePrev = java.lang.Double.NaN;

		for (java.util.Map.Entry<java.lang.Double, java.lang.Double> me : _mapOFMap.entrySet()) {
			dblVariate = me.getKey();

			dblOF = me.getValue();

			if (!java.lang.Double.isNaN (dblVariatePrev) && !java.lang.Double.isNaN (dblOFPrev) &&
				(((dblOF - dblOFGoal) * (dblOFPrev - dblOFGoal)) < 0.)) {
				try {
					bop.done (dblVariatePrev, dblVariate, dblOFPrev, dblOF,
						org.drip.math.solver1D.VariateIteratorPrimitive.Bisection (dblVariatePrev,
							dblVariate));
				} catch (java.lang.Exception e) {
				}

				return true;
			}

			dblOFPrev = dblOF;
			dblVariatePrev = dblVariate;
		}

		try {
			bop.done (dblVariateLeft, dblVariateRight, dblOFLeft, dblOFRight,
				org.drip.math.solver1D.VariateIteratorPrimitive.Bisection (dblVariateLeft, dblVariateRight));
		} catch (java.lang.Exception e) {
		}

		return true;
	}

	private boolean isInConvergenceZone (
		final double dblConvergenceZoneVariate,
		final double dblOFGoal,
		final org.drip.math.solver1D.ConvergenceOutput cop)
		throws java.lang.Exception
	{
		if (!cop.incrOFCalcs())
			throw new java.lang.Exception
				("ExecutionInitializer::isInConvergenceZone => Cannot increment OF in the output");

		double dblOFValue = evaluateOF (dblConvergenceZoneVariate) - dblOFGoal;

		if (!org.drip.math.common.NumberUtil.IsValid (dblOFValue))
			throw new java.lang.Exception
				("ExecutionInitializer::isInConvergenceZone => Cannot evaluate OF for variate " +
					dblConvergenceZoneVariate);

		if (!cop.incrOFDerivCalcs())
			throw new java.lang.Exception
				("ExecutionInitializer::isInConvergenceZone => Cannot increment OF deriv count in the output");

		org.drip.math.solver1D.Differential diff1D = _of.calcDerivative (dblConvergenceZoneVariate, 1);

		if (null == diff1D)
			throw new java.lang.Exception
				("ExecutionInitializer::isInConvergenceZone => Cannot evaluate OF first deriv for variate " +
					dblConvergenceZoneVariate);

		if (!cop.incrOFDerivCalcs() && !cop.incrOFDerivCalcs())
			throw new java.lang.Exception
				("ExecutionInitializer::isInConvergenceZone => Cannot increment OF deriv in the output");

		org.drip.math.solver1D.Differential diff2D = _of.calcDerivative (dblConvergenceZoneVariate, 2);

		if (null == diff2D)
			throw new java.lang.Exception
				("ExecutionInitializer::isInConvergenceZone => Cannot evaluate OF second deriv for variate "
					+ dblConvergenceZoneVariate);

		return java.lang.Math.abs (dblOFValue * diff2D.calcSlope (false)) < (diff1D.calcSlope (false) *
			diff1D.calcSlope (false) * _ccp.getConvergenceZoneEdgeLimit());
	}

	/**
	 * ExecutionInitializer constructor
	 * 
	 * @param of Objective Function
	 * @param bcp Bracketing Control Parameters
	 * @param ccp Convergence Control Parameters
	 * 
	 * @throws java.lang.Exception Thrown if inputs are invalid
	 */

	public ExecutionInitializer (
		final org.drip.math.solver1D.ObjectiveFunction of,
		final org.drip.math.solver1D.BracketingControlParams bcp,
		final org.drip.math.solver1D.ConvergenceControlParams ccp)
		throws java.lang.Exception
	{
		if (null == (_of = of))
			throw new java.lang.Exception ("ExecutionInitializer constructor: Invalid inputs");

		if (null == (_bcp = bcp)) _bcp = new org.drip.math.solver1D.BracketingControlParams();

		if (null == (_ccp = ccp)) _ccp = new org.drip.math.solver1D.ConvergenceControlParams();
	}

	/**
	 * Initializes the starting bracket
	 * 
	 * @param dblOFGoal The OF Goal
	 * 
	 * @return Results of the initialization
	 */

	public org.drip.math.solver1D.BracketingOutput initializeBracket (
		final double dblOFGoal)
	{
		if (!org.drip.math.common.NumberUtil.IsValid (dblOFGoal)) return null;

		int iNumExpansions = _bcp.getNumExpansions();

		double dblBracketWidth = _bcp.getStartingBracketWidth();

		org.drip.math.solver1D.BracketingOutput bop = new org.drip.math.solver1D.BracketingOutput();

		StartingVariateOF sv = initializeBracketingVariate (bop);

		if (null == sv) return bop;

		double dblOFLeft = sv._dblOF;
		double dblOFRight = sv._dblOF;
		double dblPreviousOFLeft = sv._dblOF;
		double dblPreviousOFRight = sv._dblOF;
		double dblVariateLeft = sv._dblVariate;
		double dblVariateRight = sv._dblVariate;
		boolean bLeftOFValidityEdgeReached = false;
		boolean bRightOFValidityEdgeReached = false;
		double dblPreviousVariateLeft = sv._dblVariate;
		double dblPreviousVariateRight = sv._dblVariate;

		while (0 <= iNumExpansions--) {
			if (!bop.incrIterations()) return null;

			if (bLeftOFValidityEdgeReached && bRightOFValidityEdgeReached) return bop;

			if (!bLeftOFValidityEdgeReached) {
				dblPreviousVariateLeft = dblVariateLeft;
				dblVariateLeft -= dblBracketWidth;
				dblPreviousOFLeft = dblOFLeft;

				try {
					if (bracketingDone (dblVariateLeft, dblVariateRight, dblOFLeft = evaluateOF
						(dblVariateLeft), dblOFRight, dblOFGoal, bop) && bop.incrOFCalcs())
						return bop;
				} catch (java.lang.Exception e) {
					dblOFLeft = java.lang.Double.NaN;
				}

				if (!org.drip.math.common.NumberUtil.IsValid (dblOFLeft)) {
					dblOFLeft = dblPreviousOFLeft;
					bLeftOFValidityEdgeReached = true;
					dblVariateLeft = dblPreviousVariateLeft;
				}
			}

			if (!bRightOFValidityEdgeReached) {
				dblPreviousVariateRight = dblVariateRight;
				dblVariateRight += dblBracketWidth;
				dblPreviousOFRight = dblOFRight;

				try {
					if (bracketingDone (dblVariateLeft, dblVariateRight, dblOFLeft, dblOFRight = evaluateOF
						(dblVariateRight), dblOFGoal, bop) && bop.incrOFCalcs())
						return bop;
				} catch (java.lang.Exception e) {
					dblOFRight = java.lang.Double.NaN;
				}

				if (!org.drip.math.common.NumberUtil.IsValid (dblOFRight)) {
					dblOFRight = dblPreviousOFRight;
					bRightOFValidityEdgeReached = true;
					dblVariateRight = dblPreviousVariateRight;
				}
			}

			if (bracketingDone (dblVariateLeft, dblVariateRight, dblOFLeft, dblOFRight, dblOFGoal, bop))
				return bop;

			dblBracketWidth *= _bcp.getBracketWidthExpansionFactor();
		}

		return null;
	}

	/**
	 * Initialize the starting variate to within the fixed point convergence zone
	 * 
	 * @param dblOFGoal The OF Goal
	 * 
	 * @return The Convergence Zone Output
	 */

	public org.drip.math.solver1D.ConvergenceOutput initializeVariate (
		final double dblOFGoal)
	{
		if (!org.drip.math.common.NumberUtil.IsValid (dblOFGoal)) return null;

		org.drip.math.solver1D.ConvergenceOutput cop = new org.drip.math.solver1D.ConvergenceOutput();

		org.drip.math.solver1D.BracketingOutput bop = initializeBracket (dblOFGoal);

		if (null != bop && bop.done()) return bop.makeConvergenceVariate();

		double dblConvergenceZoneVariate = _ccp.getConvergenceZoneVariateBegin();

		int iFixedPointConvergenceIterations = _ccp.getFixedPointConvergenceIterations();

		while (0 != iFixedPointConvergenceIterations--) {
			if (!cop.incrIterations()) return cop;

			try {
				if (isInConvergenceZone (dblConvergenceZoneVariate, dblOFGoal, cop)) {
					cop.done (dblConvergenceZoneVariate);

					return cop;
				}
			} catch (java.lang.Exception e) {
				// e.printStackTrace();
			}

			try {
				if (isInConvergenceZone (-1. * dblConvergenceZoneVariate, dblOFGoal, cop)) {
					cop.done (-1. * dblConvergenceZoneVariate);

					return cop;
				}
			} catch (java.lang.Exception e) {
				// e.printStackTrace();
			}

			dblConvergenceZoneVariate *= _ccp.getConvergenceZoneVariateBumpFactor();
		}

		return null;
	}
}
