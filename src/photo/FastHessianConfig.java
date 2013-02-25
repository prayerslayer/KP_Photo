package photo;

public class FastHessianConfig {
	/**
	 * Minimum feature intensity. Image dependent.  Start tuning at 1.
	 */
	public float detectThreshold = 10f;
	/**
	 * Radius used for non-max-suppression.  Typically 1 or 2.
	 */
	public int extractRadius = 2;
	/**
	 * Number of features it will find or if <= 0 it will return all features it finds.
	 */
	public int maxFeaturesPerScale = 100;
	/**
	 * How often pixels are sampled in the first octave.  Typically 1 or 2.
	 */
	public int initialSampleSize = 2;
	/**
	 * Typically 9.
	 */
	public int initialSize = 9;
	/**
	 * Typically 4.
	 */
	public int numberScalesPerOctave = 3;
	/**
	 * Typically 4.
	 */
	public int numberOfOctaves = 4;

	public FastHessianConfig() {
		
	}
	
	public FastHessianConfig(float detectThreshold,
							 int extractRadius,
							 int maxFeaturesPerScale,
							 int initialSampleSize,
							 int initialSize,
							 int numberScalesPerOctave,
							 int numberOfOctaves) {
		this.detectThreshold = detectThreshold;
		this.extractRadius = extractRadius;
		this.maxFeaturesPerScale = maxFeaturesPerScale;
		this.initialSampleSize = initialSampleSize;
		this.initialSize = initialSize;
		this.numberScalesPerOctave = numberScalesPerOctave;
		this.numberOfOctaves = numberOfOctaves;
	}

	public float getDetectThreshold() {
		return detectThreshold;
	}

	public int getExtractRadius() {
		return extractRadius;
	}

	public int getMaxFeaturesPerScale() {
		return maxFeaturesPerScale;
	}

	public int getInitialSampleSize() {
		return initialSampleSize;
	}

	public int getInitialSize() {
		return initialSize;
	}

	public int getNumberScalesPerOctave() {
		return numberScalesPerOctave;
	}

	public int getNumberOfOctaves() {
		return numberOfOctaves;
	}
}
