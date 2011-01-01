package neoauth.model;

public class RowKeyCounter implements Comparable<RowKeyCounter> {
	private String key;
	private int hits;
	
	public RowKeyCounter(String key, int hits) {
		setKey(key);
		setHits(hits);
	}
	
	public void setHits(int hits) {
		this.hits = hits;
	}
	public int getHits() {
		return hits;
	}
	public int compareTo(RowKeyCounter o) {
		return (o.getHits()-getHits());
	}
	public void incrHit() {
		this.hits++;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getKey() {
		return key;
	}
}
