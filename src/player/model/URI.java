package player.model;

public class URI implements Comparable<URI>{
	public String identifier;
	public long offset;
	public float priority;
	public boolean isUsed;

	public URI(String id, long off) {
		this.identifier = id;
		this.offset = off;
		this.priority = 0;
		this.isUsed = false;
	}

	@Override
	public boolean equals(Object o) {
		if(o instanceof URI){
			URI uri = (URI) o;
			if(this.identifier.equals(uri.identifier) && (this.offset == uri.offset))
				return true;
		}
		return false;
	}
	@Override
	public int hashCode() {
		return toString().hashCode();
	}
	@Override
	public String toString() {
		return (this.identifier + this.offset); 
	}

	@Override
	public int compareTo(URI arg0) {
		// TODO Auto-generated method stub
		if(this.priority > arg0.priority)
			return 1;
		else if(this.priority < arg0.priority)
			return -1;
		else
			return 0;
	}
}