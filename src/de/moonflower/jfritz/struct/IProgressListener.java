package de.moonflower.jfritz.struct;

public interface IProgressListener {
	public void setMin(int min);
	public void setMax(int max);
	public void setProgress(int progress);
	public void finished();
}
